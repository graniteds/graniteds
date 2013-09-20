/**
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
    import flash.events.Event;
    
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.collections.ItemResponder;
    import mx.collections.errors.ItemPendingError;
    import mx.rpc.Fault;
    import mx.rpc.IResponder;
    import mx.utils.StringUtil;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestSeamMergeEntityColl
    {
        private var _ctx:Context;
        
        
        private var _person:Person;
        
        [Before]
        public function setUp():void {
            MockSeam.reset();
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
            _ctx = MockSeam.getInstance().getSeamContext();
            
            _person = new Person();
            _person.id = 12;
            _person.uid = "UID12B";
        }
        
        [Test(async)]
        public function testMergeEntityColl():void {
            _ctx.personHome.instance = _person;
            _ctx.personHome.id = 12;
            _ctx.personHome.instance.contacts;
            _ctx.personHome.find(Async.asyncHandler(this, findResult, 1000));
        }
        
        private function findResult(event:TideResultEvent, pass:Object = null):void {
			Assert.assertTrue(event.context.personHome.instance.contacts.isInitialized());
            
            var c:Contact = new Contact();
            c.uid = "UID2B";
            c.email = "tutu.com";
            event.context.personHome.instance.contacts.addItem(c);
            event.context.personHome.update(updateResult, Async.asyncHandler(this, updateFault, 1000));
        }
        
        private function updateResult(event:TideResultEvent, pass:Object = null):void {
			Assert.fail("Validation not failed");
        }
        
        private function updateFault(event:TideFaultEvent, pass:Object = null):void {
            _ctx.personHome.instance.contacts.getItemAt(1).email = "tutu@tutu.com";
            _ctx.personHome.update(Async.asyncHandler(this, updateResult2, 1000));
        }
        
        private function updateResult2(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals(2, _ctx.personHome.instance.contacts.length);
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
