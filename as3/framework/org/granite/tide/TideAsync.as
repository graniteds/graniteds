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
	
    import flash.events.EventDispatcher;
    import flash.events.TimerEvent;
    import flash.net.LocalConnection;
    import flash.utils.Dictionary;
    import flash.utils.Proxy;
    import flash.utils.Timer;
    import flash.utils.flash_proxy;
    
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.controls.Alert;
    import mx.core.Application;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.events.MessageAckEvent;
    import mx.messaging.events.MessageEvent;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.InvokeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.mxml.Operation;
    import mx.rpc.remoting.mxml.RemoteObject;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    
    import org.granite.gravity.Consumer;
    import org.granite.tide.service.IServiceInitializer;
    import org.granite.tide.events.TidePluginEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    

	[Bindable]
    /**
     * 	Plugin for managing asynchronous events with a Gravity Consumer
     * 
     * 	@author William DRAI
     */
	public class TideAsync extends EventDispatcher implements ITidePlugin {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.TideAsync");
		
	    private static var _tideAsync:TideAsync;
	    		
	    private var _destination:String = null;
	    private var _tide:Tide = null;
		protected var _consumer:Consumer = null;
		
		
		public function TideAsync(destination:String = null) {
			_destination = (destination != null ? destination : "tideAsync");
		}
		
		
		public static function getInstance(destination:String = null):TideAsync {
		    if (!_tideAsync)
		        _tideAsync = new TideAsync(destination);
		    
		    return _tideAsync;
		}
		
		public function set tide(tide:Tide):void {
		    log.info("Initializing Tide async proxy");
		    _tide = tide;
		    _tide.addEventListener(Tide.PLUGIN_SET_CREDENTIALS, setCredentials);
		    _tide.addEventListener(Tide.PLUGIN_LOGIN_SUCCESS, loginSuccess);
		    _tide.addEventListener(Tide.PLUGIN_LOGIN_FAULT, loginFault);
		    _tide.addEventListener(Tide.PLUGIN_LOGOUT, logout);
		    
	        _consumer = new Consumer();
	        var serviceInitializer:IServiceInitializer = IServiceInitializer(_tide.getContext().byType(IServiceInitializer));
	        if (serviceInitializer != null)
	        	serviceInitializer.initialize(_consumer);
	        _consumer.destination = _destination;
			
		    log.info("Tide async proxy initialized");
		}
		
		
		private function setCredentials(event:TidePluginEvent):void {
//			_consumer.setIdentity(username, password);
		}
		
		private function loginSuccess(event:TidePluginEvent):void {
		    _consumer.topic = "tide.events." + event.params.sessionId;
		    _consumer.subscribe();
		    _consumer.addEventListener(MessageEvent.MESSAGE, messageHandler);
		}
		
		private function loginFault(event:TidePluginEvent):void {
		}
		
		private function logout(event:TidePluginEvent):void {
		    _consumer.addEventListener(MessageAckEvent.ACKNOWLEDGE, unsubscribeHandler);
		    _consumer.unsubscribe();
		}
		
		private function unsubscribeHandler(e:*):void {
		    _consumer.disconnect();
		    _consumer.removeEventListener(MessageAckEvent.ACKNOWLEDGE, unsubscribeHandler);
		}


        protected function messageHandler(event:MessageEvent):void {
            log.debug("message received {0}", event.toString());
            
            var savedCallContext:Object = _tide.getContext().meta_saveAndResetCallContext();
            
            _tide.result(_tide.getContext(), "", event);
	      	
	      	_tide.getContext().meta_restoreCallContext(savedCallContext);
        }
	}
}
