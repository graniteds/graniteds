package org.granite.tide.test
{
    import flash.events.Event;
    
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.collections.errors.ItemPendingError;
    import mx.core.Application;
    import mx.rpc.Fault;
    import mx.rpc.IResponder;
    
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamMergeEntityLazyVersion extends TestCase
    {
        public function TestSeamMergeEntityLazyVersion() {
            super("testMergeEntityLazyVersion");
        }
        
        private var _ctx:Context;
        
        
        private var _person:Person;
        
        public override function setUp():void {
            super.setUp();
            
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            _ctx.application = Application.application;
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
        }
        
        
        public function testMergeEntityLazyVersion():void {
            _ctx.personHome.find(addAsync(findResult, 1000));
        }
        
        private function findResult(event:TideResultEvent):void {
            assertEquals(1, event.result.length);
            var p:Person = event.result.getItemAt(0);
            assertTrue(p.contacts is PersistentCollection);
            assertFalse(PersistentCollection(p.contacts).isInitialized());
            
            var coll:PersistentCollection = p.contacts as PersistentCollection;
            var init:Boolean = false;
            try {
                var tmp:* = coll.getItemAt(0);
            }
            catch (e:ItemPendingError) {
                init = true;
                e.addResponder(new SimpleResponder(addAsync(respondResult, 1000, coll), respondFault));
            }
        }
        
        private function respondResult(event:Event, coll:PersistentCollection):void {
            assertTrue(coll.isInitialized());
            
            _ctx.personHome.find(addAsync(findResult2, 1000));
        }
        
        private function respondFault(event:Event):void {
            fail("No response from initializer");
        }
        
        private function findResult2(event:TideResultEvent):void {
            assertEquals(1, event.result.length);
            var p:Person = event.result.getItemAt(0);
            assertTrue(p.contacts is PersistentCollection);
            // Should not have been uninitialized, version has not changed
            assertTrue(PersistentCollection(p.contacts).isInitialized());
            
            _ctx.personHome.find2(addAsync(findResult3, 1000));
        }
        
        private function findResult3(event:TideResultEvent):void {
            assertEquals(1, event.result.length);
            var p:Person = event.result.getItemAt(0);
            assertTrue(p.contacts is PersistentCollection);
            // Should have been uninitialized, version has changed
            assertFalse(PersistentCollection(p.contacts).isInitialized());
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
import org.granite.tide.test.MockSeamAsyncToken;
import org.granite.tide.test.Person;
import org.granite.tide.test.Contact;
import org.granite.persistence.PersistentSet;


class MockSimpleCallAsyncToken extends MockSeamAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
    	var res:ArrayCollection = new ArrayCollection();
        var person:Person = new Person();
        if (componentName == "personHome" && op == "find") {
            person.id = 12;
            person.version = 1;
            person.uid = "P12";
            person.lastName = "test";
            person.contacts = new PersistentSet(false);
            res.addItem(person);
            return buildResult(res);
        }
        else if (componentName == "personHome" && op == "find2") {
        	person.id = 12;
        	person.version = 2;
            person.uid = "P12";
        	person.lastName = "test";
            person.contacts = new PersistentSet(false);
            res.addItem(person);
            return buildResult(res);
        }
        
        return buildFault("Server.Error");
    }
    
    protected override function buildInitializerResponse(call:InvocationCall, entity:Object, propertyName:String):AbstractEvent {
        if (entity is Person && propertyName == "contacts") {
            var person:Person = new Person();
            person.id = entity.id;
            person.version = entity.version;
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