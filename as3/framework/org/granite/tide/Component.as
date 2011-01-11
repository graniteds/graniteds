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

    import flash.utils.flash_proxy;
    
    import mx.collections.IList;
    import mx.collections.errors.ItemPendingError;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.mxml.RemoteObject;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    
    import org.granite.tide.impl.ComponentProperty;
    import org.granite.tide.impl.ContextExpression;
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    /**
     * 	Component is the default implementation of remote proxies
     * 	It can handle deferred property set/get on proxies
     * 
     *  @author William DRAI
     */
    public dynamic class Component extends ObjectProxy implements IComponent, IPropertyHolder {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.Component");

        private var _name:String;
        protected var _context:BaseContext;
        
        private var _templateObject:Object = null;


        public function meta_init(name:String, context:BaseContext):void {
            proxyClass = ComponentProperty;
            log.info("init {0} ", name);
            _name = name;
            _context = context;
        }
        
        public function set meta_templateObject(templateObject:Object):void {
        	_templateObject = templateObject;
        }
        
        public function get meta_name():String {
            return _name;
        }
        
        public function get meta_context():BaseContext {
            return _context;
        }
        
        public function get meta_remoteObject():RemoteObject {
        	return _context.meta_tide.getRemoteObject(_name);
        }
        
        
        object_proxy override function get object():Object {
            return super.object;
        }

        public function get object():Object {
            return object_proxy::object;
        }
        
        
        public function meta_clear():void {
            for (var name:String in object) {
                this[name] = null;
                delete object[name];
            }
        }

		
		protected function setInternalProperty(propertyName:*, value:*):void {
			super.setProperty(propertyName, value);
		}
		
		
        flash_proxy override function setProperty(propertyName:*, value:*):void {
            var propName:String = propertyName is QName ? QName(propertyName).localName : propertyName;

            var val:Object = meta_internalSetProperty(propName, value, true);
        }

        private function meta_internalSetProperty(propName:String, value:*, addUpdate:Boolean = false):* {
            var previousValue:Object = object[propName];
            
            var val:Object = value;
            if (val is IEntity) {
                log.debug("setProperty (entity) {0}.{1}", _name, propName);
                _context.meta_addReference(val, null, new ContextExpression(_name, propName));
            }
            else if (val == null) {
                log.debug("setProperty (null) {0}.{1}", _name, propName);
            }
            else if (val != null && !ObjectUtil.isSimple(val) && !(val is IList) && object.propertyIsEnumerable(propName)) {
                log.debug("setProperty (complex) {0}.{1} > {2}", _name, propName, val);
                val = new ComponentProperty(_name, propName, _context, val);
            }
            else
                log.debug("setProperty (simple) {0}.{1}", _name, propName);

            if (addUpdate)
                _context.meta_addUpdate(_name, propName, val);
            
            super.setProperty(propName, val);

            return val;
        }


        public function meta_propertyResultHandler(propName:String, event:ResultEvent):void {
            var val:Object = event.result;
            // Warning: DO NOT USE the default toString for val, it triggers the lazy initialization of collection elements
            // log.debug("propertyResultHandler {0} isSimple: {1} object {2} > {3}", propName, ObjectUtil.isSimple(val), object, BaseContext.toString(val));

            meta_internalSetProperty(propName, val, false);
        }

        flash_proxy override function getProperty(propertyName:*):* {
            var propName:String = propertyName is QName ? QName(propertyName).localName : propertyName;

            var value:Object = super.getProperty(propertyName);

            _context.meta_addResult(_name, propName);
            
            // Don't create proxy for template properties to keep type safety
            if (_templateObject && _templateObject.hasOwnProperty(propName))
                return value;

            return value;
        }


        flash_proxy override function callProperty(name:*, ...args):* {
            if (name == "toString" || name == "valueOf" || name == "addEventListener" || name == "removeEventListener")
                return super.callProperty(name);

            var op:String = name is QName ? QName(name).localName : name;
            if (args && args.length > 0 && args[0] is BaseContext) {
            	var context:BaseContext = BaseContext(args[0]);
            	return context.meta_callComponent(this, op, args.slice(1));
            }
            
            return _context.meta_callComponent(this, op, args);
        }
    }
}
