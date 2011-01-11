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
        
        public var idPropertyName:String;
        public var versionPropertyName:String;
        public var mergeGDS20:Boolean;
        public var mergeGDS21:Boolean;
        
        
        public function EntityDescriptor(entity:IEntity):void {
			
			var type:Type = Type.forInstance(entity);
			var fields:Array = type.getAnnotatedFieldNamesNoCache('Id');
			if (fields.length > 0)
				idPropertyName = String(fields[0]);
			
			fields = type.getAnnotatedFieldNamesNoCache('Version');
			if (fields.length > 0)
				versionPropertyName = String(fields[0]);

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
