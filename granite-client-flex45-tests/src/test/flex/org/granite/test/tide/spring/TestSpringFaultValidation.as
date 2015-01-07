/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.spring
{
	import flash.events.Event;
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ItemResponder;
	import mx.collections.errors.ItemPendingError;
	import mx.core.FlexGlobals;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.gravity.TopicMessageAgent;
	import org.granite.test.tide.Person;
	import org.granite.tide.BaseContext;
	import org.granite.tide.events.TideFaultEvent;
	import org.granite.tide.events.TideResultEvent;
	import org.granite.tide.events.TideValidatorEvent;
	import org.granite.tide.spring.PagedQuery;
	import org.granite.tide.validators.ValidatorExceptionHandler;
    
    
    public class TestSpringFaultValidation
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
			
			MockSpring.getInstance().addExceptionHandler(ValidatorExceptionHandler);
        }
        
        
		private var _invalid:Boolean = false;
		
        [Test("Fault validation", async)]
        public function testSpringFaultValidation():void {
			_ctx.addEventListener(TideValidatorEvent.INVALID, validationHandler, false, 0, true);
			
			_ctx.testComponent.testOperation("toto", testRemoteCallResult, Async.asyncHandler(this, testRemoteCallFault, 1000));
        }
		
		private function validationHandler(event:TideValidatorEvent):void {
			_invalid = true;
		}
		
        private function testRemoteCallResult(resultEvent:TideResultEvent, pass:Object = null):void {
			Assert.fail("Call should not succeed");
		}
		
		private function testRemoteCallFault(faultEvent:TideFaultEvent, pass:Object = null):void {
			Assert.assertEquals("Validation.Failed", faultEvent.fault.faultCode);
			Assert.assertTrue("Extended data", faultEvent.extendedData.invalidValues is Array);
			
			FlexGlobals.topLevelApplication.callLater(Async.asyncHandler(this, testValidationEvent, 1000), []);
		}
		
		private function testValidationEvent(event:Event, pass:Object = null):void {
			Assert.assertTrue("Validation event dispatched", _invalid);
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
import org.granite.tide.spring.Spring;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.invocation.ContextUpdate;
import mx.messaging.messages.AcknowledgeMessage;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.test.tide.Person;


class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "testComponent" && op == "testOperation") {
			return buildFault("Validation.Failed", "", { invalidValues: [] });
		}
        
        return buildFault("Server.Error");
    }
}
