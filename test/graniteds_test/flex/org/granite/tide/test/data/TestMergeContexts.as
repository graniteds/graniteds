package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestMergeContexts extends TestCase
    {
        public function TestMergeContexts() {
            super("testMergeContexts");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
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
        	assertNotNull("Person merged", p);
        	assertEquals("Person copied uid", p.uid, person.uid);
        	assertFalse("Person copied collection", p.contacts === person.contacts);
        }
    }
}
