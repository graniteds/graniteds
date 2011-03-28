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
	import org.granite.tide.events.TideResultEvent;
	import org.granite.tide.events.TideFaultEvent;
	import org.granite.tide.spring.PagedQuery;
	import org.granite.test.tide.Person;
    
    
    public class TestSpringFaultExtendedData
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
        }
        
        
        [Test("Fault extended data", async)]
        public function testSpringFaultExtendedData():void {
			_ctx.testComponent.testOperation("toto", testRemoteCallResult, Async.asyncHandler(this, testRemoteCallFault, 1000));
        }
		
        private function testRemoteCallResult(resultEvent:TideResultEvent, pass:Object = null):void {
			Assert.fail("Call should not succeed");
		}
		
		private function testRemoteCallFault(faultEvent:TideFaultEvent, pass:Object = null):void {
			Assert.assertEquals("Validation.Failed", faultEvent.fault.faultCode);
			Assert.assertEquals("Extended data", "test", faultEvent.extendedData.exd);
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
			return buildFault("Validation.Failed", "", { exd: "test" });
		}
        
        return buildFault("Server.Error");
    }
}
