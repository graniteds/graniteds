package org.granite.tide.test
{
    import flexunit.framework.TestCase;
    
    import mx.rpc.Fault;
    import mx.utils.StringUtil;
    
    import org.granite.tide.Tide;
	import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamInjectedEntityCall extends TestCase
    {
        public function TestSeamInjectedEntityCall() {
            super("testInjectedEntityCall");
        }
        
        private var _ctx:Context;
        
        
        private var _name:String;
        
        public override function setUp():void {
            super.setUp();
            _name = "";
            for (var i:int = 0; i < 10; i++)
                _name += String.fromCharCode(32+96*Math.random());
                
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
        }        
        
        public function testInjectedEntityCall():void {
        	MockSeam.getInstance().setComponentRemoteSync("helloTo", Tide.SYNC_BIDIRECTIONAL);
            var user:User = new User();
            user.name = _name;
            _ctx.helloTo = user;
            _ctx.helloWorld.hello(addAsync(helloResult, 1000));
        }
        
        private function helloResult(event:TideResultEvent):void {
            assertEquals("Remote value", _name, event.result);
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
        var user:User = null;
        for each (var upd:ContextUpdate in call.updates) {
            if (upd.path == "helloTo") {
                user = upd.value as User;
                break;
            }
        }
        
        if (componentName == "helloWorld" && op == "hello")
            return buildResult(user.name);
        
        return buildFault("Server.Error");
    }
}
