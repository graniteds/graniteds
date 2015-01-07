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
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestResetEntityNoVersion
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testResetEntityNoVersion():void {
        	var person:PersonNoVersion = new PersonNoVersion();
        	var contact:ContactNoVersion = new ContactNoVersion();
        	contact.person = person;
        	_ctx.contact = _ctx.meta_mergeExternal(contact);
        	contact = _ctx.contact;
        	_ctx.meta_result(null, null, null, null);
        	contact.person = new PersonNoVersion();
        	_ctx.meta_resetEntity(contact);
        	
        	Assert.assertStrictlyEquals("Entity reset", person, contact.person);
        	
        	var p:PersonNoVersion = new PersonNoVersion();
        	p.contacts = new ArrayCollection();
        	var c:ContactNoVersion = new ContactNoVersion();
        	c.person = p;
        	p.contacts.addItem(c);        	
        	_ctx.person = _ctx.meta_mergeExternal(p);
        	person = _ctx.person;
        	_ctx.meta_result(null, null, null, null);
        	
        	person.contacts.removeItemAt(0);
        	
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertEquals("Person contact collection restored", 1, person.contacts.length);
        	Assert.assertStrictlyEquals("Person contact restored", c, person.contacts.getItemAt(0));
        }
		
		[Test]
		public function testResetEntityNoVersion2():void {
			var person:PersonNoVersion = new PersonNoVersion();
			var contact:ContactNoVersion = new ContactNoVersion();
			contact.person = person;
			_ctx.contact = _ctx.meta_mergeExternalData(contact);
			contact = _ctx.contact;
			
			contact.person = null;
			_ctx.meta_resetEntity(contact);
			
			Assert.assertStrictlyEquals("Entity reset", person, contact.person);
		}
    }
}
