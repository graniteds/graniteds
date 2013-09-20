/**
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
    
    import flash.utils.ByteArray;
    import flash.utils.Dictionary;

    import mx.collections.IList;
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
    import org.granite.util.Enum;


    use namespace meta;


	/**
	 * 	DirtyCheckContext handles dirty checking of managed entities
	 * 
     * 	@author William DRAI
	 */
    public class EntityGraphUninitializer {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.EntityGraphUninitializer");
    
        protected var _context:BaseContext;
        protected var _savedProperties:Dictionary;

        private var _tmpContext:BaseContext = null;


        public function EntityGraphUninitializer(context:BaseContext) {
            _context = context;
            _savedProperties = context.meta_getSavedProperties();

            // Temporary context to store complete entities so we can uninitialize all collections
            // when possible
            _tmpContext = _context.newTemporaryContext();
        }

        /**
         *  Build a Change object for the entity in the current context
         *
         * 	@return the change for this entity in the context
         */
        public function uninitializeEntityGraph(entity:IEntity):IEntity {
            var dest:IEntity = _tmpContext.meta_mergeFromContext(_context, entity) as IEntity;

            internalUninitializeEntityGraph(dest, new Dictionary());
			
			// Detach all entities from temporary context
			_tmpContext.meta_clear();
			
            return dest;
        }

        private function internalUninitializeEntityGraph(entity:Object, cache:Dictionary):Boolean {
            if (ObjectUtil.isSimple(entity) || entity is Enum || entity is IValue || entity is XML || entity is ByteArray)
                return false;

            if (cache[entity] != null)
                return false;

            cache[entity] = true;

            var dirty:Boolean = false;
            var save:Object = _savedProperties[_context.meta_getCachedObject(entity)];
            if (entity is IEntity && save != null)
                dirty = true;
			
            var desc:EntityDescriptor = _context.meta_tide.getEntityDescriptor(entity);
            var cinfo:Object = ObjectUtil.getClassInfo(entity, null, { includeTransient: false, includeReadOnly: false });
            for each (var p:String in cinfo.properties) {
                var v:Object = entity[p];
                if (v == null || (desc != null && p == desc.versionPropertyName))
                    continue;
                if (entity is IEntity && !entity.meta::isInitialized(p))
                    continue;

                var propDirty:Boolean = save != null && save.hasOwnProperty(p);

                if (v is IList || v is Array) {
                    for each (var e:Object in v) {
                        if (internalUninitializeEntityGraph(e, cache))
                            propDirty = true;
                    }
                    if (propDirty)
                        dirty = true;
                    else if (desc != null && desc.lazy[p] && v is IPersistentCollection)
                        IPersistentCollection(v).uninitialize();
                }
                else if (v is IMap) {
                    for each (var k:Object in IMap(v).keySet) {
                        if (internalUninitializeEntityGraph(k, cache))
                            propDirty = true;
                        if (internalUninitializeEntityGraph(v.get(k), cache))
                            propDirty = true;
                    }
                    if (propDirty)
                        dirty = true;
                    else if (desc != null && desc.lazy[p] && v is IPersistentCollection)
                        IPersistentCollection(v).uninitialize();
                }
                else {
                    var traversed:Boolean = cache[v] != null;
                    if (internalUninitializeEntityGraph(v, cache))
                        dirty = true;
                    else if (desc != null && v is IEntity && desc.lazy[p] && !traversed) {
                        var proxy:Object = Type.forInstance(v).constructor.newInstance();
                        if (proxy.meta::defineProxy3(v))
                            entity[p] = proxy;
                    }
                }
            }

            return dirty;
        }
    }
}
