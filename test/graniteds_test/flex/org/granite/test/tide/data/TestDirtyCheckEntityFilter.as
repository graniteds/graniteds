package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEventKind;
    
    import org.flexunit.Assert;
    import org.granite.collections.IPersistentCollection;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Classification;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityFilter
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testDirtyCheckEntityFilter():void {
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
			var contact2:Contact = new Contact();
			contact2.id = 2;		
			contact2.version = 0;
			contact2.uid = "C2";
			contact2.person = person;
			contact2.email = "test@tutu.com";
			person.contacts.addItem(contact2);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			person = Person(_ctx.person);
			person.contacts.filterFunction = function(item:Contact):Boolean {
				return item.email != "";
			};
			person.contacts.refresh();
			
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.person = person;
			contact3.email = "titi@tutu.com";
			person.contacts.addItem(contact3);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			contact3.email = "";
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			Assert.assertTrue("Contacts collection dirty", _ctx.meta_getSavedProperties()[person].contacts[0].kind == CollectionEventKind.ADD);
        }
    }
}
