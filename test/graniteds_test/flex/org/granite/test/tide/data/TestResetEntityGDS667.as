package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
	import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestResetEntityGDS667 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
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
			
			Assert.assertEquals("Person contact collection restored", 1, person.contacts.length);
			Assert.assertStrictlyEquals("Person contact restored", c, person.contacts.getItemAt(0));
        }
    }
}
