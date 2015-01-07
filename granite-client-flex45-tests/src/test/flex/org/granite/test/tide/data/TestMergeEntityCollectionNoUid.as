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
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.test.tide.Person0;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestMergeEntityCollectionNoUid
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
		[Test]
		public function testMergeEntityCollectionNoUid():void {
			var changes:int = 0;
			
			var p1:PersonNoUid = new PersonNoUid(1, "P1");
			var c1a:ContactNoUid = new ContactNoUid(1, "a@c.com");
			c1a.person = p1;
			p1.contacts = new PersistentSet();
			p1.contacts.addItem(c1a);
			p1 = _ctx.meta_mergeExternalData(p1) as PersonNoUid;
			
			var c1b:ContactNoUid = new ContactNoUid(NaN, "b@c.com");
			c1b.person = p1;
			p1.contacts.addItem(c1b);
			
			var p2:PersonNoUid = new PersonNoUid(1, "P1");
			var c2a:ContactNoUid = new ContactNoUid(1, "a@c.com");
			c2a.person = p2;
			var c2b:ContactNoUid = new ContactNoUid(2, "b@c.com");
			c2b.person = p2;
			p2.contacts = new PersistentSet();
			p2.contacts.addItem(c2a);
			p2.contacts.addItem(c2b);
			_ctx.meta_mergeExternalData(p2);
			
			Assert.assertEquals("Contacts", 2, p1.contacts.length);
		}
	}
}
