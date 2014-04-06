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
    import org.granite.tide.Component;
    import org.granite.tide.Tide;
    import org.granite.tide.cdi.Context;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    
    
    public class TestCdiRemoting
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

            MockCdi.getInstance().addComponents([HelloComponent, HelloBean, HelloObserver])
        }

        [Test(async)]
        public function testUntypedCall():void {
			var helloComponent:Component = _ctx.helloComponent as Component;
			helloComponent.hello(_name, Async.asyncHandler(this, helloResult, 1000));
        }
        
        private function helloResult(event:TideResultEvent, pass:Object = null):void {
            Assert.assertEquals(_name, event.result);
        }

        [Test(async)]
        public function testTypedCall():void {
            var helloComponent:HelloComponent = _ctx.byType(HelloComponent) as HelloComponent;
            helloComponent.hello(_name, Async.asyncHandler(this, hello2Result, 1000));
        }

        private function hello2Result(event:TideResultEvent, pass:Object = null):void {
            Assert.assertEquals(_name, event.result);
        }

        [Test(async)]
        public function testInjectedCall():void {
            var helloBean:HelloBean = _ctx.byType(HelloBean) as HelloBean;
            helloBean.name = _name;
            helloBean.hello(Async.asyncHandler(this, hello3Result, 1000));
        }

        private function hello3Result(event:TideResultEvent, pass:Object = null):void {
            var helloBean:HelloBean = event.context.byType(HelloBean) as HelloBean;
            Assert.assertEquals(_name, helloBean.message);
        }

        [Test(async)]
        public function testEventCall():void {
            var helloBean:HelloBean = _ctx.byType(HelloBean) as HelloBean;
            helloBean.name = _name;
            helloBean.helloEvent(Async.asyncHandler(this, hello4Result, 1000));
        }

        private function hello4Result(event:TideResultEvent, pass:Object = null):void {
            var helloObserver:HelloObserver = event.context.byType(HelloObserver) as HelloObserver;
            Assert.assertEquals(_name, helloObserver.message);
        }
    }
}


import mx.rpc.events.AbstractEvent;

import org.granite.reflect.Type;
import org.granite.test.tide.cdi.HelloBean;

import org.granite.test.tide.cdi.HelloComponent;
import org.granite.test.tide.cdi.HelloEvent;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.test.tide.cdi.MockCdiAsyncToken;


class MockSimpleCallAsyncToken extends MockCdiAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, componentClassName:String, op:String, params:Array):AbstractEvent {
        var name:String = null;
        for each (var u:ContextUpdate in call.updates) {
            if (u.componentClassName == Type.forClass(HelloBean).alias && u.expression == "name")
                name = u.value as String;
        }

        if (componentName == "helloComponent" && op == "hello")
            return buildResult(params[0]);
        else if (componentClassName == Type.forClass(HelloComponent).alias && op == "hello")
            return buildResult(params[0]);
        else if (componentClassName == Type.forClass(HelloBean).alias && op == "hello")
            return buildResult(null, [ [ componentName + ".message", name, null, Type.forClass(HelloBean).alias ] ]);
        else if (componentClassName == Type.forClass(HelloBean).alias && op == "helloEvent")
            return buildResult(null, null, [ new HelloEvent(name) ]);

        return buildFault("Server.Error");
    }
}
