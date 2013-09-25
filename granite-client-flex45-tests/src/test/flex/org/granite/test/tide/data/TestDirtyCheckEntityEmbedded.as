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
    
    import org.flexunit.Assert;
    import org.granite.test.tide.Contact2;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityEmbedded 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        [Test("GDS-819")]
        public function testDirtyCheckEntityEmbedded():void {
        	var person:Person4 = new Person4();
        	var person2:Person4 = new Person4(); 
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	person.version = 0;
			person.address = new EmbeddedAddress();
			person.address.address1 = "toto";
			
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.address.address1 = "tutu";
        	
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	
        	person.address.address1 = "toto";
        	
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
			Assert.assertFalse("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty", personDirty);
        }
		
		[Test("GDS-819 Nested")]
		public function testDirtyCheckEntityNestedEmbedded():void {
			var person:Person4b = new Person4b();
			var person2:Person4b = new Person4b(); 
			
			BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
			BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
			
			person.version = 0;
			person.address = new EmbeddedAddress2();
			person.address.location = new EmbeddedLocation();
			person.address.address1 = "toto";
			person.address.location.city = "test";
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.address.location.city = "truc";
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertTrue("Person dirty 2", personDirty);
			
			person.address.location.city = "test";
			
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
			Assert.assertFalse("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertFalse("Person not dirty", personDirty);
		}
		
		[Test]
		public function testDirtyCheckEntityEmbedded2():void {
			var person:Person4 = new Person4();
			
			BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
			BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
			
			person.version = 0;
			person.id = 1;
			person.uid = "P1";
			person.address = new EmbeddedAddress();
			person.address.address1 = "toto";
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.address.address1 = "tutu";
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertTrue("Person dirty 2", personDirty);
			
			var person2:Person4 = new Person4(); 
			person2.id = 1;
			person2.uid = "P1";
			person2.version = 1;
			person2.address = new EmbeddedAddress();
			person2.address.address1 = "tutu";
			
			_ctx.meta_mergeExternalData(person2);
			
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
			Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertFalse("Person not dirty 2", personDirty);
		}
		
		[Test]
		public function testDirtyCheckEntityEmbedded3():void {
			var person:Person4 = new Person4();
			
			BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
			BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
			
			person.version = 0;
			person.id = 1;
			person.uid = "P1";
			person.address = new EmbeddedAddress();
			person.address.address1 = "toto";
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.address.address1 = "tutu";
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertTrue("Person dirty 2", personDirty);
			
			var person2:Person4 = new Person4(); 
			person2.id = 1;
			person2.uid = "P1";
			person2.version = 0;
			person2.address = new EmbeddedAddress();
			person2.address.address1 = "tutu";
			
			_ctx.meta_mergeExternalData(person2);
			
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
			Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertFalse("Person not dirty", personDirty);
		}
		
		[Test]
		public function testDirtyCheckEntityEmbedded4():void {
			var person:Person12 = new Person12();			
			person.version = 0;
			person.id = 1;
			person.uid = "P1";
			person.contactList = new Contacts11();
			
			var contact:Contact2 = new Contact2();
			contact.version = 0;
			contact.id = 1;
			contact.uid = "C1";
			
			person.contactList.contacts = new ArrayCollection();
			person.contactList.contacts.addItem(contact);
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			Assert.assertFalse("Person not dirty", _ctx.meta_deepDirty(person));
			
			person.contactList.contacts.getItemAt(0).email = "toto@tutu.com";
			
			Assert.assertTrue("Person deep dirty", _ctx.meta_deepDirty(person));
		}
    }
}
