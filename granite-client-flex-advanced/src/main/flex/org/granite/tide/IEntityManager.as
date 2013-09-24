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
package org.granite.tide {
    
    import flash.events.IEventDispatcher;
    import mx.rpc.AsyncToken;

    import org.granite.tide.service.ServerSession;

    /**
	 * 	IEntityManager is the interface for entity management (!)
	 * 	It is implemented by the Tide context
	 *
	 * 	@author William DRAI
	 */
	public interface IEntityManager extends IEventDispatcher {

		/**
		 * 	Intercept a property getter
		 * 
		 *  @param entity intercepted entity
		 *  @param propName intercepted property name
		 *  @param value current value
		 */
		function meta_getEntityProperty(entity:IEntity, propName:String, value:*):*;
		
		/**
		 * 	Intercept a property setter
		 * 
		 *  @param entity intercepted entity
		 *  @param propName intercepted property name
		 *  @param oldValue old value
		 *  @param newValue new value
		 */
		function meta_setEntityProperty(entity:IEntity, propName:String, oldValue:*, newValue:*):void;
		
        /**
         *  Merge an object coming from the server in the context
         *
         *  @param obj external object
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param expr current path from the context
         *  @param parent parent object for collections
         *  @param propertyName property name of the current object in the parent object
		 *  @param setter setter function for private property
		 *  @param forceUpdate force update for externalized properties
         *
         *  @return merged object (should === previous when previous not null)
         */
        function meta_mergeExternal(obj:Object, previous:Object = null, expr:IExpression = null, parent:Object = null, propertyName:String = null, setter:Function = null, forceUpdate:Boolean = false):Object;
        
        /**
         *  Discard changes of entity from last version received from the server
         *
         *  @param entity entity to restore
         */ 
		function meta_resetEntity(entity:IEntity):void;
		
        /**
         *  Check if entity property has been changed since last remote call
         *
         *  @param entity entity to check
         *  @param propertyName property to check
         *  @param value current value to compare with saved value
         *   
         *  @return true is value has been changed
         */ 
		function meta_isEntityPropertyChanged(entity:IEntity, propName:String, value:Object):Boolean;
		
        /**
         *  Check if entity has changed since last save point
         *
         *  @param entity entity to restore
         *  @param propName property name
         *  @param value
         *   
         *  @return entity is dirty
         */ 
        function meta_isEntityChanged(entity:IEntity, propName:String = null, value:* = null):Boolean;
        
        /**
         * 	@private
         *  Indicates if an object is initialized
         *  For non managed objects, always return true
         * 
         *  @return true when initialized
         */
		function meta_isObjectInitialized(object:Object):Boolean;
        
        /**
         * 	@private
         *  Calls an object initializer
         * 
         *  @param obj collection to initialize
         */
		function meta_initializeObject(serverSession:ServerSession, entity:Object):void;
		
        /**
         *  @private 
         *  Calls an object validator
         * 
         *  @param obj entity to validate
         *  @param propertyName property to validate
         *  @param value value to validate
         * 
         *  @return the operation token
         */
		function meta_validateObject(serverSession:ServerSession, entity:IEntity, propertyName:String, value:Object):AsyncToken;
		
        /**
         *  Equality for objects, using uid property when possible
         *
         *  @param obj1 object
         *  @param obj2 object
         * 
         *  @return true when objects are instances of the same entity
         */ 
        function meta_objectEquals(obj1:Object, obj2:Object):Boolean;
	}
}
