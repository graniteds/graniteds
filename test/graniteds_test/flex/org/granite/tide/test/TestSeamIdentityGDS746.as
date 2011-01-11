package org.granite.tide.test
{
    import flexunit.framework.TestCase;
    
    import mx.rpc.Fault;
    
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamIdentityGDS746 extends TestCase
    {
        public function TestSeamIdentityGDS746() {
            super("testSeamIdentityGDS746");
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
        
        
        public function testSeamIdentityGDS746():void {
        	_ctx.identity.loggedIn = true;
        	var async:Function = addAsync(hasRoleResult, 1000);
            _ctx.identity.hasRole('admin', function(event:TideResultEvent, roleName:String):void {
        		async(event);
        	});
            _ctx.identity.hasRole('admin', hasRoleResult);
        }
        
        private function hasRoleResult(event:TideResultEvent, roleName:String = null):void {
            assertEquals("hasRole", true, event.result);
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


class MockSimpleCallAsyncToken extends MockSeamAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "identity" && op == "hasRole")
            return buildResult(true);
        
        return buildFault("Server.Error");
    }
}
