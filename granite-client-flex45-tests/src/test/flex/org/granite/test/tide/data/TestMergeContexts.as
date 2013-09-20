package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeContexts 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeContexts():void {
        	var ctx2:BaseContext = Tide.getInstance().getContext("Test#1");
        	
        	var person:Person = new Person();
        	person.id = 1; 
        	person.version = 0;
        	person.contacts = new ArrayCollection();
        	var contact:Contact = new Contact();
        	contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	ctx2.person = ctx2.meta_mergeExternal(person);
        	person = ctx2.person;
        	
        	ctx2.meta_mergeInGlobalContext();
        	
        	var p:Person = _ctx.meta_getCachedObject(person) as Person;
        	Assert.assertNotNull("Person merged", p);
        	Assert.assertEquals("Person copied uid", p.uid, person.uid);
        	Assert.assertFalse("Person copied collection", p.contacts === person.contacts);
        }
    }
}
