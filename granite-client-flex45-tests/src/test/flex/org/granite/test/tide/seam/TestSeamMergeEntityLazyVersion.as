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
    
    
    public class TestSeamMergeEntityLazyVersion
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
        public function testMergeEntityLazyVersion():void {
            _ctx.personHome.find(Async.asyncHandler(this, findResult, 1000));
        }
        
		private var res:Function = null;
		
        private function findResult(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals(1, event.result.length);
            var p:Person = event.result.getItemAt(0);
			Assert.assertTrue(p.contacts is PersistentCollection);
			Assert.assertFalse(PersistentCollection(p.contacts).isInitialized());
            
            var coll:PersistentCollection = p.contacts as PersistentCollection;
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
            
            _ctx.personHome.find(Async.asyncHandler(this, findResult2, 1000));
        }
        
        private function respondFault(event:Event, pass:Object = null):void {
			Assert.fail("No response from initializer");
        }
        
        private function findResult2(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals(1, event.result.length);
            var p:Person = event.result.getItemAt(0);
			Assert.assertTrue(p.contacts is PersistentCollection);
            // Should not have been uninitialized, version has not changed
			Assert.assertTrue(PersistentCollection(p.contacts).isInitialized());
            
            _ctx.personHome.find2(Async.asyncHandler(this, findResult3, 1000));
        }
        
        private function findResult3(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals(1, event.result.length);
            var p:Person = event.result.getItemAt(0);
			Assert.assertTrue(p.contacts is PersistentCollection);
            // Should have been uninitialized, version has changed
			Assert.assertFalse(PersistentCollection(p.contacts).isInitialized());
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