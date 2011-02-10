package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestResetEntityGDS453 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
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
        	
        	Assert.assertStrictlyEquals("Entity reset", person, contact.person);
        	
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
        	
        	Assert.assertEquals("Person contact collection restored", 1, person.contacts.length);
        	Assert.assertStrictlyEquals("Person contact restored", c, person.contacts.getItemAt(0));
        }
    }
}
