package org.granite.tide.test
{
    import flash.events.Event;
    
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.collections.ItemResponder;
    import mx.collections.errors.ItemPendingError;
    import mx.rpc.Fault;
    import mx.rpc.IResponder;
    import mx.utils.StringUtil;
    
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamMergeEntityColl extends TestCase
    {
        public function TestSeamMergeEntityColl() {
            super("testMergeEntityColl");
        }
        
        private var _ctx:Context;
        
        
        private var _person:Person;
        
        public override function setUp():void {
            super.setUp();
            
            MockSeam.reset();
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
            _ctx = MockSeam.getInstance().getSeamContext();
            
            _person = new Person();
            _person.id = 12;
            _person.uid = "UID12B";
        }
        
        
        public function testMergeEntityColl():void {
            _ctx.personHome.instance = _person;
            _ctx.personHome.id = 12;
            _ctx.personHome.instance.contacts;
            _ctx.personHome.find(addAsync(findResult, 1000));
        }
        
        private function findResult(event:TideResultEvent):void {
            assertTrue(event.context.personHome.instance.contacts.isInitialized());
            
            var c:Contact = new Contact();
            c.uid = "UID2B";
            c.email = "tutu.com";
            event.context.personHome.instance.contacts.addItem(c);
            event.context.personHome.update(updateResult, addAsync(updateFault, 1000));
        }
        
        private function updateResult(event:TideResultEvent):void {
            fail("Validation not failed");
        }
        
        private function updateFault(event:TideFaultEvent):void {
            _ctx.personHome.instance.contacts.getItemAt(1).email = "tutu@tutu.com";
            _ctx.personHome.update(addAsync(updateResult2, 1000));
        }
        
        private function updateResult2(event:TideResultEvent):void {
            assertEquals(2, _ctx.personHome.instance.contacts.length);
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
        var pid:int = 0;
        var p:Person = null;
        for each (var u:ContextUpdate in call.updates) {
            if (u.path == "personHome.instance")
                p = u.value as Person;
        }
        
        var person:Person = new Person();
        person.id = 12;
        person.uid = "UID12B";
        person.lastName = "test";
        person.contacts = new PersistentSet(true);
        
        var contact:Contact;
        if (componentName == "personHome" && op == "find") {
        	person.version = 0;
            contact = new Contact();
            contact.id = 1;
            contact.uid = "UID1B";
            contact.email = "toto@toto.net";
            contact.person = person;
            person.contacts.addItem(contact);
            return buildResult("ok", [[ "personHome.instance", person ]]);
        }
        else if (componentName == "personHome" && op == "update") {
            var valid:Boolean = true;
            for each (var c:Contact in p.contacts) {
                if (c.email != null && c.email.indexOf("@") < 0) {
                    valid = false;
                    break;
                }
            }
            if (!valid)
                return buildFault("Validation.Failed");
            
        	person.version = 1;
            var i:int = 1;
            for each (c in p.contacts) {
                contact = new Contact();
                contact.id = i++;
                contact.uid = "UID" + contact.id + "B";
                contact.email = c.email;
                contact.person = person;
                person.contacts.addItem(contact);
            }
            return buildResult("ok", [[ "personHome.instance", person ]]);
        }
        
        return buildFault("Server.Error");
    }
}
