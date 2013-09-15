package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestResetEntityGDS668 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testResetEntityGDS668():void {
        	var person:Person = new Person();
        	person.version = 0;
        	var contact:Contact = new Contact();
        	contact.version = 0;
        	contact.person = person;
        	_ctx.contact = _ctx.meta_mergeExternalData(contact);
        	contact = _ctx.contact;
			
        	contact.person = null;
        	_ctx.meta_resetEntity(contact);
        	
        	Assert.assertStrictlyEquals("Entity reset", person, contact.person);
        }
    }
}
