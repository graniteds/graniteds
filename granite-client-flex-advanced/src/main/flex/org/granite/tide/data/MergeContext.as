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
    
    import flash.events.IEventDispatcher;
    import flash.utils.ByteArray;
    import flash.utils.Dictionary;
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
    import mx.events.PropertyChangeEventKind;
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
    import org.granite.tide.IExpression;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.collections.PersistentMap;
    import org.granite.tide.data.events.TideDataConflictsEvent;
    import org.granite.tide.service.ServerSession;
    import org.granite.tide.service.ServerSession;
    import org.granite.util.Enum;


    use namespace flash_proxy;
    use namespace object_proxy;
    use namespace meta;


	/**
	 * 	PersistenceContext is the base implementation of the entity container context
	 * 
     * 	@author William DRAI
	 */
    public class MergeContext {

        private var _serverSession:ServerSession = null;
        private var _context:BaseContext = null;
        private var _entityManager:EntityManager = null;
        private var _dirtyCheckContext:DirtyCheckContext = null;
		private var _entityCache:Dictionary = null;
		private var _mergeStack:Array = [];
        public var externalData:Boolean = false;
        public var sourceContext:BaseContext = null;
        public var mergeUpdate:Boolean = false;
        private var _mergeConflicts:Conflicts = null;
        private var _versionChangeCache:Dictionary = null;
		public var merging:Boolean = false;
        public var resolvingConflict:Boolean = false;
		public var skipDirtyCheck:Boolean = false;
        public var uninitializeAllowed:Boolean = true;
		public var uninitializing:Boolean = false;
        public var proxyGetter:Function = null;
        


        public function MergeContext(context:BaseContext, entityManager:EntityManager, dirtyCheckContext:DirtyCheckContext) {
            _context = context;
            _entityManager = entityManager;
            _dirtyCheckContext = dirtyCheckContext;
        }

        public function get serverSession():ServerSession {
            return _serverSession;
        }

        public function set serverSession(serverSession:ServerSession):void {
            _serverSession = serverSession;
        }

        /**
         * 	@private
         *  Clears merge context
         */ 
        public function clear():void {
			_entityCache = null;
            _mergeConflicts = null;
            _versionChangeCache = null;
            resolvingConflict = false;
			uninitializing = false;
            merging = false;
            mergeUpdate = false;
        }
		

        public function addConflict(localEntity:IEntity, receivedEntity:Object):void {
            if (_mergeConflicts == null)
                _mergeConflicts = new Conflicts(_context,  _entityManager);
			
			var save:Object = _dirtyCheckContext.savedProperties[localEntity];
			var desc:EntityDescriptor = getEntityDescriptor(localEntity);
			var properties:Array = [];
			for (var propName:String in save) {
				if (propName == desc.versionPropertyName)
					continue;
				properties.push(propName);
			}
			properties.sort();
			
            _mergeConflicts.addConflict(localEntity, receivedEntity, properties);
        }

        public function initMergeConflicts():void {
			_entityCache = null;
            _versionChangeCache = null;
            resolvingConflict = false;
        }

        public function checkConflictsResolved():void {
            if (_mergeConflicts != null && _mergeConflicts.allResolved)
                _mergeConflicts = null;
        }

        public function get mergeConflicts():Conflicts {
            return _mergeConflicts;
        }
		
		public function get entityCache():Dictionary {
			return _entityCache;
		}
		public function initMerge():void {
			if (_entityCache == null) {
				_entityCache = new Dictionary();
				mergeUpdate = true;
			}
		}
		public function saveEntityCache():Dictionary {
			var entityCache:Dictionary = _entityCache;
			_entityCache = new Dictionary();
			return entityCache;
		}
		public function restoreEntityCache(entityCache:Dictionary):void {
			_entityCache = entityCache;
		}
		
        public function get versionChangeCache():Dictionary {
            if (_versionChangeCache == null)
                _versionChangeCache = new Dictionary(true);
            return _versionChangeCache;
        }
		
		public function pushMerge(obj:Object, dest:Object, push:Boolean = true):void {
			_entityCache[obj] = dest;
			if (push)
				_mergeStack.push(dest);
		}
		public function getCachedMerge(obj:Object):* {
			return _entityCache[obj];
		}
		public function popMerge():* {
			return _mergeStack.pop();
		}
		public function get currentMerge():* {
			return _mergeStack[_mergeStack.length-1];
		}
		public function set currentMerge(merge:*):void {
			_mergeStack[_mergeStack.length-1] = merge;
		}
		public function get mergeStackSize():uint {
			return _mergeStack.length;
		}

        public function getEntityDescriptor(entity:IEntity):EntityDescriptor {
            return _context.meta_tide.getEntityDescriptor(entity);
        }

        public function getCachedObject(object:Object, nullIfAbsent:Boolean = false):Object {
            return _entityManager.getCachedObject(object, nullIfAbsent);
        }
		
		public function getSavedProperties(object:Object):Object {
			return _dirtyCheckContext.savedProperties[object];
		}
		
		public function isUnsaved(object:Object):Boolean {
			return _dirtyCheckContext.isUnsaved(object);
		}

        public function mergeExternal(object:Object, dest:Object, parent:Object = null, propertyName:String = null):Object {
            return _entityManager.mergeExternal(object, dest, null, parent, propertyName);
        }
		
		public function attach(object:Object):void {
			_entityManager.attach(object, new Dictionary());
		}
		
        public function objectEquals(obj1:Object, obj2:Object):Boolean {
            return _entityManager.objectEquals(obj1, obj2);
        }

        public function isEntityChanged(entity:IEntity):Boolean {
            return _dirtyCheckContext.isEntityChanged(entity);
        }

        public function markNotDirty(entity:Object, owner:IEntity):void {
            _dirtyCheckContext.markNotDirty(entity, owner);
        }

        public function checkAndMarkNotDirty(entity:IEntity, source:Object):Boolean {
            return _dirtyCheckContext.checkAndMarkNotDirty(entity, source);
        }
    }
}
