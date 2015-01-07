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
package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.ns.tide;
    
    use namespace tide;
    
    
    public class TestComponentOutjection
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentOutjection():void {
        	Tide.getInstance().addComponents([MyComponentOutject, MyComponentNSOutject]);
        	
        	var contact2:Contact = new Contact();
        	_ctx.inoutContact2 = contact2;
        	
        	_ctx.raiseEvent("createContact");
        	Assert.assertStrictlyEquals("Contact created", _ctx.contact, _ctx.myComponentOutject.contact);
        	Assert.assertStrictlyEquals("Contact NS created", _ctx.nscontact, _ctx.myComponentNSOutject.tide::nscontact);
        	
        	Assert.assertNotNull("Contact inout 1", _ctx.inoutContact1);
        	Assert.assertStrictlyEquals("Contact inout 1", _ctx.inoutContact1, _ctx.myComponentOutject.inoutContact1);
        	
        	Assert.assertStrictlyEquals("Contact inout 2", _ctx.inoutContact2, contact2);
        	Assert.assertStrictlyEquals("Contact inout 2", _ctx.inoutContact2, _ctx.myComponentOutject.inoutContact2);
        	
        	Assert.assertNull("Contact inout 3", _ctx.inoutContact3);
        	Assert.assertNull("Contact inout 3", _ctx.myComponentOutject.inoutContact3);
        }
    }
}
