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
    
    import flash.events.IEventDispatcher;
    import flash.utils.ByteArray;
    import flash.utils.Dictionary;
    
    import mx.collections.IList;
    import mx.core.IUID;
    import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	import mx.events.CollectionEventKind;
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
    import org.granite.persistence.PersistentBag;
    import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.EntityDescriptor;
    import org.granite.tide.IEntity;
    import org.granite.tide.IWrapper;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.collections.PersistentMap;
    import org.granite.util.Enum;


    use namespace meta;


	/**
	 * 	DirtyCheckContext handles dirty checking of managed entities
	 * 
     * 	@author William DRAI
	 */
    public class DirtyCheckContext {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.DirtyCheckContext");
    
        protected var _context:BaseContext;
        
        private var _dirtyCount:int = 0;
        private var _savedProperties:Dictionary = new Dictionary(true);
        


        public function DirtyCheckContext(context:BaseContext) {
            _context = context;
        }
        
        
        /**
         *	Entity manager is dirty when any entity/collection/map has been modified
         *
         *  @return is dirty
         */
        public function get dirty():Boolean {
            return _dirtyCount > 0;
        }
		
		
		/**
		 * 	Current map of saved properties
		 * 
		 *  @return saved properties
		 */
		public function get savedProperties():Dictionary {
			return _savedProperties;
		}
        
        
        /**
         *  Clear dirty cache
		 * 
		 * 	@param dispatch dispatch a PropertyChangeEvent on meta_dirty if the context was dirty before clearing
         */ 
        public function clear(dispatch:Boolean = true):void {
			var wasDirty:Boolean = dirty;
        	_dirtyCount = 0;
        	_savedProperties = new Dictionary(true);
			if (wasDirty && dispatch)
				_context.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "meta_dirty", true, false));
        }
        
        
        /**
         *  Check if the object has a dirty cache
         *
         *  @param object object to check
         * 
         *  @return true is value has been changed
         */ 
        public function isSaved(object:Object):Boolean {
        	return _savedProperties[object] != null;
        }
        
        
        /**
         *  Check if entity property has been changed since last remote call
         *
         *  @param entity entity to check
         *  @param propertyName property to check
         *  @param value current value to compare with saved value
         *   
         *  @return true is value has been changed
         */ 
        public function isEntityPropertyChanged(entity:IEntity, propertyName:String, value:Object):Boolean {
            var source:Object = _savedProperties[entity];
            if (source == null)
                source = entity;
            
            return source[propertyName] != value;
        }
        
        
        /**
         *  Check if entity has changed since last save point
         *
         *  @param entity entity to check
         *  @param propName property name
         *  @param value
         *   
         *  @return entity is dirty
         */ 
        public function isEntityChanged(entity:Object, propName:String = null, value:* = null):Boolean {
            var dirty:Boolean = false;
            var saveTracking:Boolean = _context.meta_tracking;
            _context.meta_tracking = false;
			
            var cinfo:Object = ObjectUtil.getClassInfo(entity, null, { includeTransient: false, includeReadOnly: false });
            var p:String;
            var val:Object, saveval:*;
            
            var entityDesc:EntityDescriptor = entity is IEntity ? _context.meta_tide.getEntityDescriptor(IEntity(entity)) : null;
            var save:Object = _savedProperties[entity];
			var versionPropertyName:String = entityDesc != null ? entityDesc.versionPropertyName : null;
            
            for each (p in cinfo.properties) {
                if (p == versionPropertyName || p == 'meta_dirty')
                    continue;
                
                val = (p == propName ? value : entity[p]);
                saveval = save ? save[p] : null;
                var o:Object;
                if (save && ((val && (ObjectUtil.isSimple(val) || val is ByteArray))
                	|| (saveval && (ObjectUtil.isSimple(saveval) || saveval is ByteArray)))) {
                    if (saveval !== undefined && ObjectUtil.compare(val, save[p]) != 0) {
                        dirty = true;
                        break;
                    }
                }
				else if (save && (val is IValue || saveval is IValue || val is Enum || saveval is Enum)) {
					if (saveval !== undefined && ((!val && saveval) || !val.equals(saveval))) {
						dirty = true;
						break;
					} 
				}
				else if (save && (val is IUID || saveval is IUID)) {
					if (saveval !== undefined && val !== save[p]) {
						dirty = true;
						break;
					}
				}
                else if ((val is IList || val is IMap) && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
                    var savedArray:Array = saveval as Array;
                    if (savedArray && savedArray.length > 0) {
                        dirty = true;
                        break;
                    }
                }
				else if (val != null && val is IEventDispatcher 
					&& !(val is IUID || val is Enum || val is IValue || val is ByteArray || val is XML) 
					&& isEntityChanged(val)) {
					dirty = true;
					break;
				}
            }
            
            _context.meta_tracking = saveTracking;
            return dirty;
        }


		private function isSame(val1:*, val2:*):Boolean {
			if (val1 == null && isEmpty(val2))
				return true;
			else if (val2 == null && isEmpty(val1))
				return true;
			else if (ObjectUtil.isSimple(val1) && ObjectUtil.isSimple(val2))
				return ObjectUtil.compare(val1, val2, 0) == 0;
			else if (val1 is ByteArray && val2 is ByteArray)
				return ObjectUtil.compare(val1, val2, 0) == 0;
			else if ((val1 is IValue && val2 is IValue) || (val1 is Enum && val2 is Enum))
				return val1.equals(val2);
			else {
				var n:* = val1 is IWrapper ? val1.object : val1;
				var o:* = val2 is IWrapper ? val2.object : val2;
				if (n is IUID && o is IUID)
					return n.uid === o.uid;
				else
					return n === o;
			}
			return val1 === val2;
		}
		
		private function isSameExt(val1:*, val2:*):Boolean {
			var found:Boolean, idx:uint, e:Object, f:Object;
			if (val1 == null && isEmpty(val2))
				return true;
			else if (val2 == null && isEmpty(val1))
				return true;
			else if (ObjectUtil.isSimple(val1) && ObjectUtil.isSimple(val2))
				return ObjectUtil.compare(val1, val2, 0) == 0;
			else if (val1 is ByteArray && val2 is ByteArray)
				return ObjectUtil.compare(val1, val2, 0) == 0;
			else if ((val1 is IValue && val2 is IValue) || (val1 is Enum && val2 is Enum))
				return val1.equals(val2);
			else if (val1 is Array && val2 is Array) {
				if (val1.length != val2.length)
					return false;
				for (idx = 0; idx < val1.length; idx++) {
					if (!isSameExt(val1[idx], val2[idx]))
						return false;
				}
				return true;
			}
			else if ((val1 is PersistentSet && val2 is PersistentSet) || (val1 is PersistentBag && val2 is PersistentBag)) {
				if (val1.length != val2.length)
					return false;
				for each (e in val1) {
					found = false;
					for each (f in val2) {
						if (isSameExt(e, f)) {
							found = true;
							break;
						}
					}
					if (!found)
						return false;
				}
				for each (e in val2) {
					found = false;
					for each (f in val1) {
						if (isSameExt(e, f)) {
							found = true;
							break;
						}
					}
					if (!found)
						return false;
				}
				return true;
			}
			else if (val1 is IList && val2 is IList) {
				if ((val1 is IPersistentCollection && !val1.isInitialized()) || (val2 is IPersistentCollection && !val2.isInitialized()))
					return false;
				if (val1.length != val2.length)
					return false;
				for (idx = 0; idx < val1.length; idx++) {
					if (!isSameExt(val1.getItemAt(idx), val2.getItemAt(idx)))
						return false;
				}
				return true;
			}
			else if (val1 is IMap && val2 is IMap) {
				if ((val1 is IPersistentCollection && !val1.isInitialized()) || (val2 is IPersistentCollection && !val2.isInitialized()))
					return false;
				if (val1.length != val2.length)
					return false;
				for each (e in val1.keySet) {
					found = false;
					for each (f in val2.keySet) {
						if (isSameExt(e, f)) {
							found = true;
							break;
						}
					}
					if (!found)
						return false;
					if (!isSameExt(val1.get(e), val2.get(e)))
						return false;
				}
				for each (e in val2.keySet) {
					found = false;
					for each (f in val1) {
						if (isSameExt(e, f)) {
							found = true;
							break;
						}
					}
					if (!found)
						return false;
					if (!isSameExt(val1.get(e), val2.get(e)))
						return false;
				}
				return true;
			}
			else {
				var n:* = val1 is IWrapper ? val1.object : val1;
				var o:* = val2 is IWrapper ? val2.object : val2;
				if (n is IUID && o is IUID)
					return n.uid === o.uid;
				else
					return n === o;
			}
			return val1 === val2;
		}
		
        /**
         *  @private 
         *  Interceptor for managed entity setters
         *
         *  @param entity entity to intercept
         *  @param propName property name
         *  @param oldValue old value
         *  @param newValue new value
         */ 
        public function setEntityProperty(entity:IEntity, propName:String, oldValue:*, newValue:*):void {
			entityPropertyChangeHandler(entity, entity, propName, oldValue, newValue);
        }
		
		
		private function entityPropertyChangeHandler(owner:IEntity, entity:Object, propName:String, oldValue:*, newValue:*):void {
			var oldDirty:Boolean = _dirtyCount > 0;
			
			var diff:Boolean = !isSame(oldValue, newValue);
			
			if (diff) {
				var oldDirtyEntity:Boolean = isEntityChanged(owner, propName, oldValue);
				
				var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(owner);
				var save:Object = _savedProperties[entity];
				var unsaved:Boolean = !save;
				
				if (unsaved || (desc.versionPropertyName != null && 
					save[desc.versionPropertyName] != Object(owner)[desc.versionPropertyName] 
						&& !(isNaN(save[desc.versionPropertyName]) && isNaN(Object(owner)[desc.versionPropertyName])))) {
					
					save = new Object();
					if (desc.versionPropertyName != null)
						save[desc.versionPropertyName] = Object(owner)[desc.versionPropertyName];
					_savedProperties[entity] = save;
					save[propName] = oldValue;
					if (unsaved)
						_dirtyCount++;
				}
				
				if (save && (desc.versionPropertyName == null 
					|| save[desc.versionPropertyName] == Object(owner)[desc.versionPropertyName]
					|| (isNaN(save[desc.versionPropertyName]) && isNaN(Object(owner)[desc.versionPropertyName])))) {
					
					if (!save.hasOwnProperty(propName))
						save[propName] = oldValue;
					
					if (isSame(save[propName], newValue)) {
						delete save[propName];
						var count:int = 0;
						for (var p:Object in save) {
							if (p != desc.versionPropertyName)
								count++;
						}
						if (count == 0) {
							delete _savedProperties[entity];
							_dirtyCount--;
						}
					}
				}
				
				var newDirtyEntity:Boolean = isEntityChanged(owner);
				if (newDirtyEntity !== oldDirtyEntity) {
					var pce:PropertyChangeEvent = new PropertyChangeEvent("dirtyChange", false, false, 
						PropertyChangeEventKind.UPDATE, "meta_dirty", oldDirtyEntity, newDirtyEntity);
					owner.dispatchEvent(pce);
				}
			}
			
			if ((_dirtyCount > 0) !== oldDirty)
				_context.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "meta_dirty", oldDirty, _dirtyCount > 0));
		}
		
		
		/**
		 *  @private 
		 *  Embedded event handler to save changes on managed objects
		 *
		 *  @param event collection event
		 */ 
		public function entityEmbeddedChangeHandler(event:PropertyChangeEvent):void {
			var owner:Object = _context.meta_getOwnerEntity(event.target);
			if (owner == null) {
				log.warn("Owner entity not found for embedded object {0}, cannot process dirty checking", BaseContext.toString(event.target));
				return;
			}
			
			if (owner is Array && owner[0] is IEntity)
				entityPropertyChangeHandler(IEntity(owner[0]), event.target, event.property as String, event.oldValue, event.newValue);
		}


        /**
         *  @private 
         *  Collection event handler to save changes on managed collections
         *
         *  @param event collection event
         */ 
        public function entityCollectionChangeHandler(event:CollectionEvent):void {
            var oldDirty:Boolean = _dirtyCount > 0;
            
			var ownerEntity:Object = _context.meta_getOwnerEntity(event.target);
			if (ownerEntity == null) {
				log.warn("Owner entity not found for collection {0}, cannot process dirty checking", BaseContext.toString(event.target));
				return;
			}
			
			var owner:Object = ownerEntity[0]
			var propName:String = String(ownerEntity[1]);
			
			var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(owner));
			var oldDirtyEntity:Boolean = isEntityChanged(owner);
			
			var esave:Object = _savedProperties[owner];
			var unsaved:Boolean = !esave;
			
			if (unsaved || (desc.versionPropertyName != null && 
				(esave[desc.versionPropertyName] != Object(owner)[desc.versionPropertyName] 
					&& !(isNaN(esave[desc.versionPropertyName]) && isNaN(Object(owner)[desc.versionPropertyName]))))) {

				esave = new Object();
				if (desc.versionPropertyName != null)
					esave[desc.versionPropertyName] = Object(owner)[desc.versionPropertyName];
				_savedProperties[owner] = esave;
				if (unsaved)
					_dirtyCount++;
			}
		
        	var save:Array = esave[propName] as Array;
			if (save == null)
				save = esave[propName] = [];
            
			if (esave && (desc.versionPropertyName == null 
				|| esave[desc.versionPropertyName] == Object(owner)[desc.versionPropertyName]
				|| (isNaN(esave[desc.versionPropertyName]) && isNaN(Object(owner)[desc.versionPropertyName])))) {
				
	            var found:Boolean = false;
				var count:int, p:Object, i:int, j:int;
	            var pce:PropertyChangeEvent = null, ce:CollectionEvent = null;
				var location:int = event.location;
				
				var actualLocations:Array = [];
				for (i = 0; i < save.length; i++) {
					actualLocations[i] = save[i].location;
                    for (j = 0; j < i; j++) {
                        if (save[j].kind == CollectionEventKind.REMOVE && actualLocations[i] <= save[j].location)
                            actualLocations[j]--;
						else if (save[j].kind == CollectionEventKind.ADD && actualLocations[i] <= save[j].location)
							actualLocations[j]++;
					}
				}

	            for (i = 0; i < save.length; i++) {						
	                if (((event.kind == CollectionEventKind.ADD && save[i].kind == CollectionEventKind.REMOVE)
	                    || (event.kind == CollectionEventKind.REMOVE && save[i].kind == CollectionEventKind.ADD))
	                    && event.items.length == 1 && save[i].items.length == 1 
						&& isSame(event.items[0], save[i].items[0]) && location == actualLocations[i]) {
						
	                    save.splice(i, 1);
						// Adjust location of other saved events because an element added/removed locally has been removed/added
						for each (ce in save) {
							if (event.kind == CollectionEventKind.REMOVE && ce.kind == CollectionEventKind.ADD && ce.location > event.location)
								ce.location--;
							else if (event.kind == CollectionEventKind.ADD && ce.kind == CollectionEventKind.REMOVE && ce.location > event.location)
								ce.location++;
						}
						
	                    if (save.length == 0) {
	                        delete esave[propName];
							count = 0;
							for (p in esave) {
								if (p != desc.versionPropertyName)
									count++;
							}
							if (count == 0) {
								delete _savedProperties[owner];
								_dirtyCount--;
							}
	                    }
	                    i--;
	                    found = true;
	                }
					else if (event.kind == CollectionEventKind.REPLACE && save[i].kind == CollectionEventKind.REPLACE
						&& event.items.length == 1 && save[i].items.length == 1
						&& event.location == actualLocations[i]) {
						
						if (isSame(event.items[0].oldValue, save[i].items[0].newValue)
							&& isSame(event.items[0].newValue, save[i].items[0].oldValue)) {
							
							save.splice(i, 1);
							if (save.length == 0) {
								delete esave[propName];
								count = 0;
								for (p in esave) {
									if (p != desc.versionPropertyName)
										count++;
								}
								if (count == 0) {
									delete _savedProperties[owner];
									_dirtyCount--;
								}
							}
							i--;
						}
						else {
							save[i].items[0].newValue = event.items[0].newValue;
						}
						found = true;
					}
	            }
	            if (!found)
	                save.push(event);
			}
			
			var newDirtyEntity:Boolean = isEntityChanged(owner);
			if (newDirtyEntity !== oldDirtyEntity) {
				pce = new PropertyChangeEvent("dirtyChange", false, false, 
					PropertyChangeEventKind.UPDATE, "meta_dirty", oldDirtyEntity, newDirtyEntity);
				owner.dispatchEvent(pce);
			}
	       	
	        if ((_dirtyCount > 0) || oldDirty)
                _context.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "meta_dirty", oldDirty, _dirtyCount > 0));
        }


        /**
         *  @private 
         *  Collection event handler to save changes on managed maps
         *
         *  @param event map event
         */ 
        public function entityMapChangeHandler(event:CollectionEvent):void {
			var oldDirty:Boolean = _dirtyCount > 0;
			
			var ownerEntity:Object = _context.meta_getOwnerEntity(event.target);
			if (ownerEntity == null) {
				log.warn("Owner entity not found for map {0}, cannot process dirty checking", BaseContext.toString(event.target));
				return;
			}
			var owner:Object = ownerEntity[0]
			var propName:String = String(ownerEntity[1]);
			
			var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(owner));
			var oldDirtyEntity:Boolean = isEntityChanged(owner);
			
			var esave:Object = _savedProperties[owner];
			var unsaved:Boolean = !esave;
			
			if (unsaved || (desc.versionPropertyName != null && 
				(esave[desc.versionPropertyName] != Object(owner)[desc.versionPropertyName] 
					&& !(isNaN(esave[desc.versionPropertyName]) && isNaN(Object(owner)[desc.versionPropertyName]))))) {
				
				esave = new Object();
				if (desc.versionPropertyName != null)
					esave[desc.versionPropertyName] = Object(owner)[desc.versionPropertyName];
				_savedProperties[owner] = esave;
				if (unsaved)
					_dirtyCount++;
			}
			
			var save:Array = esave[propName] as Array;
			if (save == null)
				save = esave[propName] = [];
			
			if (esave && (desc.versionPropertyName == null 
				|| esave[desc.versionPropertyName] == Object(owner)[desc.versionPropertyName]
				|| (isNaN(esave[desc.versionPropertyName]) && isNaN(Object(owner)[desc.versionPropertyName])))) {
		            
	            var found:Boolean = false;
				var count:int, p:Object;
	            var pce:PropertyChangeEvent = null;
	            for (var i:int = 0; i < save.length; i++) {
	                if (((event.kind == CollectionEventKind.ADD && save[i].kind == CollectionEventKind.REMOVE)
	                    || (event.kind == CollectionEventKind.REMOVE && save[i].kind == CollectionEventKind.ADD))
	                    && event.items.length == 1 && save[i].items.length == 1 && event.location == save[i].location 
	                    && event.items[0][0] === save[i].items[0][0] && event.items[0][1] === save[i].items[0][1]) {
						
						save.splice(i, 1);
						if (save.length == 0) {
							delete esave[propName];
							count = 0;
							for (p in esave) {
								if (p != desc.versionPropertyName)
									count++;
							}
							if (count == 0) {
								delete _savedProperties[owner];
								_dirtyCount--;
							}
						}
						i--;
						found = true;
	                }
					else if (event.kind == CollectionEventKind.REPLACE && save[i].kind == CollectionEventKind.REPLACE
						&& event.items.length == 1 && save[i].items.length == 1
						&& event.items[0].property == save[i].items[0].property) {
						
						if (isSame(event.items[0].oldValue, save[i].items[0].newValue)
							&& isSame(event.items[0].newValue, save[i].items[0].oldValue)) {
						
							save.splice(i, 1);
							if (save.length == 0) {
								delete esave[propName];
								count = 0;
								for (p in esave) {
									if (p != desc.versionPropertyName)
										count++;
								}
								if (count == 0) {
									delete _savedProperties[owner];
									_dirtyCount--;
								}
							}
							i--;
						}
						else {
							save[i].items[0].newValue = event.items[0].newValue;
						}
						found = true;
					}
	            }
	            if (!found)
	                save.push(event);
			}
			
			var newDirtyEntity:Boolean = isEntityChanged(owner);
			if (newDirtyEntity !== oldDirtyEntity) {
				pce = new PropertyChangeEvent("dirtyChange", false, false, 
					PropertyChangeEventKind.UPDATE, "meta_dirty", oldDirtyEntity, newDirtyEntity);
				owner.dispatchEvent(pce);
			}
			
			if ((_dirtyCount > 0) || oldDirty)
				_context.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "meta_dirty", oldDirty, _dirtyCount > 0));
        }


        /**
         *  @private 
         *  Mark an object merged from the server as not dirty
         *
         *  @param object merged object
         */ 
        public function markNotDirty(object:Object, entity:IEntity = null):void {
        	if (_savedProperties[object] == null)
        		return;
        	
            var oldDirty:Boolean = _dirtyCount > 0;
        	
        	var oldDirtyEntity:Boolean = false;
        	if (!entity && object is IEntity)
        		entity = object as IEntity;
        	if (entity)
                oldDirtyEntity = isEntityChanged(entity);
        	
            delete _savedProperties[object];
            
            if (entity) {
                var newDirtyEntity:Boolean = isEntityChanged(entity);
                if (newDirtyEntity !== oldDirtyEntity) {
                	var pce:PropertyChangeEvent = new PropertyChangeEvent("dirtyChange", false, false, 
                		PropertyChangeEventKind.UPDATE, "meta_dirty", oldDirtyEntity, newDirtyEntity);
                	entity.dispatchEvent(pce);
                }
            }
            
            _dirtyCount--;

	        if ((_dirtyCount > 0) !== oldDirty)
                _context.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "meta_dirty", oldDirty, _dirtyCount > 0));
      	}
		
		
		/**
		 *  @private 
		 *  Check if dirty properties of an object are the same than those of another entity
		 *  When they are the same, unmark the dirty flag
		 *
		 *  @param entity merged entity
		 *  @param source source entity
		 *  @return true if the entity is still dirty after comparing with incoming object
		 */ 
		public function checkAndMarkNotDirty(entity:IEntity, source:Object):Boolean {
			var save:Object = _savedProperties[entity];
			if (save == null)
				return false;
			
			var oldDirty:Boolean = _dirtyCount > 0;			
			var oldDirtyEntity:Boolean = isEntityChanged(entity);
			
			var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(entity));
			var merged:Array = [];
			
			for (var propName:String in save) {
				if (propName == desc.versionPropertyName)
					continue;
				var localValue:Object = entity[propName];
				var sourceValue:Object = source.hasOwnProperty(propName) ? source[propName] : null;
				if (localValue is PersistentCollection)
					localValue = localValue.list;
				else if (localValue is PersistentMap)
					localValue = localValue.map;
				if (isSameExt(localValue, sourceValue))
					merged.push(propName);
			}
			
			for each (propName in merged)
				delete save[propName];
			
			var count:uint = 0;
			for (propName in save) {
				if (propName != desc.versionPropertyName)
					count++;
			}
			if (count == 0) {
				delete _savedProperties[entity];
				_dirtyCount--;
			}
				
			var newDirtyEntity:Boolean = isEntityChanged(entity);
			if (newDirtyEntity !== oldDirtyEntity) {
				var pce:PropertyChangeEvent = new PropertyChangeEvent("dirtyChange", false, false, 
					PropertyChangeEventKind.UPDATE, "meta_dirty", oldDirtyEntity, newDirtyEntity);
				entity.dispatchEvent(pce);
			}
			
			if ((_dirtyCount > 0) !== oldDirty)
				_context.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "meta_dirty", oldDirty, _dirtyCount > 0));
			
			return newDirtyEntity;
		}
        
        
        /**
         *	@private
         *  Internal implementation of entity reset
         */ 
        public function resetEntity(entity:IEntity, cache:Dictionary):void {
	    	try {
	    		// Should not try to reset uninitialized entities
		    	if (!Object(entity).meta::isInitialized())
		    		return;
		    }
		    catch (e:ReferenceError) {
		    	// The entity class does not implement meta:isInitialized
		    }
        	
            if (cache[entity] != null)
                return;
            cache[entity] = entity;
            
            var save:Object = _savedProperties[entity];
            var a:int, b:int, z:int;
            var ce:CollectionEvent;
            var savedArray:Array
            var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(entity);
            
            var cinfo:Object = ObjectUtil.getClassInfo(entity, null, { includeTransient: false, includeReadOnly: false });
            for each (var p:String in cinfo.properties) {
                if (p == desc.versionPropertyName)
                    continue;
                
                var val:Object = entity[p];
                var o:Object, removed:Array = null;
				if (val is IList && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
					savedArray = save ? save[p] as Array : null;
					if (savedArray) {
						for (a = savedArray.length-1; a >= 0; a--) {
							ce = savedArray[a] as CollectionEvent;
							if (ce.kind == CollectionEventKind.ADD) {
								if (removed == null)
									removed = [];
								for (z = 0; z < ce.items.length; z++)
									removed.push(ce.target.removeItemAt(ce.location));
							}
							else if (ce.kind == CollectionEventKind.REMOVE) {
								for (z = 0; z < ce.items.length; z++)
									ce.target.addItemAt(ce.items[z], ce.location+z); 
							}
							else if (ce.kind == CollectionEventKind.REPLACE) {
								if (removed == null)
									removed = [];
								for (z = 0; z < ce.items.length; z++) {
									removed.push(ce.target.setItemAt(ce.items[z].oldValue, ce.location+z));
								}
							}
						}
						
						// Must be here because collection reset has triggered other useless CollectionEvents
						markNotDirty(val, entity);
					}
					for each (o in val) {
						if (o is IEntity)
							resetEntity(IEntity(o), cache);
					}
					if (removed != null) {
						for each (o in removed) {
							if (o is IEntity)
								resetEntity(IEntity(o), cache);
						}
					}
				}
				else if (val is IMap && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
					var map:IMap = IMap(val);
					savedArray = save ? save[p] as Array : null;
					if (savedArray) {
						for (a = savedArray.length-1; a >= 0; a--) {
							ce = savedArray[a] as CollectionEvent;
							if (ce.kind == CollectionEventKind.ADD) {
								if (removed == null)
									removed = [];
								for (z = 0; z < ce.items.length; z++) {
									removed.push(map.remove(ce.items[z][0]));
									removed.push(ce.items[z][0]);
								}
							}
							else if (ce.kind == CollectionEventKind.REMOVE) {
								for (z = 0; z < ce.items.length; z++)
									map.put(ce.items[z][0], ce.items[z][1]);
							}
							else if (ce.kind == CollectionEventKind.REPLACE) {
								if (removed == null)
									removed = [];
								for (z = 0; z < ce.items.length; z++) {
									removed.push(map.put(ce.items[z].property, ce.items[z].oldValue));
								}
							}
						}
						
						// Must be here because collection reset has triggered other useless CollectionEvents
						markNotDirty(val, entity);
					}
					for each (var key:Object in map.keySet) {
						var value:Object = map.get(key);
						if (key is IEntity)
							resetEntity(IEntity(key), cache);
						if (value is IEntity)
							resetEntity(IEntity(value), cache);
					}
					if (removed != null) {
						for each (o in removed) {
							if (o is IEntity)
								resetEntity(IEntity(o), cache);
						}
					}
				}
				else if (save && (ObjectUtil.isSimple(val) || ObjectUtil.isSimple(save[p]))) {
                    if (save[p] !== undefined)
                        entity[p] = save[p];
                }
				else if (save && (val is Enum || save[p] is Enum || val is IValue || save[p] is IValue)) {
					if (save[p] !== undefined)
						entity[p] = save[p];
				} 
                else if (val is IEntity || (save && save[p] is IEntity)) {
                	if (save && save[p] !== undefined && !_context.meta_objectEquals(val, save[p]))
                		entity[p] = save[p];
                	else
                    	resetEntity(IEntity(val), cache);
                }
            }
            
            // Must be here because entity reset may have triggered useless new saved properties
            markNotDirty(entity);
        }
		
		
		/**
		 *	@private
		 *  Internal implementation of entity reset all
		 */ 
		public function resetAllEntities(cache:Dictionary):void {
			var found:Boolean = false;
			do {
				found = false;
				for (var entity:Object in _savedProperties) {
					if (entity is IEntity) {
						found = true;
						resetEntity(IEntity(entity), cache);
						break;
					}
				}
			}
			while (found);
			
			if (_dirtyCount > 0)
				log.error("Incomplete reset of context, could be a bug");
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
