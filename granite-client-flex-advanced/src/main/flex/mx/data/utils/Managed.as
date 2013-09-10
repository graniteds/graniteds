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

package mx.data.utils {
	
	import flash.utils.getQualifiedClassName;
	import flash.utils.Dictionary;
    import mx.logging.ILogger;
    import mx.logging.Log;
	import mx.data.IManaged;
	import mx.events.PropertyChangeEvent;
	
	import org.granite.meta;
	import org.granite.tide.IEntity;
	import org.granite.tide.IEntityManager;
	import org.granite.tide.EntityDescriptor;
	
	
	/**
	 * 	Implementation of the Flex Managed class with Tide EntityManager
	 * 	
	 * 	@author William DRAI
	 */
	public class Managed {
	    
        private static var log:ILogger = Log.getLogger("org.granite.mx.data.utils.Managed");
        
	    public static function createUpdateEvent(obj:IManaged, property:Object, event:PropertyChangeEvent):PropertyChangeEvent {
	        return event;
	    }
	    
	    public static function getDestination(obj:IManaged):String {
	    	var em:IEntityManager = getEntityManager(obj);
	        if (em)
	            return em.meta_destination;
	        return null;
	    }
	    
	    public static function setProperty(obj:IManaged, property:Object, oldValue:*, newValue:*):void {
	    	try {
	    		// Stop here when entity is not initialized
		    	if (!obj.meta::isInitialized())
		    		return;
		    }
		    catch (e:ReferenceError) {
		    	// The entity class does not implement meta:isInitialized
		    }
		    
	    	var em:IEntityManager = getEntityManager(obj);
	        if (em)
	            em.meta_setEntityProperty(obj, property.toString(), oldValue, newValue);
	        
	        if (newValue !== oldValue && (!(oldValue is Number) || !(newValue is Number) || !(isNaN(newValue) && isNaN(oldValue))))
	            obj.dispatchEvent(PropertyChangeEvent.createUpdateEvent(obj, property, oldValue, newValue));
	    }
	    
	    public static function getProperty(obj:IManaged, property:Object, value:*):* {
	    	try {
		    	if (!value && !obj.meta::isInitialized())
		    		log.error("Entity [{0} {1}] not initialized: property {2} cannot be retrieved", getQualifiedClassName(obj), obj.uid, property);
		    }
		    catch (e:ReferenceError) {
		    	// The entity class does not implement meta:isInitialized
		    }
	    	
	    	var em:IEntityManager = getEntityManager(obj);
	        if (em)
	            return em.meta_getEntityProperty(obj, property.toString(), value);
	        return value;
	    }
	    
	    
	    public static const ENTITY_MANAGER_GDS20:int = 20;
	    public static const ENTITY_MANAGER_GDS21:int = 21;
	    public static const ENTITY_MANAGER_DICTIONARY:int = 0;
	    
	    public static var _entityManagerMode:int = -1;
	    
	    public static function get entityManagerMode():int {
	    	return _entityManagerMode;
	    }
	    
	    public static function set entityManagerMode(mode:int):void {
	    	if (mode == ENTITY_MANAGER_GDS21)
	    		log.info("Using GDS 2.1+ entityManager mode");
	    	else if (mode == ENTITY_MANAGER_GDS20)
	    		log.info("Using GDS 2.0 entityManager mode");
	    	else if (mode == ENTITY_MANAGER_DICTIONARY)
	    		log.warn("Using default entityManager mode. Having a meta::entityManager property in entity classes gives much better performance.");
	    	else
	    		throw new Error("Illegal EntityManager mode: " + mode);
	    	
	    	_entityManagerMode = mode;
	    }
	    
	    
	    private static var _entityManagers:Dictionary = new Dictionary(true);
	    
	    public static function getEntityManager(obj:IEntity):IEntityManager {
	    	if (entityManagerMode == -1)
	    		new EntityDescriptor(obj);	// Force detection of entityManager mode
	    	
	    	if (entityManagerMode == ENTITY_MANAGER_GDS21)
	    		return IEntityManager(obj.meta::entityManager);
	    	else if (entityManagerMode == ENTITY_MANAGER_GDS20)
	    		return IEntityManager(Object(obj).meta_getEntityManager());
	    	return IEntityManager(_entityManagers[obj]);
	    }
	    
	    public static function setEntityManager(obj:IEntity, em:IEntityManager):void {
	    	if (entityManagerMode == -1)
	    		new EntityDescriptor(obj);	// Force detection of entityManager mode
	    	
	    	if (entityManagerMode == ENTITY_MANAGER_GDS21)
	    		obj.meta::entityManager = em;
	    	else if (entityManagerMode == ENTITY_MANAGER_GDS20)
	    		Object(obj).meta_setEntityManager(em);
	    	else {
	    		if (em != null)
	    			_entityManagers[obj] = em;
	    		else
	    			delete _entityManagers[obj];
	    	}
	    }
	    
	    public static function resetEntity(obj:IEntity):void {
	    	var em:IEntityManager = getEntityManager(obj);
	    	if (em)
	    		em.meta_resetEntity(obj);
	    	else
	    		log.warn("Could not reset entity {0}: no entity manager attached", obj.uid);
	    }
	}
}
