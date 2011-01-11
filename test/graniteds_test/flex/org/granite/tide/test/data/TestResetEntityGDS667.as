package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
	import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestResetEntityGDS667 extends TestCase
    {
        public function TestResetEntityGDS667() {
            super("testResetEntityGDS667");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testResetEntityGDS667():void {
			var p:Person = new Person();
			p.version = 0;
			p.contacts = new PersistentSet();
			var c:Contact = new Contact();
			c.version = 0;
			c.person = p;
			p.contacts.addItem(c);        	
			_ctx.person = _ctx.meta_mergeExternalData(p);
			var person:Person = _ctx.person;
			
			person.contacts.removeAll();
			
			_ctx.meta_resetEntity(person);
			
			assertEquals("Person contact collection restored", 1, person.contacts.length);
			assertStrictlyEquals("Person contact restored", c, person.contacts.getItemAt(0));
        }
    }
}
