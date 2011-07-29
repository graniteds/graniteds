package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.data.utils.Managed;
    import mx.events.CollectionEventKind;
    
    import org.flexunit.Assert;
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Classification;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityCollGDS898
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testDirtyCheckEntityCollection():void {
			var person:Person = new Person();
			person.id = 1;
			person.version = 0;
			person.uid = "P1";
			person.contacts = new PersistentSet(true);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = Person(_ctx.person);
			
			var contact:Contact = new Contact();
			contact.id = 1;		
			contact.version = 0;
			contact.uid = "C1";
			contact.person = person;
			contact.email = "toto@tutu.com";
			person.contacts.addItem(contact);
			var contact2:Contact = new Contact();
			contact2.id = 2;		
			contact2.version = 0;
			contact2.uid = "C2";
			contact2.person = person;
			contact2.email = "test@tutu.com";
			person.contacts.addItem(contact2);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			person.contacts.removeItemAt(0);
			
			Assert.assertEquals("Saved events", 1, _ctx.meta_getSavedProperties()[person].contacts.length);
			Assert.assertEquals("Saved event location", 0, _ctx.meta_getSavedProperties()[person].contacts[0].location);
        }
		
		[Test]
		public function testDirtyCheckEntityCollection2():void {
			var person:Person = new Person();
			person.id = 1;
			person.version = 0;
			person.uid = "P1";
			person.contacts = new PersistentSet(true);
			var contact:Contact = new Contact();
			contact.id = 1;		
			contact.version = 0;
			contact.uid = "C1";
			contact.person = person;
			contact.email = "toto@tutu.com";
			person.contacts.addItem(contact);
			var contact2:Contact = new Contact();
			contact2.id = 2;		
			contact2.version = 0;
			contact2.uid = "C2";
			contact2.person = person;
			contact2.email = "test@tutu.com";
			person.contacts.addItem(contact2);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = Person(_ctx.person);
			
			person.contacts.removeItemAt(1);
			person.contacts.removeItemAt(0);
			
			var contact3:Contact = new Contact();
			contact3.id = 2;		
			contact3.version = 0;
			contact3.uid = "C2";
			contact3.person = person;
			contact3.email = "test@tutu.com";
			person.contacts.addItemAt(contact3, 0);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Saved events", 1, _ctx.meta_getSavedProperties()[person].contacts.length);
			Assert.assertEquals("Saved remove event location", 0, _ctx.meta_getSavedProperties()[person].contacts[0].location);
		}
    }
}
