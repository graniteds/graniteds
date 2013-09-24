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
package org.granite.tide.impl {

    import flash.events.EventDispatcher;
    import flash.events.TimerEvent;
    import flash.net.LocalConnection;
    import flash.utils.Dictionary;
    import flash.utils.Timer;
    import flash.utils.flash_proxy;
    
    import mx.collections.IList;
    import mx.collections.ItemResponder;
    import mx.collections.errors.ItemPendingError;
    import mx.core.ClassFactory;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.InvokeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    
    import org.granite.reflect.Parameter;
    import org.granite.reflect.Type;
    import org.granite.tide.Tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    /**
     *	ComponentFactory is a factory that handles instantiation of components and inject static dependencies 	
     * 
     * 	@author William DRAI
     */
    [ExcludeClass]
    public class ComponentFactory {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.impl.ComponentFactory");


        private var _type:Type;
        private var _properties:Object;


        public function ComponentFactory(type:Type, properties:Object):void {
            _type = type;
            _properties = properties;
        }
        
        public function get type():Type {
        	return _type;
       	}
	    
	    
    	/**
    	 * Instantiate component and perform injection
    	 * 
    	 * @param name component name
    	 * @param context context
    	 * 
    	 * @return new component instance
    	 */ 
        public function newInstance(name:String, context:BaseContext):* {
    		var instance:Object = instantiateComponent(name, context);
    		injectProperties(instance, context);
    		return instance;
    	}
    	
    	/**
    	 * Instantiate component
    	 * 
    	 * @param name component name
    	 * @param context context
    	 * 
    	 * @return new component instance
    	 */ 
    	private function instantiateComponent(name:String, context:BaseContext):Object {
    		var instance:Object = null;
            try {
            	instance = _type.constructor.newInstance();
            	
            	if (instance is IComponent)
            		IComponent(instance).meta_init(name, context);
            }
            catch (e:ArgumentError) {
                instance = null;
            }
            
            return instance;
      	}
	     
    	/**
    	 * Inject static properties
    	 *  
    	 * @param instance instance to inject
    	 * @param context context
    	 */ 
	    private function injectProperties(instance:Object, context:BaseContext):void {   
    		if (instance != null && _properties != null) {
            	for (var p:String in _properties) {
            	    var value:* = _properties[p];
            	    if (value is String && String(value).match(/#{.*}/)) {
            	        var val:String = String(value);
            	        var prop:String = val.substring(2, val.length-1);
            	        var chain:Array = prop.split(".");
            	        value = context;
            	        for each (var c:String in chain) 
            	            value = value[c];
            	    }
            	    
            		instance[p] = value;
    			}
            }
    	}
    }
}
