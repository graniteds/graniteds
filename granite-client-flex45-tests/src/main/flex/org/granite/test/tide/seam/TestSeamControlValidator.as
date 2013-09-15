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
	import org.granite.tide.seam.validators.TideControlValidator;
	
	import org.granite.test.tide.Person;
    
	
    public class TestSeamControlValidator
    {
        private var _ctx:Context;
        
        
		private var textInput:TextInput = null;
		private var validator:TideControlValidator = null;
		
        
		[Before(async,ui)]
        public function setUp():void {
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
			
			textInput = new TextInput();
			textInput.id = "lastName";
			Async.proceedOnEvent(this, textInput, FlexEvent.CREATION_COMPLETE, 500);
			UIImpersonator.addChild(textInput);
			validator = new TideControlValidator();
			validator.source = textInput;
			validator.property = "text";
        }
		
		[After(async,ui)]
		public function tearDown():void {
			UIImpersonator.removeChild(textInput);
			textInput = null;
		}
        
        [Test(async,ui,description="ControlValidator invalid value")]
        public function testSimpleControlInvalid():void {
			Async.proceedOnEvent(this, validator, ValidationResultEvent.INVALID, 500);
			
			_ctx.someComponent.someOperation();
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
			var msg:TideMessage = new TideMessage(TideMessage.ERROR, "Bla bla");
			var msgs:ArrayCollection = new ArrayCollection([ msg ]);
			return buildResult(null, null, null, { "lastName" : msgs });
		}
		
		return buildFault("Server.Error");
	}
}
