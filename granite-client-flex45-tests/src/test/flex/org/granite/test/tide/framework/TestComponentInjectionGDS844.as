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
	import __AS3__.vec.Vector;
	
	import org.flexunit.Assert;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.test.tide.Contact;
    
    
    public class TestComponentInjectionGDS844
    {
        private var _ctx:BaseContext;
        
  		
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			Tide.getInstance().addComponent("myComponentSession", MyComponentInjectGDS844, false);
			Tide.getInstance().addComponent("myComponentConversation", MyComponentInjectGDS844, true);
        }
        
        
        [Test]
        public function testComponentInjectionGDS844():void {
			Assert.assertStrictlyEquals("Session component injected", _ctx.byType(MyService), _ctx.myComponentSession.service);
			_ctx.dispatchEvent(new MyConversationEvent(null));
			var ctx:BaseContext = Tide.getInstance().getContext("test");
			Assert.assertStrictlyEquals("Conversation component injected", ctx.byType(MyService), ctx.myComponentConversation.service);
			ctx.myComponentConversation.end();
			Assert.assertNotNull("Session component disinjected", ctx.myComponentSession.service);
        }
    }
}
