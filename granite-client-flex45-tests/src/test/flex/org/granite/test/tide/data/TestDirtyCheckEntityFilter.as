/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
			person.contacts.addItem(contact);
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
			Assert.assertEquals("Contacts collection snapshot", 2, _ctx.meta_getSavedProperties()[person].contacts.length);
        }
		
		[Test]
		public function testDirtyCheckNewEntityAddedToFilteredColl():void {
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
			contact2.email = "";
			person.contacts.addItem(contact2);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person = Person(_ctx.person);
			person.contacts.filterFunction = function(item:Contact):Boolean {
				return item.email != "";
			};
			person.contacts.refresh();
			
			// Should not be dirty after filtering
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.person = person;
			contact3.email = "test@test.com";
			person.contacts.addItem(contact3);
			
			Assert.assertTrue("Context dirty after new item", _ctx.meta_dirty);
			
			contact3.email = "toto@tutu.com";
			
			Assert.assertTrue("Context dirty after item change", _ctx.meta_dirty);
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty after item removed", _ctx.meta_dirty);
		}
		
		[Test]
		public function testDirtyCheckNewEntityAddedToFilteredColl2():void {
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
			contact2.email = "";
			person.contacts.addItem(contact2);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person = Person(_ctx.person);
			person.contacts.filterFunction = function(item:Contact):Boolean {
				return item.email != "";
			};
			person.contacts.refresh();
			
			// Should not be dirty after filtering
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.person = person;
			contact3.email = "test@test.com";
			person.contacts.addItem(contact3);
			
			Assert.assertTrue("Context dirty after new item", _ctx.meta_dirty);
			
			contact3.email = "toto@tutu.com";
			
			Assert.assertTrue("Context dirty after item change", _ctx.meta_dirty);
			
			Managed.resetEntity(person);
			
			// Ensure dirty checking is still enabled
			var c:Contact = Contact(person.contacts.getItemAt(0));
			Assert.assertFalse("First element not dirty", c.meta_dirty);
			
			c.email = "toto@toto.com";
			
			Assert.assertTrue("First element dirty", c.meta_dirty);
			
			Managed.resetEntity(c);
			
			Assert.assertFalse("Context not dirty after item removed", _ctx.meta_dirty);
		}
    }
}
