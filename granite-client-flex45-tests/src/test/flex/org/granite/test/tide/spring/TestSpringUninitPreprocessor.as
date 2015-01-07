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
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.test.tide.PersonServiceFold;
    import org.granite.tide.BaseContext;
    import org.granite.tide.data.UninitializeArgumentPreprocessor;
    import org.granite.tide.events.TideResultEvent;

    public class TestSpringUninitPreprocessor
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
            MockSpring.getInstance().addComponent("personService", PersonServiceFold);
            MockSpring.getInstance().addComponent("argPreprocessor", UninitializeArgumentPreprocessor);
        }
        

        [Test(async)]
        public function testFoldPreprocessEntity():void {
            var person:Person = new Person();
            person.uid = "P1";
            person.id = 1;
            person.version = 0;
            person.contacts = new PersistentSet(true);
            var contact:Contact = new Contact();
            contact.uid = "C1";
            contact.id = 1;
            contact.version = 0;
            contact.person = person;
            person.contacts.addItem(contact);
            _ctx.person = _ctx.meta_mergeExternal(person);

            person = _ctx.person;

            person.lastName = "Test";

            _ctx.personService.modifyPerson(person, Async.asyncHandler(this, modifyResult, 1000));
        }

        private function modifyResult(event:TideResultEvent, pass:Object = null):void {
            Assert.assertEquals("Uninitialized collection", "Test", event.result);
        }
    }
}


import mx.rpc.events.AbstractEvent;

import org.granite.meta;

import org.granite.test.tide.Person;

import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.invocation.InvocationCall;

class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "personService" && op == "modifyPerson") {
            if (params[0] is Person && !params[0].meta::isInitialized("contacts"))
                return buildResult(params[0].lastName);
        }
        
        return buildFault("Server.Error");
    }
}
