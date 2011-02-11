package org.granite.test.tide.cdi
{
    import mx.rpc.Fault;
    import mx.utils.StringUtil;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.tide.Tide;
    import org.granite.tide.cdi.Context;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    
    
    public class TestCdiInjectedCall
    {
        private var _ctx:Context;
        
        
        private var _name:String;
        
		[Before]
        public function setUp():void {
            _name = "";
            for (var i:int = 0; i < 10; i++)
                _name += String.fromCharCode(32+96*Math.random());
                
            MockCdi.reset();
            _ctx = MockCdi.getInstance().getCdiContext();
            MockCdi.getInstance().token = new MockSimpleCallAsyncToken();
			
			MockCdi.getInstance().addComponents([HelloComponent]);
        }
        
		[Ignore("TODO Improve CDI support")]
        [Test(async)]
        public function testInjectedCall():void {
			var helloComponent:HelloComponent = _ctx.byType(HelloComponent) as HelloComponent;
			helloComponent.name = _name;
			helloComponent.hello(Async.asyncHandler(this, helloResult, 1000));
        }
        
        private function helloResult(event:TideResultEvent, pass:Object = null):void {
            Assert.assertEquals(_name, event.result);
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
import org.granite.test.tide.cdi.MockCdiAsyncToken;


class MockSimpleCallAsyncToken extends MockCdiAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, componentClassName:String, op:String, params:Array):AbstractEvent {
        var name:String = null;
//        for each (var upd:ContextUpdate in call.updates) {
//            if (upd.componentClassName == "org.granite.tide.test.cdi.HelloComponent" && upd.expression == "name") {
//                name = upd.value as String;
//                break;
//            }
//        }
        
        if (componentClassName == "org.granite.tide.test.cdi.HelloComponent" && op == "hello")
            return buildResult(name);
        
        return buildFault("Server.Error");
    }
}
