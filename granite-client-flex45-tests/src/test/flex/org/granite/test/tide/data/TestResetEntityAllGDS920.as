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
    
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestResetEntityAllGDS920
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testResetEntityGDS920():void {
        	var person:Person = new Person();
			person.id = 1;
			person.uid = "P1";
        	person.version = 0;
			person.contacts = new PersistentSet();
        	var contact:Contact = new Contact();
			contact.id = 1;
			contact.uid = "C1";
        	contact.version = 0;
        	contact.person = person;
			person.contacts.addItem(contact);
			
			var person2:Person = new Person();
			person2.id = 2;
			person2.uid = "P2";
			person2.version = 0;
			person2.contacts = new PersistentSet();
			var contact2:Contact = new Contact();
			contact2.id = 2;
			contact2.uid = "C2";
			contact2.version = 0;
			contact2.person = person2;
			person2.contacts.addItem(contact2);
			
			var p:Array = _ctx.meta_mergeExternalData([person, person2]) as Array;
			person = p[0] as Person;
			person2 = p[1] as Person;

			person.lastName = "test";
			person2.contacts.removeItemAt(0);
			
			Assert.assertTrue("Person dirty", person.meta_dirty);
			Assert.assertTrue("Person2 dirty", person2.meta_dirty);
			
			_ctx.meta_resetAllEntities();
			
			Assert.assertFalse("Person not dirty", person.meta_dirty);
			Assert.assertFalse("Person2 not dirty", person2.meta_dirty);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
        }
    }
}
