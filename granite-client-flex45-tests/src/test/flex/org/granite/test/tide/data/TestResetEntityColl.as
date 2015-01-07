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
    import mx.collections.ArrayCollection;
    import mx.data.utils.Managed;
    
    import org.flexunit.Assert;
    import org.granite.collections.BasicMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestResetEntityColl
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
		
		[Test]
		public function testResetEntityCollectionGDS991():void {
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
			contact.email = "test1@tutu.com";
			person.contacts.addItem(contact);
			var contact2:Contact = new Contact();
			contact2.id = 2;		
			contact2.version = 0;
			contact2.uid = "C2";
			contact2.person = person;
			contact2.email = "test2@tutu.com";
			person.contacts.addItem(contact2);
			var contact3:Contact = new Contact();
			contact3.id = 3;		
			contact3.version = 0;
			contact3.uid = "C3";
			contact3.person = person;
			contact3.email = "test3@tutu.com";
			person.contacts.addItemAt(contact3, 0);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Saved snapshot", 0, _ctx.meta_getSavedProperties()[person].contacts.length);
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testResetEntityCollection2():void {
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
			contact.email = "test1@tutu.com";
			person.contacts.addItem(contact);
			var contact2:Contact = new Contact();
			contact2.id = 2;		
			contact2.version = 0;
			contact2.uid = "C2";
			contact2.person = person;
			contact2.email = "test2@tutu.com";
			person.contacts.addItem(contact2);
			var contact3:Contact = new Contact();
			contact3.id = 3;		
			contact3.version = 0;
			contact3.uid = "C3";
			contact3.person = person;
			contact3.email = "test3@tutu.com";
			person.contacts.addItem(contact3);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = Person(_ctx.person);			
			
			person.contacts.removeItemAt(1);
			person.contacts.removeItemAt(1);
			person.contacts.removeItemAt(0);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Saved events", 3, _ctx.meta_getSavedProperties()[person].contacts.length);
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Order 1: ", 1, person.contacts.getItemAt(0).id);
			Assert.assertEquals("Order 2: ", 2, person.contacts.getItemAt(1).id);
			Assert.assertEquals("Order 3: ", 3, person.contacts.getItemAt(2).id);
		}
		
		[Test]
		public function testResetEntityCollection3():void {
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
			contact.email = "test1@tutu.com";
			person.contacts.addItem(contact);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = Person(_ctx.person);			
			
			var contact2:Contact = new Contact();
			contact2.id = 2;		
			contact2.version = 0;
			contact2.uid = "C2";
			contact2.person = person;
			contact2.email = "test2@tutu.com";
			person.contacts.addItemAt(contact2, 0);
			var contact3:Contact = new Contact();
			contact3.id = 3;		
			contact3.version = 0;
			contact3.uid = "C3";
			contact3.person = person;
			contact3.email = "test3@tutu.com";
			person.contacts.addItemAt(contact3, 0);			
			person.contacts.removeItemAt(2);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Saved snapshot", 1, _ctx.meta_getSavedProperties()[person].contacts.length);
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Order 1: ", 1, person.contacts.getItemAt(0).id);
		}
		
		[Test]
		public function testResetEntityCollectionSort():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.id = 1;
			contact1.version = 0;
			contact1.person = person;
			person.contacts.addItem(contact1);
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.id = 2;
			contact2.version = 0;
			contact2.person = person;
			person.contacts.addItem(contact2);
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.id = 3;
			contact3.version = 0;
			contact3.person = person;
			person.contacts.addItem(contact3);			
			person = _ctx.person = _ctx.meta_mergeExternal(person);
			
			var c:Contact = person.contacts.removeItemAt(0) as Contact;
			person.contacts.addItemAt(c, 2);
			c = person.contacts.removeItemAt(1) as Contact;
			person.contacts.addItemAt(c, 0);
			
			Assert.assertEquals("Saved snapshot", 3, _ctx.meta_getSavedProperties()[person].contacts.length);
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Order 1: ", 1, person.contacts.getItemAt(0).id);
			Assert.assertEquals("Order 2: ", 2, person.contacts.getItemAt(1).id);
			Assert.assertEquals("Order 3: ", 3, person.contacts.getItemAt(2).id);
		}
		
		[Test]
		public function testResetEntityCollectionSort2a():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.id = 1;
			contact1.version = 0;
			contact1.person = person;
			person.contacts.addItem(contact1);
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.id = 2;
			contact2.version = 0;
			contact2.person = person;
			person.contacts.addItem(contact2);
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.id = 3;
			contact3.version = 0;
			contact3.person = person;
			person.contacts.addItem(contact3);			
			person = _ctx.person = _ctx.meta_mergeExternal(person);
			
			var contact4:Contact = new Contact();
			contact4.uid = "C4";
			contact4.id = 4;
			contact4.version = 0;
			contact4.person = person;
			person.contacts.addItem(contact4);
			
			var c:Contact = person.contacts.removeItemAt(2) as Contact;
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Order 1: ", 1, person.contacts.getItemAt(0).id);
			Assert.assertEquals("Order 2: ", 2, person.contacts.getItemAt(1).id);
			Assert.assertEquals("Order 3: ", 3, person.contacts.getItemAt(2).id);
		}
		
		[Test]
		public function testResetEntityCollectionSort2b():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.id = 1;
			contact1.version = 0;
			contact1.person = person;
			person.contacts.addItem(contact1);
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.id = 2;
			contact2.version = 0;
			contact2.person = person;
			person.contacts.addItem(contact2);
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.id = 3;
			contact3.version = 0;
			contact3.person = person;
			person.contacts.addItem(contact3);			
			person = _ctx.person = _ctx.meta_mergeExternal(person);
			
			var contact4:Contact = new Contact();
			contact4.uid = "C4";
			contact4.id = 4;
			contact4.version = 0;
			contact4.person = person;
			person.contacts.addItem(contact4);
			
			var c:Contact = person.contacts.removeItemAt(2) as Contact;
			person.contacts.addItemAt(c, 1);
			c = person.contacts.removeItemAt(1) as Contact;
			person.contacts.addItemAt(c, 0);
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Order 1: ", 1, person.contacts.getItemAt(0).id);
			Assert.assertEquals("Order 2: ", 2, person.contacts.getItemAt(1).id);
			Assert.assertEquals("Order 3: ", 3, person.contacts.getItemAt(2).id);
		}
		
		[Test]
		public function testResetEntityCollectionSort2c():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.id = 1;
			contact1.version = 0;
			contact1.person = person;
			person.contacts.addItem(contact1);
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.id = 2;
			contact2.version = 0;
			contact2.person = person;
			person.contacts.addItem(contact2);
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.id = 3;
			contact3.version = 0;
			contact3.person = person;
			person.contacts.addItem(contact3);			
			person = _ctx.person = _ctx.meta_mergeExternal(person);
			
			var c:Contact = person.contacts.removeItemAt(2) as Contact;
			person.contacts.addItemAt(c, 1);
			
			var contact4:Contact = new Contact();
			contact4.uid = "C4";
			contact4.id = 4;
			contact4.version = 0;
			contact4.person = person;
			person.contacts.addItem(contact4);
			
			c = person.contacts.removeItemAt(1) as Contact;
			person.contacts.addItemAt(c, 0);
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			Assert.assertEquals("Order 1: ", 1, person.contacts.getItemAt(0).id);
			Assert.assertEquals("Order 2: ", 2, person.contacts.getItemAt(1).id);
			Assert.assertEquals("Order 3: ", 3, person.contacts.getItemAt(2).id);
		}
	}
}
