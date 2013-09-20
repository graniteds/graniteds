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

package org.granite.tide {
    
    import mx.data.utils.Managed;
    import mx.logging.ILogger;
    import mx.logging.Log;
    
    import org.granite.meta;
    import org.granite.reflect.Field;
    import org.granite.reflect.Type;
    
    
    /**
     * 	EntityDescriptor contains the definition of entities ([Version] properties for now)
     *
     * 	@author William DRAI
     */
	[ExcludeClass]
    public class EntityDescriptor {
	    
        private static var log:ILogger = Log.getLogger("org.granite.tide.EntityDescriptor");
        
		public var className:String;
        public var idPropertyName:String;
        public var versionPropertyName:String;
        public var lazy:Object = {};
        public var mergeGDS20:Boolean;
        public var mergeGDS21:Boolean;
        
        
        public function EntityDescriptor(entity:Object):void {
			
			var type:Type = Type.forInstance(entity);
			className = type.alias;
			
			var fields:Array = type.getAnnotatedFieldNamesNoCache('Id');
			if (fields.length > 0)
				idPropertyName = String(fields[0]);
			
			fields = type.getAnnotatedFieldNamesNoCache('Version');
			if (fields.length > 0)
				versionPropertyName = String(fields[0]);

            fields = type.getAnnotatedFieldNamesNoCache('Lazy');
            for each (var f:String in fields)
                lazy[f] = true;

			if (!(entity is IEntity))
				return;
			
			try {
	        	if (Object(entity).meta::merge !== undefined) {
	            	mergeGDS21 = true;
	            	log.debug("Using GDS 2.1+ meta::merge for entity {0}", type.name);
	            }
	        }
	        catch (e:ReferenceError) {
	        	try {
		            if (Object(entity).meta_merge !== undefined) {
		            	mergeGDS20 = true;
		            	log.debug("Using GDS 2.0 meta_merge for entity {0}", type.name);
		            }
		        }
		        catch (f:ReferenceError) {
		        	// No merge method
		            log.debug("Using defaultMerge for entity {0}", type.name);
		        }
	        }
            
            if (Managed.entityManagerMode == -1) {
            	try {
	            	if (Object(entity).meta::entityManager !== undefined)
	            		Managed.entityManagerMode = Managed.ENTITY_MANAGER_GDS21;
	           	}
	           	catch (e:ReferenceError) {
	           		try {
	            		if (Object(entity).meta_getEntityManager !== undefined && Object(entity).meta_setEntityManager !== undefined)
	            			Managed.entityManagerMode = Managed.ENTITY_MANAGER_GDS20;
	            	}
	            	catch (f:ReferenceError) {
	            		Managed.entityManagerMode = Managed.ENTITY_MANAGER_DICTIONARY;
	            	}
            	}            	
            }
        }
    }
}
