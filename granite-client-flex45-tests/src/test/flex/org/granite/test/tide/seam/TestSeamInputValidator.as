/*
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
	import flash.events.FocusEvent;
    import mx.rpc.Fault;
    import mx.utils.StringUtil;
	import mx.binding.utils.BindingUtils;
	import mx.events.FlexEvent;
	import mx.controls.TextInput;
	import mx.events.ValidationResultEvent;
	
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
	import org.fluint.uiImpersonation.UIImpersonator;
	import org.fluint.sequence.SequenceRunner;
	import org.fluint.sequence.SequenceEventDispatcher;
	
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
	import org.granite.tide.validators.TideInputValidator;
	
	import org.granite.test.tide.Person;
    
	
    public class TestSeamInputValidator
    {
        private var _ctx:Context;
        
        
		private var textInput:TextInput = null;
		private var validator:TideInputValidator = null;
		
		[Bindable]
		public var person:Person;
		
        
		[Before(async,ui)]
        public function setUp():void {
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
			
			person = new Person();
			person.uid = "P1";
			_ctx.person = person;
			
			textInput = new TextInput();
			Async.proceedOnEvent(this, textInput, FlexEvent.CREATION_COMPLETE, 500);
			UIImpersonator.addChild(textInput);
			validator = new TideInputValidator();
			validator.source = textInput;
			validator.property = "text";
			validator.entity = person;
			validator.entityProperty = "lastName";
        }
		
		[After(async,ui)]
		public function tearDown():void {
			UIImpersonator.removeChild(textInput);
			textInput = null;
		}
        
        [Test(async,ui,description="InputValidator invalid value")]
        public function testSimpleInputInvalid():void {
			textInput.text = "test";
			
			Async.proceedOnEvent(this, validator, ValidationResultEvent.INVALID, 500);
			
			var seqRunner:SequenceRunner = new SequenceRunner(this);
			seqRunner.addStep(new SequenceEventDispatcher(textInput, new FocusEvent(FocusEvent.FOCUS_OUT)));
			seqRunner.run();
        }
		
		[Test(async,ui,description="InputValidator valid value")]
		public function testSimpleInputValid():void {
			textInput.text = "az";
			
			Async.proceedOnEvent(this, validator, ValidationResultEvent.VALID, 500);
			
			var seqRunner:SequenceRunner = new SequenceRunner(this);
			seqRunner.addStep(new SequenceEventDispatcher(textInput, new FocusEvent(FocusEvent.FOCUS_OUT)));
			seqRunner.run();
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
import org.granite.tide.validators.InvalidValue;
import mx.messaging.messages.AcknowledgeMessage;
import org.granite.test.tide.seam.MockSeamAsyncToken;
import org.granite.test.tide.Person;


class MockSimpleCallAsyncToken extends MockSeamAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildValidationResponse(call:InvocationCall, entity:Object, propertyName:String, value:Object):AbstractEvent {
		if (entity is Person && propertyName == "lastName") {
	        if (value.length > 3) {
				var iv:InvalidValue = new InvalidValue(entity, entity, null, propertyName, value, "Invalid value");
	            return buildResult([iv]);
			}
			else {
				return buildResult([]);
			}
		}
        
        return buildFault("Server.Error");
    }
}
