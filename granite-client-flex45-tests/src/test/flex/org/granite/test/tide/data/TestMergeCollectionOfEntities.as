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
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.meta;
    import org.granite.persistence.PersistentList;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    
    
    public class TestMergeCollectionOfEntities 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeCollectionOfEntities():void {
        	var person:Person = new Person();
        	person.uid = "P01";
        	person.version = 0;
        	person.contacts = new ArrayCollection();
        	var c1:Contact = new Contact();
        	c1.uid = "C01";
        	c1.version = 0;
        	c1.person = person;
        	c1.email = "toto@toto.com";
        	person.contacts.addItem(c1);
        	var c2:Contact = new Contact();
        	c2.uid = "C02";
        	c2.version = 0;
        	c2.person = person;
        	c2.email = "toto@toto.net";
        	person.contacts.addItem(c2);
        	_ctx.person = _ctx.meta_mergeExternal(person);
        	
        	var person2:Person = new Person();
        	person2.uid = "P01";
        	person2.version = 0;
        	person2.contacts = new ArrayCollection();
        	var c21:Contact = new Contact();
        	c21.uid = "C01";
        	c21.version = 0;
        	c21.person = person;
        	c21.email = "toto@toto.com";
        	person2.contacts.addItem(c21);
        	var c22:Contact = new Contact();
        	c22.uid = "C02";
        	c22.version = 0;
        	c22.person = person;
        	c22.email = "toto@toto.net";
        	person2.contacts.addItem(c22);
        	var c23:Contact = new Contact();
        	c23.uid = "C03";
        	c23.version = 0;
        	c23.person = person;
        	c23.email = "toto@toto.org";
        	person2.contacts.addItem(c23);
        	
        	_ctx.meta_mergeExternal(person2, person);
        	
        	Assert.assertEquals("Collection merged", 3, person.contacts.length);
        }
		
		
		[Test]
		public function testMergeCollectionOfEntitiesRemove():void {
			var person0:Person = new Person();
			person0.uid = "P01";
			person0.contacts = new ArrayCollection();
			_ctx.person = person0;
			var c01:Contact = new Contact();
			c01.uid = "C01";
			c01.person = person0;
			c01.email = "toto@toto.com";
			person0.contacts.addItem(c01);
			var c02:Contact = new Contact();
			c02.uid = "C02";
			c02.person = person0;
			c02.email = "toto@toto.net";
			person0.contacts.addItem(c02);

			var person:Person = new Person();
			person.uid = "P01";
			person.version = 0;
			person.contacts = new PersistentSet();
			var c1:Contact = new Contact();
			c1.uid = "C01";
			c1.version = 0;
			c1.person = person;
			c1.email = "toto@toto.com";
			person.contacts.addItem(c1);
			var c2:Contact = new Contact();
			c2.uid = "C02";
			c2.version = 0;
			c2.person = person;
			c2.email = "toto@toto.net";
			person.contacts.addItem(c2);
			_ctx.meta_mergeExternalData(person);
			_ctx.meta_clearCache();	// clear context cache to simulate remote call
			
			var person2:Person = new Person();
			person2.uid = "P01";
			person2.version = 0;
			person2.contacts = new PersistentSet(false);
			var c21:Contact = new Contact();
			c21.uid = "C01";
			c21.version = 0;
			c21.person = person2;
			c21.email = "toto@toto.com";
			
			_ctx.meta_mergeExternalData(person2, null, false, [ c21 ]);
			
			Assert.assertEquals("Removals merged", 1, person.contacts.length);
		}
		
		
		[Test]
		public function testMergeCollectionOfEntities2():void {
			_ctx.meta_uninitializeAllowed = false;
			
			var p:Person = new Person();
			p.uid = "P1";
			p.id = 1;
			p.version = 0;
			p.contacts = new PersistentList();
			var c1:Contact = new Contact();
			c1.uid = "C1";
			c1.id = 1;
			c1.version = 0;
			c1.person = p;
			c1.email = "toto@toto.com";
			p.contacts.addItem(c1);
			var c2:Contact = new Contact();
			c2.uid = "C2";
			c2.id = 2;
			c2.version = 0;
			c2.person = p;
			c2.email = "toto@toto.net";
			p.contacts.addItem(c2);
			_ctx.person = _ctx.meta_mergeExternalData(p);
			p = _ctx.person;
			
			var pb:Person = new Person();
			pb.uid = "P1";
			pb.id = 1;
			pb.version = 0;
			pb.contacts = new PersistentList();
			var pc:Person = new Person();
			pc.uid = "P1";
			pc.id = 1;
			pc.version = 0;
			pc.contacts = new PersistentList(false);
			var c1b:Contact = new Contact();
			c1b.uid = "C1";
			c1b.id = 1;
			c1b.version = 0;
			c1b.person = pb;
			c1b.email = "toto@toto.com";
			pb.contacts.addItem(c1b);
			var c2b:Contact = new Contact();
			c2b.uid = "C2";
			c2b.id = 2;
			c2b.version = 0;
			c2b.person = pb;
			c2b.email = "toto@toto.net";
			pb.contacts.addItem(c2b);
			var c3b:Contact = new Contact();
			c3b.uid = "C3";
			c3b.version = 0;
			c3b.person = pb;
			c3b.email = "toto@toto.org";
			pb.contacts.addItem(c3b);
			var c3c:Contact = new Contact();
			c3c.uid = "C3";
			c3c.version = 0;
			c3c.person = pc;
			c3c.email = "toto@toto.org";
			pb.contacts.addItem(c3c);
			
			_ctx.meta_mergeExternal(pb, p);
			
			Assert.assertStrictlyEquals("Person em", _ctx, p.meta::entityManager);
			for each (var c:Contact in p.contacts)
				Assert.assertStrictlyEquals("Contact attached", p, c.person);
		}
		
		
		[Test]
		public function testMergeCollectionOfEntities3():void {
			_ctx.meta_uninitializeAllowed = false;
			
			var p:Person = new Person();
			p.uid = "P1";
			p.id = 1;
			p.version = 0;
			p.contacts = new PersistentList();
			var c1:Contact = new Contact();
			c1.uid = "C1";
			c1.id = 1;
			c1.version = 0;
			c1.person = p;
			c1.email = "toto@toto.com";
			p.contacts.addItem(c1);
			var c2:Contact = new Contact();
			c2.uid = "C2";
			c2.id = 2;
			c2.version = 0;
			c2.person = p;
			c2.email = "toto@toto.net";
			p.contacts.addItem(c2);
			_ctx.person = _ctx.meta_mergeExternalData(p);
			_ctx.meta_clearCache();
			p = _ctx.person;
			
			var c3:Contact = new Contact();
			c3.uid = "C3";
			c3.person = p;
			c3.email = "toto@toto.org";
			p.contacts.addItem(c3);
			
			var coll:Object = p.contacts;
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			var pb:Person = new Person();
			pb.uid = "P1";
			pb.id = 1;
			pb.version = 0;
			pb.contacts = new PersistentList();
			var pc:Person = new Person();
			pc.uid = "P1";
			pc.id = 1;
			pc.version = 0;
			pc.contacts = new PersistentList(false);
			var c1b:Contact = new Contact();
			c1b.uid = "C1";
			c1b.id = 1;
			c1b.version = 0;
			c1b.person = pb;
			c1b.email = "toto@toto.com";
			pb.contacts.addItem(c1b);
			var c2b:Contact = new Contact();
			c2b.uid = "C2";
			c2b.id = 2;
			c2b.version = 0;
			c2b.person = pb;
			c2b.email = "toto@toto.net";
			pb.contacts.addItem(c2b);
			var c3b:Contact = new Contact();
			c3b.uid = "C3";
			c3b.id = 3;
			c3b.version = 0;
			c3b.person = pb;
			c3b.email = "toto@toto.org";
			pb.contacts.addItem(c3b);
			
			_ctx.meta_mergeExternalData(pb);
			_ctx.meta_clearCache();
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			Assert.assertStrictlyEquals("Same collection", coll, p.contacts);
			
			p.contacts.removeItemAt(0);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testMergeCollectionOfEntities4():void {
			_ctx.meta_uninitializeAllowed = false;
			
			var p:Person = new Person();
			p.uid = "P1";
			p.id = 1;
			p.version = 0;
			p.contacts = new PersistentSet();
			_ctx.person = _ctx.meta_mergeExternalData(p);
			_ctx.meta_clearCache();
			p = _ctx.person;
			
			var c1:Contact = new Contact();
			c1.uid = "C1";
			c1.person = p;
			c1.email = "toto@toto.org";
			p.contacts.addItem(c1);
			
			var coll:Object = p.contacts;
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			var pb:Person = new Person();
			pb.uid = "P1";
			pb.id = 1;
			pb.version = 0;
			pb.contacts = new PersistentSet();
			var c1b:Contact = new Contact();
			c1b.uid = "C1";
			c1b.id = 1;
			c1b.version = 0;
			c1b.person = pb;
			c1b.email = "toto@toto.org";
			pb.contacts.addItem(c1b);
			
			_ctx.meta_mergeExternalData(pb);
			_ctx.meta_clearCache();
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			p.contacts.removeItemAt(0);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testMergeMapOfEntities3():void {
			_ctx.meta_uninitializeAllowed = false;
			
			var p:Person11 = new Person11();
			p.uid = "P1";
			p.id = 1;
			p.version = 0;
			p.map = new PersistentMap(true);
			var k1:Key = new Key();
			k1.uid = "K1";
			k1.id = 1;
			k1.version = 0;
			k1.name = "Key1";
			var v1:Value = new Value();
			v1.uid = "V1";
			v1.id = 1;
			v1.version = 0;
			v1.name = "Value1";
			p.map.put(k1, v1);
			_ctx.person = _ctx.meta_mergeExternalData(p);
			_ctx.meta_clearCache();
			p = _ctx.person;
			
			var k2:Key = new Key();
			k2.uid = "K2";
			k2.name = "Key2";
			var v2:Value = new Value();
			v2.uid = "V2";
			v2.name = "Value2";
			p.map.put(k2, v2);
			
			var map:Object = p.map;
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			var pb:Person11 = new Person11();
			pb.uid = "P1";
			pb.id = 1;
			pb.version = 0;
			pb.map = new PersistentMap();
			var k1b:Key = new Key();
			k1b.uid = "K1";
			k1b.id = 1;
			k1b.version = 0;
			k1b.name = "Key1";
			var v1b:Value = new Value();
			v1b.uid = "V1";
			v1b.id = 1;
			v1b.version = 0;
			v1b.name = "Value1";
			pb.map.put(k1b, v1b);
			var k2b:Key = new Key();
			k2b.uid = "K2";
			k2b.id = 2;
			k2b.version = 0;
			k2b.name = "Key2";
			var v2b:Value = new Value();
			v2b.uid = "V2";
			v2b.id = 2;
			v2b.version = 0;
			v2b.name = "Value2";
			pb.map.put(k2b, v2b);
			
			_ctx.meta_mergeExternalData(pb);
			_ctx.meta_clearCache();
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			Assert.assertStrictlyEquals("Same lao", map, p.map);
			
			p.map.remove(k2);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testMergeCollectionOfEntities5():void {
			_ctx.meta_uninitializeAllowed = false;
			
			var p:Person5 = new Person5();
			p.uid = "P1";
			p.id = 1;
			p.version = 0;
			p.contacts = new PersistentSet();
			_ctx.meta_mergeExternalData(p);
			_ctx.meta_clearCache();
			
			var pa:Person5 = new Person5();
			pa.uid = "P1";
			pa.id = 1;
			pa.version = 1;
			pa.contacts = new PersistentSet();
			
			var c1a:Contact5 = new Contact5();
			c1a.uid = "C1";
			c1a.id = 1;
			c1a.version = 0;
			c1a.person = pa;
			c1a.email = "toto@toto.org";
			var c2a:Contact5 = new Contact5();
			c2a.uid = "C1";
			c2a.id = 1;
			c2a.version = 0;
			c2a.person = pa;
			c2a.email = "toto2@toto.org";
			pa.contacts.addItem(c1a);
			pa.contacts.addItem(c2a);
			
			var updates:Array = [ [ "UPDATE", pa ], [ "PERSIST", c1a ], [ "PERSIST", c2a ] ];
			_ctx.meta_handleUpdates(false, updates);
			
			var pb:Person5 = new Person5();
			pb.uid = "P1";
			pb.id = 1;
			pb.version = 1;
			pb.contacts = new PersistentSet();
			
			var c1b:Contact5 = new Contact5();
			c1b.uid = "C1";
			c1b.id = 1;
			c1b.version = 0;
			c1b.person = pb;
			c1b.email = "toto@toto.org";
			var c2b:Contact5 = new Contact5();
			c2b.uid = "C1";
			c2b.id = 1;
			c2b.version = 0;
			c2b.person = pb;
			c2b.email = "toto2@toto.org";
			
			updates = [ [ "UPDATE", pb ], [ "REMOVE", c1b, "person" ], [ "REMOVE", c2b, "person" ] ];
			_ctx.meta_handleUpdates(false, updates);
			
			Assert.assertNotNull("Person attached", p.meta::entityManager);
		}
		
		[Test]
		public function testMergeCollectionOfEntities6():void {
			_ctx.meta_uninitializeAllowed = false;
			
			var p:Person5 = new Person5();
			p.uid = "P1";
			p.id = 1;
			p.version = 0;
			p.contacts = new PersistentSet();
			p.locations = new PersistentSet();
			_ctx.meta_mergeExternalData(p);
			_ctx.meta_clearCache();
			
			var pa:Person5 = new Person5();
			pa.uid = "P1";
			pa.id = 1;
			pa.version = 1;
			pa.contacts = new PersistentSet();
			pa.locations = new PersistentSet();
			
			var c1a:Contact5 = new Contact5();
			c1a.uid = "C1";
			c1a.id = 1;
			c1a.version = 0;
			c1a.person = pa;
			c1a.email = "toto@toto.org";
			var c2a:Contact5 = new Contact5();
			c2a.uid = "C1";
			c2a.id = 1;
			c2a.version = 0;
			c2a.person = pa;
			c2a.email = "toto2@toto.org";
			pa.contacts.addItem(c1a);
			pa.contacts.addItem(c2a);
			var l1a:Location5 = new Location5();
			l1a.uid = "L1";
			l1a.id = 1;
			l1a.version = 0;
			l1a.person = pa;
			l1a.email = "toto@toto.org";
			var l2a:Location5 = new Location5();
			l2a.uid = "C1";
			l2a.id = 1;
			l2a.version = 0;
			l2a.person = pa;
			l2a.email = "toto2@toto.org";
			pa.locations.addItem(l1a);
			pa.locations.addItem(l2a);
			
			var updates:Array = [ [ "UPDATE", pa ], [ "PERSIST", c1a ], [ "PERSIST", c2a ], [ "PERSIST", l1a ], [ "PERSIST", l2a ] ];
			_ctx.meta_handleUpdates(false, updates);
			
			var pb:Person5 = new Person5();
			pb.uid = "P1";
			pb.id = 1;
			pb.version = 1;
			pb.contacts = new PersistentSet();
			pb.locations = new PersistentSet(false);
			
			var c1b:Contact5 = new Contact5();
			c1b.uid = "C1";
			c1b.id = 1;
			c1b.version = 0;
			c1b.person = pb;
			c1b.email = "toto@toto.org";
			var c2b:Contact5 = new Contact5();
			c2b.uid = "C1";
			c2b.id = 1;
			c2b.version = 0;
			c2b.person = pb;
			c2b.email = "toto2@toto.org";
			
			updates = [ [ "UPDATE", pb ], [ "REMOVE", c1b, "person" ], [ "REMOVE", c2b, "person" ] ];
			_ctx.meta_handleUpdates(false, updates);
			
			Assert.assertNotNull("Person attached", p.meta::entityManager);
			Assert.assertNotNull("Location attached", p.locations.getItemAt(0).meta::entityManager);
		}
    }
}
