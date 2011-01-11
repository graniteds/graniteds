package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Conflicts;
    import org.granite.tide.data.events.TideDataConflictsEvent;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestMergeConflictEntity extends TestCase
    {
        public function TestMergeConflictEntity() {
            super("testMergeConflictEntity");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var _conflicts:Conflicts;
        
        
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
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	
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
        	
        	assertEquals("Conflicts after merge", 1, _conflicts.conflicts.length);
        	
        	_conflicts.conflicts[0].acceptClient();
        	
        	assertEquals("Person last name", "toto", person.lastName);
        	assertEquals("Person contacts", 2, person.contacts.length);
        	assertEquals("Person version", 1, person.version);
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	
        	_ctx.meta_resetEntity(person);
        	
        	assertEquals("Person last name after cancel", "tutu", person.lastName);
        	assertEquals("Person contacts after cancel", 1, person.contacts.length);
        	
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
        	
        	_ctx.meta_mergeExternalData(person3, null, "S3");
        	
        	assertEquals("Conflicts after merge 2", 1, _conflicts.conflicts.length);
        	
        	_conflicts.conflicts[0].acceptServer();
        	
        	assertEquals("Person last name", "titi", person.lastName);
        	assertEquals("Person version", 2, person.version);
        	assertEquals("Person contacts", 2, person.contacts.length);
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        }
        
        private function conflictsHandler(event:TideDataConflictsEvent):void {
        	_conflicts = event.conflicts;
        }
    }
}
