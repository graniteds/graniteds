package org.granite.test.tide.seam
{
    import flash.events.Event;
    
    import mx.collections.ArrayCollection;
    import mx.collections.errors.ItemPendingError;
    import mx.core.Application;
    import mx.core.FlexGlobals;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.rpc.Fault;
    import mx.rpc.IResponder;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    import org.granite.test.tide.Person;
    
    
    public class TestSeamMergeEntityLazy
    {
        private var _ctx:Context;
        
        
        private var _person:Person;
        
        [Before]
        public function setUp():void {
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            _ctx.application = FlexGlobals.topLevelApplication;
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
        }
                
        [Test(async)]
        public function testMergeEntityLazy():void {
            _ctx.personHome.find("test", Async.asyncHandler(this, findResult, 1000));
        }
        
		private var res:Function = null;
		
        private function findResult(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals("test", event.result.lastName);
			Assert.assertTrue(event.result.contacts is PersistentCollection);
			Assert.assertFalse(PersistentCollection(event.result.contacts).isInitialized());
            
            var coll:PersistentCollection = event.result.contacts as PersistentCollection;
			res = Async.asyncHandler(this, respondResult, 1000, coll);
			coll.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangeHandler, false, 0, true);
			coll.getItemAt(0);
		}
		
		private function collectionChangeHandler(event:CollectionEvent, pass:Object = null):void {
			if (event.kind == CollectionEventKind.REFRESH)
				res(event);
		}
        
        private function respondResult(event:CollectionEvent, pass:Object = null):void {
			var coll:PersistentCollection = PersistentCollection(event.target);
			Assert.assertTrue(coll.isInitialized());
			Assert.assertEquals(1, coll.length);
			Assert.assertEquals("toto@toto.net", coll.getItemAt(0).email);
        }
        
        private function respondFault(event:Event):void {
			Assert.fail("No response from initializer");
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
import org.granite.test.tide.Person;
import org.granite.test.tide.Contact;
import org.granite.persistence.PersistentSet;


class MockSimpleCallAsyncToken extends MockSeamAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "personHome" && op == "find") {
            var person:Person = new Person();
            person.id = 12;
            person.lastName = params[0] as String;
            person.contacts = new PersistentSet(false);
            return buildResult(person);
        }
        
        return buildFault("Server.Error");
    }
    
    protected override function buildInitializerResponse(call:InvocationCall, entity:Object, propertyName:String):AbstractEvent {
        if (entity is Person && propertyName == "contacts") {
            var person:Person = new Person();
            person.id = entity.id;
            person.uid = entity.uid;
            person.lastName = entity.lastName;
            person.contacts = new PersistentSet();
            var contact:Contact = new Contact();
            contact.id = 14;
            contact.email = "toto@toto.net";
            contact.person = person;
            person.contacts.addItem(contact);
            return buildResult(person);
        }
        
        return buildFault("Server.LazyInitialization.Error");
    }
}



class SimpleResponder implements IResponder {

	private var _resultHandler:Function;
	private var _faultHandler:Function;
    
	public function SimpleResponder(result:Function, fault:Function) {
		super();

		_resultHandler = result;
		_faultHandler = fault;
	}
	
	public function result(data:Object):void {
		_resultHandler(data);
	}
	
	public function fault(info:Object):void {
		_faultHandler(info);
	}
}