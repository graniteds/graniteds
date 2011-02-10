package org.granite.test.tide.seam
{
    import mx.rpc.Fault;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    import org.granite.test.tide.*;
    
    
    public class TestSeamContextUpdatedAfterFault
    {        
        private var context:Context;
        
        
		[Before]
        public function setUp():void {
            MockSeam.reset();
            context = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockUpdateAsyncToken();
        }
        
        [Test(async)]
        public function testContextUpdatedAfterFault():void {
            context.user.login = "toto";
            context.user.password = "toto";
            context.register.register(registerResult1, Async.asyncHandler(this, registerFault1, 1000));
        }
        
        private function registerResult1(event:TideResultEvent, pass:Object = null):void {
        }
        
        private function registerFault1(event:TideFaultEvent, pass:Object = null):void {
            context.user.login = "tutu";
            context.user.password = "tutu";
            context.register.register(Async.asyncHandler(this, registerResult2, 1000), registerFault2);
        }
        
        private function registerResult2(event:TideResultEvent, pass:Object = null):void {
        }
        
        private function registerFault2(event:TideFaultEvent, pass:Object = null):void {
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
import mx.rpc.events.AbstractEvent;
import mx.rpc.events.FaultEvent;
import mx.collections.ArrayCollection;
import mx.rpc.events.ResultEvent;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.invocation.ContextUpdate;
import mx.messaging.messages.AcknowledgeMessage;
import org.granite.test.tide.seam.MockSeamAsyncToken;


class MockUpdateAsyncToken extends MockSeamAsyncToken {
    
    function MockUpdateAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        for each (var update:ContextUpdate in call.updates) {
            if (update.componentName == "user" && update.expression == "login" && update.value == "toto")
                return buildFault("Validator.Error");
                
            if (update.componentName == "user" && update.expression == "login" && update.value == "tutu")
                return buildResult("ok");
        }
        
        return buildFault("Seam.Error");
    }
}
