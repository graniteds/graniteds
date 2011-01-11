package org.granite.tide.test
{
    import flexunit.framework.TestCase;
    
    import mx.rpc.Fault;
    
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamContextUpdatedAfterFault extends TestCase
    {
        public function TestSeamContextUpdatedAfterFault() {
            super("testContextUpdatedAfterFault");
        }
        
        private var context:Context;
        
        
        public override function setUp():void {
            super.setUp();
            MockSeam.reset();
            context = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockUpdateAsyncToken();
        }
        
        
        public function testContextUpdatedAfterFault():void {
            context.user.login = "toto";
            context.user.password = "toto";
            context.register.register(registerResult1, addAsync(registerFault1, 1000));
        }
        
        private function registerResult1(event:TideResultEvent):void {
        }
        
        private function registerFault1(event:TideFaultEvent):void {
            context.user.login = "tutu";
            context.user.password = "tutu";
            context.register.register(addAsync(registerResult2, 1000), registerFault2);
        }
        
        private function registerResult2(event:TideResultEvent):void {
        }
        
        private function registerFault2(event:TideFaultEvent):void {
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
import org.granite.tide.test.MockSeamAsyncToken;


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
