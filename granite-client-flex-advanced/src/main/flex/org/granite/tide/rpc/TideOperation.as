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
    import org.granite.tide.service.ServerSession;

    use namespace mx_internal;


    [ExcludeClass]
    public class TideOperation extends Operation {

		private var _serverSession:ServerSession;
		
		
        public function TideOperation(serverSession:ServerSession, svc:RemoteObject = null, name:String = null) : void {
            super(svc, name);
            _serverSession = serverSession;
        }
        
        
    	override public function cancel(id:String = null):AsyncToken {
            _serverSession.busy = false;
    		
        	return super.cancel(id);
    	}
		
		
        mx_internal override function invoke(msg:IMessage, token:AsyncToken = null):AsyncToken {
			_serverSession.busy = true;
			
			_serverSession.processMessageInterceptors(msg, true);

            return super.invoke(msg, token);
        }
        
    	mx_internal override function preHandle(event:MessageEvent):AsyncToken {
    		_serverSession.busy = false;
    		
    		return super.preHandle(event);
        }
        
    	mx_internal override function dispatchRpcEvent(event:AbstractEvent):void {
    		super.dispatchRpcEvent(event);
    		
        	if (!event.isDefaultPrevented()) {
	    		if (event is FaultEvent && FaultEvent(event).fault && FaultEvent(event).fault.faultCode == 'Channel.Call.Failed')
                    _serverSession.disconnected = true;
	    		else
                    _serverSession.disconnected = false;
	    	}
    	}    
    }
}