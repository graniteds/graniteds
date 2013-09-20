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
    import org.flexunit.Assert;
    import org.granite.meta;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    
    
    public class TestMergeLazyEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeLazyEntity():void {
        	var person:Person = new Person();
        	person.id = 1; 
        	person.meta::setInitialized(false);
        	var contact:Contact = new Contact();
        	contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	
        	_ctx.contact = _ctx.meta_mergeExternal(contact);
        	contact = _ctx.contact;
        	
        	Assert.assertFalse("Person not initialized", contact.person.meta::isInitialized());
        	
        	var person2:Person = new Person();
        	person2.id = 1;
        	person2.firstName = "Jean";
        	person2.lastName = "Richard";
        	var contact2:Contact = new Contact();
        	contact2.id = 1;
        	contact2.uid = contact.uid;
        	contact2.version = 1;
        	contact2.person = person2;
        	
        	_ctx.meta_mergeExternal(contact2);
        	
        	Assert.assertTrue("Person initialized", contact.person.meta::isInitialized());
        	
        	var person3:Person = new Person();
        	person3.id = 1;
        	person3.meta::setInitialized(false);
        	var contact3:Contact = new Contact();
        	contact3.id = 1;
        	contact3.uid = contact.uid;
        	contact3.version = 2;
        	contact3.person = person3;
        	
        	_ctx.meta_mergeExternal(contact3);
        	
        	Assert.assertTrue("Person still initialized", contact.person.meta::isInitialized());
        }
		
		
		[Test]
		public function testMergeLazyEntity2():void {
			var contact:Contact4 = new Contact4();
			contact.id = 1;
			contact.uid = "C1";
			contact.version = 0;
			
			contact = Contact4(_ctx.meta_mergeExternal(contact));
			
			contact.person = new Person11();
			
			var tmp:BaseContext = _ctx.newTemporaryContext();
			var c:Contact4 = Contact4(tmp.meta_mergeFromContext(_ctx, contact, false, true));
			
			Assert.assertTrue("Person initialized", c.person.meta::isInitialized());
		}
		
		
		[Test]
		public function testMergeLazyEntityColl():void {
			var person:Person = new Person(NaN, NaN, "test", "test");
			var contact:Contact = new Contact(NaN, NaN, person, "test@test.com");
			person.contacts = new PersistentSet();
			person.contacts.addItem(contact);
			
			_ctx.meta_attach(person);
			
			var tmpCtx:BaseContext = _ctx.newTemporaryContext();
			var mergedPerson:Person = tmpCtx.meta_mergeFromContext(_ctx, person, false, true) as Person;
			
			Assert.assertFalse("Person contacts", person.contacts === PersistentCollection(mergedPerson.contacts).object);
		}
    }
}
