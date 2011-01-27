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

package org.granite.tide.spring {

	import flash.utils.Dictionary;
    import flash.utils.flash_proxy;
    import flash.events.Event;
    
    import mx.collections.ArrayCollection;
    import mx.collections.ItemResponder;
    import mx.core.Application;
    import mx.events.PropertyChangeEvent;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.AsyncToken;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.object_proxy;
    
    import org.granite.events.SecurityEvent;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.IIdentity;
    import org.granite.tide.ITideResponder;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.events.TideFaultEvent;
    
    use namespace flash_proxy;
    use namespace object_proxy;
    

    [Bindable]
	/**
	 * 	Implementation of the Tide identity component for Spring services
	 * 
     * 	@author William DRAI
     * 	@author Sebastien DELEUZE
     */
    public class Identity extends Component implements IIdentity {
        
        public override function meta_init(componentName:String, context:BaseContext):void {
            super.meta_init(componentName, context);
            addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, loggedInChangeHandler);
            this.loggedIn = false;
        }
        
        public function set context(context:BaseContext):void {
            _context = context;
        }
        
        
        public function get loggedIn():Boolean {
            return getProperty("loggedIn") as Boolean;
        }
        public function set loggedIn(loggedIn:Boolean):void {
            setProperty("loggedIn", loggedIn);
        }
        
        public function get username():String {
            return getProperty("username") as String;
        }
        public function set username(username:String):void {
            setProperty("username", username);
        }
        
        private function loggedInChangeHandler(event:PropertyChangeEvent):void {
            if (event.property == "loggedIn" && event.oldValue == false && event.newValue == true)
                initSecurityCache();
        }
        
        
        /**
         * 	Triggers a remote call to check is user is currently logged in
         *  Can be used at application startup to handle browser refresh cases
         * 
         *  @param resultHandler optional result handler
         *  @param faultHandler optional fault handler 
         */
        public function isLoggedIn(resultHandler:Function = null, faultHandler:Function = null):void {
            _context.meta_isLoggedIn(this, resultHandler, faultHandler);
        }
        
        
        public function login(username:String, password:String, resultHandler:Function = null, faultHandler:Function = null):void {
            _context.meta_login(this, username, password, resultHandler, faultHandler);
        }
        
        
        public function logout():void {
            this.loggedIn = false;
            this.username = null;
            
            _context.meta_logout(this);
            
            clearSecurityCache();
        }
        
        
        private var _ifAllGrantedCache:Object = new Object();
        private var _ifNotGrantedCache:Object = new Object();
        private var _ifAnyGrantedCache:Object = new Object();
        
        /**
         *	Check access for the specified roles
         *  
         *  @param roleName role name
         *  @param resultHandler result handler
         *  @param faultHandler fault handler
         *  @return true is role allowed
         */ 
        [Bindable("ifAllGrantedChanged")]
        public function ifAllGranted(roleName:String, resultHandler:Function = null, faultHandler:Function = null):Boolean {
            var has:* = _ifAllGrantedCache[roleName];
            if (has === undefined) {
                if (loggedIn) {
                    var responder:TideRoleResponder = new TideRoleResponder(ifAllGrantedResultHandler, ifAllGrantedFaultHandler, roleName);
                    responder.addHandlers(resultHandler, faultHandler);
                    _context.meta_callComponent(this, "ifAllGranted", [roleName, responder], false);
                    _ifAllGrantedCache[roleName] = responder;
                }
                return false;
            }
            if (has is TideRoleResponder) {
            	has.addHandlers(resultHandler, faultHandler);
            	return false;
            }
            if (resultHandler != null) {
                var event:TideResultEvent = new TideResultEvent(TideResultEvent.RESULT, _context, false, false, null, null, has);
                resultHandler(event, roleName);
            }
            return (has as Boolean);
        }
        
        private function ifAllGrantedResultHandler(event:TideResultEvent, roleName:String):void {
            _ifAllGrantedCache[roleName] = event.result as Boolean;
            dispatchEvent(new Event("ifAllGrantedChanged"));
        }
        
        private function ifAllGrantedFaultHandler(event:TideFaultEvent, roleName:String):void {
            delete _ifAllGrantedCache[roleName];
        }
        
        /**
         *	Check access for the specified roles
         *  
         *  @param roleName role name
         *  @param resultHandler result handler
         *  @param faultHandler fault handler
         *  @return true is role allowed
         */ 
        [Bindable("ifNotGrantedChanged")]
        public function ifNotGranted(roleName:String, resultHandler:Function = null, faultHandler:Function = null):Boolean {
            var has:* = _ifNotGrantedCache[roleName];
            if (has === undefined) {
                if (loggedIn) {
                    var responder:TideRoleResponder = new TideRoleResponder(ifNotGrantedResultHandler, ifNotGrantedFaultHandler, roleName);
                    responder.addHandlers(resultHandler, faultHandler);
                    _context.meta_callComponent(this, "ifNotGranted", [roleName, responder], false);
                    _ifNotGrantedCache[roleName] = responder;
                }
                return false;
            }
            if (has is TideRoleResponder) {
            	has.addHandlers(resultHandler, faultHandler);
            	return false;
            }
            if (resultHandler != null) {
                var event:TideResultEvent = new TideResultEvent(TideResultEvent.RESULT, _context, false, false, null, null, has);
                resultHandler(event, roleName);
            }
            return (has as Boolean);
        }
        
        private function ifNotGrantedResultHandler(event:TideResultEvent, roleName:String):void {
            _ifNotGrantedCache[roleName] = event.result as Boolean;
            dispatchEvent(new Event("ifNotGrantedChanged"));
        }
        
        private function ifNotGrantedFaultHandler(event:TideFaultEvent, roleName:String):void {
            delete _ifNotGrantedCache[roleName];
        }
        
        /**
         *	Check access for the specified roles
         *  
         *  @param roleName role name
         *  @param resultHandler result handler
         *  @param faultHandler fault handler
         *  @return true is role allowed
         */ 
        [Bindable("ifAnyGrantedChanged")]
        public function ifAnyGranted(roleName:String, resultHandler:Function = null, faultHandler:Function = null):Boolean {
            var has:* = _ifAnyGrantedCache[roleName];
            if (has === undefined) {
                if (loggedIn) {
                    var responder:TideRoleResponder = new TideRoleResponder(ifAnyGrantedResultHandler, ifAnyGrantedFaultHandler, roleName);
                    responder.addHandlers(resultHandler, faultHandler);
                    _context.meta_callComponent(this, "ifAnyGranted", [roleName, responder], false);
                    _ifAnyGrantedCache[roleName] = responder;
                }
                return false;
            }
            if (has is TideRoleResponder) {
            	has.addHandlers(resultHandler, faultHandler);
            	return false;
            }
            if (resultHandler != null) {
                var event:TideResultEvent = new TideResultEvent(TideResultEvent.RESULT, _context, false, false, null, null, has);
                resultHandler(event, roleName);
            }
            return (has as Boolean);
        }
        
        private function ifAnyGrantedResultHandler(event:TideResultEvent, roleName:String):void {
            _ifAnyGrantedCache[roleName] = event.result as Boolean;
            dispatchEvent(new Event("ifAnyGrantedChanged"));
        }
        
        private function ifAnyGrantedFaultHandler(event:TideFaultEvent, roleName:String):void {
            delete _ifAnyGrantedCache[roleName];
        }
        
        
        private var _permissionsCache:Dictionary = new Dictionary(true);
        
        /**
         *	Check permission for the specified target and action
         *  
         *  @param target target object
         *  @param action requested action
         *  @param resultHandler result handler
         *  @param faultHandler fault handler
         *  @return true is permission granted
         */ 
        [Bindable("permissionChanged")]
        public function hasPermission(target:Object, action:String, resultHandler:Function = null, faultHandler:Function = null):Boolean {
            var cache:Object = _permissionsCache[target];
            if (cache == null || (cache && cache[action] === undefined)) {
                if (loggedIn) {
                    var responder:TidePermissionResponder = new TidePermissionResponder(permissionResultHandler, permissionFaultHandler, target, action);
                    responder.addHandlers(resultHandler, faultHandler);
                    _context.meta_callComponent(this, "hasPermission", [target, action, responder], false);
                    if (cache == null) {
                        cache = new Object();
                        _permissionsCache[target] = cache;
                    }
                    cache[action] = responder;
                }
                return false;
            }
            if (cache[action] is TidePermissionResponder) {
            	cache[action].addHandlers(resultHandler, faultHandler);
            	return false;
            }
            if (resultHandler != null) {
                var event:TideResultEvent = new TideResultEvent(TideResultEvent.RESULT, _context, false, false, null, null, cache[action]);
                resultHandler(event, target, action);
            }
            return (cache[action] as Boolean);
        }
        
        private function permissionResultHandler(event:TideResultEvent, target:Object, action:String):void {
            _permissionsCache[target][action] = event.result as Boolean;
            dispatchEvent(new Event("permissionChanged"));
        }
        
        private function permissionFaultHandler(event:TideFaultEvent, target:Object, action:String):void {
            delete _permissionsCache[target];
        }
        
        
        private function initSecurityCache():void {
            dispatchEvent(new Event("ifAllGrantedChanged"));
            dispatchEvent(new Event("ifNotGrantedChanged"));
            dispatchEvent(new Event("ifAnyGrantedChanged"));
            dispatchEvent(new Event("permissionChanged"));
        }
        
        /**
         * 	Clear the security cache
         */
        public function clearSecurityCache():void {
            _ifAllGrantedCache = new Object();
            _ifNotGrantedCache = new Object();
            _ifAnyGrantedCache = new Object();
            _permissionsCache = new Dictionary(true);
        }
        
        
        public static function instance():Identity {
            return Spring.getInstance().getSpringContext().identity as Identity;
        }
    }
}


import org.granite.tide.ITideResponder;
import org.granite.tide.events.TideResultEvent;
import org.granite.tide.events.TideFaultEvent;


class TideRoleResponder implements ITideResponder {
    
    private var _resultHandlers:Array = new Array();
    private var _faultHandlers:Array = new Array();
    private var _roleName:String;
    
    
    public function TideRoleResponder(resultHandler:Function, faultHandler:Function, roleName:String):void {
        if (resultHandler != null)
            _resultHandlers.push(resultHandler);
        if (faultHandler != null)
            _faultHandlers.push(faultHandler);   
        _roleName = roleName;
    }
    
    public function addHandlers(resultHandler:Function = null, faultHandler:Function = null):void {
        if (resultHandler != null)
            _resultHandlers.push(resultHandler);
        if (faultHandler != null)
            _faultHandlers.push(faultHandler);
    }
    
    
    public function result(event:TideResultEvent):void {
        for each (var resultHandler:Function in _resultHandlers) 
            resultHandler(event, _roleName);
    }
    
    public function fault(event:TideFaultEvent):void {
        for each (var faultHandler:Function in _faultHandlers)
            faultHandler(event, _roleName);
    } 
}


class TidePermissionResponder implements ITideResponder {
    
    private var _resultHandlers:Array = new Array();
    private var _faultHandlers:Array = new Array();
    private var _target:Object;
    private var _action:String;
    
    
    public function TidePermissionResponder(resultHandler:Function, faultHandler:Function, target:Object, action:String):void {
        if (resultHandler != null)
            _resultHandlers.push(resultHandler);
        if (faultHandler != null)
            _faultHandlers.push(faultHandler);   
        _target = target;
        _action = action;
    }
    
    public function addHandlers(resultHandler:Function = null, faultHandler:Function = null):void {
        if (resultHandler != null)
            _resultHandlers.push(resultHandler);
        if (faultHandler != null)
            _faultHandlers.push(faultHandler);
    }
    
    
    public function result(event:TideResultEvent):void {
        for each (var resultHandler:Function in _resultHandlers) 
            resultHandler(event, _target, _action);
    }
    
    public function fault(event:TideFaultEvent):void {
        for each (var faultHandler:Function in _faultHandlers)
            faultHandler(event, _target, _action);
    } 
}
