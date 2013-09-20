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
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.math.Long;
    import org.granite.meta;
    import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;


	public class TestMergeEntityLazy
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
			_ctx.meta_uninitializeAllowed = false;
        }
        
        
        [Test]
        public function testMergeEntityLazy():void {
        	var p1:Person9 = new Person9();
			p1.id = 1;
			p1.uid = "P1";
			p1.version = 0;
			p1.lastName = "Test";
			p1 = Person9(_ctx.meta_mergeExternalData(p1));
			
			var c1:Contact3 = new Contact3();
			c1.id = 1;
			c1.uid = "C1";
			c1.version = 0;
			c1.email = "test@test.com";
			var p2:Person9 = new Person9();
			p2.id = 1;
			p2.meta::defineProxy3();
			c1.person = p2;
			var coll:ArrayCollection = new ArrayCollection([c1]);
			var coll2:ArrayCollection = ArrayCollection(_ctx.meta_mergeExternalData(coll));
			
			Assert.assertStrictlyEquals("Contact attached to person", p1, coll2.getItemAt(0).person);
        }
		
		[Test]
		public function testMergeEntityLazyGDS997a():void {
			var p1:Person9 = new Person9();
			p1.id = 1;
			p1.meta::defineProxy3();
			var c1:Contact3 = new Contact3();
			c1.id = 1;
			c1.uid = "C1";
			c1.version = 0;
			c1.email = "test@test.com";
			c1.person = p1;
			c1 = Contact3(_ctx.meta_mergeExternalData(c1));
			
			var p2:Person9 = new Person9();
			p2.id = 1;
			p2.uid = "P1";
			p2.version = 0;
			p2.lastName = "Test";
			var c2:Contact3 = new Contact3();
			c2.id = 1;
			c2.uid = "C1";
			c2.version = 0;
			c2.email = "test@test.com";
			c2.person = p2;
			
			_ctx.meta_mergeExternalData(c2);
			
			Assert.assertEquals("Proxy merged", "P1", p1.uid);
		}
		
		[Test]
		public function testMergeEntityLazyGDS997b():void {
			var p1:Person9b = new Person9b();
			p1.id = new Long(1);
			p1.meta::defineProxy3();
			var c1:Contact3b = new Contact3b();
			c1.id = new Long(1);
			c1.uid = "C1";
			c1.version = new Long(0);
			c1.email = "test@test.com";
			c1.person = p1;
			c1 = Contact3b(_ctx.meta_mergeExternalData(c1));
			
			var p2:Person9b = new Person9b();
			p2.id = new Long(1);
			p2.uid = "P1";
			p2.version = new Long(0);
			p2.lastName = "Test";
			var c2:Contact3b = new Contact3b();
			c2.id = new Long(1);
			c2.uid = "C1";
			c2.version = new Long(0);
			c2.email = "test@test.com";
			c2.person = p2;

			_ctx.meta_mergeExternalData(c2);
			
			Assert.assertEquals("Proxy merged", "P1", p1.uid);
		}
		
		[Test]
		public function testMergeEntityLazyGDS997c():void {
			
			var p1:PersonNoVersion = new PersonNoVersion();
			p1.id = 1;
			p1.uid = "P1";
			p1.lastName = "Test";
			var c1:ContactNoVersion = new ContactNoVersion();
			c1.id = 1;
			c1.uid = "C1";
			c1.email = "test@test.com";
			c1.person = p1;
			c1 = ContactNoVersion(_ctx.meta_mergeExternalData(c1));		
			
			var p2:PersonNoVersion = new PersonNoVersion();
			p2.id = 1;
			p2.meta::defineProxy3();
			var c2:ContactNoVersion = new ContactNoVersion();
			c2.id = 1;
			c2.uid = "C1";
			c2.email = "test@test.com";
			c2.person = p2;
			_ctx.meta_mergeExternalData(c2);
			
			Assert.assertTrue("Proxy ignored", p1.meta::isInitialized());
		}
		
		[Test]
		public function testMergeLazyResult():void {
			
			var p:Patient4c = new Patient4c();
			p.id = 1;
			p.version = 0;
			p.uid = "P1";
			p.diagnosis = new PersistentSet(true);
			p.visits = new PersistentSet(false);
			
			p = _ctx.meta_mergeExternalData(p) as Patient4c;
			
			var d:Diagnosisc = new Diagnosisc();
			d.uid = "D1";
			d.patient = p;
			d.name = "Bla";
			
			p.diagnosis.addItem(d);
			p.diagnosisAssessed = true;
			
			Assert.assertTrue("Patient dirty", _ctx.meta_isEntityChanged(p));
			
			var pb:Patient4c = new Patient4c();
			pb.id = 1;
			pb.version = 0;
			pb.uid = "P1";
			pb.diagnosis = new PersistentSet(false);
			pb.visits = new PersistentSet(true);
			var v1b:Visitc = new Visitc();
			v1b.id = 1;
			v1b.version = 0;
			v1b.uid = "V1";
			v1b.name = "visit";
			v1b.patient = pb;
			pb.visits.addItem(v1b);
			var v2b:Visitc = new Visitc();
			v2b.id = 1;
			v2b.version = 0;
			v2b.uid = "V2";
			v2b.name = "visit";
			v2b.patient = pb;
			pb.visits.addItem(v2b);
			
			_ctx.meta_mergeExternalData(pb);
			
			Assert.assertEquals("Visits", 2, p.visits.length);
			for each (var v:Visitc in p.visits)
				Assert.assertStrictlyEquals(v.patient, p);
		}
    }

}
