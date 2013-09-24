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
package org.granite.test.tide.spring
{
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ItemResponder;
	import mx.collections.errors.ItemPendingError;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.tide.BaseContext;
	import org.granite.tide.spring.PagedQuery;
	import org.granite.test.tide.Person;
    
    
    public class TestSpringRetryAfterFault
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
        }
        
        
        [Test("Retry after fault", async)]
        public function testSpringRetryAfterFault():void {
			_ctx.testComponent.testOperation("toto", testRemoteCallResult, Async.asyncHandler(this, testRemoteCallFault, 1000));
        }
		
		private var _faultEvent:Object;
		private var _retryAfterFault:Function = null;
        
        private function testRemoteCallResult(resultEvent:Object, pass:Object = null):void {
			if (_retryAfterFault == null) {
				Assert.fail("First call should not succeed");
				return;
			}
			
			_retryAfterFault(resultEvent);
		}
		
		private function testRemoteCallFault(faultEvent:Object, pass:Object = null):void {
			Assert.assertEquals("Server.Security.NotLoggedIn", faultEvent.fault.faultCode);
			_faultEvent = faultEvent;
			_ctx.identity.login("test", "test", Async.asyncHandler(this, loginResult, 1000));
		}
        
		private function loginResult(resultEvent:Object, pass:Object = null):void {
			Assert.assertTrue("Should be logged in", _ctx.identity.loggedIn);
			_retryAfterFault = Async.asyncHandler(this, retryAfterFault, 1000);
			_faultEvent.retry();
		}
		
		private function retryAfterFault(resultEvent:Object, pass:Object = null):void {
			Assert.assertEquals("Call result", "ok", resultEvent.result);
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
import org.granite.tide.spring.Spring;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.invocation.ContextUpdate;
import mx.messaging.messages.AcknowledgeMessage;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.test.tide.Person;


class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "testComponent" && op == "testOperation") {
			if (Spring.getInstance().getSpringContext().identity.loggedIn)
				return buildResult("ok");
			
			return buildFault("Server.Security.NotLoggedIn");
        }
		else if (componentName == "identity" && op == "login") {
			if (params[0] == "test" && params[1] == "test")
				return buildResult(true);
			else
				return buildFault("Server.Security.InvalidCredentials");
		}
        
        return buildFault("Server.Error");
    }
}
