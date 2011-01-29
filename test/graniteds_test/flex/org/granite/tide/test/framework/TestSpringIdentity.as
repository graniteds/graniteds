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
    
    
    public class TestSpringIdentity extends TestCase
    {
        public function TestSpringIdentity() {
            super("testSpringIdentity");
        }
        
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
			_ctx.identity.loggedIn = true;
        }
        
        
		private var f:Function = null;
		
        public function testSpringIdentity():void {
			f = addAsync(ifAllGrantedResult, 1000, "ROLE_ADMIN");
			_ctx.identity.ifAllGranted("ROLE_ADMIN", ifAllGrantedResult0);
        }
		
		private function ifAllGrantedResult0(resultEvent:Object, roleName:String):void {
			f(resultEvent);
		}
		
        private function ifAllGrantedResult(resultEvent:Object, roleName:String):void {
			assertTrue("Identity ifAllGranted not cached", resultEvent.result);
			
			_ctx.identity.ifAllGranted("ROLE_ADMIN", ifAllGrantedResult2);
		}
		
		private function ifAllGrantedResult2(resultEvent:Object, roleName:String):void {
			assertTrue("Identity ifAllGranted cached", resultEvent.result);
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
        if (componentName == "identity" && op == "ifAllGranted")
			return buildResult(true);
        
        return buildFault("Server.Error");
    }
}
