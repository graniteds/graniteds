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
package org.granite.test.tide.seam
{
    import mx.rpc.Fault;
    
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.test.tide.*;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamContextCleanAfterFault
    {
        private var context:Context;
        
        
		[Before]
        public function setUp():void {
            MockSeam.reset();
            context = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockFaultAsyncToken();
        }
                
        [Test(async)]
        public function testContextCleanAfterFault():void {
            context.search.searchString = "test";
            context.search.search(searchResult, Async.asyncHandler(this, searchFault, 1000));
        }
        
        private function searchResult(event:TideResultEvent, pass:Object = null):void {
        }
        
        private function searchFault(event:TideFaultEvent, pass:Object = null):void {
            context.user.login = "toto";
            context.user.password = "toto";
            context.register.register(Async.asyncHandler(this, registerResult, 1000), registerFault);
        }
        
        private function registerResult(event:TideResultEvent, pass:Object = null):void {
        }
        
        private function registerFault(event:TideFaultEvent, pass:Object = null):void {
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
