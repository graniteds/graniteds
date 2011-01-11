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
    
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.IEventDispatcher;
    import flash.utils.flash_proxy;
    
    import mx.utils.ObjectProxy;
    import mx.utils.object_proxy;
    
    import org.granite.tide.events.TideUIEvent;

    use namespace flash_proxy;
    use namespace object_proxy;


    [Bindable]
	/**
	 * 	Subcontext is a simple proxy that owns 'real' components in a particular context namespace
	 * 
     * 	@author William DRAI
	 */
    public dynamic class Subcontext extends ObjectProxy {
        	
        private var _name:String;
        private var _context:BaseContext;


        public function Subcontext(name:String = null, context:BaseContext = null) {
            super();
            meta_init(name, context);
        }
        
        public function meta_init(name:String, context:BaseContext):void {
            _name = name;
            _context = context;
       	}
        
        public function get meta_name():String {
            return _name;
        }
        
        public function get meta_context():BaseContext {
            return _context;
        }
        
        
        override flash_proxy function getProperty(name:*):* {
        	return _context[_name + "." + name];
        }
                
        override flash_proxy function setProperty(name:*, value:*):void {
        	_context[_name + "." + name] = value;
        }
        
        
		public function meta_internalSetProperty(propertyName:*, value:*):void {
			super.setProperty(propertyName, value);
		}
        
		public function meta_internalGetProperty(propertyName:*):* {
			return super.getProperty(propertyName);
		}
        
        
        /**
         *  Dispatch a context event 
         *   
         *  @param type event type
         *  @param params optional event parameters
         */
        public function raiseEvent(type:String, ...params:Array):void {
            _context.meta_tide.invokeObservers(_context, _name + '.', type, params);
        }
        
        /**
         *  Dispatch a context event in the current subcontext 
         *   
         *  @param event event
         *  @return event sent (returned from Flex dispatchEvent)
         */
        public override function dispatchEvent(event:Event):Boolean {
        	if (event.type == TideUIEvent.TIDE_EVENT) {
        		_context.meta_dispatchEvent(event, _name + '.');
        		return true;
        	}
        	return super.dispatchEvent(event);
        }
    }
}
