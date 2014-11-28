package org.granite.test.tide.spring
{
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Person;
import org.granite.test.tide.PersonServiceDiff;
import org.granite.tide.BaseContext;
    import org.granite.tide.data.ChangeArgumentPreprocessor;
    import org.granite.tide.events.TideResultEvent;

    public class TestSpringChangePreprocessor
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
            MockSpring.getInstance().addComponent("personService", PersonServiceDiff);
            MockSpring.getInstance().addComponent("argPreprocessor", ChangeArgumentPreprocessor);
        }
        

        [Test(async)]
        public function testChangeSetPreprocessEntity():void {
            var person:Person = new Person();
            person.uid = "P1";
            person.id = 1;
            person.version = 0;
            person.contacts = new PersistentSet(false);
            _ctx.person = _ctx.meta_mergeExternal(person);

            person = _ctx.person;

            person.lastName = "Test";

            _ctx.personService.modifyPerson(person, Async.asyncHandler(this, modifyResult, 1000));
        }

        private function modifyResult(event:TideResultEvent, pass:Object = null):void {
            Assert.assertEquals("ChangeSet property", "Test", event.result)
        }
    }
}


import mx.rpc.events.AbstractEvent;

import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.invocation.InvocationCall;

class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "personService" && op == "modifyPerson") {
            if (params[0] is ChangeSet && params[0].changes.length == 1)
                return buildResult(params[0].changes[0].changes.lastName);
        }
        
        return buildFault("Server.Error");
    }
}
