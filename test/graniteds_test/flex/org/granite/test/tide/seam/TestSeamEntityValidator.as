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
	import org.granite.tide.validators.TideEntityValidator;
	import org.granite.tide.validators.ValidatorExceptionHandler;
	
	import org.granite.test.tide.Person;
    
	
    public class TestSeamEntityValidator
    {
        private var _ctx:Context;
        
        
		private var textInput:TextInput = null;
		private var validator:TideEntityValidator = null;
		
        
		[Bindable]
		public var person:Person;
		
		
		[Before(async,ui)]
        public function setUp():void {
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
			MockSeam.getInstance().addComponents([ValidatorExceptionHandler]);
			
			person = new Person();
			person.uid = "P1";
			_ctx.person = person;
			
			textInput = new TextInput();
			textInput.id = "lastName";
			Async.proceedOnEvent(this, textInput, FlexEvent.CREATION_COMPLETE, 500);
			UIImpersonator.addChild(textInput);
			validator = new TideEntityValidator();
			validator.entity = person;
			validator.property = "lastName";
			validator.listener = textInput;
        }
		
		[After(async,ui)]
		public function tearDown():void {
			UIImpersonator.removeChild(textInput);
			textInput = null;
		}
        
        [Test(async,ui,description="EntityValidator invalid value")]
        public function testSimpleEntityInvalid():void {
			Async.proceedOnEvent(this, validator, ValidationResultEvent.INVALID, 500);
			
			_ctx.someComponent.someOperation(person);
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
import org.granite.tide.TideMessage;
import org.granite.tide.validators.InvalidValue;
import mx.messaging.messages.AcknowledgeMessage;
import org.granite.test.tide.seam.MockSeamAsyncToken;
import org.granite.test.tide.Person;


class MockSimpleCallAsyncToken extends MockSeamAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
	protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
		if (componentName == "someComponent" && op == "someOperation") {
			var person:Person = new Person();
			person.uid = params[0].uid;
			person.lastName = "test";
			var iv:InvalidValue = new InvalidValue(person, person, "Person", "lastName", "test", "Invalid value");
			return buildFault("Validation.Failed", { "invalidValues": [ iv ] });
		}
		
		return buildFault("Server.Error");
	}
}
