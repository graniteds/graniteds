package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestDirtyCheckEntity extends TestCase
    {
        public function TestDirtyCheckEntity() {
            super("testDirtyCheckEntity");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        public function testDirtyCheckEntity():void {
        	var person:Person = new Person();
        	var person2:Person = new Person(); 
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	person.contacts = new ArrayCollection();
        	person.version = 0;
        	var contact:Contact = new Contact();
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	person2.version = 0;
        	_ctx.contact = _ctx.meta_mergeExternal(contact);
        	_ctx.person2 = _ctx.meta_mergeExternal(person2);
        	contact = _ctx.contact;
        	person2 = _ctx.person2;
        	contact.person.firstName = 'toto';
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(contact.person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", _ctx.meta_dirty);
        	assertTrue("Context dirty 2", ctxDirty);
        	assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	contact.person.firstName = null;
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(contact.person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", _ctx.meta_dirty);
        	assertFalse("Context not dirty 2", ctxDirty);
        	assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	var contact2:Contact = new Contact();
        	contact2.version = 0;
        	contact2.person = person;
        	person.contacts.addItem(contact2);
        	        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(contact.person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", _ctx.meta_dirty);
        	assertTrue("Context dirty 2", ctxDirty);
        	assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	person.contacts.removeItemAt(1);
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(contact.person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", _ctx.meta_dirty);
        	assertFalse("Context not dirty 2", ctxDirty);
        	assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	contact.email = "toto";
        	person2.lastName = "tutu";
        	
        	assertTrue("Contact dirty", _ctx.meta_isEntityChanged(contact));
        	assertTrue("Person 2 dirty", _ctx.meta_isEntityChanged(person2));
        	
        	var receivedPerson:Person = new Person();
        	receivedPerson.version = 1;
        	receivedPerson.uid = person.uid;
        	var receivedContact:Contact = new Contact();
        	receivedContact.version = 1;
        	receivedContact.uid = contact.uid;
        	receivedContact.person = receivedPerson;
        	receivedPerson.contacts = new ArrayCollection();
        	receivedPerson.contacts.addItem(receivedContact);
        	
        	_ctx.meta_result(null, null, null, receivedPerson);
        	
        	assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	assertTrue("Person 2 dirty", _ctx.meta_isEntityChanged(person2));
        	assertTrue("Context dirty", _ctx.meta_dirty);
        	
        	receivedPerson = new Person();
        	receivedPerson.version = 1;
        	receivedPerson.uid = person2.uid;
        	
        	_ctx.meta_result(null, null, null, receivedPerson);
        	assertFalse("Person 2 dirty", _ctx.meta_isEntityChanged(person2));
        	assertFalse("Context dirty", _ctx.meta_dirty);
        }
    }
}
