/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
    import flash.events.Event;
    
    import mx.rpc.Fault;
    
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.test.tide.*;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamDestroyConversationGDS719
    {
        private var _ctx:Context;
        
        
        private var _name:String;
        
		[Before]
        public function setUp():void {
            MockSeam.reset();
            MockSeam.getInstance().initApplication();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockConversationAsyncToken();
        }
        
        [Test(async)]
        public function testSeamDestroyConversationGDS719():void {
        	var ctx:Context = MockSeam.getInstance().getSeamContext("23");
        	ctx.userHome.userId = "23";
            ctx.userHome.selectUser(Async.asyncHandler(this, selectResult, 1000));
        }
        
        private function selectResult(event:TideResultEvent, pass:Object = null):void {
            event.context.user = event.context.user;
        	event.context.userHome.someProperty;
            event.context.userHome.updateUser(Async.asyncHandler(this, updateResult, 1000));
        }
        
        private function updateResult(event:TideResultEvent, pass:Object = null):void {
            Assert.assertEquals("test", event.context.userHome.someProperty);
            
            if (event.context.userHome.userId == "23")
	            _ctx.application.callLater(select2, [ Async.asyncHandler(this, select2b, 1000) ]);
        }
        
        private function select2(func:Function, pass:Object = null):void {
        	func(null);
        }
        
        private function select2b(event:Event = null, pass:Object = null):void {
            var ctx:Context = MockSeam.getInstance().getSeamContext("24");
            ctx.userHome.userId = "24";
            ctx.userHome.selectUser(Async.asyncHandler(this, selectResult, 1000));
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
            re.message.headers[Tide.CONVERSATION_TAG] = call.updates[0].value;
            return re;
        }
        else if (componentName == "userHome" && op == "updateUser") {
            var u:User = call.updates.getItemAt(0).value;
            user.username = u.username;
            user.name = "test";
            var found:Boolean = false;
			for each (var r:Object in call.results) {
				if (r.path == "userHome.someProperty") {
					found = true;
					break;
				}
			}    
			if (found)        
            	re = buildResult(null, [[ "user", user, true ], [ "userHome.someProperty", "test", true ]]);
            else
            	re = buildResult(null, [[ "user", user, true ]]);
            re.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] = true;
            re.result.scope = 2;
            return re;
        }
        
        return buildFault("Server.Error");
    }
}
