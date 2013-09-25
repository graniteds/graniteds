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
    import mx.data.utils.Managed;
    
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        [Test]
        public function testDirtyCheckEntity():void {
        	var person:Person = new Person();
        	var person2:Person = new Person(); 
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	person.contacts = new ArrayCollection();
        	person.version = 0;
        	var contact:Contact = new Contact();
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	person2.version = 0;
        	_ctx.contact = _ctx.meta_mergeExternal(contact);
        	_ctx.person2 = _ctx.meta_mergeExternal(person2);
        	contact = _ctx.contact;
        	person2 = _ctx.person2;
        	contact.person.firstName = 'toto';
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(contact.person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", _ctx.meta_dirty);
        	Assert.assertTrue("Context dirty 2", ctxDirty);
        	Assert.assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	contact.person.firstName = null;
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(contact.person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
        	Assert.assertFalse("Context not dirty 2", ctxDirty);
        	Assert.assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	var contact2:Contact = new Contact();
        	contact2.version = 0;
        	contact2.person = person;
        	person.contacts.addItem(contact2);
        	        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(contact.person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", _ctx.meta_dirty);
        	Assert.assertTrue("Context dirty 2", ctxDirty);
        	Assert.assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	person.contacts.removeItemAt(1);
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(contact.person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
        	Assert.assertFalse("Context not dirty 2", ctxDirty);
        	Assert.assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	contact.email = "toto";
        	person2.lastName = "tutu";
        	
        	Assert.assertTrue("Contact dirty", _ctx.meta_isEntityChanged(contact));
        	Assert.assertTrue("Person 2 dirty", _ctx.meta_isEntityChanged(person2));
        	
        	var receivedPerson:Person = new Person();
        	receivedPerson.version = 1;
        	receivedPerson.uid = person.uid;
        	var receivedContact:Contact = new Contact();
        	receivedContact.version = 1;
        	receivedContact.uid = contact.uid;
        	receivedContact.person = receivedPerson;
        	receivedPerson.contacts = new ArrayCollection();
        	receivedPerson.contacts.addItem(receivedContact);
        	
        	_ctx.meta_result(null, null, null, receivedPerson);
        	
        	Assert.assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	Assert.assertTrue("Person 2 dirty", _ctx.meta_isEntityChanged(person2));
        	Assert.assertTrue("Context dirty", _ctx.meta_dirty);
        	
        	receivedPerson = new Person();
        	receivedPerson.version = 1;
        	receivedPerson.uid = person2.uid;
        	
        	_ctx.meta_result(null, null, null, receivedPerson);
        	Assert.assertFalse("Person 2 dirty", _ctx.meta_isEntityChanged(person2));
        	Assert.assertFalse("Context dirty", _ctx.meta_dirty);
        }
		
		[Test]
		public function testDirtyCheckEntity2():void {
			var person:Person13 = new Person13();
			
			BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
			BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
			
			person.version = 0;
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			person.testNum = 0;
			
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertTrue("Person dirty 2", personDirty);
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			person.testNum = NaN;
			
			Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertFalse("Person not dirty 2", personDirty);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			Assert.assertFalse("Context not dirty 2", ctxDirty);
			
			person.testNum = 10;
			
			Assert.assertTrue("Person testNum dirty", _ctx.meta_isEntityPropertyChanged(person, "testNum", person.testNum));
			
			person.testNum = NaN;
			person.lastName = "Test";
			
			var changed:Boolean = _ctx.meta_isEntityPropertyChanged(person, "testNum", person.testNum);
			Assert.assertFalse("Person testNum dirty", changed);
		}
		
		[Test]
		public function testDirtyCheckEntityAddedToCollReset():void {
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
			
			Managed.resetEntity(person);
			
			Assert.assertFalse("Context not dirty after item removed", _ctx.meta_dirty);
		}

        [Test]
        public function testDirtyCheckEntityMap():void {
            var person:Person9 = new Person9();
            person.id = 1;
            person.uid = "P1";
            person.version = 0;
            person.firstName = "toto";
            person.contacts = new PersistentSet(true);
            person.testMap = new PersistentMap(true);
            _ctx.person = _ctx.meta_mergeExternalData(person);
            person = Person9(person);

            Assert.assertFalse("Context not dirty", _ctx.meta_dirty);

            person.testMap.put("test", "test");

            Assert.assertTrue("Context dirty after put", _ctx.meta_dirty);

            Managed.resetEntity(person);

            Assert.assertFalse("Context not dirty after reset", _ctx.meta_dirty);
        }

        [Test]
        public function testDirtyCheckEntityMap2():void {
            var person:Person9 = new Person9();
            person.id = 1;
            person.uid = "P1";
            person.version = 0;
            person.firstName = "toto";
            person.contacts = new PersistentSet(true);
            person.testMap = new PersistentMap(true);
            person.testMap.put("test", "test");
            _ctx.person = _ctx.meta_mergeExternalData(person);
            person = Person9(person);

            Assert.assertFalse("Context not dirty", _ctx.meta_dirty);

            person.testMap.put("test", "toto");

            Assert.assertTrue("Context dirty after put", _ctx.meta_dirty);

            Managed.resetEntity(person);
			
            Assert.assertFalse("Context not dirty after reset", _ctx.meta_dirty);
            Assert.assertEquals("Map reset", "test", person.testMap.get("test"));
        }
		
		
		public var person1Dirty:Boolean = false;
		public var person2Dirty:Boolean = false;
		
		[Test]
		public function testDirtyCheckEntityPartial():void {
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
			
			BindingUtils.bindProperty(this, "person1Dirty", _ctx,
				{ name: "meta_deepDirty", getter: function(ctx:BaseContext):Boolean { return ctx.meta_deepDirty(person1); } }
			);
			BindingUtils.bindProperty(this, "person2Dirty", _ctx,
				{ name: "meta_deepDirty", getter: function(ctx:BaseContext):Boolean { return ctx.meta_deepDirty(person2); } }
			);
			
			Assert.assertFalse("Person 1 not dirty", _ctx.meta_deepDirty(person1));
			Assert.assertFalse("Person 2 not dirty", _ctx.meta_deepDirty(person2));
			Assert.assertFalse("Person 1 not dirty", person1Dirty);
			Assert.assertFalse("Person 2 not dirty", person2Dirty);
			
			person2.lastName = "toto";
			
			Assert.assertFalse("Person 1 not dirty", _ctx.meta_deepDirty(person1));
			Assert.assertTrue("Person 2 dirty", _ctx.meta_deepDirty(person2));
			Assert.assertFalse("Person 1 not dirty", person1Dirty);
			Assert.assertTrue("Person 2 dirty", person2Dirty);
			
			person2.lastName = null;
			
			Assert.assertFalse("Person 1 not dirty", _ctx.meta_deepDirty(person1));
			Assert.assertFalse("Person 2 not dirty", _ctx.meta_deepDirty(person2));
			Assert.assertFalse("Person 1 not dirty", person1Dirty);
			Assert.assertFalse("Person 2 not dirty", person2Dirty);
			
			person1.contacts.getItemAt(0).email = "toto@toto.com";
			
			Assert.assertTrue("Person 1 deep dirty", _ctx.meta_deepDirty(person1));
			Assert.assertFalse("Person 2 not dirty", _ctx.meta_deepDirty(person2));
			Assert.assertTrue("Person 1 deep dirty", person1Dirty);
			Assert.assertFalse("Person 2 not dirty", person2Dirty);
		}
	}
}
