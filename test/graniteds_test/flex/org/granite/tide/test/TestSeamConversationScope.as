package org.granite.tide.test
{
    import flexunit.framework.TestCase;
    
    import mx.rpc.Fault;
    import mx.utils.StringUtil;
    
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamConversationScope extends TestCase
    {
        public function TestSeamConversationScope() {
            super("testSeamConversationScope");
        }
        
        private var _ctx:Context;
        
        
        private var _name:String;
        
        public override function setUp():void {
            super.setUp();
            
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockConversationAsyncToken();
        }
        
        
        public function testSeamConversationScope():void {
            _ctx.userHome.selectUser(addAsync(selectResult, 1000));
        }
        
        private function selectResult(event:TideResultEvent):void {
            assertEquals(23, event.context.conversationId);
            var component:Object = event.context.conversationScopedComponent;
            assertEquals(event.context, component.meta_context);
            assertTrue(MockSeam.getInstance().isComponentInEvent(component.meta_name));
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
import org.granite.tide.test.User;


class MockConversationAsyncToken extends MockSeamAsyncToken {
    
    function MockConversationAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "userHome" && op == "selectUser") {
            var user:User = new User();
            user.username = "toto";
            var re:ResultEvent = buildResult(null, [[ "user", user ]]);
            re.result.scope = 2;
            re.message.headers["isLongRunningConversation"] = true;
            re.message.headers["conversationId"] = "23";
            return re;
        }
        
        return buildFault("Server.Error");
    }
}
