package org.granite.tide.test.framework
{
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import flexunit.framework.TestCase;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ItemResponder;
	import mx.collections.errors.ItemPendingError;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.spring.PagedQuery;
	import org.granite.tide.test.MockSpring;
	import org.granite.tide.test.Person;
    
    
    public class TestSpringRetryAfterFault extends TestCase
    {
        public function TestSpringRetryAfterFault() {
            super("testSpringRetryAfterFault");
        }
        
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
        }
        
        
        public function testSpringRetryAfterFault():void {
			_ctx.testComponent.testOperation("toto", testRemoteCallResult, addAsync(testRemoteCallFault, 1000));
        }
		
		private var _faultEvent:Object;
		private var _retryAfterFault:Function = null;
        
        private function testRemoteCallResult(resultEvent:Object):void {
			if (_retryAfterFault == null) {
				fail("First call should not succeed");
				return;
			}
			
			_retryAfterFault(resultEvent);
		}
		
		private function testRemoteCallFault(faultEvent:Object):void {
			assertEquals("Server.Security.NotLoggedIn", faultEvent.fault.faultCode);
			_faultEvent = faultEvent;
			_ctx.identity.login("test", "test", addAsync(loginResult, 1000));
		}
        
		private function loginResult(resultEvent:Object):void {
			assertTrue("Should be logged in", _ctx.identity.loggedIn);
			_retryAfterFault = addAsync(retryAfterFault, 1000);
			_faultEvent.retry();
		}
		
		private function retryAfterFault(resultEvent:Object):void {
			assertEquals("Call result", "ok", resultEvent.result);
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
import org.granite.tide.test.MockSpringAsyncToken;
import org.granite.tide.test.Person;


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
