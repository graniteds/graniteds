/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.seam
{
    import flash.events.TimerEvent;
    import flash.utils.Timer;
    
    import mx.collections.ArrayCollection;
    import mx.messaging.messages.AcknowledgeMessage;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.rpc.AsyncToken;
    import mx.rpc.Fault;
    import mx.rpc.IResponder;
    import mx.rpc.events.AbstractEvent;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    
    import org.granite.tide.invocation.ContextUpdate;
    import org.granite.tide.invocation.InvocationCall;
    import org.granite.tide.invocation.InvocationResult;
    
    
    public class MockSeamAsyncToken extends AsyncToken {
        
        private var _operation:String = null;
        private var _args:Array = null;
        private var _timer:Timer = new Timer(50, 1);
        private var _responders:Array = new Array();
        
        
        function MockSeamAsyncToken(message:IMessage) {
            super(message);
            _timer.addEventListener(TimerEvent.TIMER_COMPLETE, timerHandler);
        }
        
        public function send(operation:String, args:Array):void {
            _responders = new Array();
            _operation = operation;
            _args = args;
            _timer.reset();
            _timer.start();
        }
        
        public override function addResponder(responder:IResponder):void {
            _responders.push(responder);
        }
        
        protected function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
            return null;
        }
        
        protected function buildInitializerResponse(call:InvocationCall, entity:Object, propertyName:String):AbstractEvent {
            return null;
        }
		
		protected function buildValidationResponse(call:InvocationCall, entity:Object, propertyName:String, value:Object):AbstractEvent {
			return null;
		}
        
        
        private function timerHandler(event:TimerEvent):void {
            var re:AbstractEvent = null;
             
            if (_operation == "invokeComponent") { 
                var componentName:String = _args[0];
                var op:String = _args[2];
                var params:Array = _args[3];
                re = buildResponse(_args[4] as InvocationCall, componentName, op, params);
            }
            else if (_operation == "initializeObject") {
                var entity:Object = _args[0];
                var propertyName:String = _args[1];
                re = buildInitializerResponse(_args[2] as InvocationCall, entity, propertyName);
            }
			else if (_operation == "validateObject") {
				var ventity:Object = _args[0];
				var vpropertyName:String = _args[1];
				var value:Object = _args[2];
				re = buildValidationResponse(_args[3] as InvocationCall, ventity, vpropertyName, value);
			}
            
            var resp:IResponder = null;
            if (re is FaultEvent) {
                for each (resp in _responders)
                    resp.fault(re as FaultEvent);
            }
            else if (re is ResultEvent) {
                for each (resp in _responders)
                    resp.result(re as ResultEvent);
            }
        }
        
        protected function buildFault(faultCode:String, extendedData:Object = null):FaultEvent {
            var emsg:ErrorMessage = new ErrorMessage();			
            emsg.faultCode = faultCode;
			emsg.extendedData = extendedData;
            return new FaultEvent(FaultEvent.FAULT, false, true, null, this, emsg);
        }
        
        protected function buildResult(result:Object = null, results:Array = null, messages:ArrayCollection = null, keyedMessages:Object = null):ResultEvent {
            var msg:AcknowledgeMessage = new AcknowledgeMessage();
            var res:InvocationResult = new InvocationResult();
            res.result = result;
            res.scope = 3;
            res.results = new ArrayCollection();
            if (results) {
                for each (var rs:Array in results) {
                    var r:String = rs[0] as String;
                    var v:Object = rs[1];
                    var idx:int = r.indexOf(".");
                    var u:ContextUpdate = null;
                    if (idx > 0)
                        u = new ContextUpdate(r.substring(0, idx), r.substring(idx+1), v);
                    else
                        u = new ContextUpdate(r, null, v);
                    if (rs.length > 2 && rs[2] == true)
                        u.scope = 2;
                    res.results.addItem(u);
                }
            }
            res.events = new ArrayCollection();
            res.messages = messages != null ? messages : new ArrayCollection();
			res.keyedMessages = keyedMessages;
            return new ResultEvent(ResultEvent.RESULT, false, false, res, this, msg);
        }
    }
}