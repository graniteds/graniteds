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
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
	import mx.events.PropertyChangeEvent;

import org.granite.persistence.PersistentSet;
import org.granite.test.tide.Contact;

import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
import org.granite.tide.collections.PersistentCollection;


public class TestMergeEntityEmbedded
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeEntityEmbedded():void {
			var a1:EmbeddedAddress = new EmbeddedAddress();
			a1.address1 = "12 Main Street";
        	var p1:Person4 = new Person4();
			p1.id = 1;
			p1.uid = "P1";
			p1.version = 0;
			p1.address = a1;
			p1.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, pcHandler);
			a1.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, pcHandler2);
			_ctx.meta_mergeExternalData(p1);
			var a2:EmbeddedAddress = new EmbeddedAddress();
			a2.address1 = "14 Main Street";
        	var p2:Person4 = new Person4();
			p2.id = 1;
			p2.uid = "P1";
			p2.version = 1;
			p2.address = a2;
        	var p:Person4 = _ctx.meta_mergeExternalData(p2) as Person4;
			
			Assert.assertStrictlyEquals("Embedded address merged", p.address, a1);
			Assert.assertFalse("No event on Person", _addrChanged);
			Assert.assertEquals("Address updated", a1.address1, a2.address1);
			Assert.assertTrue("Event on Address", _addrValueChanged);
        }
		
		private var _addrChanged:Boolean = false;
		private var _addrValueChanged:Boolean = false;
		
		private function pcHandler(event:PropertyChangeEvent):void {
			if (event.property == "address")
				_addrChanged = true;
		}
		private function pcHandler2(event:PropertyChangeEvent):void {
			if (event.property == "address1")
				_addrValueChanged = true;
		}

        [Test(async="true")]
        public function testMergeEmbeddedLazyCollection():void {
            var p1:Person12 = new Person12();
            p1.id = 1;
            p1.uid = "P1";
            p1.version = 0;
            p1.contactList = new Contacts11();
            p1.contactList.contacts = new PersistentSet();
            var c1:Contact = new Contact();
            c1.id = 1;
            c1.uid = "C1";
            c1.version = 0;
            p1.contactList.contacts.addItem(c1);

            var p:Person12 = Person12(_ctx.meta_mergeExternalData(p1));

            Assert.assertTrue("Contacts wrapped", p.contactList.contacts is PersistentCollection);
            Assert.assertStrictlyEquals("Owner is person", p, PersistentCollection(p.contactList.contacts).entity);
            Assert.assertStrictlyEquals("PropertyName", "contactList.contacts", PersistentCollection(p.contactList.contacts).propertyName);


        }
    }

}
