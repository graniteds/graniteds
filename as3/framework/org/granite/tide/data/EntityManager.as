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
    import flash.utils.Proxy;
    import flash.utils.flash_proxy;
    import flash.utils.getQualifiedClassName;
    
    import mx.collections.ICollectionView;
    import mx.collections.IList;
    import mx.collections.ListCollectionView;
    import mx.core.IUID;
    import mx.data.IManaged;
    import mx.data.utils.Managed;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    
    import org.granite.IValue;
    import org.granite.collections.IMap;
    import org.granite.collections.IPersistentCollection;
    import org.granite.collections.UIDWeakSet;
    import org.granite.meta;
    import org.granite.reflect.Type;
    import org.granite.tide.BaseContext;
    import org.granite.tide.EntityDescriptor;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IEntityRef;
    import org.granite.tide.IExpression;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.collections.PersistentMap;
    import org.granite.tide.data.events.TideDataConflictsEvent;
    import org.granite.util.Enum;


    use namespace flash_proxy;
    use namespace object_proxy;
    use namespace meta;


	/**
	 * 	PersistenceContext is the base implementation of the entity container context
	 * 
     * 	@author William DRAI
	 */
    public class EntityManager {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.EntityManager");
    
    	private var _context:BaseContext;
        private var _customMergers:Array = null;

        private var _dirtyCheckContext:DirtyCheckContext = null;
        private var _entitiesByUID:UIDWeakSet = new UIDWeakSet();
        private var _entityReferences:Dictionary = new Dictionary(true);
        private var _trackingListeners:Dictionary = new Dictionary(true);

        private var _mergeContext:MergeContext;



        public function EntityManager(context:BaseContext) {
            super();
            _context = context;
            _dirtyCheckContext = new DirtyCheckContext(_context);
            _mergeContext = new MergeContext(_context, this, _dirtyCheckContext);
        }
        
        
        /**
         * 	@private
         *  Clear the current context
         *  Destroys all components/context variables
         */
        public function clear():void {
            for each (var e:Object in _entitiesByUID.data) {
            	if (e is IEntity)
            		Managed.setEntityManager(IEntity(e), null);
            }
        	_entitiesByUID = new UIDWeakSet();
        	_entityReferences = new Dictionary(true);
            _dirtyCheckContext.clear();
            _mergeContext.clear();
            
            for (var obj:Object in _trackingListeners) {
            	switch (_trackingListeners[obj]) {
            	case "entityCollection":
            		IEventDispatcher(obj).removeEventListener(CollectionEvent.COLLECTION_CHANGE, entityCollectionChangeHandler);
            		break;
            	case "collection":
                    IEventDispatcher(obj).removeEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_collectionChangeHandler);
                    break;
            	case "entityMap":
            		IEventDispatcher(obj).removeEventListener(CollectionEvent.COLLECTION_CHANGE, entityMapChangeHandler);
            		break;
            	case "map":
                    IEventDispatcher(obj).removeEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_mapChangeHandler);
                    break;
				case "entityEmbedded":
					IEventDispatcher(obj).removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, entityEmbeddedChangeHandler);
					break;
                }
            }
            _trackingListeners = new Dictionary(true);
        }
                
        /**
         * 	@private
         *  Clears entity cache
         */ 
        public function clearCache():void {
            _mergeContext.clear();
        }
		
		
		public function initTmpMergeContext():MergeContext {
			_mergeContext = new MergeContext(_context, this, _dirtyCheckContext);
			return _mergeContext;
		}
		
		public function restoreMergeContext(mergeContext:MergeContext):void {
			_mergeContext = mergeContext;
		}
		
		
		/**
		 *  Clear the current dirty state context
		 *  @param dispatch dispatch a PropertyChangeEvent on meta_dirty if the context was dirty before clearing
		 */
		public function clearDirtyState(dispatch:Boolean = true):void {
			_dirtyCheckContext.clear(dispatch);
		}
		

        /**
         *  @private
         *  Initialize the merge process
         */
        public function initMerge():void {
            var customMergers:Array = _context.allByType(ICustomMerger);
            if (customMergers != null && customMergers.length > 0)
                _customMergers = customMergers;
            else
                _customMergers = null;
        }

        /**
         *	@private 	
         *  'threadlocal' indicating that incoming data does not come from the current session 
         *  
		 * 	@param externalData external data
         */ 
        public function set externalData(externalData:Boolean):void {
            _mergeContext.externalData = externalData;
        }
        
        /**
         *	@private 	
         *  'threadlocal' indicating that incoming data comes from another context 
         *  
		 * 	@param sourceContext source context of incoming data
         */ 
        public function set sourceContext(sourceContext:BaseContext):void {
            _mergeContext.sourceContext = sourceContext;
        }
        
        /**
         *	@private 	
         *  'threadlocal' indicating that incoming data comes from another context 
         *  
		 * 	@return source context of incoming data
         */ 
        public function get sourceContext():BaseContext {
            return _mergeContext.sourceContext;
        }
        
        
        /**
         * 	@private
         *	Allow uninitialize of persistent collections
         *
         *  @param allowed allow uninitialize of collections
         */
        public function set uninitializeAllowed(allowed:Boolean):void {
        	_mergeContext.uninitializeAllowed = allowed;
        }
		
		/**
         * 	@private
		 *  @return allow uninitialize of collections
		 */
		public function get uninitializeAllowed():Boolean {
			return _mergeContext.uninitializeAllowed;
		}
		
		/**
		 * 	@private
		 *  Force uninitialize of persistent collections
		 * 
		 *  @param uninitializing force uninitializing of collections during merge
		 */
		public function set uninitializing(uninitializing:Boolean):void {
			_mergeContext.uninitializing = uninitializing;
		}
        
        
        /**
         *	Entity manager is dirty when any entity/collection/map has been modified
         *
         *  @return is dirty
         */
        public function get dirty():Boolean {
            return _dirtyCheckContext.dirty;
        }
        
        
        /**
         *	List of conflicts detected during last merge operation
         * 
         *  @return conflicts list 
         */
        public function get mergeConflicts():Conflicts {
        	return _mergeContext.mergeConflicts;
        }
		
		
		/**
		 *  @private
		 *  Attach an entity to this context
		 * 
		 *  @param entity an entity
		 *  @param putInCache put entity in cache
		 */
		public function attachEntity(entity:IEntity, putInCache:Boolean = true):void {
			var em:IEntityManager = Managed.getEntityManager(entity);
			if (em != null && em !== _context && !Object(em).meta_finished) {
			 	throw new Error("The entity instance " + BaseContext.toString(entity) 
			 		+ " cannot be attached to two contexts (current: " + (Object(em).meta_isGlobal() ? "global" : Object(em).contextId) 
			 		+ ", new: " + (_context.meta_isGlobal() ? "global" : _context.contextId) + ")");
			}
			
			Managed.setEntityManager(entity, _context);
			if (putInCache) {
				if (_entitiesByUID.put(entity) == null)
					_dirtyCheckContext.addUnsaved(entity);
			}
		}
		
		/**
		 *  @private
		 *  Detach an entity from this context only if it's not persistent
		 * 
		 *  @param entity an entity
		 *  @param removeFromCache remove entity from cache
		 */
		public function detachEntity(entity:IEntity, removeFromCache:Boolean = true, forceRemove:Boolean = false):void {
			if (!forceRemove) {
				var versionPropName:String = _context.meta_tide.getEntityDescriptor(entity).versionPropertyName;
				if (versionPropName == null || !isNaN(entity[versionPropName]))
					return;
			}
			
			_dirtyCheckContext.markNotDirty(entity, entity);
			
			Managed.setEntityManager(entity, null);
			if (removeFromCache)
				_entitiesByUID.remove(getQualifiedClassName(entity) + ":" + entity.uid);
		}
        
		public static function isSimple(object:Object):Boolean {
			return ObjectUtil.isSimple(object) || object is Enum || object is ByteArray || object is IValue || object is XML;
		}
		
        /**
         *	@private
         *  Internal implementation of object attach
         * 
         *  @param object object
         *  @param cache internal cache to avoid graph loops
         */ 
        public function attach(object:Object, cache:Dictionary):void {
            if (object == null || isSimple(object))
            	return;
            
            if (cache[object] != null)
                return;
            cache[object] = object;
            
            if (object is IEntity)
                attachEntity(IEntity(object));
            
            var cinfo:Object = ObjectUtil.getClassInfo(object, null, { includeTransient: false });
            for each (var p:String in cinfo.properties) {
                var val:Object = object[p];
                
                if (val is IList && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
                    var coll:IList = IList(val);
                    for each (var o:Object in coll)
                    	attach(o, cache);
                }
                else if (val is IMap && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
                    var map:IMap = IMap(val);
                    for each (var key:Object in map.keySet) {
                        var value:Object = map.get(key);
                        attach(key, cache);
                        attach(value, cache);
                    }
                }
				else if (val is Array) {
					for each (var e:Object in val)
						attach(e, cache);
				}
				else if (!isSimple(val)) {
					attach(val, cache);
				}
            }
        }
		
		/**
		 *	@private
		 *  Internal implementation of object detach
		 * 
		 *  @param object object
		 *  @param cache internal cache to avoid graph loops
		 */ 
		public function detach(object:Object, cache:Dictionary, forceRemove:Boolean = false):void {
			if (object == null || isSimple(object))
				return;
			
			if (cache[object] != null)
				return;
			cache[object] = object;
			
			var excludes:Array = [ 'uid' ];
			if (object is IEntity) {
				var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(object));
				if (desc.idPropertyName != null)
					excludes.push(desc.idPropertyName);
				if (desc.versionPropertyName != null)
					excludes.push(desc.versionPropertyName);
			}
			
			var cinfo:Object = ObjectUtil.getClassInfo(object, excludes, { includeTransient: false });
			var p:String, val:Object;
			
			if (object is IEntity && _entityReferences[object] == null) {
				detachEntity(IEntity(object), true, forceRemove);

				for each (p in cinfo.properties) {
					val = object[p];
					
					removeReference(val, object, p);
				}
			}
			
			for each (p in cinfo.properties) {
				val = object[p];
				
				if (val is IList && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
					var coll:IList = IList(val);
					for each (var o:Object in coll)
						detach(o, cache, forceRemove);
				}
				else if (val is IMap && !(val is IPersistentCollection && !IPersistentCollection(val).isInitialized())) {
					var map:IMap = IMap(val);
					for each (var key:Object in map.keySet) {
						var value:Object = map.get(key);
						detach(key, cache, forceRemove);
						detach(value, cache, forceRemove);
					}
				}
				else if (val is Array) {
					for each (var e:Object in val)
						detach(e, cache, forceRemove);
				}
				else if (val != null && !isSimple(val)) {
					detach(val, cache, forceRemove);
				}
			}
		}
        

        /**
         *  @private 
         * 	Retrives an entity in the cache from its uid
         *   
         *  @param object an entity
         *  @param nullIfAbsent return null if entity not cached in context
         */
        public function getCachedObject(object:Object, nullIfAbsent:Boolean = false):Object {
            var entity:Object = null;
        	if (object is IEntity) {
        		entity = _entitiesByUID.get(getQualifiedClassName(object) + ":" + object.uid);
            }
            else if (object is IEntityRef) {
                entity = _entitiesByUID.get(object.className + ":" + object.uid);
            }
			else if (object is String) {
				entity = _entitiesByUID.get(String(object));
			}

            if (entity)
                return entity;
            if (nullIfAbsent)
                return null;
			
        	return object;
        }

        /** 
         *  @private 
         * 	Retrives the owner entity of the provided object (collection/map/entity)
         *   
         *  @param object an entity
         */
        public function getOwnerEntity(object:Object):Object {
            var refs:Array = _entityReferences[object];
            if (!refs)
                return null;
            
            for (var i:int = 0; i < refs.length; i++) {
                if (refs[i] is Array && refs[i][0] is String)
                    return [ _entitiesByUID.get(refs[i][0] as String), refs[i][1] ];
            }
            return null;
        }

        /**
         *  @private
         * 	Retrives the owner entity of the provided object (collection/map/entity)
         *
         *  @param object an entity
         */
        public function getOwnerEntities(object:Object):Array {
            var refs:Array = _entityReferences[object];
            if (!refs)
                return null;

            var owners:Array = [];
            for (var i:int = 0; i < refs.length; i++) {
                if (refs[i] is Array && refs[i][0] is String)
                    owners.push([ _entitiesByUID.get(refs[i][0] as String), refs[i][1] ]);
            }
            return owners;
        }

		
		public function getRefs(object:Object):Array {
			return _entityReferences[object] as Array;
		}
        
        /**
         *  @private
         *  Retrieves context expression path for the specified entity (internal implementation)
         *   
         *  @param obj an entity
         *  @param recurse should recurse until 'real' context path, otherwise object reference can be returned
         *  @param cache graph visitor cache
         *  @return the path from the entity context (or null is no path found)
         */
        public function getReference(obj:Object, recurse:Boolean, cache:Dictionary = null):IExpression {
        	if (cache) {
	            if (cache[obj] != null)    // We are in a graph loop, no reference can be found from this path
	                return null;
	            cache[obj] = obj;
	        }
	        else if (recurse)
	        	throw new Error("Cache must be provided to get reference recursively");
            
            var refs:Array = _entityReferences[obj];
            if (!refs)
                return null;
            
            for (var i:int = 0; i < refs.length; i++) {
            	// Return first context expression reference that is remote enabled
                if (refs[i] is IExpression && _context.meta_tide.getComponentRemoteSync(IExpression(refs[i]).componentName) != Tide.SYNC_NONE)
                    return refs[i] as IExpression;
            }
            
            if (recurse) {
            	var ref:Object;
                for (i = 0; i < refs.length; i++) {
                    if (refs[i] is Array && refs[i][0] is String) {
                        ref = _entitiesByUID.get(refs[i][0] as String);
                        if (ref != null) {
                            ref = getReference(ref, recurse, cache);
                            if (ref != null)
                                return IExpression(ref);
                        }
                    }
                    else if (refs[i] is Array && !(refs[i] is IExpression)) {
                    	ref = refs[i][0];
                    	if (ref != null) {
                    		ref = getReference(ref, recurse, cache);
                            if (ref != null)
                                return IExpression(ref);
                    	} 
                    }
                }
            }
            return null;
        }
        
        /**
         *  @private
         *  Init references array for an object
         *   
         *  @param obj an entity
         */
        private function initRefs(obj:Object):Array {
            var refs:Array = _entityReferences[obj];
            if (!refs) {
                refs = new Array();
                _entityReferences[obj] = refs;
            }
            return refs;
        }
        
        /**
         *  @private 
         *  Registers a reference to the provided object with either a parent or res
         * 
         *  @param obj an entity
         *  @param parent the parent entity
		 *  @param propName name of the parent entity property that references the entity
         *  @param res the context expression
         */ 
        public function addReference(obj:Object, parent:Object, propName:String, res:IExpression = null):void {
            if (obj is IEntity)
                attachEntity(IEntity(obj));
			
			if (obj is ListCollectionView && parent != null)
				obj = obj.list;
			
            var refs:Array = _entityReferences[obj] as Array;
            if (!(obj is IPersistentCollection) && res != null) {
                refs = initRefs(obj);
                var found:Boolean = false;
                for (var i:int = 0; i < refs.length; i++) {
                    if (!(refs[i] is IExpression))
                        continue; 
                    var r:IExpression = refs[i] as IExpression;
                    if (r.componentName == res.componentName && r.expression == res.expression) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    refs.push(res);
            }
			found = false;
            if (parent is IUID) {
                var ref:String = getQualifiedClassName(parent) + ":" + parent.uid;
                if (refs == null)
					refs = initRefs(obj);
				else {
					for (i = 0; i < refs.length; i++) {
						if (refs[i] is Array && refs[i][0] === ref) {
							found = true;
							break;
						}
					}
				}
				if (!found)
                	refs.push([ref, propName]);
            }
	       	else if (parent) {
				if (refs == null)
					refs = initRefs(obj);
				else {
					for (i = 0; i < refs.length; i++) {
						if (refs[i] is Array && refs[i][0] === parent) {
							found = true;
							break;
						}
					}
				}
	       		if (!found)
	       			refs.push([parent, propName]);
	       	}
        }
        
        /**
         *	@private  
         *  Removes a reference on the provided object
         *
         *  @param obj an entity
         *  @param parent the parent entity to dereference
		 *  @param propName name of the parent entity property that references the entity
         *  @param res expression to remove
         */ 
        public function removeReference(obj:Object, parent:Object = null, propName:String = null, res:IExpression = null):Boolean {
			if (obj is ListCollectionView && parent != null)
				obj = obj.list;
			
            var refs:Array = _entityReferences[obj];
            if (!refs)
                return true;
            var idx:int = -1, i:int;
            if (parent) {
				for (i = 0; i < refs.length; i++) {
					if (refs[i] is Array && refs[i][0] == getQualifiedClassName(parent) + ":" + parent.uid && refs[i][1] == propName) {
                		idx = i;
						break;
					}
				}
			}
            else if (res) {
                for (i = 0; i < refs.length; i++) {
                    if (refs[i] is IExpression && IExpression(refs[i]).path == res.path) {
                        idx = i;
                        break;
                    }
                }
            }
            if (idx >= 0)
                refs.splice(idx, 1);
            
			var removed:Boolean = false;
            if (refs.length == 0) {
            	delete _entityReferences[obj];
				removed = true;
				
				if (obj is IEntity)
					detachEntity(IEntity(obj), true);
			}
            
			if (obj is IPersistentCollection && !IPersistentCollection(obj).isInitialized())
				return removed;
			
            var elt:Object = null;
            if (obj is IList || obj is Array) {
            	for each (elt in obj)
            		removeReference(elt, parent, propName);
            }
            else if (obj is IMap) {
            	for (elt in obj) {
            		var val:Object = obj.get(elt);
            		removeReference(val, parent, propName);
            		removeReference(elt, parent, propName);
            	}
            }
			return removed;
        }
        

        /**
         *  Merge an object coming from the server in the context
         *
         *  @param obj external object
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param expr current path from the context
         *  @param parent parent object for collections
         *  @param propertyName property name of the current object in the parent object
		 *  @param setter setter function to update the private property
		 *  @param forceUpdate force update of property (used for externalized properties)
         *
         *  @return merged object (should === previous when previous not null)
         */
        public function mergeExternal(obj:Object, previous:Object = null, expr:IExpression = null, 
									  parent:Object = null, propertyName:String = null, setter:Function = null, forceUpdate:Boolean = false):Object {
			_mergeContext.initMerge();
			
            var saveMergeUpdate:Boolean = _mergeContext.mergeUpdate;
			var saveMerging:Boolean = _mergeContext.merging;
			
			try {
				_mergeContext.merging = true;
				var stackSize:uint = _mergeContext.mergeStackSize;
				
				var addRef:Boolean = false;
	            var fromCache:Boolean = false;
	            var prev:Object = _mergeContext.getCachedMerge(obj);
	            var next:Object = obj;
	            if (prev) {
	                next = prev;
	                fromCache = true;
	            }
	            else {
                    // Give a chance to intercept received value so we can apply changes on private values
					var currentMerge:Object = _mergeContext.currentMerge;
					if (currentMerge is IEntity && currentMerge is Proxy) {
						if (!currentMerge.hasOwnProperty(propertyName))
							return previous;
						next = obj = currentMerge[propertyName];
					}
					
	                // Clear change tracking
	            	removeTrackingListeners(previous, parent);
	                
					if (obj == null) {
						next = null;
					}
					else if (((obj is IPersistentCollection && !IPersistentCollection(obj).isInitialized()) 
	                	|| (obj is IPersistentCollection && !(previous is IPersistentCollection))) && parent is IEntity && propertyName) {
	                    next = mergePersistentCollection(IPersistentCollection(obj), previous, null, IEntity(parent), propertyName);
	                    addRef = true;
	                }
	                else if (obj is IList) {
	                    next = mergeCollection(IList(obj), previous, parent == null ? expr : null, parent, propertyName);
	                    addRef = true;
	                }
	                else if (obj is Array) {
	                    next = mergeArray(obj as Array, previous, parent == null ? expr : null, parent, propertyName);
	                    addRef = true;
	                }
	                else if (obj is IMap) {
	                    next = mergeMap(IMap(obj), previous, parent == null ? expr : null, parent, propertyName);
	                    addRef = true;
	                }
	                else if (obj is Enum) {
	                	next = Enum.normalize(obj as Enum);
	                }
					else if (obj is IEntity) {
						next = mergeEntity(obj, previous, expr, parent, propertyName);
						addRef = true;
					}
                    else {
                        var merged:Boolean = false;
                        if (_customMergers != null) {
                            for each (var merger:ICustomMerger in _customMergers) {
                                if (merger.accepts(obj)) {
                                    next = merger.merge(_mergeContext, obj, previous, parent == null ? expr : null, parent, propertyName);

                                    // Keep notified of collection updates to notify the server at next remote call
                                    addTrackingListeners(previous, parent);
                                    merged = true;
                                    addRef = true;
                                }
                            }
                        }
	                    if (!merged && !isSimple(obj)) {
	                        next = mergeEntity(obj, previous, expr, parent, propertyName);
	                	    addRef = true;
                        }
	                }
	            }
				
	            if (next && !fromCache && addRef
	                && (expr != null || (prev == null && parent != null))) {
	                // Store reference from current object to its parent entity or root component expression
	                // If it comes from the cache, we are probably in a circular graph 
	                addReference(next, parent, propertyName, expr);
	            }
	            
	            _mergeContext.mergeUpdate = saveMergeUpdate;
	            
	            if ((_mergeContext.mergeUpdate || forceUpdate) && setter != null && parent != null && propertyName != null && parent is IManaged) {
	            	if (!_mergeContext.resolvingConflict || propertyName != _context.meta_tide.getEntityDescriptor(IEntity(parent)).versionPropertyName) {
		                setter(next);
		                Managed.setProperty(IManaged(parent), propertyName, previous, next);
		            }
	            }
				
				if ((_mergeContext.mergeUpdate || forceUpdate) && !fromCache && obj is IEntity) {
					// @TODO Try to improve performance here by not iterating on child contexts where unnecessary  
					// && _context.meta_isGlobal()) {
					
					// Propagate to existing conversation contexts where the entity is present
					_context.meta_contextManager.forEachChildContext(_context, function(ctx:BaseContext, entity:IEntity):void {
						if (ctx === _mergeContext.sourceContext)
							return;
						if (ctx.meta_getCachedObject(entity, true) != null)
							ctx.meta_mergeFromContext(_context, entity, _mergeContext.externalData);
					}, obj);
				}
			}
			finally {
				if (_mergeContext.mergeStackSize > stackSize)
					_mergeContext.popMerge();
				
				_mergeContext.merging = saveMerging;
			}
            
            return next;
        }


        /**
         *  @private 
         *  Merge an entity coming from the server in the context
         *
         *  @param obj external entity
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param expr current path from the context
         *  @param parent parent object for collections
         *  @param propertyName propertyName from the owner object
         *
         *  @return merged entity (=== previous when previous not null)
         */ 
        private function mergeEntity(obj:Object, previous:Object, expr:IExpression = null, parent:Object = null, propertyName:String = null):Object {
        	if (obj != null || previous != null)
            	log.debug("mergeEntity: {0} previous {1}{2}", BaseContext.toString(obj), BaseContext.toString(previous), obj === previous ? " (same)" : "");
        	
            var dest:Object = obj;
            var p:Object = null;
            var desc:EntityDescriptor = null;
            if (obj is IEntity && !obj.meta::isInitialized()) {
                desc = _context.meta_tide.getEntityDescriptor(IEntity(obj));
                if (desc.idPropertyName != null) {
                    p = _entitiesByUID.find(function(o:Object):Boolean {
                        return (getQualifiedClassName(o) === getQualifiedClassName(obj) && objectEquals(obj[desc.idPropertyName], o[desc.idPropertyName]));
                    });

                    if (p) {
                        previous = p;
                        dest = previous;
                    }
                }
            }
            else if (obj is IUID) {
				if (obj is Proxy && obj is IDataProxy)
					p = _entitiesByUID.get(obj.flash_proxy::qualifiedClassName + ":" + IUID(obj).uid);
				else
                	p = _entitiesByUID.get(getQualifiedClassName(obj) + ":" + IUID(obj).uid);
                if (p) {
					// Trying to merge an entity that is already cached with itself: stop now, this is not necessary to go deeper in the object graph
					// it should be already instrumented and tracked
					if (obj === p)
						return obj;
					
					previous = p;
					dest = previous;
                }
            }
            if (dest !== previous && previous && (objectEquals(previous, obj)
				|| (parent != null && !(previous is IUID)))) 	// GDS-649 Case of embedded objects 
                dest = previous;
            			
            if (dest === obj && p == null && obj != null && _mergeContext.sourceContext != null) {
				// When merging from another context, ensure we create a new copy of the entity
            	dest = Type.forInstance(obj).constructor.newInstance();
            	if (obj is IUID)
            		dest.uid = obj.uid;
            }

			try {
	        	if (obj is IEntity && !obj.meta::isInitialized() && objectEquals(previous, obj)) {
	        		// Don't overwrite existing entity with an uninitialized proxy
        			log.debug("ignored received uninitialized proxy");
                    // Don't mark the object not dirty as we only received a proxy
        			// _dirtyCheckContext.markNotDirty(previous);
	    			return previous;
	        	}
	            
	            if (dest is IEntity && !dest.meta::isInitialized())
	            	log.debug("initialize lazy entity: {0}", BaseContext.toString(dest));
	        }
	        catch (e:ReferenceError) {
	        	// Entity class does not implement meta:isInitialized, consider as true
	        }
            
            if (dest != null && dest is IEntity && dest === obj) {
            	log.debug("received entity {0} used as destination (ctx: {1})", BaseContext.toString(obj), 
            		_context.meta_isGlobal() ? "global" : _context.contextId);
            }
            
            var fromCache:Boolean = (p && dest === p); 
            
            if (!fromCache && dest is IUID)
                _entitiesByUID.put(IUID(dest));            
            
			_mergeContext.pushMerge(obj, dest);
            var versionChangeCache:Dictionary = _mergeContext.versionChangeCache;

			var ignore:Boolean = false;
            if (dest is IEntity) {
                desc = _context.meta_tide.getEntityDescriptor(IEntity(dest));

                if (_mergeContext.uninitializing && parent is IEntity && propertyName != null) {
                    if (desc.versionPropertyName != null && !isNaN(obj[desc.versionPropertyName]) 
							&& _context.meta_tide.getEntityDescriptor(IEntity(parent)).lazy[propertyName]) {
                       	if (dest.meta::defineProxy3(obj))	// Only if entity can be proxied (has a detachedState)
                        	return dest;
                    }
                }

                // Associate entity with the current context
                attachEntity(IEntity(dest), false);
				
                if (previous && dest === previous) {
                    // Check version for optimistic locking
                    if (desc.versionPropertyName != null && !_mergeContext.resolvingConflict) {
                        var newVersion:Number = obj[desc.versionPropertyName] as Number;
                        var oldVersion:Number = dest[desc.versionPropertyName] as Number;
                    	if (newVersion < oldVersion || (isNaN(newVersion) && !isNaN(oldVersion))) {
                    		log.warn("ignored merge of older version of {0} (current: {1}, received: {2})", 
                    			BaseContext.toString(dest), oldVersion, newVersion);
                        	ignore = true;
                        }
                    	else if (newVersion > oldVersion || (!isNaN(newVersion) && isNaN(oldVersion))) {
							// Handle changes when version number is increased
                    		versionChangeCache[dest] = true;
                    		
							var entityChanged:Boolean = _dirtyCheckContext.isEntityChanged(IEntity(dest));
                    		if (_mergeContext.externalData && entityChanged) {
                    			// Conflict between externally received data and local modifications
                    			log.error("conflict with external data detected on {0} (current: {1}, received: {2})",
                    				BaseContext.toString(dest), oldVersion, newVersion);
                    			
								if (_dirtyCheckContext.checkAndMarkNotDirty(IEntity(dest), IEntity(obj))) {
									// Incoming data is different from local data
	                				_mergeContext.addConflict(dest as IEntity, obj as IEntity);
	                    			
	                    			ignore = true;
								}
								else
									_mergeContext.mergeUpdate = true;
                    		}
                    		else
                    			_mergeContext.mergeUpdate = true;
                    	}
                    	else {
                    		// Data has been changed locally and not persisted, don't overwrite when version number is unchanged
							_mergeContext.mergeUpdate = !_dirtyCheckContext.isEntityChanged(IEntity(dest));
                    	}
                    }
                    else if (!_mergeContext.resolvingConflict)
                    	_mergeContext.versionChangeCache[dest] = true;
                }
                else
                	_mergeContext.versionChangeCache[dest] = true;
                
                if (!ignore) {
                	if (desc.mergeGDS21) {
						if (obj is Proxy) {
							_mergeContext.currentMerge = obj;
							dest.meta::merge(_context, obj.flash_proxy::object);
						}
						else
                			dest.meta::merge(_context, obj);
					}
                	else if (desc.mergeGDS20)
                		dest.meta_merge(_context, obj);
                	else
                		EntityManager.defaultMerge(_context, obj, dest, _mergeContext.mergeUpdate, expr, parent, propertyName);
                }
            }
            else
                EntityManager.defaultMerge(_context, obj, dest, _mergeContext.mergeUpdate, expr, parent, propertyName);
			
			if (dest is IEntity && !ignore && !_mergeContext.skipDirtyCheck && !_mergeContext.resolvingConflict)
				_dirtyCheckContext.checkAndMarkNotDirty(IEntity(dest), obj);
			
			if (dest != null)
				log.debug("mergeEntity result: {0}", BaseContext.toString(dest));
			
			// Keep notified of collection updates to notify the server at next remote call
			addTrackingListeners(previous, parent);
            
            return dest;
        }
        

        /**
         *  @private 
         *  Merge a collection coming from the server in the context
         *
         *  @param coll external collection
         *  @param previous previously existing collection in the context (can be null if no existing collection)
         *  @param expr current path from the context
         *  @param parent owner object for collections
         *  @param propertyName property name in owner object
         * 
         *  @return merged collection (=== previous when previous not null)
         */ 
        private function mergeCollection(coll:IList, previous:Object, expr:IExpression, parent:Object = null, propertyName:String = null):IList {
            log.debug("mergeCollection: {0} previous {1}", BaseContext.toString(coll), BaseContext.toString(previous));
			
			if (_mergeContext.uninitializing && parent is IEntity && propertyName != null) {
				var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(parent));
				if (desc.versionPropertyName != null && !isNaN(parent[desc.versionPropertyName]) && desc.lazy[propertyName]
					&& previous is IPersistentCollection && IPersistentCollection(previous).isInitialized()) {
					
					log.debug("uninitialize lazy collection {0}", BaseContext.toString(previous));
					_mergeContext.pushMerge(coll, previous);

					IPersistentCollection(previous).uninitialize();
					return IList(previous);
				}
			}
			
            if (previous && previous is IPersistentCollection && !IPersistentCollection(previous).isInitialized()) {
                log.debug("initialize lazy collection {0}", BaseContext.toString(previous));
				_mergeContext.pushMerge(coll, previous);
                
                IPersistentCollection(previous).initializing();
                
                for (var i:int = 0; i < coll.length; i++) {
                    var obj:Object = coll.getItemAt(i);

                    obj = mergeExternal(obj, null, null, propertyName != null ? parent : null, propertyName);
                    previous.addItem(obj);
                }
                
                IPersistentCollection(previous).initialize();
    
                // Keep notified of collection updates to notify the server at next remote call
            	addTrackingListeners(previous, parent);

                return IList(previous);
            }

			var tracking:Boolean = false;
			
			var nextList:IList = null;            
            var list:IList = null;
            if (previous && previous is IList)
                list = IList(previous);
            else if (_mergeContext.sourceContext != null)
				list = Type.forInstance(coll).constructor.newInstance() as IList;
            else
                list = coll;
                            
			_mergeContext.pushMerge(coll, list);
            
            // Restore collection sort/filter state
            var prevColl:IList = list !== coll ? list : null;
            var destColl:IList = prevColl;
            if (destColl is ListCollectionView && (ListCollectionView(destColl).sort != null || ListCollectionView(destColl).filterFunction != null))
                destColl = ListCollectionView(destColl).list;
            else if (destColl is ICollectionView && coll is ICollectionView) {
                ICollectionView(coll).sort = ICollectionView(destColl).sort;
                ICollectionView(coll).filterFunction = ICollectionView(destColl).filterFunction;
                ICollectionView(coll).refresh();
            }

            if (prevColl && _mergeContext.mergeUpdate) {
            	// Enable tracking before modifying collection when resolving a conflict
            	// so the dirty checking can save changes
	            if (_mergeContext.resolvingConflict) {
	            	addTrackingListeners(prevColl, parent);
	            	tracking = true;
	            }
	            
                for (i = 0; i < destColl.length; i++) {
                    obj = destColl.getItemAt(i);
                    found = false;
                    for (j = 0; j < coll.length; j++) {
                        var next:Object = coll.getItemAt(j);
                        if (objectEquals(next, obj)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        destColl.removeItemAt(i);
                        i--;
                    }
                }
            }
            for (i = 0; i < coll.length; i++) {
                obj = coll.getItemAt(i);
                if (destColl) {
                    var found:Boolean = false;
                    for (var j:int = i; j < destColl.length; j++) {
                        var prev:Object = destColl.getItemAt(j);
                        if (i < destColl.length && objectEquals(prev, obj)) {
                            obj = mergeExternal(obj, prev, propertyName != null ? expr : null, propertyName != null ? parent : null, propertyName);
                            
                            if (j != i) {
                            	destColl.removeItemAt(j);
                            	if (i < destColl.length)
                            		destColl.addItemAt(obj, i);
                            	else
                            		destColl.addItem(obj);
                            	if (i > j)
                            		j--;
                            }
                            else if (obj !== prev)
                                destColl.setItemAt(obj, i);
                            
                            found = true;
                        }
                    }
                    if (!found) {
                        obj = mergeExternal(obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null, propertyName);
                        
                        if (_mergeContext.mergeUpdate) {
	                        if (i < prevColl.length)
	                        	destColl.addItemAt(obj, i);
	                        else
	                        	destColl.addItem(obj);
	                    }
                    }
                }
                else {
                	prev = obj;
                    obj = mergeExternal(obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null, propertyName);
                    if (obj !== prev)
                		coll.setItemAt(obj, i);
                }
            }
            if (destColl && _mergeContext.mergeUpdate) {
            	if (!_mergeContext.resolvingConflict && !_mergeContext.skipDirtyCheck)
					_dirtyCheckContext.markNotDirty(previous, parent as IEntity);
                
                nextList = prevColl;
            }
            else if (prevColl is IPersistentCollection && !_mergeContext.mergeUpdate) {
				nextList = prevColl;
			}
			else
            	nextList = coll;
            
            // Wrap persistent collections
            if (parent is IEntity && propertyName != null && nextList is IPersistentCollection && !(nextList is PersistentCollection)) {
                log.debug("create initialized persistent collection from {0}", BaseContext.toString(nextList));
				
            	nextList = new PersistentCollection(IEntity(parent), propertyName, IPersistentCollection(nextList));
            }
            else
            	log.debug("mergeCollection result: {0}", BaseContext.toString(nextList));
            
			_mergeContext.pushMerge(coll, nextList, false);
            
            if (!tracking)
            	addTrackingListeners(nextList, parent);

            return nextList;
        }
        
        /**
         *  @private 
         *  Merge an array coming from the server in the context
         *
         *  @param array external collection
         *  @param previous previously existing array in the context (can be null if no existing array)
         *  @param expr current path from the context
         *  @param parent owner objects
         *  @param propertyName property name in owner object
         * 
         *  @return merged array
         */ 
        private function mergeArray(array:Array, previous:Object, expr:IExpression, parent:Object = null, propertyName:String = null):Array {
            log.debug("mergeArray: {0} previous {1}", BaseContext.toString(array), BaseContext.toString(previous));
            
            var prevArray:Array = previous is Array && _mergeContext.sourceContext == null ? previous as Array : new Array();
            if (prevArray.length > 0 && prevArray !== array)
                prevArray.splice(0, prevArray.length);
			_mergeContext.pushMerge(array, prevArray);
            
            for (var i:int = 0; i < array.length; i++) {
                var obj:Object = array[i];
                obj = mergeExternal(obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null, propertyName);
                
                if (_mergeContext.mergeUpdate) {
	                if (prevArray !== array)
	                	prevArray.push(obj);
	               	else
	               		prevArray[i] = obj;
	            }
            }
            
            log.debug("mergeArray result: {0}", BaseContext.toString(prevArray));
            
            return prevArray;
        }

        /**
         *  @private 
         *  Merge a map coming from the server in the context
         *
         *  @param map external map
         *  @param previous previously existing map in the context (null if no existing map)
         *  @param expr current path from the context
         *  @param parent owner object for the map if applicable
         * 
         *  @return merged map (=== previous when previous not null)
         */ 
        private function mergeMap(map:IMap, previous:Object, expr:IExpression, parent:Object = null, propertyName:String = null):IMap {
            log.debug("mergeMap: {0} previous {1}", BaseContext.toString(map), BaseContext.toString(previous));
			
            if (_mergeContext.uninitializing && parent is IEntity && propertyName != null) {
				var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(parent));
				if (desc.versionPropertyName != null && !isNaN(parent[desc.versionPropertyName]) && desc.lazy[propertyName] 
					&& previous is IPersistentCollection && IPersistentCollection(previous).isInitialized()) {
					
                    log.debug("uninitialize lazy map {0}", BaseContext.toString(previous));
					_mergeContext.pushMerge(map, previous);
                    IPersistentCollection(previous).uninitialize();
                    return IMap(previous);
                }
            }

            var value:Object;
            var key:Object;
            
            if (previous && previous is IPersistentCollection && !IPersistentCollection(previous).isInitialized()) {
                log.debug("initialize lazy map {0}", BaseContext.toString(previous));
				_mergeContext.pushMerge(map, previous);
                
                IPersistentCollection(previous).initializing();
                
                for each (key in map.keySet) {
                    value = map.get(key);
                    key = mergeExternal(key, null, null, propertyName != null ? parent: null, propertyName);
                    value = mergeExternal(value, null, null, propertyName != null ? parent : null, propertyName);
                    previous.put(key, value);
                }
                
                IPersistentCollection(previous).initialize();
    
                // Keep notified of collection updates to notify the server at next remote call
            	addTrackingListeners(previous, parent);

                return IMap(previous);
            }
            
			var tracking:Boolean = false;
			
            var nextMap:IMap = null;
            var m:IMap = null;
            if (previous && previous is IMap)
                m = IMap(previous);
            else if (_mergeContext.sourceContext != null)
				m = Type.forInstance(map).constructor.newInstance() as IMap;
            else
                m = map;
			_mergeContext.pushMerge(map, m);
            
            var prevMap:IMap = m !== map ? m : null;
            
            if (prevMap) {
	            if (_mergeContext.resolvingConflict) {
	            	addTrackingListeners(prevMap, parent);
	            	tracking = true;
	            }
	            
                if (map !== prevMap) {
                    for each (key in map.keySet) {
                        value = map.get(key);
                        var newKey:Object = mergeExternal(key, null, null, parent, propertyName);
                        value = mergeExternal(value, null, null, parent, propertyName);
                        if (_mergeContext.mergeUpdate || prevMap.containsKey(newKey))
                        	prevMap.put(newKey, value);
                    }
                    
                    if (_mergeContext.mergeUpdate) {
                    	for each (key in prevMap.keySet) {
                    		var found:Boolean = false;
                    		for each (var k:Object in map.keySet) {
                    			if (objectEquals(k, key)) {
                    				found = true;
                    				break;
                    			}
                    		}
                    		if (!found)
                    			prevMap.remove(key);
                    	}
                    }
                }
                
                if (_mergeContext.mergeUpdate && !_mergeContext.resolvingConflict && !_mergeContext.skipDirtyCheck)
					_dirtyCheckContext.markNotDirty(previous, parent as IEntity);
                
                nextMap = prevMap;
            }
            else {
	            var addedToMap:Array = new Array();
	            for each (key in map.keySet) {
	                value = mergeExternal(map.get(key), null, null, parent, propertyName);
	                key = mergeExternal(key, null, null, parent, propertyName);
	                addedToMap.push([ key, value ]);
	            }
	            map.clear();
	            for each (var obj:Object in addedToMap)
	                map.put(obj[0], obj[1]);
	                
	            nextMap = map;
	        }
                
            if (parent is IEntity && propertyName != null && nextMap is IPersistentCollection && !(nextMap is PersistentMap)) {
                log.debug("create initialized persistent map from {0}", BaseContext.toString(nextMap));
				
            	nextMap = new PersistentMap(IEntity(parent), propertyName, IPersistentCollection(nextMap));
            }
            else
            	log.debug("mergeMap result: {0}", BaseContext.toString(nextMap));
			
			_mergeContext.pushMerge(map, nextMap, false);
			
            if (!tracking)
            	addTrackingListeners(nextMap, parent);
            
            return nextMap;
        } 


        /**
         *  @private 
         *  Wraps a persistent collection to manage lazy initialization
         *
         *  @param coll the collection to wrap
         *  @param previous the previous existing collection
         *  @param expr the path expression from the context
         *  @param parent the owner object
         *  @param propertyName owner property
         * 
         *  @return the wrapped persistent collection
         */ 
        protected function mergePersistentCollection(coll:IPersistentCollection, previous:Object, expr:IExpression, parent:IEntity, propertyName:String):Object {
            var oldVersion:Number;
            var newVersion:Number;
            var uninitialize:Boolean = true;

            if (previous is PersistentCollection) {
				_mergeContext.pushMerge(coll, previous);
                if (PersistentCollection(previous).isInitialized()) {
                	if (_mergeContext.uninitializeAllowed && _mergeContext.versionChangeCache[PersistentCollection(previous).entity] != null) {
	                    log.debug("uninitialize lazy collection {0}", BaseContext.toString(previous));
	                    PersistentCollection(previous).uninitialize();
	                }
	                else
	                	log.debug("keep initialized collection {0}", BaseContext.toString(previous));
                }
            	addTrackingListeners(previous, parent);
                return previous;
            }
            else if (previous is PersistentMap) {
				_mergeContext.pushMerge(coll, previous);
                if (PersistentMap(previous).isInitialized()) {
                	if (_mergeContext.uninitializeAllowed && _mergeContext.versionChangeCache[PersistentMap(previous).entity] != null) {
	                    log.debug("uninitialize lazy map {0}", BaseContext.toString(previous));
	                    PersistentMap(previous).uninitialize();
	                }
	                else
	                	log.debug("keep initialized map {0}", BaseContext.toString(previous));
                }
            	addTrackingListeners(previous, parent);
                return previous;
            }
            
            if (coll is IMap) {
				var pm:IPersistentCollection = IPersistentCollection(coll);
				if (previous is IPersistentCollection)
					pm = IPersistentCollection(previous);	// Force wrapping of existing map when there is one
				if (coll is PersistentMap)
					pm = duplicatePersistentCollection(PersistentMap(coll).object, parent, propertyName);
				else if (_mergeContext.sourceContext != null)
					pm = duplicatePersistentCollection(pm, parent, propertyName);
            	var pmap:PersistentMap = new PersistentMap(parent, propertyName, pm);
				_mergeContext.pushMerge(coll, pmap);
            	if (pmap.isInitialized()) {
	                for each (var key:Object in pmap.keySet) {
	                    var value:Object = pmap.remove(key);
	                    key = mergeExternal(key, null, null, parent, propertyName);
	                    value = mergeExternal(value, null, null, parent, propertyName);
	                    pmap.put(key, value);
	                }
            		addTrackingListeners(pmap, parent);
	            }
                else if (parent is IEntity && propertyName != null)
                    _context.meta_tide.getEntityDescriptor(IEntity(parent)).lazy[propertyName] = true;
            	return pmap;
            }
            
			var pc:IPersistentCollection = IPersistentCollection(coll);
			if (previous is IPersistentCollection)
				pc = IPersistentCollection(previous);	// Force wrapping of existing collection when there is one
			if (pc is PersistentCollection)
				pc = duplicatePersistentCollection(PersistentCollection(coll).object, parent, propertyName);
			else if (_mergeContext.sourceContext != null)
				pc = duplicatePersistentCollection(pc, parent, propertyName);
            var pcoll:PersistentCollection = new PersistentCollection(parent, propertyName, pc);
			_mergeContext.pushMerge(coll, pcoll);
            if (pcoll.isInitialized()) {
	            for (var i:int = 0; i < pcoll.length; i++) {
					var obj:Object = mergeExternal(pcoll.getItemAt(i), null, null, parent, propertyName);
					if (obj !== pcoll.getItemAt(i)) 
						pcoll.setItemAt(obj, i);
	            }
            	addTrackingListeners(pcoll, parent);
	        }
            else if (parent is IEntity && propertyName != null)
                _context.meta_tide.getEntityDescriptor(IEntity(parent)).lazy[propertyName] = true;
            return pcoll;
        }
        
        private function duplicatePersistentCollection(coll:Object, parent:Object, propertyName:String):IPersistentCollection {
        	if (!(coll is IPersistentCollection))
				throw new Error("Not a persistent collection/map " + BaseContext.toString(coll));
			
    		var ccoll:IPersistentCollection = coll.clone() as IPersistentCollection;
			
			if (_mergeContext.uninitializing && parent != null && propertyName != null) {
				var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(parent));
				if (desc.versionPropertyName != null && !isNaN(parent[desc.versionPropertyName]) && desc.lazy[propertyName])
					ccoll.uninitialize();
			}
			return ccoll;
        }
        
        
        /**
         *  Merge conversation context variables in global context 
         *  Only applicable to conversation contexts 
         */
        public function mergeInContext(context:BaseContext):void {
        	var cache:Dictionary = new Dictionary();
            for each (var obj:Object in _entitiesByUID.data) {
            	// Reset local dirty state, only server state can safely be merged in global context
            	if (obj is IEntity)
            		resetEntity(IEntity(obj), cache);
            	context.meta_mergeFromContext(_context, obj);
            }
        }
        

        /**
         *  Remove elements from cache and managed collections
         *
         *  @param removals array of entity instances to remove from the cache
         */
        public function handleRemovalsAndPersists(removals:Array, persists:Array = null):void {
            for each (var removal:Object in removals) {
                var entity:Object = getCachedObject(removal, true);
                if (entity == null) {
					log.debug("Entity to remove not found {0}:{1}", removal.className, removal.uid);
                    continue;
				}

                if (_mergeContext.externalData && !_mergeContext.resolvingConflict && _dirtyCheckContext.isEntityChanged(IEntity(entity))) {
                    // Conflict between externally received data and local modifications
                    log.error("conflict with external data removal detected on {0}", BaseContext.toString(entity));

                    _mergeContext.addConflict(entity as IEntity, null);
                }
                else {
					var saveMerging:Boolean = _mergeContext.merging;
					try {
						_mergeContext.merging = true;	// Don't track changes on collections to prevent from 'dirtying' the context
						
	                    var owners:Array = getOwnerEntities(entity);
	                    var cinfo:Object, p:String, val:Object;
	                    if (owners != null) {
	                        for each (var owner:Object in owners) {
	                            val = owner[0][owner[1]];
	                            if (val is IPersistentCollection && !IPersistentCollection(val).isInitialized())
	                                continue;
	                            var idx:int = 0;
	                            if (val is IList) {
	                                idx = val.getItemIndex(entity);
	                                if (idx >= 0)
	                                    val.removeItemAt(idx);
	                            }
	                            else if (val is Array) {
	                                idx = val.indexOf(entity);
	                                if (idx >= 0)
	                                    val.splice(idx, 1);
	                            }
	                            else if (val is IMap) {
	                                if (val.containsKey(entity))
	                                    val.remove(entity);
									
	                                for each (var key:Object in val.keySet) {
	                                    if (objectEquals(val[key], entity))
	                                        val.remove(key);
	                                }
	                            }
	                        }
	                    }
						
						delete _entityReferences[entity];
						
	                    detach(IEntity(entity), new Dictionary(), true);
					}
					finally {
						_mergeContext.merging = saveMerging;
					}
                }
            }
			
			_dirtyCheckContext.fixRemovalsAndPersists(removals, persists);
        }

        /**
         *  Dispatch an event when last merge generated conflicts 
         */
        public function handleMergeConflicts():void {
            // Clear thread cache so acceptClient/acceptServer can work inside the conflicts handler
            _mergeContext.initMergeConflicts();

        	if (_mergeContext.mergeConflicts != null && !_mergeContext.mergeConflicts.empty)
        		_context.dispatchEvent(new TideDataConflictsEvent(_context, _mergeContext.mergeConflicts));
        }
        
        public function resolveMergeConflicts(modifiedEntity:Object, localEntity:Object, resolving:Boolean):void {
			var saveResolvingConflict:Boolean = _mergeContext.resolvingConflict;
			if (resolving)
				_mergeContext.resolvingConflict = true;
            if (modifiedEntity == null)
                handleRemovalsAndPersists([localEntity]);
            else
				mergeExternal(modifiedEntity, localEntity);
			
			if (resolving)
				_mergeContext.resolvingConflict = saveResolvingConflict;

            _mergeContext.checkConflictsResolved();
        }
		
		
		/**
		 * 	Enables or disabled dirty checking in this context
		 *  
		 *  @param enabled
		 */
		public function set dirtyCheckEnabled(enabled:Boolean):void {
			_mergeContext.merging = !enabled;
		}
		
		
		/**
		 * 	Current map of saved properties
		 * 
		 *  @return saved properties
		 */
		public function get savedProperties():Dictionary {
			return _dirtyCheckContext.savedProperties;
		}
        
        
        /**
         *  Default implementation of entity merge for simple ActionScript beans with public properties
         *  Can be used to implement Tide managed entities with simple objects
         *
         *  @param em the context
         *  @param obj source object
         *  @param dest destination object
         *  @param expr current path of the entity in the context (mostly for internal use)
         *  @param parent owning object
         *  @param propertyName property name of the owning object
         */ 
        public static function defaultMerge(em:IEntityManager, obj:Object, dest:Object, mergeUpdate:Boolean = true, expr:IExpression = null, parent:Object = null, propertyName:String = null):void {
            var cinfo:Object = ObjectUtil.getClassInfo(obj, null, { includeTransient: false, includeReadOnly: false });
			var rw:Array = [];
            for each (var p:String in cinfo.properties) {
                var o:Object = obj[p];
				var d:Object = dest[p];
                o = em.meta_mergeExternal(o, d, expr, parent != null ? parent : dest, propertyName != null ? propertyName + '.' + p : p);
                if (o !== d && mergeUpdate)
                	dest[p] = o;
				rw.push(p);
            }
			cinfo = ObjectUtil.getClassInfo(obj, rw, { includeReadOnly: true });
			for each (p in cinfo.properties) {
				if (obj[p] is IUID || dest[p] is IUID)
					throw new Error("Cannot merge the read-only property " + p + " on bean " + obj + " with an IUID value, this will break local unicity and caching. Change property access to read-write.");  
				
				em.meta_mergeExternal(obj[p], dest[p], expr, parent != null ? parent : dest, propertyName != null ? propertyName + '.' + p : p);
			}
        }

    
        /**
         *  Equality for objects, using uid property when possible
         *
         *  @param obj1 object
         *  @param obj2 object
         * 
         *  @return true when objects are instances of the same entity
         */ 
        public function objectEquals(obj1:Object, obj2:Object):Boolean {
            if ((obj1 is IPropertyHolder && obj2 is IEntity) || (obj1 is IEntity && obj2 is IPropertyHolder))
                return false;

            if (obj1 is IUID && obj2 is IUID && getQualifiedClassName(obj1) == getQualifiedClassName(obj2)) {
            	try {
	            	if (obj1 is IEntity && (!obj1.meta::isInitialized() || !obj2.meta::isInitialized())) {
	            		// Compare with identifier for uninitialized entities
	            		var edesc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(IEntity(obj1));
	            		if (edesc.idPropertyName != null)
	            			return objectEquals(obj1[edesc.idPropertyName], obj2[edesc.idPropertyName]);
	            	}
	            }
	            catch (e:ReferenceError) {
	            	// Entity class does not implement meta::isInitialized, consider as true
	            }
                return IUID(obj1).uid == IUID(obj2).uid;
            }
            
            if (obj1 is Enum && obj2 is Enum && obj1.equals(obj2))
            	return true; 
            
            if (obj1 is IValue && obj2 is IValue && obj1.equals(obj2))
            	return true; 

            return obj1 === obj2;
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
        	return _dirtyCheckContext.isEntityPropertyChanged(entity, propertyName, value);
        }
        
        
        /**
         *  Check if entity has changed since last save point
         *
         *  @param entity entity to restore
         *  @param propName property name
         *  @param value
         *   
         *  @return entity is dirty
         */ 
        public function isEntityChanged(entity:IEntity, propName:String = null, value:* = null):Boolean {
            return _dirtyCheckContext.isEntityChanged(entity, propName, value);
        }
        
        
        /**
         *  Discard changes of entity from last version received from the server
         *
         *  @param entity entity to restore
         */ 
        public function resetEntity(entity:IEntity, cache:Dictionary):void {
			var saveMerging:Boolean = _mergeContext.merging;
			// Disable dirty check during reset of entity
			try {
				_mergeContext.merging = true;
        		_dirtyCheckContext.resetEntity(entity, cache);
			}
			finally {
				_mergeContext.merging = saveMerging;
			}
        }
		
		/**
		 *  Discard changes of all cached entities from last version received from the server
		 */ 
		public function resetAllEntities(cache:Dictionary):void {
			var saveMerging:Boolean = _mergeContext.merging;
			// Disable dirty check during reset of entity
			try {
				_mergeContext.merging = true;
				_dirtyCheckContext.resetAllEntities(cache);
			}
			finally {
				_mergeContext.merging = saveMerging;
			}
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
        	if (newValue !== oldValue) {
        		if (oldValue != null) {
        			removeReference(oldValue, entity, propName);
        			removeTrackingListeners(oldValue, entity);
        		}
        		
            	if (newValue is IUID || newValue is IList || newValue is IMap || newValue is Array) {
            		addReference(newValue, entity, propName);
            		addTrackingListeners(newValue, entity);
            	}
            }
            
			if (!_mergeContext.merging || _mergeContext.resolvingConflict)
            	_dirtyCheckContext.setEntityProperty(entity, propName, oldValue, newValue);
        }


        /**
         *  @private 
         *  Interceptor for managed entity getters
         *
         *  @param entity entity to intercept
         *  @param propName property name
         *  @param value value
         * 
         *  @return value
         */ 
        public function getEntityProperty(entity:IEntity, propName:String, value:*):* {
            if (propName == "meta_dirty")
                return _dirtyCheckContext.isEntityChanged(entity);
            
            return value;
        }


        /**
         *	@private 
         *  Remove tracking events
         *
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param parent parent object for collections
         */
        private function addTrackingListeners(previous:Object, parent:Object):void {
        	if (_trackingListeners == null || previous == null || previous is XMLList)
        		return;
        	
            if (previous != null && previous is ListCollectionView) {
                if (parent != null) {
					previous = previous.list;
                    previous.addEventListener(CollectionEvent.COLLECTION_CHANGE, entityCollectionChangeHandler, false, 0, true);
                    _trackingListeners[previous] = "entityCollection";
                }
                else {
                    ListCollectionView(previous).addEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_collectionChangeHandler, false, 0, true);
                    _trackingListeners[previous] = "collection";
                }
            }
            else if (previous != null && previous is IMap) {
                if (parent != null) {
                    IMap(previous).addEventListener(CollectionEvent.COLLECTION_CHANGE, entityMapChangeHandler, false, 0, true);
                    _trackingListeners[previous] = "entityMap";
                }
                else {
                    IMap(previous).addEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_mapChangeHandler, false, 0, true);
                    _trackingListeners[previous] = "map";
                }
            }
			else if (previous is IEventDispatcher && !(previous is IEntity) && parent is IEntity) {
				IEventDispatcher(previous).addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, entityEmbeddedChangeHandler, false, 0, true);
				_trackingListeners[previous] = "entityEmbedded";
			}
        }

        /**
         *	@private 
         *  Remove tracking events
         *
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param parent parent object for collections
         */
        private function removeTrackingListeners(previous:Object, parent:Object):void {
        	if (_trackingListeners == null || previous == null || previous is XMLList)
        		return;
        	
            if (previous is ListCollectionView) {
                if (parent != null) {
					previous = previous.list;
                    previous.removeEventListener(CollectionEvent.COLLECTION_CHANGE, entityCollectionChangeHandler);
				}
                else
                    ListCollectionView(previous).removeEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_collectionChangeHandler);
            }
            else if (previous is IMap) {
                if (parent != null)
                    IMap(previous).removeEventListener(CollectionEvent.COLLECTION_CHANGE, entityMapChangeHandler);
                else
                    IMap(previous).removeEventListener(CollectionEvent.COLLECTION_CHANGE, _context.meta_mapChangeHandler);
            }
			else if (previous is IEventDispatcher && !(previous is IEntity) && parent is IEntity) {
				IEventDispatcher(previous).removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, entityEmbeddedChangeHandler);
			}
            
            delete _trackingListeners[previous];
        }
		
		
		/**
		 *  @private 
		 *  Property event handler to save changes on embedded objects
		 *
		 *  @param event collection event
		 */ 
		private function entityEmbeddedChangeHandler(event:PropertyChangeEvent):void {
			if (_mergeContext.sourceContext === _context || _context.meta_finished)
				return;
			
			log.debug("embedded changed: {0} {1}", event.kind, BaseContext.toString(event.target));
			
			if (!_mergeContext.merging || _mergeContext.resolvingConflict)
				_dirtyCheckContext.entityEmbeddedChangeHandler(event);
		}


        /**
         *  @private 
         *  Collection event handler to save changes on managed collections
         *
         *  @param event collection event
         */ 
        private function entityCollectionChangeHandler(event:CollectionEvent):void {
        	if (_mergeContext.sourceContext === _context || _context.meta_finished)
        		return;
        	
            var i:int = 0;
            
			var parent:Object = null;
            if (event.kind == CollectionEventKind.ADD && event.items && event.items.length > 0) {
            	parent = getOwnerEntity(event.target);
                for (i = 0; i < event.items.length; i++) {
                    if (event.items[i] is IEntity) {
                    	if (parent)
                    		addReference(IEntity(event.items[i]), parent[0], String(parent[1]));
                    	else
                        	attachEntity(IEntity(event.items[i]));
                    }
                }
            }
			else if (event.kind == CollectionEventKind.REMOVE && event.items && event.items.length > 0) {
				parent = getOwnerEntity(event.target);
				if (parent) {
					for (i = 0; i < event.items.length; i++) {
						if (event.items[i] is IEntity)
							removeReference(IEntity(event.items[i]), parent[0], String(parent[1]));
					}
				}
			}
			else if (event.kind == CollectionEventKind.REPLACE && event.items && event.items.length > 0) {
				parent = getOwnerEntity(event.target);
				for (i = 0; i < event.items.length; i++) {
					var newValue:Object = event.items[i].newValue;
					if (newValue is IEntity) {
						if (parent)
							addReference(IEntity(newValue), parent[0], String(parent[1]));
						else
							attachEntity(IEntity(newValue));
					}
				}
			}
            
            if (event.kind != CollectionEventKind.ADD && event.kind != CollectionEventKind.REMOVE 
				&& event.kind != CollectionEventKind.RESET && event.kind != CollectionEventKind.REPLACE)
                return;
			
			if (event.kind == CollectionEventKind.RESET && event.target is IPersistentCollection && !IPersistentCollection(event.target).isInitialized())
				return;
            
            log.debug("collection changed: {0} {1}", event.kind, BaseContext.toString(event.target));
            
			if (!_mergeContext.merging || _mergeContext.resolvingConflict)
            	_dirtyCheckContext.entityCollectionChangeHandler(event);
            
            _context.meta_entityCollectionChangeHandler(event);
        }
        
        
        /**
         *  @private 
         *  Collection event handler to save changes on managed maps
         *
         *  @param event map event
         */ 
        private function entityMapChangeHandler(event:CollectionEvent):void {
        	if (_mergeContext.sourceContext === _context || _context.meta_finished)
        		return;
        	
            var i:int = 0;
			var parent:Object = null;
			var obj:Array = null;
			
            if (event.kind == CollectionEventKind.ADD && event.items && event.items.length > 0) {
            	parent = getOwnerEntity(event.target);
                for (i = 0; i < event.items.length; i++) {
                    if (event.items[i] is IEntity) {
                    	if (parent)
                    		addReference(IEntity(event.items[i]), parent[0], String(parent[1]));
                    	else
                        	attachEntity(IEntity(event.items[i]));
                    }
                    else if (event.items[i] is Array) {
                        obj = event.items[i] as Array;
	                    if (obj[0] is IEntity) {
	                    	if (parent)
	                    		addReference(IEntity(obj[0]), parent[0], String(parent[1]));
	                    	else
	                        	attachEntity(IEntity(obj[0]));
	                    }
	                    if (obj[1] is IEntity) {
	                    	if (parent)
	                    		addReference(IEntity(obj[1]), parent[0], String(parent[1]));
	                    	else
	                        	attachEntity(IEntity(obj[1]));
	                    }
                    }
                }
            }
			else if (event.kind == CollectionEventKind.REMOVE && event.items && event.items.length > 0) {
				parent = getOwnerEntity(event.target);
				if (parent) {
					for (i = 0; i < event.items.length; i++) {
						if (event.items[i] is IEntity)
							removeReference(IEntity(event.items[i]), parent[0], String(parent[1]));
						else if (event.items[i] is Array) {
							obj = event.items[i] as Array;
							if (obj[0] is IEntity)
								removeReference(IEntity(obj[0]), parent[0], String(parent[1]));
							if (obj[1] is IEntity)
								removeReference(IEntity(obj[1]), parent[0], String(parent[1]));
						}
					}
				}
			}
			else if (event.kind == CollectionEventKind.REPLACE && event.items && event.items.length > 0) {
				parent = getOwnerEntity(event.target);
				for (i = 0; i < event.items.length; i++) {
					var newValue:Object = event.items[i].newValue;
					if (newValue is IEntity) {
						if (parent)
							addReference(IEntity(newValue), parent[0], String(parent[1]));
						else
							attachEntity(IEntity(newValue));
					}
					else if (newValue is Array) {
						obj = newValue as Array;
						if (obj[0] is IEntity) {
							if (parent)
								addReference(IEntity(obj[0]), parent[0], String(parent[1]));
							else
								attachEntity(IEntity(obj[0]));
						}
						if (obj[1] is IEntity) {
							if (parent)
								addReference(IEntity(obj[1]), parent[0], String(parent[1]));
							else
								attachEntity(IEntity(obj[1]));
						}
					}
				}
			}
            
            if (event.kind != CollectionEventKind.ADD && event.kind != CollectionEventKind.REMOVE
				&& event.kind != CollectionEventKind.RESET && event.kind != CollectionEventKind.REPLACE)
                return;
            
            log.debug("map changed: {0} {1}", event.kind, BaseContext.toString(event.target));
            
			if (!_mergeContext.merging || _mergeContext.resolvingConflict)
            	_dirtyCheckContext.entityMapChangeHandler(event);
            
            _context.meta_entityMapChangeHandler(event);
        }
    }
}
