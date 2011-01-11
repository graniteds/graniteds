package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.data.utils.Managed;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestDestroyContext extends TestCase
    {
        public function TestDestroyContext() {
            super("testDestroyContext");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testDestroyContext():void {
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
        	
        	ctx2.meta_end(true);
        	
        	var contact2:Contact = new Contact();
        	person.contacts.addItem(contact2);
        	// Context 2 should not listen to collections any more and contact2 should not be attached  
        	assertNull("Context listeners removed", Managed.getEntityManager(contact2));
        }
    }
}
