/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.IEventDispatcher;
    
    import mx.core.Application;
    import mx.rpc.Fault;
    
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.test.tide.*;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamClientConversation implements IEventDispatcher
    {
        private var _ctx:Context;
        private var dispatcher:EventDispatcher;
        
        
        private var _name:String;
        
		[Before]
        public function setUp():void {
            dispatcher = new EventDispatcher(this);
            
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockConversationAsyncToken();
            
            MockSeam.getInstance().addComponents([MyComponentConversation]);
        }
                
        [Test(async)]
        public function testSeamClientConversation():void {
        	_ctx.test = this;
        	
        	dispatchEvent(new TideUIConversationEvent(null, "start"));
        }
        
        private var _convId:Array = new Array();
        
        public function startResult(event:TideResultEvent, pass:Object = null):void {
        	_convId.push(event.context.contextId);
        	if (_convId.length < 2) {
        		dispatchEvent(new TideUIConversationEvent(null, "start"));
        		return;
        	}
        	
            Assert.assertEquals("23", _convId[0]);
			Assert.assertEquals("24", _convId[1]);
            
            _ctx.application.callLater(checkEnd);
       	}
       	
       	private function checkEnd():void {
            dispatchEvent(new TideUIConversationEvent("23", "end"));
			Assert.assertTrue("Conversation 23 ended", MockSeam.getInstance().getSeamContext("23").myComponent.ended);
			Assert.assertFalse("Conversation 24 not ended", MockSeam.getInstance().getSeamContext("24").myComponent.ended);
        }
        
        public function dispatchEvent(event:Event):Boolean {
        	return dispatcher.dispatchEvent(event);
        }
        
        public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
        	dispatcher.addEventListener(type, listener, useCapture, priority, useWeakReference);
        }
        
        public function hasEventListener(type:String):Boolean {
        	return dispatcher.hasEventListener(type);
        }
        
        public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
        	dispatcher.removeEventListener(type, listener, useCapture);
        }
        
        public function willTrigger(type:String):Boolean {
        	return dispatcher.willTrigger(type);
        }
    }
}


import flash.utils.Timer;
import flash.events.TimerEvent;
import mx.rpc.AsyncToken;
import mx.rpc.IResponder;
import mx.messaging.messages.IMessage;
import mx.messaging.messages.ErrorMessage;
import mx.rpc.Fault;
import mx.rpc.events.FaultEvent;
import mx.collections.ArrayCollection;
import mx.rpc.events.AbstractEvent;
import mx.rpc.events.ResultEvent;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.invocation.ContextUpdate;
import mx.messaging.messages.AcknowledgeMessage;
import org.granite.test.tide.seam.MockSeamAsyncToken;
import org.granite.test.tide.User;


class MockConversationAsyncToken extends MockSeamAsyncToken {
    
	private static var _conversationId:int = 23; 

    function MockConversationAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "conversation" && op == "start") {
            var re:ResultEvent = buildResult(null, []);
            re.result.scope = 2;
            re.message.headers["isLongRunningConversation"] = true;
            re.message.headers["conversationId"] = new String(_conversationId++);
            return re;
        }
        
        return buildFault("Server.Error");
    }
}
