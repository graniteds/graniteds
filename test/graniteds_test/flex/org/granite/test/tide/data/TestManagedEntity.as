package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.binding.utils.ChangeWatcher;
    import mx.collections.ArrayCollection;
    import mx.events.PropertyChangeEvent;
    
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestManagedEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var changed:String;
        
        [Test]
        public function testManagedEntity():void {
        	var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;        	
			person.contacts = new PersistentSet(false);
			_ctx.person = _ctx.meta_mergeExternal(person);
			var person2:Person = new Person();
			person2.uid = "P2";
			person2.id = 2;
			person2.version = 0;        	
			person2.contacts = new PersistentSet(false);
			_ctx.person2 = _ctx.meta_mergeExternal(person2);
			
			var contact:Contact = new Contact();
			contact.uid = "C1";
			contact.id = 1;
			contact.version = 0;
			_ctx.contact = _ctx.meta_mergeExternal(contact);
			
			person.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler, false, 0, true);
			contact.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler, false, 0, true);
			
			person.firstName = "Toto";
			Assert.assertEquals("String property change from null", "firstName", changed);
			changed = null;
			
			person.firstName = null;
			Assert.assertEquals("String property change to null", "firstName", changed);
			changed = null;
			
			person.firstName = null;
			Assert.assertNull("String property set without change", changed);
			
			person.firstName = "Toto";
			changed = null;			
			person.firstName = "Tutu";
			Assert.assertEquals("String property changed", "firstName", changed);
			changed = null;			
			
			person.age = NaN;
			Assert.assertNull("Number property changed from NaN to NaN", changed);
			changed = null;
			
			person.age = 12;
			Assert.assertTrue("Number property changed from NaN", "age", changed);
			changed = null;
			
			person.age = 67;
			Assert.assertTrue("Number property changed", "age", changed);
			changed = null;
			
			contact.person = person;
			Assert.assertTrue("Entity property changed", "person", changed);
			changed = null;
			
			contact.person = person;
			Assert.assertNull("Entity property not changed", changed);
			
			contact.person = person2;
			Assert.assertTrue("Entity property changed", "person", changed);
			changed = null;
			
			contact.person = null;
			Assert.assertTrue("Entity property changed to null", "person", changed);
			changed = null;
        }
		
		
		private function propertyChangeHandler(event:PropertyChangeEvent):void {
			changed = event.property as String;
		}
    }
}
