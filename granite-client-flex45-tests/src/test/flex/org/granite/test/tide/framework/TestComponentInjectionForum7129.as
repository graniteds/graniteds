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
package org.granite.test.tide.framework
{
    import mx.core.Application;
    
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.seam.Seam;
    
    
    public class TestComponentInjectionForum7129
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Seam.resetInstance();
            _ctx = Seam.getInstance().getContext();
            Seam.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentInjectionForum7129():void {
        	Seam.getInstance().addComponents([MyComponent5, MyPanel5]);
        	
        	_ctx.raiseEvent("testEvent");
			Assert.assertNotNull("Component injected in ctl", _ctx.myComponent5.myQuery);
			Assert.assertNotNull("Panel injected in ctl", _ctx.myComponent5.myPanel5);
			Assert.assertNotNull("Component injected in panel", _ctx.myPanel5.myQuery);
			Assert.assertStrictlyEquals("Same Component", _ctx.myComponent5.myQuery, _ctx.myPanel5.myQuery);
        	
			UIImpersonator.removeChild(_ctx.myPanel5); 
        }
    }
}
