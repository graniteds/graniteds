/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
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
        private var _ctx:Context;
        
        
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
			contact1 = person1.contacts.getItemAt(0) as Contact;
			
			person2.uid = "P2";
			person2.contacts = new ArrayCollection();
			person2.version = 0;
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.version = 0;
			contact2.person = person2;
			person2.contacts.addItem(contact2);
			_ctx.person2 = _ctx.meta_mergeExternalData(person2);
			person2 = _ctx.person2;
			contact2 = person2.contacts.getItemAt(0) as Contact;
			
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
			
			person2.lastName = null;
			
			Assert.assertFalse("Save all disabled", form.saveAllButton.enabled);
			Assert.assertFalse("Save enabled", form.saveButton.enabled);
			
			contact1.email = "toto@tutu.com";
			
			Assert.assertTrue("Save all enabled", form.saveAllButton.enabled);
			Assert.assertTrue("Save enabled", form.saveAllButton.enabled);
			
			contact1.email = null;
			contact2.email = "test@tutu.com";
			
			Assert.assertTrue("Save all enabled", form.saveAllButton.enabled);
			Assert.assertFalse("Save disabled", form.saveButton.enabled);
			
			contact1.email = "toto@tutu.com";
			
			Assert.assertTrue("Save all enabled", form.saveAllButton.enabled);
			Assert.assertTrue("Save enabled", form.saveButton.enabled);

			contact1.email = null;
			contact2.email = null;
			
			Assert.assertFalse("Save all disabled", form.saveAllButton.enabled);
			Assert.assertFalse("Save enabled", form.saveButton.enabled);
		}
	}
}
