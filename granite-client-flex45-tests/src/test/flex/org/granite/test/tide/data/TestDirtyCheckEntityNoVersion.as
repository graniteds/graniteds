/**
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
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityNoVersion
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
        	var person:PersonNoVersion = new PersonNoVersion();
        	var person2:PersonNoVersion = new PersonNoVersion(); 
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	person.contacts = new ArrayCollection();
        	var contact:ContactNoVersion = new ContactNoVersion();
        	contact.person = person;
        	person.contacts.addItem(contact);
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
        	
			contact.person.firstName = 'tata';
			
        	contact.person.firstName = null;
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(contact.person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
        	Assert.assertFalse("Context not dirty 2", ctxDirty);
        	Assert.assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	
        	var contact2:ContactNoVersion = new ContactNoVersion();
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
        	
        	var receivedPerson:PersonNoVersion = new PersonNoVersion();
        	receivedPerson.uid = person.uid;
        	var receivedContact:ContactNoVersion = new ContactNoVersion();
        	receivedContact.uid = contact.uid;
        	receivedContact.person = receivedPerson;
        	receivedPerson.contacts = new ArrayCollection();
        	receivedPerson.contacts.addItem(receivedContact);
        	
        	_ctx.meta_result(null, null, null, receivedPerson);
        	
        	Assert.assertFalse("Contact not dirty", _ctx.meta_isEntityChanged(contact));
        	Assert.assertTrue("Person 2 dirty", _ctx.meta_isEntityChanged(person2));
        	Assert.assertTrue("Context dirty", _ctx.meta_dirty);
        	
        	receivedPerson = new PersonNoVersion();
        	receivedPerson.uid = person2.uid;
        	
        	_ctx.meta_result(null, null, null, receivedPerson);
        	Assert.assertFalse("Person 2 dirty", _ctx.meta_isEntityChanged(person2));
        	Assert.assertFalse("Context dirty", _ctx.meta_dirty);
        }
		
		[Test]
		public function testDirtyCheckEntityAddedToCollReset():void {
			var person:PersonNoVersion = new PersonNoVersion();
			person.id = 1;
			person.uid = "P1";
			person.firstName = "toto";
			person.contacts = new ArrayCollection();
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = PersonNoVersion(person);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			var contact:ContactNoVersion = new ContactNoVersion();
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
            var person:PersonNoVersion = new PersonNoVersion();
            person.id = 1;
            person.uid = "P1";
            person.firstName = "toto";
            person.contacts = new PersistentSet(true);
            person.testMap = new PersistentMap(true);
            _ctx.person = _ctx.meta_mergeExternalData(person);
            person = PersonNoVersion(person);

            Assert.assertFalse("Context not dirty", _ctx.meta_dirty);

            person.testMap.put("test", "test");

            Assert.assertTrue("Context dirty after put", _ctx.meta_dirty);

            Managed.resetEntity(person);

            Assert.assertFalse("Context not dirty after reset", _ctx.meta_dirty);
        }

        [Test]
        public function testDirtyCheckEntityMap2():void {
            var person:PersonNoVersion = new PersonNoVersion();
            person.id = 1;
            person.uid = "P1";
            person.firstName = "toto";
            person.contacts = new PersistentSet(true);
            person.testMap = new PersistentMap(true);
            person.testMap.put("test", "test");
            _ctx.person = _ctx.meta_mergeExternalData(person);
            person = PersonNoVersion(person);

            Assert.assertFalse("Context not dirty", _ctx.meta_dirty);

            person.testMap.put("test", "toto");

            Assert.assertTrue("Context dirty after put", _ctx.meta_dirty);

            Managed.resetEntity(person);

            Assert.assertFalse("Context not dirty after reset", _ctx.meta_dirty);
            Assert.assertEquals("Map reset", "test", person.testMap.get("test"));
        }
    }
}
