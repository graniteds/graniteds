/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
		
		[Test]
		public function testDirtyCheckNewEntityAddedToCollReset():void {
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
    }
}
