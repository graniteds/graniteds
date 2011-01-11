package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestDirtyCheckEntityGDS614 extends TestCase
    {
        public function TestDirtyCheckEntityGDS614() {
            super("testDirtyCheckEntityGDS614");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        public function testDirtyCheckEntityGDS614():void {
        	var person:Person = new Person();
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	_ctx.person = person;
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	
        	person.lastName = "Test";
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", ctxDirty);
        	
        	person.lastName = null;
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", ctxDirty);
        	
        	person.firstName = "Toto";
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", ctxDirty);
        	
        	person.firstName = "";
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", ctxDirty);
        	
        	person.contacts = new ArrayCollection();
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", ctxDirty);
        	
        	var contact:Contact = new Contact();
        	contact.person = person;
        	person.contacts.addItem(contact);
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", ctxDirty);
        	
			// The contact is now dirty too, this make the test finally fail 
			contact.email = "toto@example.org";
			         	
        	assertTrue("Contact dirty", _ctx.meta_isEntityChanged(contact)); 
        	assertTrue("Context dirty", ctxDirty); 
        	
			// Removing the the dirty contact doesn't make the context clean 
        	person.contacts.removeItemAt(0);
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertTrue("Context still dirty", ctxDirty);
			
			// When setting the contact mail to null again, the test passes, which means 
			// that the newly created contact is still managed 
			contact.email = null; 

			// This fails if email is not cleand 
        	assertFalse("Context not dirty", ctxDirty);
        }
    }
}
