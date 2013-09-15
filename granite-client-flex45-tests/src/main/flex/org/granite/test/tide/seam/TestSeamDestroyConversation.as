package org.granite.test.tide.seam
{
    import mx.rpc.Fault;
    
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.test.tide.*;
    import org.granite.tide.InvalidContextError;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamDestroyConversation
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
        public function testSeamDestroyConversation():void {
            _ctx.userHome.selectUser(Async.asyncHandler(this, selectResult, 1000));
        }
        
        private function selectResult(event:TideResultEvent, pass:Object = null):void {
            event.context.user = event.context.user;
            event.context.userHome.updateUser(Async.asyncHandler(this, updateResult, 1000));
        }
        
        private function updateResult(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals("test", event.context.user.name);
            
            var finished:Boolean = false; 
            try { 
                event.context.userHome.updateUser();
            }
            catch (e:InvalidContextError) {
                finished = true;
            }
            
            Assert.assertTrue(finished);
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
import org.granite.tide.Tide;
import org.granite.tide.seam.Seam;
import org.granite.test.tide.seam.MockSeam;


class MockConversationAsyncToken extends MockSeamAsyncToken {
    
    function MockConversationAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        var user:User = new User();
        var re:ResultEvent;
        if (componentName == "userHome" && op == "selectUser") {
            user.username = "toto";
            re = buildResult(null, [[ "user", user, true ]]);
            re.result.scope = 2;
            re.message.headers[Tide.IS_LONG_RUNNING_CONVERSATION_TAG] = true;
            re.message.headers[Tide.CONVERSATION_TAG] = "23";
            return re;
        }
        else if (componentName == "userHome" && op == "updateUser") {
            var u:User = call.updates.getItemAt(0).value;
            user.username = u.username;
            user.name = "test";
            re = buildResult(null, [[ "user", user, true ]]);
            re.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] = true;
            re.result.scope = 2;
            return re;
        }
        
        return buildFault("Server.Error");
    }
}
