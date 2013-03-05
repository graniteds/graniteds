package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.core.mx_internal;
    import mx.data.utils.Managed;
    import mx.events.FlexEvent;
	import mx.binding.PropertyWatcher;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.spring.Context;
    import org.granite.tide.spring.Spring;
    
    
    public class TestDirtyCheckEntityUI
    {
        private var _ctx:Context = Spring.getInstance().getSpringContext();
        
        
        [Before]
        public function setUp():void {
            Spring.resetInstance();
            _ctx = Spring.getInstance().getSpringContext();
        }
        
        
		[Test]
		public function testDirtyCheckEntityUI():void {
			var person1:Person = new Person();
			var person2:Person = new Person(); 
			
			person1.uid = "P1";
			person1.contacts = new ArrayCollection();
			person1.version = 0;
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.version = 0;
			contact1.person = person1;
			person1.contacts.addItem(contact1);
			_ctx.person1 = _ctx.meta_mergeExternalData(person1);
			person1 = _ctx.person1;
			
			person2.uid = "P2";
			person2.contacts = new ArrayCollection();
			_ctx.person2 = _ctx.meta_mergeExternalData(person2);
			person2 = _ctx.person2;
			
			var form:TestForm = new TestForm();
			_ctx.testForm = form;
			UIImpersonator.addChild(form);
			
			Assert.assertFalse("Save all disabled", form.saveAllButton.enabled);
			Assert.assertFalse("Save enabled", form.saveButton.enabled);
			
			person1.lastName = "toto";
			
			Assert.assertTrue("Save all enabled", form.saveAllButton.enabled);
			Assert.assertTrue("Save enabled", form.saveAllButton.enabled);
			
			person1.lastName = null;
			person2.lastName = "test";
			
			Assert.assertTrue("Save all enabled", form.saveAllButton.enabled);
			Assert.assertFalse("Save disabled", form.saveButton.enabled);
		}
	}
}
