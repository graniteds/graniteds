/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
    
    import mx.logging.Log;
    import mx.logging.ILogger;
    
    import mx.collections.IList;
    import mx.collections.ItemResponder;
    import mx.collections.errors.ItemPendingError;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.InvokeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    
    import org.granite.tide.IEntity;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.BaseContext;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    /**
     *	Property proxy for component proxies 
     * 
     * 	@author William DRAI
     */
    [ExcludeClass]
    public dynamic class ComponentProperty extends ObjectProxy implements IPropertyHolder {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.impl.ComponentProperty");

        private var _componentName:String;
        private var _propertyName:String;
        private var _context:BaseContext;

        private var _itemsPending:Object = new Object();


        public function ComponentProperty(componentName:String, propertyName:String, context:BaseContext, object:Object = null) {
            super(object);
            log.info("init: {0}.{1} > {2}", componentName, propertyName, object);
            _componentName = componentName;
            _propertyName = propertyName;
            _context = context;
        }


        override object_proxy function get object():Object {
            return super.object;
        }

        public function get object():Object {
            return object_proxy::object;
        }


        override flash_proxy function setProperty(propertyName:*, value:*):void {
            var propName:String = propertyName is QName ? QName(propertyName).localName : propertyName;

            var val:Object = meta_internalSetProperty(propName, value, true);

            super.setProperty(propertyName, val);
        }

        private function meta_internalSetProperty(propName:String, value:*, addUpdate:Boolean = false):* {
            var previousValue:Object = object[propName];
            
            var val:Object = value;
            if (val is IEntity) {
                log.debug("setProperty (entity) {0}.{1}.{2}", _componentName, _propertyName, propName);
                _context.meta_attachEntity(IEntity(val));
            }
            else if (val == null) {
                log.debug("setProperty (null) {0}.{1}.{2}", _componentName, _propertyName, propName);
            }
            else if (val != null && !ObjectUtil.isSimple(val) && !(val is IList) && object.propertyIsEnumerable(propName)) {
                log.debug("setProperty (complex) {0}.{1}.{2}", _componentName, _propertyName, propName);
                val = new ComponentProperty(_componentName, _propertyName + "." + propName, _context, val);
            }
            else
                log.debug("setProperty (simple) {0}.{1}.{2}", _componentName, _propertyName, propName);

            if (addUpdate)
                _context.meta_addUpdate(_componentName, _propertyName + "." + propName, val);

            super.setProperty(propName, val);

            return val;
        }


        public function meta_propertyResultHandler(propName:String, event:ResultEvent):void {
            var val:Object = event.result;
            // Warning: DO NOT USE the default toString for val, it triggers the lazy initialization of collection elements
            // log.debug("propertyResultHandler {0} isSimple: {1} object {2} > {3}", propName, ObjectUtil.isSimple(val), object, BaseContext.toString(val));

            meta_internalSetProperty(propName, val, false);

            var ipe:ItemPendingError = _itemsPending[propName];
            if (ipe != null) {
                if (ipe.responders) {
                    for (var j:int = 0; j < ipe.responders.length; j++) {
                        log.debug("{0} property {1}.{2} respond", _componentName, _propertyName, propName);
                        ipe.responders[j].result(event);
                    }
                }

                delete _itemsPending[propName];
            }
        }

        override flash_proxy function getProperty(propertyName:*):* {
            var propName:String = propertyName is QName ? QName(propertyName).localName : propertyName;

            var value:Object = super.getProperty(propertyName);

            _context.meta_addResult(_componentName, _propertyName + "." + propName);

            if (value == null || (value is Number && isNaN(value as Number))) { // && !ObjectUtil.isSimple(value) && !(value is IList) && object.propertyIsEnumerable(propName)) {
                value = new ComponentProperty(_componentName, _propertyName + "." + propName, _context);
                
                super.setProperty(propName, value);
            }

            return value;
        }
    }
}
