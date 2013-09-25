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
package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentOutjectionGDS427
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentOutjectionGDS427():void {
        	Tide.getInstance().addComponent("injected1", MyComponentInject2);
        	Tide.getInstance().addComponent("injected2", MyComponentInject2);
        	
        	Tide.getInstance().addComponents([MyComponentOutject2]);
        	
        	var injected1:MyComponentInject2 = _ctx.injected1;
        	var injected2:MyComponentInject2 = _ctx.injected2;
        	
        	_ctx.raiseEvent("createContact");
        	Assert.assertStrictlyEquals("Contact created", injected1.contact, _ctx.myComponentOutject2.contact);
        	Assert.assertStrictlyEquals("Contact created", injected2.contact, _ctx.myComponentOutject2.contact);
        	
        	_ctx.raiseEvent("updateContact");
        	Assert.assertStrictlyEquals("Contact updated", injected1.contact, _ctx.myComponentOutject2.contact);
        	Assert.assertStrictlyEquals("Contact updated", injected2.contact, _ctx.myComponentOutject2.contact);
        	
        	_ctx.raiseEvent("updateContact");
        	Assert.assertStrictlyEquals("Contact updated 2", injected2.contact, _ctx.myComponentOutject2.contact);
        	Assert.assertStrictlyEquals("Contact updated 2", injected2.contact, _ctx.myComponentOutject2.contact);
        }
    }
}
