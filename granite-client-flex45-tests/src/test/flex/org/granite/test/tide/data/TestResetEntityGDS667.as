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
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
	import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestResetEntityGDS667 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
		[Ignore("GDS-667 Complex fix: RESET event does not include all content")]
        [Test]
        public function testResetEntityGDS667():void {
			var p:Person = new Person();
			p.version = 0;
			p.contacts = new PersistentSet();
			var c:Contact = new Contact();
			c.version = 0;
			c.person = p;
			p.contacts.addItem(c);        	
			_ctx.person = _ctx.meta_mergeExternalData(p);
			var person:Person = _ctx.person;
			
			person.contacts.removeAll();
			
			_ctx.meta_resetEntity(person);
			
			Assert.assertEquals("Person contact collection restored", 1, person.contacts.length);
			Assert.assertStrictlyEquals("Person contact restored", c, person.contacts.getItemAt(0));
        }
    }
}
