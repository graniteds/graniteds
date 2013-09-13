package org.granite.test.tide.spring
{
	import mx.collections.ArrayCollection;
	import mx.collections.ItemResponder;
	import mx.collections.errors.ItemPendingError;
	import mx.core.UIComponent;
	import mx.core.FlexGlobals;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.tide.BaseContext;
	import org.granite.tide.spring.PagedQuery;
	import org.granite.test.tide.Person;
    
    
    public class TestSpringExceptionHandler
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
			
			MockSpring.getInstance().addExceptionHandler(ExtendedExceptionHandler);
			MockSpring.getInstance().addExceptionHandler(StandardExceptionHandler);
			MockSpring.getInstance().addExceptionHandler(DefaultExceptionHandler);
        }
        
        
		private var _afterFault:Function;
		
        [Test("GDS-827 DefaultExceptionHandler", async)]
        public function testDefaultExceptionHandlerGDS827():void {
			_afterFault = Async.asyncHandler(this, testRemoteCallFault, 1000);
			_ctx.addEventListener("handled.default", _afterFault, false, 0, true);
			_ctx.testComponent.testOperation("toto");
        }
        
        private function testRemoteCallFault(faultEvent:Object, pass:Object = null):void {
			_ctx.removeEventListener("handled.default", _afterFault);
		}
		
		[Test("Standard ExceptionHandler", async)]
		public function testStandardExceptionHandler():void {
			_afterFault = Async.asyncHandler(this, testRemoteCallFault3, 1000);
			_ctx.addEventListener("handled.standard", _afterFault, false, 0, true);
			_ctx.testComponent.testOperation2("titi");
		}
		
		private function testRemoteCallFault3(faultEvent:Object, pass:Object = null):void {
			_ctx.removeEventListener("handled.standard", _afterFault);
		}		
		
		[Test("GDS-829 ExtendedExceptionHandler", async)]
		public function testExtendedExceptionHandlerGDS829():void {
			_afterFault = Async.asyncHandler(this, testRemoteCallFault2, 1000);			
			_ctx.addEventListener("handled.extended", _afterFault, false, 0, true);
			_ctx.testComponent.testOperation3("tutu");
		}
		
		private function testRemoteCallFault2(faultEvent:Object, pass:Object = null):void {
			_ctx.removeEventListener("handled.extended", _afterFault);
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
		if (op == "testOperation2")
			return buildFault("Server.Standard.Error");
		else if (op == "testOperation3")
			return buildFault("Server.Extended.Error");
		return buildFault("Server.Error");
    }
}
