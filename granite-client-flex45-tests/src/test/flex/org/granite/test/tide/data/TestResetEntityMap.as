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
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestResetEntityMap
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testResetEntityMap():void {
        	var person:Person11 = new Person11();
        	person.version = 0;
        	person.lastName = "Toto";
			person.map = new BasicMap();
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
        	person.map.put(2, "toto");
			
			Assert.assertTrue("Person dirty", _ctx.meta_dirty);
			
        	Managed.resetEntity(person);
        	
			Assert.assertEquals("Person map reset", 0, person.map.length);
		}
		
		[Test]
		public function testResetEntityMap2():void {
			var person:Person11 = new Person11();
			person.version = 0;
			person.lastName = "Toto";
			person.map = new BasicMap();
			person.map.put(2, "toto");
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			person.map.put(2, "tutu");
			
			Assert.assertTrue("Person dirty", _ctx.meta_dirty);
			
			Managed.resetEntity(person);
			
			Assert.assertEquals("Person map reset", 1, person.map.length);
			Assert.assertEquals("Person map value", "toto", person.map.get(2));
		}
		
		[Test]
		public function testResetEntityMap3():void {
			var person:Person11 = new Person11();
			person.version = 0;
			person.lastName = "Toto";
			person.map = new BasicMap();
			var value:Value = new Value("toto");
			person.map.put(2, value);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			value = person.map.get(2);
			
			value.name = "tutu";
			
			Assert.assertTrue("Person dirty", _ctx.meta_dirty);
			
			Managed.resetEntity(person);
			
			Assert.assertEquals("Person map reset", 1, person.map.length);
			Assert.assertEquals("Person map value", "toto", Value(person.map.get(2)).name);
		}
		
		[Test]
		public function testResetEntityMap4():void {
			var person:Person11 = new Person11();
			person.version = 0;
			person.lastName = "Toto";
			person.map = new BasicMap();
			var value:Value = new Value("toto");
			person.map.put(2, value);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			person.map.put(2, new Value("tutu"));
			person.map.put(3, new Value("tata"));
			
			Assert.assertTrue("Person dirty", _ctx.meta_dirty);
			Assert.assertEquals("Person map reset", 2, person.map.length);
			Assert.assertEquals("Person map value", "tutu", Value(person.map.get(2)).name);
			
			Managed.resetEntity(person);
			
			Assert.assertEquals("Person map reset", 1, person.map.length);
			Assert.assertEquals("Person map value", "toto", Value(person.map.get(2)).name);
		}
		
		[Test]
		public function testResetEntityMap5():void {
			var person:Person11 = new Person11();
			person.version = 0;
			person.lastName = "Toto";
			person.map = new BasicMap();
			var value:Value = new Value("toto");
			person.map.put(2, value);
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			var toto:Value = Value(person.map.get(2));
			
			person.map.put(2, new Value("tutu"));
			
			Assert.assertTrue("Person dirty", _ctx.meta_dirty);
			Assert.assertEquals("Person map reset", 1, person.map.length);
			Assert.assertEquals("Person map value", "tutu", Value(person.map.get(2)).name);
			
			person.map.put(2, new Value("tata"));
			person.map.put(2, toto);
			
			Assert.assertFalse("Person dirty", _ctx.meta_dirty);
			
			person.map.put(2, new Value("tata"));
			Assert.assertTrue("Person dirty", _ctx.meta_dirty);
			Managed.resetEntity(person);
			
			Assert.assertEquals("Person map value", "toto", Value(person.map.get(2)).name);
			Assert.assertFalse("Person dirty", _ctx.meta_dirty);
		}
    }
}
