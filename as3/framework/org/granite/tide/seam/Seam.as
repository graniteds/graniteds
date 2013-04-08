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

package org.granite.tide.seam {
	
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
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.events.TideContextEvent;
    import org.granite.tide.seam.security.Identity;
    import org.granite.tide.seam.framework.StatusMessages;
    import org.granite.tide.seam.framework.ConversationList;
    

	[Bindable]
    /**
     * 	Implementation of the Tide singleton for Seam services
     * 
     *  @author William DRAI
     */
	public class Seam extends Tide {
		
		public function Seam(destination:String) {
		    super(destination);
		}
		
		public static function getInstance(destination:String = null):Seam {
			return Tide.getInstance(destination != null ? destination : "server", Seam) as Seam;
		}
		
		/**
		 *	Clear Tide singleton (should be used only for testing)
		 */
		public static function resetInstance():void {
			Tide.resetInstance();
		}
		
		protected override function init(contextClass:Class, componentClass:Class):void {
		    super.init(Context, Component);
		    addComponent("identity", Identity);
			getDescriptor("identity").scope = Tide.SCOPE_SESSION;			// Default scope for remote proxies is EVENT
		    addComponent("statusMessages", StatusMessages);
			getDescriptor("statusMessages").scope = Tide.SCOPE_SESSION;		// Default scope for remote proxies is EVENT
			getDescriptor("statusMessages").remoteSync = Tide.SYNC_SERVER_TO_CLIENT;
		    getDescriptor("statusMessages").destroyMethodName = "destroy";
		    addComponent("conversationList", ConversationList);
			getDescriptor("conversationList").scope = Tide.SCOPE_SESSION;	// Default scope for remote proxies is EVENT
			getDescriptor("conversationList").remoteSync = Tide.SYNC_SERVER_TO_CLIENT;
		    getDescriptor("conversationList").destroyMethodName = "destroy";
		    
		    addContextEventListener(ConversationList.CONVERSATION_TIMEOUT, conversationTimeoutHandler, true); 
		}
		
		public function getSeamContext(conversationId:String = null):Context {
		    return super.getContext(conversationId) as Context;
		}
		
		protected override function initRemoteObject():void {
		    _ro = createRemoteObject();
		    
			var op1:TideOperation = createOperation("invokeComponent");
			var op3:TideOperation = createOperation("validateObject");
			var op4:TideOperation = createOperation("logout");
			var op5:TideOperation = createOperation("resyncContext");
			_ro.operations = { invokeComponent: op1, validateObject: op3, logout: op4, resyncContext: op5 };
			
			_roInitialize = createRemoteObject();
			var op2:TideOperation = createOperation("initializeObject");
			_roInitialize.operations = { initializeObject: op2 };
		}
		
		public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
			var op:TideOperation = new SeamOperation(this, ro);
			op.name = name;
			return op;
		}
		
		
		protected override function isLoggedIn():Boolean {
		    return getSeamContext().identity.loggedIn;
		}
		
		protected override function setLoggedIn(value:Boolean):void {
		    getSeamContext().identity.loggedIn = value;
		}
		
		
		public override function login(ctx:BaseContext, component:IComponent, username:String, password:String, responder:ITideResponder = null, charset:String = null):AsyncToken {
			super.login(ctx, component, username, password, responder, charset);
			
			return invokeComponent(ctx, component, "login", [], responder, true, loginSuccessHandler, loginFaultHandler);
		}
		
		public override function checkLoggedIn(ctx:BaseContext, component:IComponent, responder:ITideResponder = null):AsyncToken {
			return invokeComponent(ctx, component, "isLoggedIn", [], responder, true, isLoggedInSuccessHandler, null);
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
        
        
        private function conversationTimeoutHandler(event:TideContextEvent):void {
        	var conversationId:String = event.params[0] as String;
        	if (conversationId != null) {
        		var ctx:Context = getSeamContext(conversationId);
        		if (ctx) {
	        		// Mark timed out context as finished
	        		ctx.meta_markAsFinished();
	        	}
	        }
        }
	}
}
