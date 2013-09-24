/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.tide.data {
	
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.utils.ByteArray;
	import flash.utils.Dictionary;
	import flash.utils.getQualifiedClassName;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.collections.ICollectionView;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.IPropertyChangeNotifier;
	import mx.core.IUID;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	import mx.events.PropertyChangeEvent;
	import mx.events.PropertyChangeEventKind;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.utils.ObjectUtil;
	
	import org.granite.IValue;
	import org.granite.collections.IMap;
	import org.granite.collections.IPersistentCollection;
	import org.granite.meta;
	import org.granite.reflect.Type;
	import org.granite.tide.BaseContext;
	import org.granite.tide.EntityDescriptor;
	import org.granite.tide.IEntity;
	import org.granite.tide.IEntityManager;
	import org.granite.tide.IExpression;
	import org.granite.tide.IPropertyHolder;
	import org.granite.tide.IWrapper;
	import org.granite.tide.Tide;
	import org.granite.tide.collections.PersistentCollection;
	import org.granite.tide.collections.PersistentMap;
	import org.granite.util.Enum;
	
	
	use namespace meta;
	
	
	/**
	 * 	DirtyCheckContext handles dirty checking of managed entities
	 * 
	 * 	@author William DRAI
	 */
	public class ChangeSetBuilder {
		
		private static var log:ILogger = Log.getLogger("org.granite.tide.data.ChangeSetBuilder");
		
		protected var _context:BaseContext;
		protected var _savedProperties:Dictionary;
		private var _local:Boolean = true;
		
		private var _tmpContext:BaseContext = null;
		
		
		public function ChangeSetBuilder(context:BaseContext, local:Boolean = true) {
			_context = context;
			_savedProperties = context.meta_getSavedProperties();
			_local = local;
			
			// Temporary context to store complete entities so we can uninitialize all collections
			// when possible
			_tmpContext = _context.newTemporaryContext();
		}
		
		/**
		 *  Build a ChangeSet object for the current context
		 *	
		 * 	@return the change set for this context 
		 */ 
		public function buildChangeSet():ChangeSet {
			return internalBuildChangeSet(_savedProperties);
		}
		
		/**
		 *  Build a Change object for the entity in the current context
		 *
		 * 	@return the change for this entity in the context
		 */
		public function buildEntityChangeSet(entity:IEntity):ChangeSet {
			var entitySavedProperties:Dictionary = new Dictionary();
			collectEntitySavedProperties(entity, entitySavedProperties);
			if (entitySavedProperties[entity] == null)
				entitySavedProperties[entity] = { version: entity[_context.meta_tide.getEntityDescriptor(entity).versionPropertyName] };
			
			var changeSet:ChangeSet = internalBuildChangeSet(entitySavedProperties);
			// Place Change for initial entity first
			var pos:uint = 0;
			for (var i:uint = 0; i < changeSet.changes.length; i++) {
				if (Change(changeSet.changes[i]).isForEntity(entity)) {
					pos = i;
					break;
				}
			}
			if (pos > 0) {
				var change:Change = changeSet.changes.splice(pos, 1)[0];
				changeSet.changes.unshift(change);
			}
			return changeSet;
		}
		
		private function collectEntitySavedProperties(entity:Object, savedProperties:Dictionary, cache:Dictionary = null):void {
			if (cache == null)
				cache = new Dictionary(true);
			else if (cache[entity] != null)
				return;
			
			cache[entity] = true;
			if (entity is IEntity && _savedProperties[entity] != null)
				savedProperties[entity] = _savedProperties[entity];
			
			var cinfo:Object = ObjectUtil.getClassInfo(entity, null, { includeTransient: false, includeReadOnly: false });
			for each (var p:String in cinfo.properties) {
				var v:Object = entity[p];
				if (v == null)
					continue;
				if (entity is IEntity && !entity.meta::isInitialized(p))
					continue;
				
				if (v is IList || v is Array) {
					for each (var e:Object in v) {
						collectEntitySavedProperties(e, savedProperties, cache);
					}
				}
				else if (v is IMap) {
					for each (var k:Object in IMap(v).keySet) {
						collectEntitySavedProperties(k, savedProperties, cache);
						collectEntitySavedProperties(v.get(k), savedProperties, cache);
					}
				}
				else if (!ObjectUtil.isSimple(v) && !(v is Enum || v is IValue || v is XML || v is ByteArray)) {
					collectEntitySavedProperties(v, savedProperties, cache);
				}
			}
		}
		
		private function internalBuildChangeSet(savedProperties:Dictionary):ChangeSet {
			var changeSet:ChangeSet = new ChangeSet([], _local);
			var changeMap:Dictionary = new Dictionary(true);
			
			for (var entity:Object in savedProperties) {
				if (savedProperties[entity] is Array)
					entity = _context.meta_getOwnerEntity(entity);
				
				if (!(entity is IEntity) && !(entity is IUID))
					entity = _context.meta_getOwnerEntity(entity);
				
				if (changeMap[entity] != null)
					continue;
				
				var save:Object = savedProperties[entity];
				
				var change:Change = null;
				var versionPropertyName:String = null;
				if (entity is IEntity) {
					var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(entity));
					versionPropertyName = desc.versionPropertyName;
					// Unsaved objects should not be part of the ChangeSet
					if (isNaN(save[desc.versionPropertyName]))
						continue;
					
					change = new Change(getQualifiedClassName(entity), entity.uid,
						desc.idPropertyName != null ? entity[desc.idPropertyName] : null,
						entity[versionPropertyName], _local);
				}
				else if (entity is IUID) {
					change = new Change(getQualifiedClassName(entity), entity.uid, null, NaN, _local);
				}
				if (change == null) {
					changeMap[entity] = false;
					continue;
				}
				
				changeMap[entity] = change;
				changeSet.addChange(change);
				var changes:Object = change.changes;
				
				var cinfo:Object = ObjectUtil.getClassInfo(entity, [ 'uid', versionPropertyName ], { includeTransient: false, includeReadOnly: false });
				for each (var p:String in cinfo.properties) {
					var v:Object = entity[p];
					if (save != null && save.hasOwnProperty(p)) {
						if (v is IList || v is IMap) {
							var collEvents:Array = save[p] as Array;
							var collChanges:CollectionChanges = new CollectionChanges();
							changes[p] = collChanges;
							
							for each (var event:CollectionEvent in collEvents) {
								if (event.target is IMap) {
									if (event.kind == CollectionEventKind.ADD && event.items.length == 1) {
										collChanges.addChange(1, buildRef(event.items[0][0]), buildRef(event.items[0][1]));
									}
									else if (event.kind == CollectionEventKind.REMOVE && event.items.length == 1) {
										collChanges.addChange(-1, buildRef(event.items[0][0]), buildRef(event.items[0][1]));
									}
									else if (event.kind == CollectionEventKind.REPLACE && event.items.length == 1) {
										collChanges.addChange(0, buildRef(event.items[0].property), buildRef(event.items[0].newValue));
									}
								}
								else {
									if (event.kind == CollectionEventKind.ADD && event.items.length == 1) {
										collChanges.addChange(1, event.location, buildRef(event.items[0]));
									}
									else if (event.kind == CollectionEventKind.REMOVE && event.items.length == 1) {
										collChanges.addChange(-1, event.location, buildRef(event.items[0]));
									}
									else if (event.kind == CollectionEventKind.REPLACE && event.items.length == 1) {
										collChanges.addChange(0, event.location, buildRef(event.items[0]));
									}
								}
							}
						}
						else {
							changes[p] = buildRef(v);
						}
					}
					else if (savedProperties[v] != null && !(v is IEntity || v is IUID)) {
						// Embedded objects
						changes[p] = v;
						changeMap[v] = false;
					}
				}
			}
			
			// Cleanup tmp context to detach all new entities
			_tmpContext.meta_clear();
			
			return changeSet;
		}
		
		private function buildRef(object:Object):Object {
			if (object is IEntity) {
				var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(object));
				if (desc.versionPropertyName == null)
					throw new Error("Cannot build ChangeSet for non versioned entities");
				
				if (!isNaN(object[desc.versionPropertyName]))
					return new ChangeRef(getQualifiedClassName(object), object.uid, object[desc.idPropertyName]);
				else {
					// Force attachment/init of uids of ref object in case some deep elements in the graph are not yet managed in the current context
					// So the next merge in the tmp context does not attach newly added objects to the tmp context
					_context.meta_attach(object);
					return _tmpContext.meta_mergeFromContext(_context, object, false, true);
				}
			}
			return object;
		}
		
		
		/**
		 *  @private 
		 *  Check if a value is empty
		 *
		 *  @return value is empty
		 */ 
		public function isEmpty(val:*):Boolean {
			if (val == null)
				return true;
			else if (val is String)
				return val == "";
			else if (val is Number)
				return isNaN(val as Number);
			else if (val is Enum)
				return val == null;
			else if (val is Array)
				return (val as Array).length == 0;
			else if (val is Date)
				return (val as Date).time == 0;
			else if (val is IList)
				return (val as IList).length == 0;
			else if (val as IMap)
				return (val as IMap).length == 0;
			else
				return !val; 
		}
	}
}
