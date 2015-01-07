/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.ejb {

    import mx.events.PropertyChangeEvent;
    import mx.utils.object_proxy;
    import flash.utils.flash_proxy;
    import flash.events.Event;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.IIdentity;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.service.ServerSession;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    [Bindable]
	/**
	 * 	Implementation of the Tide identity component for EJB3 services
	 * 
     * 	@author William DRAI
     */
    public class Identity extends Component implements IIdentity {

        public function Identity(serverSession:ServerSession = null):void {
            super(serverSession);
        }

        public override function meta_init(componentName:String, context:BaseContext):void {
            super.meta_init(componentName, context);
            addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, loggedInChangeHandler);
            this.loggedIn = false;
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
            _context.meta_isLoggedIn(_serverSession, this, resultHandler, faultHandler);
        }
        
        public function login(username:String, password:String, resultHandler:Function = null, faultHandler:Function = null, charset:String = null):void {
            _context.meta_login(_serverSession, this, username, password, resultHandler, faultHandler, charset);
        }

        public function logout():void {
            this.loggedIn = false;
            this.username = null;
            
            _context.meta_logout(_serverSession, this);
            
            clearSecurityCache();
        }
        
        
        private var _rolesCache:Object = new Object();
        
        /**
         *	Check role permission for the specified role
         *  
         *  @param roleName role name
         *  @param resultHandler result handler
         *  @param faultHandler fault handler
         *  @return true is role allowed
         */ 
        [Bindable("roleChanged")]
        public function hasRole(roleName:String, resultHandler:Function = null, faultHandler:Function = null):Boolean {
            var has:* = _rolesCache[roleName];
            if (has === undefined) {
                if (loggedIn) {
                    var responder:TideRoleResponder = new TideRoleResponder(roleResultHandler, roleFaultHandler, roleName);
                    responder.addHandlers(resultHandler, faultHandler);
                    _context.meta_callComponent(_serverSession, this, "hasRole", [roleName, responder], false);
                    _rolesCache[roleName] = responder;
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
        
        private function roleResultHandler(event:TideResultEvent, roleName:String):void {
            _rolesCache[roleName] = event.result as Boolean;
            dispatchEvent(new Event("roleChanged"));
        }
        
        private function roleFaultHandler(event:TideFaultEvent, roleName:String):void {
            delete _rolesCache[roleName];
        }
        
        
        public function initSecurityCache():void {
            dispatchEvent(new Event("roleChanged"));
        }
        
        /**
         * 	Clear the security cache
         */
        public function clearSecurityCache():void {
            _rolesCache = new Object();
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
