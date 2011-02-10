package org.granite.test.tide.seam
{
    import mx.rpc.Fault;
    import mx.utils.StringUtil;
    
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.test.tide.*;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamConversationScope2
    {
        private var _ctx:Context;
        
        
        private var _name:String;
        
		[Before]
        public function setUp():void {
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockConversationAsyncToken();
        }
        
        [Test(async)]
        public function testSeamConversationScope2():void {
            _ctx.userHome.selectUser(Async.asyncHandler(this, selectResult, 1000));
        }
        
        private function selectResult(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals(23, event.context.conversationId);
            var userHome:Object = event.context.userHome;
			Assert.assertEquals(event.context, userHome.meta_context);
			Assert.assertFalse(_ctx.userHome === userHome);
			Assert.assertTrue(MockSeam.getInstance().isComponentInConversation(userHome.meta_name));
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
