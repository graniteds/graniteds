package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestResetEntityNoVersion
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testResetEntityNoVersion():void {
        	var person:PersonNoVersion = new PersonNoVersion();
        	var contact:ContactNoVersion = new ContactNoVersion();
        	contact.person = person;
        	_ctx.contact = _ctx.meta_mergeExternal(contact);
        	contact = _ctx.contact;
        	_ctx.meta_result(null, null, null, null);
        	contact.person = new PersonNoVersion();
        	_ctx.meta_resetEntity(contact);
        	
        	Assert.assertStrictlyEquals("Entity reset", person, contact.person);
        	
        	var p:PersonNoVersion = new PersonNoVersion();
        	p.contacts = new ArrayCollection();
        	var c:ContactNoVersion = new ContactNoVersion();
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
		
		[Test]
		public function testResetEntityNoVersion2():void {
			var person:PersonNoVersion = new PersonNoVersion();
			var contact:ContactNoVersion = new ContactNoVersion();
			contact.person = person;
			_ctx.contact = _ctx.meta_mergeExternalData(contact);
			contact = _ctx.contact;
			
			contact.person = null;
			_ctx.meta_resetEntity(contact);
			
			Assert.assertStrictlyEquals("Entity reset", person, contact.person);
		}
    }
}
