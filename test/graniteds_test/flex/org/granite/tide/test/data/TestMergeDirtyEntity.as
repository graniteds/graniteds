package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestMergeDirtyEntity extends TestCase
    {
        public function TestMergeDirtyEntity() {
            super("testMergeDirtyEntity");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testMergeDirtyEntity():void {
        	var person:Person = new Person();
        	person.id = 1; 
        	person.version = 0;
        	person.contacts = new ArrayCollection();
        	var contact:Contact = new Contact();
        	contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	_ctx.person = _ctx.meta_mergeExternal(person);
        	person = _ctx.person;
        	
        	person.lastName = "toto";
        	var addedContact:Contact = new Contact();
        	addedContact.version = 0;
        	addedContact.person = person;
        	person.contacts.addItem(addedContact);
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	
        	var person2:Person = new Person();
        	person2.contacts = new ArrayCollection();
        	person2.id = person.id;
        	person2.version = 0;
        	person2.uid = person.uid;
        	var contact2:Contact = new Contact();
        	contact2.id = contact.id;
        	contact2.version = 0;
        	contact2.uid = contact.uid;
        	contact2.person = person2;
        	person2.contacts.addItem(contact2);
        	
        	_ctx.meta_clearCache();
        	_ctx.meta_mergeExternal(person2);
        	
        	assertTrue("Person dirty after merge", _ctx.meta_isEntityChanged(person));
        	assertEquals("Person contacts after merge", 2, person.contacts.length);
        	
        	person2 = new Person();
        	person2.contacts = new ArrayCollection();
        	person2.id = person.id;
        	person2.version = 1;
        	person2.uid = person.uid;
        	contact2 = new Contact();
        	contact2.id = contact.id;
        	contact2.version = 0;
        	contact2.uid = contact.uid;
        	contact2.person = person2;
        	person2.contacts.addItem(contact2);
        	
        	_ctx.meta_clearCache();
        	_ctx.meta_mergeExternal(person2);
        	
        	assertFalse("Person not dirty after merge", _ctx.meta_isEntityChanged(person));
        	assertEquals("Person contacts after merge", 1, person.contacts.length);
        }
    }
}
