/*
	GRANITE DATA SERVICES
	Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.
	
	This file is part of Granite Data Services.
	
	Granite Data Services is free software; you can redistribute it and/or modify
	it under the terms of the GNU Library General Public License as published by
	the Free Software Foundation; either version 2 of the License, or (at your
	option) any later version.
	
	Granite Data Services is distributed in the hope that it will be useful, but
	WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
	for more details.
	
	You should have received a copy of the GNU Library General Public License
	along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.tide.data {
	
	import flash.utils.ByteArray;
	import flash.utils.Dictionary;
	import flash.utils.getQualifiedClassName;
	
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.IUID;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.utils.ObjectUtil;
	
	import org.granite.IValue;
	import org.granite.collections.IMap;
	import org.granite.collections.IPersistentCollection;
	import org.granite.meta;
	import org.granite.reflect.Type;
	import org.granite.tide.*;
	import org.granite.util.Enum;
	
	
	/**
	 * 	Interface for custom data mergers
	 *
	 * 	@author William DRAI
	 */
	public class ChangeMerger implements ICustomMerger {
		
		private static var log:ILogger = Log.getLogger("org.granite.tide.data.ChangeMerger");
		
		/**
		 * 	Should return true if this merger is able to handle the specified object
		 *
		 *  @param obj an object
		 *  @return true if object can be handled
		 */
		public function accepts(obj:Object):Boolean {
			return obj is ChangeSet || obj is Change;
		}
		
		/**
		 *  Merge an entity coming from the server in the entity manager
		 *
		 *  @param obj external entity
		 *  @param previous previously existing object in the context (null if no existing object)
		 *  @param expr current path from the context
		 *  @param parent parent object for collections
		 *  @param propertyName property name of the collection in the owner object
		 *
		 *  @return merged entity (=== previous when previous not null)
		 */
		public function merge(mergeContext:MergeContext, changeSet:Object, previous:Object, expr:IExpression, parent:Object, propertyName:String):Object {
			if (changeSet != null || previous != null)
				log.debug("merge Change: {0} previous {1} (change)", BaseContext.toString(changeSet), BaseContext.toString(previous));
			
			var next:Object = null;
			
			// Local ChangeSet should not be replaced by its context value
			if (changeSet is ChangeSet && changeSet.local)
				next = changeSet;
			
			var changes:Array = changeSet is ChangeSet ? changeSet.changes : [ changeSet ];
			var local:Boolean = changeSet is ChangeSet ? ChangeSet(changeSet).local : Change(changeSet).local;
			
			for each (var change:Change in changes) {
				
				if (change.local && next == null) {
					// Changes built locally must not be replaced merged
					next = change;
				}
				
				var dest:Object = mergeContext.getCachedObject(change, true);
				if (dest == null) {
					// Entity not found locally : nothing to do, we can't apply incremental changes
					log.warn("Incoming change received for unknown entity {0}", change.className + ":" + change.uid);
					continue;
				}
				
				if (dest !== previous && previous && !change.isForEntity(previous)) {
					// Cannot apply changes if provided change has not the same uid than the previous object
					continue;
				}
				
				var p:String, val:Object, saveSkipDirtyCheck:Boolean, saveUninitAllowed:Boolean;
				
				if (local) {
					saveSkipDirtyCheck = mergeContext.skipDirtyCheck;
					saveUninitAllowed = mergeContext.uninitializeAllowed;
					try {
						mergeContext.skipDirtyCheck = true;
						mergeContext.uninitializeAllowed = false;
						
						// Changes built locally just need to have their referenced content merged
						// to initialize their uid and attach them to the local context
						for (p in change.changes) {
							val = change.changes[p];
							if (val is CollectionChanges) {
								for each (var cc:CollectionChange in val.changes) {
									if (cc.key != null && !(cc.key is IEntityRef))
										mergeContext.mergeExternal(cc.key, null, dest, p);
									if (cc.value != null && !(cc.value is IEntityRef))
										mergeContext.mergeExternal(cc.value, null, dest, p);
								}
							}
							else
								mergeContext.mergeExternal(val, null, dest, p);
						}
					}
					finally {
						mergeContext.uninitializeAllowed = saveUninitAllowed;
						mergeContext.skipDirtyCheck = saveSkipDirtyCheck;
					}
					
					continue;
				}
				
				if (next == null)
					next = dest;
				
				var desc:EntityDescriptor = mergeContext.getEntityDescriptor(IEntity(dest));
				
				saveUninitAllowed = mergeContext.uninitializeAllowed;				
				try {
					mergeContext.uninitializeAllowed = false;
					
					var mergedChanges:Object = {};
					var templateObject:Object = Type.forInstance(dest).constructor.newInstance();
					var incomingEntity:Object = lookupEntity(mergeContext, change.changes, dest);
					
					// Create an entity proxy for the current processed target and apply changes on it
					for (p in change.changes) {
						val = change.changes[p];
						
						if (val is CollectionChanges) {
							var coll:Object = dest[p];
							if (coll is IPersistentCollection && !IPersistentCollection(coll).isInitialized()) {
								// Cannot update an uninitialized collection
								log.debug("Incoming change for uninitialized collection {0}:{1}.{2}", change.className, change.uid, p);
								continue;
							}
							
							var cacheKey:String = "CollChange::" + getQualifiedClassName(dest) + ":" + dest.uid + "." + p;
							if (mergeContext.getCachedMerge(cacheKey) != null) {
								log.warn("Incoming change skipped {0}:{1}.{2}, already processed", change.className, change.uid, p);
								continue;
							}
							mergeContext.pushMerge(cacheKey, dest[p], false);
							
							var saved:Object = mergeContext.getSavedProperties(dest);
							var unsaved:Boolean = mergeContext.isUnsaved(dest);
							var receivedEntity:Object;
							
							if (coll is IList) {
								var mergedColl:IList = null;
								receivedEntity = lookupEntity(mergeContext, val, dest);
								// Check if we can find the complete initialized list in the incoming changes and use it instead of incremental updates
								if (receivedEntity != null && receivedEntity[p] is IPersistentCollection && receivedEntity[p].isInitialized())
									mergedColl = receivedEntity[p];
								else {
									mergedColl = Type.forInstance(coll is IPropertyHolder ? IPropertyHolder(coll).object : coll).constructor.newInstance() as IList;
									if (!unsaved) {
										var srcColl:IList = coll is ListCollectionView ? ListCollectionView(coll).list : IList(coll);
										for each (var elt:Object in srcColl)
											mergedColl.addItem(elt);
									}
									applyListChanges(mergeContext, mergedColl, CollectionChanges(val), saved && saved[p] is Array ? (saved[p] as Array) : null);
								}
								
								if (mergedColl is ListCollectionView) {
									ListCollectionView(mergedColl).filterFunction = ListCollectionView(coll).filterFunction;
									ListCollectionView(mergedColl).sort = ListCollectionView(coll).sort;
									ListCollectionView(mergedColl).refresh();
								}
								
								mergedChanges[p] = mergedColl;
							}
							else if (coll is IMap) {
								var mergedMap:IMap = null;
								receivedEntity = lookupEntity(mergeContext, val, dest);
								// Check if we can find the complete initialized map in the incoming changes and use it instead of incremental updates
								if (receivedEntity != null && receivedEntity[p] is IPersistentCollection && receivedEntity[p].isInitialized())
									mergedMap = receivedEntity[p];
								else {
									mergedMap = Type.forInstance(coll is IPropertyHolder ? IPropertyHolder(coll).object : coll).constructor.newInstance() as IMap;
									if (!unsaved) {
										for each (var key:Object in coll.keySet)
											mergedMap.put(key, coll.get(key));
									}
									applyMapChanges(mergeContext, mergedMap, CollectionChanges(val), saved && saved[p] is Array ? (saved[p] as Array) : null);
								}
								
								mergedChanges[p] = mergedMap;
							}
						}
						else
							mergedChanges[p] = val;
					}
					
					var version:Number = change.version;
					// If dest version is greater than received change, use it instead
					// That means that the received Change change is probably inconsistent with its content
					if (incomingEntity != null && !isNaN(incomingEntity[desc.versionPropertyName]) && incomingEntity[desc.versionPropertyName] > version)
						version = incomingEntity[desc.versionPropertyName];
					
					var changeProxy:ChangeProxy = new ChangeProxy(change.uid, desc.idPropertyName, change.id, 
						desc.versionPropertyName, version, mergedChanges, templateObject);
					
					// Merge the proxy (only actual changes will be merged, values not in mergedChanges will be ignored)
					mergeContext.mergeExternal(changeProxy, dest, parent, propertyName);
					
					// Ensure updated collections/maps will be processed only once
					// Mark them in the current merge cache
					for (p in mergedChanges) {
						if (dest[p] is IList || dest[p] is IMap)
							mergeContext.pushMerge(dest[p], dest[p], false);
					}
				}
				finally {
					mergeContext.uninitializeAllowed = saveUninitAllowed;
				}
				
				if (dest != null)
					log.debug("merge change result: {0}", BaseContext.toString(dest));
			}
			
			return next;
		}
		
		
		private function applyListChanges(mergeContext:MergeContext, coll:IList, ccs:CollectionChanges, savedArray:Array):void {
			var found:Boolean, i:uint, j:uint, ce:CollectionEvent, cc:CollectionChange, item:*;
			
			// Detect element permutations (1 REMOVE + 1 ADD for same entity)
			var moves:Array = [];
			
			if (savedArray != null) {
				var savedList:Array = [];
				for each (var e:Object in coll)
					savedList.push(e);
				
				// Apply saved operations
				for (var ice:int = savedArray.length-1; ice >= 0; ice--) {
					ce = CollectionEvent(savedArray[ice]);
					if (ce.kind == CollectionEventKind.ADD)
						savedList.splice(ce.location, 1);
					else if (ce.kind == CollectionEventKind.REMOVE)
						savedList.splice(ce.location, 0, ce.items[0]);
					else if (ce.kind == CollectionEventKind.REPLACE)
						savedList[ce.location] = ce.items[0];
				}
				
				// Apply received operations
				for each (cc in ccs.changes) {
					if (cc.type == -1) {
						// Not found in local changes, apply remote change
						if (cc.key != null && cc.key >= 0 && cc.value is ChangeRef && cc.value.isForEntity(savedList[cc.key]))
							savedList.splice(cc.key, 1);
						else if (cc.key != null && cc.key >= 0 && mergeContext.objectEquals(cc.value, savedList[cc.key]))
							savedList.splice(cc.key, 1);
						else if (cc.key == null && cc.value is ChangeRef) {
							for (i = 0; i < savedList.length; i++) {
								if (cc.value.isForEntity(savedList[i])) {
									savedList.splice(i, 1);
									i--;
								}
							}
						}
						else if (cc.key == null) {
							for (i = 0; i < savedList.length; i++) {
								if (mergeContext.objectEquals(cc.value, savedList[i])) {
									savedList.splice(i, 1);
									i--;
								}
							}
						}
					}
					else if (cc.type == 1) {
						// Not found in local changes, apply remote change
						if (cc.key != null && cc.key >= 0)
							savedList.splice(cc.key, 0, cc.value);
						else if (cc.key != null)
							savedList.push(cc.value);
						else
							savedList.push(cc.value);
					}
					else if (cc.type == 0 && cc.key != null && cc.key >= 0) {
						savedList[cc.key] = cc.value;
					}
				}
				
				// Check if local changes and remote changes give the same result
				if (savedList.length == coll.length) {
					var isSame:Boolean = true;
					for (i = 0; i < savedList.length; i++) {
						if (savedList[i] is ChangeRef) {
							if (!savedList[i].isForEntity(coll.getItemAt(i))) {
								isSame = false;
								break;
							}
						}
						else if (!mergeContext.objectEquals(coll.getItemAt(i), savedList[i])) {
							isSame = false;
							break;
						}
					}
					if (isSame) {
						// Replace content by received objects
						for (i = 0; i < savedList.length; i++) {
							if (coll.getItemAt(i) !== savedList[i] && !(savedList[i] is IEntityRef))
								coll.setItemAt(savedList[i], i);
						}
						return;
					}
				}
				
				for (i = 0; i < ccs.changes.length; i++) {
					if (ccs.changes[i].key == null)
						continue;
					
					for (j = i+1; j < ccs.changes.length; j++) {
						if (ccs.changes[j].type != -ccs.changes[i].type || !mergeContext.objectEquals(ccs.changes[i].value, ccs.changes[j].value))
							continue;
					
						var value:Object = ccs.changes[i].value;
						var fromIndex:int = -1, toIndex:int = -1;
						if (ccs.changes[i].type == -1) {
							fromIndex = ccs.changes[i].key;
							toIndex = ccs.changes[j].key;
						}
						else if (ccs.changes[i].key < ccs.changes[j].key) {
							fromIndex = ccs.changes[j].key-1;
							toIndex = ccs.changes[i].key;
						}
						else {
							fromIndex = ccs.changes[j].key;
							toIndex = ccs.changes[i].key-1;
						}
						
						// Lookup in savedArray
						found = false;
						
						for (var i2:int = 0; i2 < savedArray.length; i2++) {
							for (var j2:int = i2+1; j2 < savedArray.length; j2++) {
								if (((savedArray[i2].kind == CollectionEventKind.REMOVE && savedArray[j2].kind == CollectionEventKind.ADD)
									|| (savedArray[i2].kind == CollectionEventKind.ADD && savedArray[j2].kind == CollectionEventKind.REMOVE))
									&& savedArray[i2].items[0] === savedArray[j2].items[0]) {
									var fromIndex2:int = -1, toIndex2:int = -1;
									if (savedArray[i2].kind == CollectionEventKind.REMOVE) {
										fromIndex2 = savedArray[i2].location;
										toIndex2 = savedArray[j2].location;
									}
									else if (savedArray[i2].location < savedArray[j2].location) {
										fromIndex2 = savedArray[j2].location-1;
										toIndex2 = savedArray[i2].location;
									}
									else {
										fromIndex2 = savedArray[j2].location;
										toIndex2 = savedArray[i2].location-1;
									}
									
									if ((fromIndex2 == fromIndex && toIndex2 == toIndex && mergeContext.objectEquals(value, savedArray[i2].items[0]))
										|| (fromIndex2 == toIndex && toIndex2 == fromIndex && mergeContext.objectEquals(value, coll.getItemAt(fromIndex2)))) {
										moves.push(ccs.changes[i]);
										moves.push(ccs.changes[j]);
										found = true;
										break;
									}
								}
							}							
							if (found)
								break;
						}
						if (found)
							break;
					}
				}
			}
			
			for each (cc in ccs.changes) {
				if (moves.indexOf(cc) >= 0)
					continue;
				
				if (cc.type == -1) {
					found = false;
					// Check in current local changes if the received chance has already been applied
					if (savedArray != null) {
						for (i = 0; i < savedArray.length; i++) {
							ce = CollectionEvent(savedArray[i]);
							if (ce.kind == CollectionEventKind.REMOVE && (cc.key == null || ce.location == cc.key)) {
								for each (item in ce.items) {
									if ((cc.value is ChangeRef && cc.value.isForEntity(item)) || mergeContext.objectEquals(cc.value, item)) {
										found = true;
										break;
									}
								}
							}
						}
					}
					
					if (!found) {
						// Not found in local changes, apply remote change
						if (cc.key != null && cc.key >= 0 && cc.value is ChangeRef && cc.value.isForEntity(coll.getItemAt(cc.key)))
							coll.removeItemAt(cc.key);
						else if (cc.key != null && cc.key >= 0 && mergeContext.objectEquals(cc.value, coll.getItemAt(cc.key)))
							coll.removeItemAt(cc.key);
						else if (cc.key == null && cc.value is ChangeRef) {
							for (i = 0; i < coll.length; i++) {
								if (cc.value.isForEntity(coll.getItemAt(i))) {
									coll.removeItemAt(i);
									i--;
								}
							}
						}
						else if (cc.key == null) {
							for (i = 0; i < coll.length; i++) {
								if (mergeContext.objectEquals(cc.value, coll.getItemAt(i))) {
									coll.removeItemAt(i);
									i--;
								}
							}
						}
					}
				}
				else if (cc.type == 1) {
					found = false;
					// Check in current local changes if the received change has already been applied
					if (savedArray != null) {
						for (i = 0; i < savedArray.length; i++) {
							ce = CollectionEvent(savedArray[i]);
							if (ce.kind == CollectionEventKind.ADD && (cc.key == null || ce.location == cc.key)) {
								for (j = 0; j < ce.items.length; j++) {
									if (mergeContext.objectEquals(cc.value, ce.items[j]) || (cc.value is IEntityRef && cc.value.isForEntity(ce.items[j]))) {
										// If local change found, update the added value with the one received from the server
										if (cc.value is IEntity) {
											// Adjust location of stored event with following changes
											var currentLocation:int = ce.location;
											if (i < savedArray.length-1) {
												for (var k:uint = i+1; k < savedArray.length; k++) {
													var cek:CollectionEvent = CollectionEvent(savedArray[k]);
													if (cek.kind == CollectionEventKind.ADD && cek.location <= currentLocation)
														currentLocation += cek.items.length;
													else if (cek.kind == CollectionEventKind.REMOVE && cek.location <= currentLocation)
														currentLocation -= cek.items.length;
												}
											}
											coll[currentLocation] = cc.value;
										}
										found = true;
										break;
									}
								}
							}
						}
					}
					
					if (!found) {
						// Not found in local changes, apply remote change
						if (cc.key != null && cc.key >= 0)
							coll.addItemAt(cc.value, cc.key);
						else if (cc.key != null)
							coll.addItem(cc.value);
						else
							coll.addItem(cc.value);
					}
				}
				else if (cc.type == 0 && cc.key != null && cc.key >= 0) {
					coll.setItemAt(cc.value, cc.key);
				}
			}
		}
		
		private function applyMapChanges(mergeContext:MergeContext, map:IMap, ccs:CollectionChanges, savedArray:Array):void {
			var found:Boolean, i:uint, j:uint, ce:CollectionEvent, k:*, key:*, entry:*;
			
			for each (var cc:CollectionChange in ccs.changes) {
				if (cc.type == -1) {
					found = false;
					// Check in current local changes if the received change has already been applied
					if (savedArray != null) {
						for (i = 0; i < savedArray.length; i++) {
							ce = CollectionEvent(savedArray[i]);
							if (ce.kind == CollectionEventKind.REMOVE) {
								for each (entry in ce.items) {
									if (((cc.key is ChangeRef && cc.key.isForEntity(entry[0])) || mergeContext.objectEquals(cc.key, entry[0]))
										&& ((cc.value is ChangeRef && cc.value.isForEntity(entry[1])) || mergeContext.objectEquals(cc.value, entry[1]))) {
										found = true;
										break;
									}
								}
							}
						}
					}
					
					if (!found) {
						// Not found in local changes, apply remote change
						key = cc.key is ChangeRef ? mergeContext.getCachedObject(cc.key, true) : cc.key;
						
						if (key != null && cc.value is ChangeRef && cc.value.isForEntity(map.get(key)))
							map.remove(key);
						else if (key != null && mergeContext.objectEquals(cc.value, map.get(key)))
							map.remove(key);
					}
				}
				else if (cc.type == 1) {
					found = false;
					// Check in current local changes if the received change has already been applied
					if (savedArray != null) {
						for (i = 0; i < savedArray.length; i++) {
							ce = CollectionEvent(savedArray[i]);
							if (ce.kind == CollectionEventKind.ADD) {
								for each (entry in ce.items) {
									if (((cc.key is ChangeRef && cc.key.isForEntity(entry[0])) || mergeContext.objectEquals(cc.key, entry[0]))
										&& ((cc.value is ChangeRef && cc.value.isForEntity(entry[1])) || mergeContext.objectEquals(cc.value, entry[1]))) {
										// If local change found, update the added value with the one received from the server
										
										key = entry[0];
										for each (k in map.keySet) {
											if (mergeContext.objectEquals(k, entry[0])) {
												key = k;
												break;
											}
										}
										
										if (key is IEntity) {
											map.remove(key);
											map.put(cc.key, cc.value);
										}
										else if (cc.value is IEntity) {
											map.put(key, cc.value);
										}
										
										found = true;
										break;
									}
								}
							}
						}
					}
					
					if (!found) {
						// Not found in local changes, apply remote change
						key = cc.key is ChangeRef ? mergeContext.getCachedObject(cc.key, true) : cc.key;						
						map.put(key, cc.value);
					}
				}
			}
		}
		
		
		private function mergeChangeProxy(mergeContext:MergeContext, changeProxy:ChangeProxy, dest:Object, expr:IExpression, parent:Object, propertyName:String):Object {
			var next:Object = null;
			
			try {
				// Apply property updates
				// proxyGetter allows to intercept the merge of some values to replace them by those from the Change object
				// This allow to merge even private properties that we cannot update directly on the merged object							
				mergeContext.proxyGetter = function(obj:Object, parent:Object, propName:String):Object {
					if (parent === dest)
						return changeProxy[propName];
					return obj;
				};
				
				next = mergeContext.mergeExternal(changeProxy.merged, dest, parent, propertyName);
			}
			finally {
				mergeContext.proxyGetter = null;
			}
			
			return next;
		}
		
		
		private function lookupEntity(mergeContext:MergeContext, graph:Object, obj:Object, cache:Dictionary = null):Object {
			if (!(graph is Array) && (ObjectUtil.isSimple(graph) || graph is XML || graph is IValue || graph is ByteArray || graph is Enum))
				return null;
			
			if (cache == null)
				cache = new Dictionary();
			
			if (cache[graph] != null)
				return null;
			cache[graph] = true;
			
			if (graph is IEntity && !graph.meta::isInitialized())
				return null;
			
			if (mergeContext.objectEquals(graph, obj) && graph !== obj)
				return graph;
			
			var found:Object = null;
			if (graph is CollectionChanges) {
				for each (var cc:CollectionChange in graph.changes) {
					found = lookupEntity(mergeContext, cc, obj, cache);
					if (found != null)
						return found;
				}
			}
			else if (graph is CollectionChange) {
				if (graph.key != null) {
					found = lookupEntity(mergeContext, graph.key, obj, cache);
					if (found != null)
						return found;
				}
				if (graph.value != null) {
					found = lookupEntity(mergeContext, graph.value, obj, cache);
					if (found != null)
						return found;
				}
				return null;
			}

			var elt:Object, key:Object;
			if (graph is Array || graph is IList) {
				if (graph is IPersistentCollection && !graph.isInitialized())
					return null;
				for each (elt in graph) {
					found = lookupEntity(mergeContext, elt, obj, cache);
					if (found != null)
						return found;
				}
				return null;
			}
			else if (graph is IMap) {
				if (graph is IPersistentCollection && !graph.isInitialized())
					return null;
				for (key in graph.keySet) {
					found = lookupEntity(mergeContext, key, obj, cache);
					if (found != null)
						return found;
					found = lookupEntity(mergeContext, graph.get(key), obj, cache);
					if (found != null)
						return found;
				}
				return null;
			}
			else {
				var cinfo:Object = ObjectUtil.getClassInfo(graph, [ 'uid' ], { includeTransient: false, includeReadOnly: true });
				for each (var p:String in cinfo.properties) {
					found = lookupEntity(mergeContext, graph[p], obj, cache);
					if (found != null)
						return found;
				}
				return null;
			}
			
			return null;			
		}
	}
}
