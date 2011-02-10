package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestDirtyCheckEntityGDS614 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        [Test]
        public function testDirtyCheckEntityGDS614():void {
        	var person:Person = new Person();
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	_ctx.person = person;
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	
        	person.lastName = "Test";
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", ctxDirty);
        	
        	person.lastName = null;
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", ctxDirty);
        	
        	person.firstName = "Toto";
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", ctxDirty);
        	
        	person.firstName = "";
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", ctxDirty);
        	
        	person.contacts = new ArrayCollection();
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", ctxDirty);
        	
        	var contact:Contact = new Contact();
        	contact.person = person;
        	person.contacts.addItem(contact);
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", ctxDirty);
        	
			// The contact is now dirty too, this make the test finally fail 
			contact.email = "toto@example.org";
			         	
        	Assert.assertTrue("Contact dirty", _ctx.meta_isEntityChanged(contact)); 
        	Assert.assertTrue("Context dirty", ctxDirty); 
        	
			// Removing the the dirty contact doesn't make the context clean 
        	person.contacts.removeItemAt(0);
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertTrue("Context still dirty", ctxDirty);
			
			// When setting the contact mail to null again, the test passes, which means 
			// that the newly created contact is still managed 
			contact.email = null; 

			// This fails if email is not cleand 
        	Assert.assertFalse("Context not dirty", ctxDirty);
        }
    }
}
