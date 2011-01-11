package org.granite.tide.test
{
    import flexunit.framework.TestCase;
    
    import mx.rpc.Fault;
    
    import org.granite.tide.seam.Context;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.events.TideFaultEvent;
    
    
    public class TestSeamContextCleanAfterFault2 extends TestCase
    {
        public function TestSeamContextCleanAfterFault2() {
            super("testContextCleanAfterFault2");
        }
        
        private var context:Context;
        
        
        public override function setUp():void {
            super.setUp();
            MockSeam.reset();
            context = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockFaultAsyncToken();
        }
        
        
        public function testContextCleanAfterFault2():void {
            context.search.searchString = "test";
            context.search.search(searchResult, addAsync(searchFault, 1000));
        }
        
        private function searchResult(event:TideResultEvent):void {
        }
        
        private function searchFault(event:TideFaultEvent):void {
            context.identity.username = "toto";
            context.identity.password = "toto";
            context.identity.login(addAsync(loginResult, 1000), loginFault);
        }
        
        private function loginResult(event:TideResultEvent):void {
        }
        
        private function loginFault(event:TideFaultEvent):void {
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
import org.granite.tide.test.MockSeamAsyncToken;


class MockFaultAsyncToken extends MockSeamAsyncToken {
    
    function MockFaultAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "search")
            return buildFault("Server.Security.NotLoggedIn");
        
        for each (var update:ContextUpdate in call.updates) {
            if (update.componentName == "search")
                return buildFault("Server.Security.NotLoggedIn");
        }
        
        return buildResult();
    }
}
