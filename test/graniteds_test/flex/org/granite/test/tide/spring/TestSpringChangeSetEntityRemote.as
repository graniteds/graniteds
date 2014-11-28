package org.granite.test.tide.spring
{
    import mx.collections.Sort;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.granite.meta;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Address;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.test.tide.data.Contact4;
    import org.granite.test.tide.data.Person11;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.events.TideResultEvent;
    
    
    public class TestSpringChangeSetEntityRemote 
    {
		private var _ctx:BaseContext = MockSpring.getInstance().getContext();
		
		
		[Before]
		public function setUp():void {
			MockSpring.reset();
			_ctx = MockSpring.getInstance().getSpringContext();
			MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
			
			MockSpring.getInstance().addComponents([ ChangeMerger ]);
		}
        
        
        [Test(async)]
        public function testSpringChangeSetEntityRemote():void {
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
		
		
		[Test(async)]
		public function testSpringChangeSetEntityRemoteNew():void {
			var person:Person11 = new Person11();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.meta::detachedState = "Bla";
			person.contacts = new PersistentSet(false);
			_ctx.person = _ctx.meta_mergeExternal(person);
			_ctx.meta_clearCache();
			
			person = new Person11();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;        	
			person.meta::detachedState = "Bla";
			person.contacts = new PersistentSet(true);
			var contact:Contact4 = new Contact4();
			contact.uid = "C1";
			contact.id = 1;
			contact.version = 0;
			contact.person = person;
			contact.address = new Address();			
			person.contacts.addItem(contact);
			
			_ctx.person = _ctx.meta_mergeExternal(person);
			_ctx.meta_clearCache();
			
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
			
			var contact2a:Contact4 = new Contact4();
			contact2a.email = "test@truc.net";
			contact2a.person = person;
			contact2a.address = new Address();
			person.contacts.addItem(contact2a);
			var contact2b:Contact4 = new Contact4();
			contact2b.email = "test@truc.com";
			contact2b.person = person;
			contact2b.address = new Address();
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
			
			var contact3:Contact4 = new Contact4();
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
			
			_ctx.personService.modifyPerson(changeSet, Async.asyncHandler(this, changeNewHandler, 1000));
		}
		
		private function changeNewHandler(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals("Result ok", "ok", event.result);
		}

			
		[Test(async)]
		public function testSpringChangeSetEntityRemoteAddFirst():void {
			var person:Person11 = new Person11();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			
			var contact1:Contact4 = new Contact4();
			contact1.uid = "C1";
			contact1.version = 0;
			contact1.person = person;
			contact1.address = new Address();
			contact1.email = "test@test.com";
			person.contacts.addItem(contact1);
			
			person = _ctx.person = _ctx.meta_mergeExternalData(person);
			_ctx.meta_clearCache();
			
			var contact2:Contact4 = new Contact4();
			contact2.uid = "C2";
			contact2.person = person;
			contact2.address = new Address();
			contact2.email = "truc@test.com";
			person.contacts.addItem(contact2);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			
			_ctx.personService.addContact(changeSet, Async.asyncHandler(this, addContactHandler, 1000));
		}
		
		[Test(async)]
		public function testSpringChangeSetEntityRemoteAddFirstSorted():void {
			var person:Person11 = new Person11();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet();
			
			var contact1:Contact4 = new Contact4();
			contact1.uid = "C1";
			contact1.version = 0;
			contact1.person = person;
			contact1.address = new Address();
			contact1.email = "test@test.com";
			person.contacts.addItem(contact1);
			
			person = _ctx.person = _ctx.meta_mergeExternalData(person);
			_ctx.meta_clearCache();
			
			person.contacts.filterFunction = function(item:Contact4):Boolean {
				return item.email.indexOf("@test.com") > 0;
			};
			person.contacts.sort = new Sort();
			person.contacts.sort.fields = [];
			person.contacts.sort.compareFunction = function(item1:Contact4, item2:Contact4, items:Array = null):int {
				if (item1.email > item2.email)
					return 1;
				if (item1.email < item2.email)
					return -1;
				return 0;
			};
			person.contacts.refresh();
			
			var contact2:Contact4 = new Contact4();
			contact2.uid = "C2";
			contact2.person = person;
			contact2.address = new Address();
			contact2.email = "truc@test.com";
			person.contacts.addItem(contact2);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			
			_ctx.personService.addContact(changeSet, Async.asyncHandler(this, addContactHandler, 1000));
		}
		
		private function addContactHandler(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals("Result ok", "ok", event.result);
			
			var person:Person11 = Person11(_ctx.person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			person.contacts.removeItemAt(1);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			
			_ctx.personService.removeContact(changeSet, Async.asyncHandler(this, removeContactHandler, 1000));
		}
		
		private function removeContactHandler(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals("Result ok", "ok", event.result);
			
			var person:Person11 = Person11(_ctx.person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			var contact3:Contact4 = new Contact4();
			contact3.uid = "C3";
			contact3.person = person;
			contact3.address = new Address();
			contact3.email = "toto@test.com";
			person.contacts.addItem(contact3);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			
			_ctx.personService.addContact(changeSet, Async.asyncHandler(this, addContact2Handler, 1000));
		}
		
		private function addContact2Handler(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals("Result ok", "ok", event.result);
			
			var person:Person11 = Person11(_ctx.person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			person.contacts.removeItemAt(1);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}
		
		
		[Test(async)]
		public function testSpringChangeSetEntityRemoteAddFirstSorted2():void {
			var person:Person11 = new Person11();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet();
			
			var contact1:Contact4 = new Contact4();
			contact1.uid = "C1";
			contact1.version = 0;
			contact1.person = person;
			contact1.address = new Address();
			contact1.email = "test@test.com";
			person.contacts.addItem(contact1);
			
			person = _ctx.person = _ctx.meta_mergeExternalData(person);
			_ctx.meta_clearCache();
			
			person.contacts.filterFunction = function(item:Contact4):Boolean {
				return item.email.indexOf("@test.com") > 0;
			};
			person.contacts.sort = new Sort();
			person.contacts.sort.fields = [];
			person.contacts.sort.compareFunction = function(item1:Contact4, item2:Contact4, fields:Array = null):int {
				if (item1.email > item2.email)
					return 1;
				if (item1.email < item2.email)
					return -1;
				return 0;
			};
			person.contacts.refresh();
			
			var contact2:Contact4 = new Contact4();
			contact2.uid = "C2";
			contact2.person = person;
			contact2.address = new Address();
			contact2.email = "truc@test.com";
			person.contacts.addItem(contact2);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			
			_ctx.personService.addContact2(changeSet, Async.asyncHandler(this, addContact4Handler, 1000));
		}
		
		private function addContact4Handler(event:TideResultEvent, pass:Object = null):void {
			Assert.assertEquals("Result ok", "ok", event.result);
			
			var person:Person11 = Person11(_ctx.person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			person.contacts.removeItemAt(1);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			
			_ctx.personService.removeContact(changeSet, Async.asyncHandler(this, removeContactHandler, 1000));
		}
	}
}


import mx.rpc.events.AbstractEvent;

import org.flexunit.Assert;
import org.granite.persistence.PersistentSet;
import org.granite.reflect.Type;
import org.granite.test.tide.Address;
import org.granite.test.tide.data.Contact4;
import org.granite.test.tide.data.Person11;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.CollectionChanges;
import org.granite.tide.invocation.InvocationCall;

class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
	
	function MockSimpleCallAsyncToken() {
		super(null);
	}
	
	protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
		var cs:ChangeSet, c:Contact4, change:Change, cr:ChangeRef, collChanges:CollectionChanges;
		
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
		else if (componentName == "personService" && op.indexOf("addContact") == 0) {
			if (params[0] is ChangeSet && params[0].changes.length == 1) {
				var person:Person11 = new Person11();
				person.uid = "P1";
				person.id = 1;
				person.version = 0;
				person.contacts = new PersistentSet(false);
								
				cs = ChangeSet(params[0]);
				c = CollectionChanges(cs.changes[0].changes.contacts).getChange(0).value as Contact4;
				
				var cid:Number = new Number(c.uid.substring(1));
				var contact:Contact4 = new Contact4();
				contact.uid = c.uid;
				contact.id = cid;
				contact.version = 0;
				contact.person = person;
				contact.address = new Address();
				contact.address.uid = "A" + cid;
				contact.address.id = cid;
				contact.address.version = 0;
				contact.email = "test" + cid + "@test.com";
				
				change = new Change(Type.forClass(Person11).name, person.uid, person.id, person.version);
				collChanges = new CollectionChanges();
				collChanges.addChange(1, null, contact);
				change.changes.contacts = collChanges;
				
				if (op == "addContact")
					return buildResult("ok", null, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
				else if (op == "addContact2")
					return buildResult("ok", null, [[ 'PERSIST', contact ], [ 'PERSIST', contact.address ], [ 'UPDATE', new ChangeSet([ change ]) ]]);
			}
		}
		else if (componentName == "personService" && op == "removeContact") {
			if (params[0] is ChangeSet && params[0].changes.length == 1) {
				cs = ChangeSet(params[0]);
				cr = CollectionChanges(cs.changes[0].changes.contacts).getChange(0).value as ChangeRef;
				
				change = new Change(Type.forClass(Person11).name, "P1", 1, 0);
				collChanges = new CollectionChanges();
				collChanges.addChange(-1, null, new ChangeRef(cr.className, cr.uid, cr.id));
				change.changes.contacts = collChanges;
				
				return buildResult("ok", null, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
			}
		}
		
		return buildFault("Server.Error");
	}
}
