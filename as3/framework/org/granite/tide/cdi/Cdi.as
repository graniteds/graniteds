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

package org.granite.tide.cdi {
	
    import flash.events.EventDispatcher;
    import flash.events.TimerEvent;
    import flash.net.LocalConnection;
    import flash.utils.Dictionary;
    import flash.utils.Proxy;
    import flash.utils.Timer;
    import flash.utils.flash_proxy;
    
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.collections.ItemResponder;
    import mx.controls.Alert;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.events.MessageEvent;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.InvokeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.mxml.Operation;
    import mx.rpc.remoting.mxml.RemoteObject;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    
    import org.granite.events.SecurityEvent;
    import org.granite.tide.Tide;
    import org.granite.tide.ITideResponder;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Component;
    import org.granite.tide.IInvocationCall;
	import org.granite.tide.IIdentity;
    import org.granite.tide.rpc.ComponentResponder;
    import org.granite.tide.rpc.TideOperation;
    

	[Bindable]
	/**
	 * 	Implementation of the Tide singleton for EJB3 services
	 * 
     * 	@author William DRAI
     */
	public class Cdi extends Tide {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.cdi.Cdi");
		
		private static const IDENTITY_NAME:String = "org.granite.tide.cdi.identity";
		
		
		public function Cdi(destination:String) {
		    super(destination);
		}
		
		public static function getInstance(destination:String = null):Cdi {
			return Tide.getInstance(destination != null ? destination : "server", Cdi) as Cdi;
		}
		
		/**
		 *	Clear Tide singleton (should be used only for testing)
		 */
		public static function resetInstance():void {
			Tide.resetInstance();
		}
		
		protected override function init(contextClass:Class, componentClass:Class):void {
		    super.init(Context, Component);
			addComponent(IDENTITY_NAME, Identity);
			setComponentRemoteSync(IDENTITY_NAME, Tide.SYNC_NONE);
			getDescriptor(IDENTITY_NAME).scope = Tide.SCOPE_SESSION;
		}
		
		public function getCdiContext(contextId:String = null):Context {
		    return super.getContext(contextId) as Context;
		}
		
		protected override function initRemoteObject():void {
		    _ro = createRemoteObject();
			var op1:TideOperation = createOperation("invokeComponent");
			var op3:TideOperation = createOperation("validateObject");
			var op4:TideOperation = createOperation("login");
			var op5:TideOperation = createOperation("logout");
			var op6:TideOperation = createOperation("hasRole");
			var op7:TideOperation = createOperation("resyncContext");
			_ro.operations = { invokeComponent: op1, validateObject: op3, login: op4, logout: op5, hasRole: op6, resyncContext: op7 };
			
			_roInitialize = createRemoteObject();
			var op2:TideOperation = createOperation("initializeObject");
			_roInitialize.operations = { initializeObject: op2 };
		}
		
		public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
			var op:TideOperation = new CdiOperation(this, ro);
			op.name = name;
			return op;
		}
		
		
		protected override function isLoggedIn():Boolean {
			return getCdiContext().byType(IIdentity).loggedIn;
		}
		
		protected override function setLoggedIn(value:Boolean):void {
			getCdiContext().byType(IIdentity).loggedIn = value;
		}
		
		public override function checkLoggedIn(ctx:BaseContext, component:IComponent, responder:ITideResponder = null):AsyncToken {
			return invokeComponent(ctx, component, "isLoggedIn", [], responder, true, isLoggedInSuccessHandler, isLoggedInFaultHandler);
		}
        
        public override function isLoggedInSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
			if (componentName == IDENTITY_NAME) {
				getCdiContext()[IDENTITY_NAME].username = data.result.result as String;
				getCdiContext()[IDENTITY_NAME].loggedIn = data.result.result != null;
			}
        	
        	super.isLoggedInSuccessHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }
        
        public override function isLoggedInFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
			if (componentName == IDENTITY_NAME) {
				getCdiContext()[IDENTITY_NAME].username = null;
				getCdiContext()[IDENTITY_NAME].loggedIn = false;
			}
        	
        	super.isLoggedInFaultHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }
		
		public override function login(ctx:BaseContext, component:IComponent, username:String, password:String, responder:ITideResponder = null, charset:String = null):AsyncToken {
			super.login(ctx, component, username, password, responder, charset);
		    
			if (component.meta_name == IDENTITY_NAME) {
				var operation:AbstractOperation = ro.getOperation("login");
				var call:IInvocationCall = ctx.meta_prepareCall(operation);
				var token:AsyncToken = operation.send(call);
				token.addResponder(new ComponentResponder(ctx, cdiLoginSuccessHandler, cdiLoginFaultHandler, component, null, null, operation, false, responder, username));
				return token;
			}
			else
				return invokeComponent(ctx, component, "login", [], responder, true, loginSuccessHandler, loginFaultHandler);
		}
		
        public function cdiLoginSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, username:String, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
			getCdiContext()[IDENTITY_NAME].username = username;
			getCdiContext()[IDENTITY_NAME].loggedIn = true;
        	
        	super.loginSuccessHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }
		
        public function cdiLoginFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, username:String, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
			getCdiContext()[IDENTITY_NAME].username = null;
			getCdiContext()[IDENTITY_NAME].loggedIn = false;
        	
        	super.loginFaultHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }
	    
        
        protected override function extractContextId(event:MessageEvent, fromFault:Boolean = false):String {
            if (event.message.headers[IS_LONG_RUNNING_CONVERSATION_TAG]) // || fromFault ??
                return event.message.headers[CONVERSATION_TAG];
            
            return DEFAULT_CONTEXT;
        }
        protected override function wasConversationEnded(event:MessageEvent):Boolean {
        	return event.message.headers[WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] != null;
        }
        protected override function wasConversationCreated(event:MessageEvent):Boolean {
        	return event.message.headers[WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG] != null;
        }
	}
}
