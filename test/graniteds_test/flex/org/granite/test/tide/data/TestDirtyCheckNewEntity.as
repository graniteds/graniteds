package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.data.utils.Managed;
    
    import org.flexunit.Assert;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckNewEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
		// [Ignore("Test failed, should consider pull request from A. Busch")]
        [Test("GDS-857")]
        public function testDirtyCheckNewEntity():void {
        	var person:Person = new Person();
        	person.firstName = "toto";			
			_ctx.person = person;
			
			var person2:Person = new Person();
			person2.id = 1;
			person2.uid = person.uid;
			person2.version = 0;
			person.firstName = "toto";
			
			_ctx.meta_mergeExternalData(person2);
			
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
        }
		
		
		[Test]
		public function testDirtyCheckNewEntityAddedToColl():void {
			var person:Person = new Person();
			person.id = 1;
			person.uid = "P1";
			person.version = 0;
			person.firstName = "toto";
			person.contacts = new ArrayCollection();
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = Person(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			var contact:Contact = new Contact();
			contact.uid = "C1";
			contact.person = person;
			person.contacts.addItem(contact);
			
			Assert.assertTrue("Context dirty after new item", _ctx.meta_dirty);
			
			contact.email = "test@test.com";
			
			Assert.assertTrue("Context dirty after item change", _ctx.meta_dirty);
			
			person.contacts.removeItemAt(0);
			
			Assert.assertFalse("Context not dirty after item removed", _ctx.meta_dirty);
		}
    }
}
