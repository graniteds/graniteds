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
    
    
    public class TestSeamMergeEntityLazyRef extends TestCase
    {
        public function TestSeamMergeEntityLazyRef() {
            super("testMergeEntityLazyRef");
        }
        
        private var _ctx:Context;
        
        
        private var _person:Person;
        
        public override function setUp():void {
            super.setUp();
            
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            _ctx.application = Application.application;
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken(12, "Z48TX", "test");
        }
        
        
        public function testMergeEntityLazyRef():void {
            _ctx.personHome.find("test", addAsync(findResult, 1000));
        }
        
        private function findResult(event:TideResultEvent):void {
            event.context.personHome.instance = event.result as Person;
            
            var coll:PersistentCollection = event.context.personHome.instance.contacts as PersistentCollection;
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
            assertEquals(1, coll.length);
            assertEquals("toto@toto.net", coll.getItemAt(0).email);
        }
        
        private function respondFault(event:Event):void {
            fail("No response from initializer");
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
    
    private var _id:Number;
    private var _uid:String;
    private var _name:String;
    
    function MockSimpleCallAsyncToken(id:Number, uid:String, name:String) {
        super(null);
        _id = id;
        _uid = uid;
        _name = name;
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "personHome" && op == "find") {
            var person:Person = new Person();
            person.id = _id;
            person.uid = _uid;
            person.lastName = _name;
            person.contacts = new PersistentSet(false);
            var re:ResultEvent = buildResult(person);
            re.result.scope = 2;
            re.message.headers["isLongRunningConversation"] = true;
            re.message.headers["conversationId"] = "23";
            return re;
        }
        
        return buildFault("Server.Error");
    }
    
    protected override function buildInitializerResponse(call:InvocationCall, entity:Object, propertyName:String):AbstractEvent {
        if (entity == "personHome.instance" && propertyName == "contacts") {
            var person:Person = new Person();
            person.id = _id;
            person.uid = _uid;
            person.lastName = _name;
            person.contacts = new PersistentSet();
            var contact:Contact = new Contact();
            contact.id = 14;
            contact.email = "toto@toto.net";
            contact.person = person;
            person.contacts.addItem(contact);
            var re:ResultEvent = buildResult(person);
            re.result.scope = 2;
            re.message.headers["isLongRunningConversation"] = true;
            re.message.headers["conversationId"] = "23";
            return re;
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