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
			
			Assert.assertEquals("Saved snapshot", 0, _ctx.meta_getSavedProperties()[person].contacts.length);
        }

        [Test]
        public function testDirtyCheckEntityCollection1():void {
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
			person.contacts.addItemAt(contact2, 0);

			Assert.assertTrue("Context dirty", _ctx.meta_dirty);

			person.contacts.removeItemAt(1);

			Assert.assertEquals("Saved snapshot", 0, _ctx.meta_getSavedProperties()[person].contacts.length);

            person.contacts.removeItemAt(0);
            Assert.assertFalse("Context dirty", _ctx.meta_dirty);
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
			
			Assert.assertEquals("Saved snapshot", 2, _ctx.meta_getSavedProperties()[person].contacts.length);

            var contact4:Contact = new Contact();
            contact4.id = 1;
            contact4.version = 0;
            contact4.uid = "C1";
            contact4.person = person;
            contact4.email = "toto@tutu.com";
            person.contacts.addItemAt(contact4, 0);

            Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}

        [Test]
        public function testDirtyCheckEntityCollection3():void {
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
            contact.email = "t1@tutu.com";
            person.contacts.addItem(contact);
            var contact2:Contact = new Contact();
            contact2.id = 2;
            contact2.version = 0;
            contact2.uid = "C2";
            contact2.person = person;
            contact2.email = "t2@tutu.com";
            person.contacts.addItem(contact2);
            var contact3:Contact = new Contact();
            contact3.id = 3;
            contact3.version = 0;
            contact3.uid = "C3";
            contact3.person = person;
            contact3.email = "t3@tutu.com";
            person.contacts.addItem(contact3);
            var contact4:Contact = new Contact();
            contact4.id = 4;
            contact4.version = 0;
            contact4.uid = "C4";
            contact4.person = person;
            contact4.email = "t4@tutu.com";
            person.contacts.addItem(contact4);
            var contact5:Contact = new Contact();
            contact5.id = 5;
            contact5.version = 0;
            contact5.uid = "C5";
            contact5.person = person;
            contact5.email = "t5@tutu.com";
            person.contacts.addItem(contact5);
            var contact6:Contact = new Contact();
            contact6.id = 6;
            contact6.version = 0;
            contact6.uid = "C6";
            contact6.person = person;
            contact6.email = "t6@tutu.com";
            person.contacts.addItem(contact6);
            _ctx.person = _ctx.meta_mergeExternalData(person);
            person = Person(_ctx.person);

            var c3:Contact = Contact(person.contacts.removeItemAt(2));

            Assert.assertTrue("Context dirty after remove 1", _ctx.meta_dirty);

            var c6:Contact = Contact(person.contacts.removeItemAt(4));

            Assert.assertTrue("Context dirty after remove 2", _ctx.meta_dirty);

            person.contacts.addItemAt(c3, 2);
            person.contacts.addItemAt(c6, 5);

            Assert.assertFalse("Context dirty", _ctx.meta_dirty);
        }

        [Test]
        public function testDirtyCheckEntityCollection4():void {
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
            contact.email = "t1@tutu.com";
            person.contacts.addItem(contact);
            var contact2:Contact = new Contact();
            contact2.id = 2;
            contact2.version = 0;
            contact2.uid = "C2";
            contact2.person = person;
            contact2.email = "t2@tutu.com";
            person.contacts.addItem(contact2);
            var contact3:Contact = new Contact();
            contact3.id = 3;
            contact3.version = 0;
            contact3.uid = "C3";
            contact3.person = person;
            contact3.email = "t3@tutu.com";
            person.contacts.addItem(contact3);
            var contact4:Contact = new Contact();
            contact4.id = 4;
            contact4.version = 0;
            contact4.uid = "C4";
            contact4.person = person;
            contact4.email = "t4@tutu.com";
            person.contacts.addItem(contact4);
            var contact5:Contact = new Contact();
            contact5.id = 5;
            contact5.version = 0;
            contact5.uid = "C5";
            contact5.person = person;
            contact5.email = "t5@tutu.com";
            person.contacts.addItem(contact5);
            var contact6:Contact = new Contact();
            contact6.id = 6;
            contact6.version = 0;
            contact6.uid = "C6";
            contact6.person = person;
            contact6.email = "t6@tutu.com";
            person.contacts.addItem(contact6);
            _ctx.person = _ctx.meta_mergeExternalData(person);
            person = Person(_ctx.person);

            var c3:Contact = Contact(person.contacts.removeItemAt(2));
            var c6:Contact = Contact(person.contacts.removeItemAt(4));

            person.contacts.addItemAt(c6, 1);
            person.contacts.addItemAt(c3, 4);

            Assert.assertTrue("Context dirty", _ctx.meta_dirty);

            person.contacts.removeItemAt(1);
            person.contacts.removeItemAt(3);

            person.contacts.addItemAt(c6, 4);
            person.contacts.addItemAt(c3, 2);

            Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
        }
    }
}
