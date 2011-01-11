package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestResetEntityGDS668 extends TestCase
    {
        public function TestResetEntityGDS668() {
            super("testResetEntityGDS668");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
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
        	
        	assertStrictlyEquals("Entity reset", person, contact.person);
        }
    }
}
