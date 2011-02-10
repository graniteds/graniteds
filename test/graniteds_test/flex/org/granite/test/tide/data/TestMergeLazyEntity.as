package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeLazyEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeLazyEntity():void {
        	var person:Person = new Person();
        	person.id = 1; 
        	person.meta::setInitialized(false);
        	var contact:Contact = new Contact();
        	contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	
        	_ctx.contact = _ctx.meta_mergeExternal(contact);
        	contact = _ctx.contact;
        	
        	Assert.assertFalse("Person not initialized", contact.person.meta::isInitialized());
        	
        	var person2:Person = new Person();
        	person2.id = 1;
        	person2.firstName = "Jean";
        	person2.lastName = "Richard";
        	var contact2:Contact = new Contact();
        	contact2.id = 1;
        	contact2.uid = contact.uid;
        	contact2.version = 1;
        	contact2.person = person2;
        	
        	_ctx.meta_mergeExternal(contact2);
        	
        	Assert.assertTrue("Person initialized", contact.person.meta::isInitialized());
        	
        	var person3:Person = new Person();
        	person3.id = 1;
        	person3.meta::setInitialized(false);
        	var contact3:Contact = new Contact();
        	contact3.id = 1;
        	contact3.uid = contact.uid;
        	contact3.version = 2;
        	contact3.person = person3;
        	
        	_ctx.meta_mergeExternal(contact3);
        	
        	Assert.assertTrue("Person still initialized", contact.person.meta::isInitialized());
        }
    }
}
