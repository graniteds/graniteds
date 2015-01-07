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
    import mx.binding.utils.ChangeWatcher;
    import mx.collections.ArrayCollection;
    import mx.events.PropertyChangeEvent;
    
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.test.tide.TestEntity;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestManagedEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var changed:String;
        
        [Test]
        public function testManagedEntity():void {
        	var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;        	
			person.contacts = new PersistentSet(false);
			_ctx.person = _ctx.meta_mergeExternal(person);
			var person2:Person = new Person();
			person2.uid = "P2";
			person2.id = 2;
			person2.version = 0;        	
			person2.contacts = new PersistentSet(false);
			_ctx.person2 = _ctx.meta_mergeExternal(person2);
			
			var contact:Contact = new Contact();
			contact.uid = "C1";
			contact.id = 1;
			contact.version = 0;
			_ctx.contact = _ctx.meta_mergeExternal(contact);
			
			person.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler, false, 0, true);
			contact.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler, false, 0, true);
			
			person.firstName = "Toto";
			Assert.assertEquals("String property change from null", "firstName", changed);
			changed = null;
			
			person.firstName = null;
			Assert.assertEquals("String property change to null", "firstName", changed);
			changed = null;
			
			person.firstName = null;
			Assert.assertNull("String property set without change", changed);
			
			person.firstName = "Toto";
			changed = null;			
			person.firstName = "Tutu";
			Assert.assertEquals("String property changed", "firstName", changed);
			changed = null;			
			
			person.age = NaN;
			Assert.assertNull("Number property changed from NaN to NaN", changed);
			changed = null;
			
			person.age = 12;
			Assert.assertTrue("Number property changed from NaN", "age", changed);
			changed = null;
			
			person.age = 67;
			Assert.assertTrue("Number property changed", "age", changed);
			changed = null;
			
			contact.person = person;
			Assert.assertTrue("Entity property changed", "person", changed);
			changed = null;
			
			contact.person = person;
			Assert.assertNull("Entity property not changed", changed);
			
			contact.person = person2;
			Assert.assertTrue("Entity property changed", "person", changed);
			changed = null;
			
			contact.person = null;
			Assert.assertTrue("Entity property changed to null", "person", changed);
			changed = null;
        }
		
		
		private function propertyChangeHandler(event:PropertyChangeEvent):void {
			changed = event.property as String;
		}
		
		
		private var _idChanged:Boolean = false;
		private var _versionChanged:Boolean = false;
		
		[Test]
		public function testEntitySaved():void {
			var test:TestEntity = new TestEntity(NaN, NaN, "TE1");
			BindingUtils.bindSetter(function(val:*):void { 
				if (!isNaN(val)) { _idChanged = true } 
			}, test, "id");
			BindingUtils.bindSetter(function(val:*):void { 
				if (!isNaN(val)) { _versionChanged = true } 
			}, test, "version");
			
			_ctx.test = test;
			
			var test2:TestEntity = new TestEntity(1, 0, "TE1");
			_ctx.meta_mergeExternalData(test2);
			
			Assert.assertTrue("Id changed", _idChanged);
			Assert.assertTrue("Version changed", _versionChanged);
		}
    }
}
