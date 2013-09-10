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

package org.granite.tide.rpc {

    import mx.rpc.remoting.mxml.Operation;
    import mx.core.mx_internal;
    
    import mx.rpc.AsyncToken;
    import mx.messaging.messages.IMessage;
	import mx.messaging.events.MessageEvent;
    import mx.rpc.remoting.mxml.RemoteObject;
	import mx.rpc.events.AbstractEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.events.FaultEvent;
    
    import org.granite.tide.Tide;
    
    use namespace mx_internal;


    [ExcludeClass]
    public class TideOperation extends Operation {

		private var _tide:Tide;
		
		
        public function TideOperation(tide:Tide, svc:RemoteObject = null, name:String = null) : void {
            super(svc, name);
            _tide = tide;
        }
        
        
    	override public function cancel(id:String = null):AsyncToken {
    		_tide.busy = false;
    		
        	return super.cancel(id);
    	}
		
		
        mx_internal override function invoke(msg:IMessage, token:AsyncToken = null):AsyncToken {
			_tide.busy = true;
			
			_tide.processInterceptors(msg, true);

            return super.invoke(msg, token);
        }
        
    	mx_internal override function preHandle(event:MessageEvent):AsyncToken {
    		_tide.busy = false;
    		
    		return super.preHandle(event);
        }
        
    	mx_internal override function dispatchRpcEvent(event:AbstractEvent):void {
    		super.dispatchRpcEvent(event);
    		
        	if (!event.isDefaultPrevented()) {
	    		if (event is FaultEvent && FaultEvent(event).fault && FaultEvent(event).fault.faultCode == 'Channel.Call.Failed')
	    			_tide.disconnected = true;
	    		else
	    			_tide.disconnected = false;
	    	}
    	}    
    }
}