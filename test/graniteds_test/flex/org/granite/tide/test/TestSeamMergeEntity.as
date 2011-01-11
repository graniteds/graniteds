package org.granite.tide.test
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.rpc.Fault;
    import mx.utils.StringUtil;
    
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamMergeEntity extends TestCase
    {
        public function TestSeamMergeEntity() {
            super("testMergeEntity");
        }
        
        private var _ctx:Context;
        
        
        private var _user:User;
        
        public override function setUp():void {
            super.setUp();
            
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
        }
        
        
        public function testMergeEntity():void {
            _user = new User();
            _user.username = "test";
            _ctx.userHome.update(_user, addAsync(updateResult, 1000));
        }
        
        private function updateResult(event:TideResultEvent):void {
            assertEquals("toto", _user.name);
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


class MockSimpleCallAsyncToken extends MockSeamAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "userHome" && op == "update") {
            var user:User = new User();
            user.username = params[0].username;
            user.name = "toto";
            return buildResult(user);
        }
        
        return buildFault("Server.Error");
    }
}
