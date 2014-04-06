/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
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
