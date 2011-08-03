package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;

import org.granite.persistence.PersistentSet;

import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Conflicts;
    import org.granite.tide.data.events.TideDataConflictsEvent;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeConflictEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
			_conflicts = null;
        }
        
        
        private var _conflicts:Conflicts = null;
        
        
        [Test]
        public function testMergeConflictEntity():void {
        	var person:Person = new Person();
        	person.id = 1; 
        	person.version = 0;
        	person.contacts = new ArrayCollection();
        	var contact:Contact = new Contact();
        	contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	_ctx.person = _ctx.meta_mergeExternalData(person, null, null);
        	person = _ctx.person;
        	
        	person.lastName = "toto";
        	var addedContact:Contact = new Contact();
        	addedContact.id = 2;
        	addedContact.version = 0;
        	addedContact.person = person;
        	person.contacts.addItem(addedContact);
        	contact.email = "test";
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	
        	var person2:Person = new Person();
        	person2.contacts = new ArrayCollection();
        	person2.id = person.id;
        	person2.version = 1;
        	person2.uid = person.uid;
        	person2.lastName = "tutu";
        	var contact2:Contact = new Contact();
        	contact2.id = contact.id;
        	contact2.version = 1;
        	contact2.uid = contact.uid;
        	contact2.email = "test2";
        	contact2.person = person2;
        	person2.contacts.addItem(contact2);
        	
        	_ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, conflictsHandler);
        	
        	_ctx.meta_mergeExternalData(person2, null, "S2");
        	
        	Assert.assertEquals("Conflicts after merge", 1, _conflicts.conflicts.length);
			Assert.assertTrue("Person dirty after merge", _ctx.meta_isEntityChanged(person));
        	
        	_conflicts.conflicts[0].acceptClient();
        	
        	Assert.assertEquals("Person last name", "toto", person.lastName);
        	Assert.assertEquals("Person contacts", 2, person.contacts.length);
        	Assert.assertEquals("Person version", 1, person.version);
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertEquals("Person last name after cancel", "tutu", person.lastName);
        	Assert.assertEquals("Person contacts after cancel", 1, person.contacts.length);
        	
        	person.lastName = "toto";
        	
        	var person3:Person = new Person();
        	person3.contacts = new ArrayCollection();
        	person3.id = person.id;
        	person3.version = 2;
        	person3.uid = person.uid;
        	person3.lastName = "titi";
        	var contact3:Contact = new Contact();
        	contact3.id = contact.id;
        	contact3.version = 1;
        	contact3.uid = contact.uid;
        	contact3.email = "test2";
        	contact3.person = person3;
        	person3.contacts.addItem(contact3);
        	var contact3b:Contact = new Contact();
        	contact3b.id = 3;
        	contact3b.version = 0;
        	contact3b.email = "test3";
        	contact3b.person = person3;
        	person3.contacts.addItem(contact3);
        	
        	_ctx.meta_mergeExternalData(person3, null, "S2");
        	
        	Assert.assertEquals("Conflicts after merge 2", 1, _conflicts.conflicts.length);
        	
        	_conflicts.conflicts[0].acceptServer();
        	
        	Assert.assertEquals("Person last name", "titi", person.lastName);
        	Assert.assertEquals("Person version", 2, person.version);
        	Assert.assertEquals("Person contacts", 2, person.contacts.length);
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        }
		
		[Test]
		public function testMergeConflictEntity2():void {
			var person:Person = new Person();
			person.id = 1; 
			person.version = 0;
			person.contacts = new ArrayCollection();
			var contact:Contact = new Contact();
			contact.id = 1;
			contact.version = 0;
			contact.person = person;
			person.contacts.addItem(contact);
			_ctx.person = _ctx.meta_mergeExternalData(person, null, null);
			person = _ctx.person;
			
			person.lastName = "toto";
			var addedContact:Contact = new Contact();
			addedContact.id = 2;
			addedContact.version = 0;
			addedContact.person = person;
			person.contacts.addItem(addedContact);
			contact.email = "test@test.com";
			
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			
			var person2:Person = new Person();
			person2.contacts = new ArrayCollection();
			person2.id = person.id;
			person2.version = 1;
			person2.uid = person.uid;
			person2.lastName = "toto";
			var contact2a:Contact = new Contact();
			contact2a.id = contact.id;
			contact2a.version = 0;
			contact2a.uid = contact.uid;
			contact2a.person = person2;
			var contact2b:Contact = new Contact();
			contact2b.id = addedContact.id;
			contact2b.version = 0;
			contact2b.uid = addedContact.uid;
			contact2b.person = person2;
			person2.contacts.addItem(contact2a);
			person2.contacts.addItem(contact2b);
			
			_ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, conflictsHandler);
			
			_ctx.meta_mergeExternalData(person2, null, "S2");
			
			Assert.assertNull("Conflicts after merge", _conflicts);
			Assert.assertFalse("Person dirty after merge", _ctx.meta_isEntityChanged(person));
		}
		
		[Test]
		public function testMergeConflictEntity3():void {
			var person:Person = new Person();
			person.id = 1; 
			person.version = 0;
			person.firstName = "toto";
			person.lastName = "toto";
			person.contacts = new PersistentSet(true);
			var contact:Contact = new Contact();
			contact.id = 1;
			contact.version = 0;
			contact.person = person;
			person.contacts.addItem(contact);
			_ctx.person = _ctx.meta_mergeExternalData(person, null, null);
			person = _ctx.person;
			
			person.lastName = "tutu";
			
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			
			var person2:Person = new Person();
			person2.contacts = new PersistentSet(true);
			person2.id = person.id;
			person2.version = 1;
			person2.uid = person.uid;
			person2.firstName = "tutu";
			person2.lastName = "toto";
			var contact2:Contact = new Contact();
			contact2.id = contact.id;
			contact2.version = 0;
			contact2.uid = contact.uid;
			contact2.person = person2;
			person2.contacts.addItem(contact2);
			
			_ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, acceptServerConflictsHandler);
			
			_ctx.meta_mergeExternalData([person2], null, "S2");

			Assert.assertFalse("Person dirty after merge", _ctx.meta_isEntityChanged(person));
			Assert.assertEquals("Person firstName", "tutu", person.firstName);
			Assert.assertEquals("Person lastName", "toto", person.lastName);
		}

        [Test]
        public function testMergeConflictEntity4():void {
            var person:Person = new Person();
            person.id = 1;
            person.version = 0;
            person.firstName = "toto";
            person.lastName = "toto";
            person.contacts = new ArrayCollection();
            var contact:Contact = new Contact();
            contact.id = 1;
            contact.version = 0;
            contact.person = person;
            person.contacts.addItem(contact);
            _ctx.person = _ctx.meta_mergeExternalData(person, null, null);
            person = Person(_ctx.person);
            contact = Contact(person.contacts.getItemAt(0));

            contact.email = "test@test.com";

            Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(contact));

            var person2:Person = new Person();
            person2.contacts = new ArrayCollection();
            person2.id = person.id;
            person2.version = 1;
            person2.uid = person.uid;
            person2.firstName = "toto";
            person2.lastName = "toto";
            var contact2:Contact = new Contact();
            contact2.id = contact.id;
            contact2.version = 1;
            contact2.uid = contact.uid;
            contact2.person = person2;
            contact2.email = "toto@toto.net";
            person2.contacts.addItem(contact2);

            _ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, acceptServerConflictsHandler);

            _ctx.meta_mergeExternalData([person2], null, "S2");

            Assert.assertFalse("Contact dirty after merge", _ctx.meta_isEntityChanged(contact));
            Assert.assertEquals("Contact email", "toto@toto.net", contact.email);
        }

        private function conflictsHandler(event:TideDataConflictsEvent):void {
        	_conflicts = event.conflicts;
        }

        private function acceptServerConflictsHandler(event:TideDataConflictsEvent):void {
        	event.conflicts.acceptAllServer();
        }
    }
}
