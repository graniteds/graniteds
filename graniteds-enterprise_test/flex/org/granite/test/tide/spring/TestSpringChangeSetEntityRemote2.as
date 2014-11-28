package org.granite.test.tide.spring
{
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.meta;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.events.TideResultEvent;
    
    
    public class TestSpringChangeSetEntityRemote2
    {
		private var _ctx:BaseContext = MockSpring.getInstance().getContext();
		
		
		[Before]
		public function setUp():void {
			MockSpring.reset();
			_ctx = MockSpring.getInstance().getSpringContext();
			MockSpring.getInstance().token = new MockSimpleCallAsyncToken();

			MockSpring.getInstance().addComponents([ChangeMerger]);
		}
        
        
        [Test(async)]
        public function testSpringChangeSetEntityRemote2():void {
        	var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;        	
			person.meta::detachedState = "Bla";
        	person.contacts = new PersistentSet(false);
			_ctx.person = _ctx.meta_mergeExternal(person);
			
			person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;        	
			person.meta::detachedState = "Bla";
			person.contacts = new PersistentSet(true);
        	var contact:Contact = new Contact();
			contact.uid = "C1";
			contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	
        	_ctx.person = _ctx.meta_mergeExternal(person);

        	person = _ctx.person;
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet empty", 0, changeSet.length);
			
        	person.lastName = 'toto';
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			
			person.contacts.removeItemAt(0);
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 2", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			var coll:CollectionChanges = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 1, coll.length);
			Assert.assertEquals("ChangeSet collection type", -1, coll.getChange(0).type);
			Assert.assertEquals("ChangeSet collection index", 0, coll.getChange(0).key);
			Assert.assertEquals("ChangeSet collection value", "C1", coll.getChange(0).value.uid);
			
			var contact2a:Contact = new Contact();
			contact2a.email = "test@truc.net";
			contact2a.person = person;
			person.contacts.addItem(contact2a);
			var contact2b:Contact = new Contact();
			contact2b.email = "test@truc.com";
			contact2b.person = person;
			person.contacts.addItem(contact2b);
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 3", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			coll = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 3, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(1).type);
			Assert.assertEquals("ChangeSet collection index", 0, coll.getChange(1).key);
			Assert.assertFalse("ChangeSet collection element uninitialized", coll.getChange(1).value.meta::isInitialized("person"));
			Assert.assertEquals("ChangeSet collection element reference", coll.getChange(1).value.person.id, coll.getChange(2).value.person.id);
			
			var contact3:Contact = new Contact();
			contact3.email = "tutu@tutu.net";
			contact3.version = 0;
			contact3.uid = "C3";
			contact3.id = 3;
			_ctx.contact3 = _ctx.meta_mergeExternal(contact3);
			contact3 = _ctx.contact3;
			
			person.contacts.addItem(contact3);
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 4", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			coll = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 4, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(3).type);
			Assert.assertEquals("ChangeSet collection index", 2, coll.getChange(3).key);
			Assert.assertTrue("ChangeSet collection value", coll.getChange(3).value is ChangeRef);
			Assert.assertStrictlyEquals("ChangeSet collection value", contact3.uid, coll.getChange(3).value.uid);
			
			_ctx.personService.modifyPerson(changeSet, Async.asyncHandler(this, changeHandler, 1000));
		}
		
		private function changeHandler(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals("Result ok", "ok", event.result);
        }
    }
}


import mx.rpc.events.AbstractEvent;

import org.flexunit.Assert;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChanges;
import org.granite.tide.invocation.InvocationCall;

class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
	
	function MockSimpleCallAsyncToken() {
		super(null);
	}
	
	protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
		if (componentName == "personService" && op == "modifyPerson") {
			if (params[0] is ChangeSet && params[0].changes.length == 1) {
				var changeSet:ChangeSet = params[0] as ChangeSet;
				Assert.assertEquals("ChangeSet count 4", 1, changeSet.length);
				Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
				var coll:CollectionChanges = changeSet.getChange(0).changes.contacts as CollectionChanges;
				Assert.assertEquals("ChangeSet collection", 4, coll.length);
				Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(3).type);
				Assert.assertEquals("ChangeSet collection index", 2, coll.getChange(3).key);
				Assert.assertTrue("ChangeSet collection value", coll.getChange(3).value is ChangeRef);
				Assert.assertStrictlyEquals("ChangeSet collection value", "C3", coll.getChange(3).value.uid);
				return buildResult("ok");
			}
		}
		
		return buildFault("Server.Error");
	}
}
