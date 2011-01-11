package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestResetEntityGDS453 extends TestCase
    {
        public function TestResetEntityGDS453() {
            super("testResetEntityGDS453");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testResetEntityGDS453():void {
        	var person:Person = new Person();
        	person.version = 0;
        	var contact:Contact = new Contact();
        	contact.version = 0;
        	contact.person = person;
        	_ctx.contact = _ctx.meta_mergeExternal(contact);
        	contact = _ctx.contact;
        	_ctx.meta_result(null, null, null, null);
        	contact.person = new Person();
        	_ctx.meta_resetEntity(contact);
        	
        	assertStrictlyEquals("Entity reset", person, contact.person);
        	
        	var p:Person = new Person();
        	p.version = 0;
        	p.contacts = new ArrayCollection();
        	var c:Contact = new Contact();
        	c.version = 0;
        	c.person = p;
        	p.contacts.addItem(c);        	
        	_ctx.person = _ctx.meta_mergeExternal(p);
        	person = _ctx.person;
        	_ctx.meta_result(null, null, null, null);
        	
        	person.contacts.removeItemAt(0);
        	
        	_ctx.meta_resetEntity(person);
        	
        	assertEquals("Person contact collection restored", 1, person.contacts.length);
        	assertStrictlyEquals("Person contact restored", c, person.contacts.getItemAt(0));
        }
    }
}
