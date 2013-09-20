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
    import mx.containers.Panel;
    
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestUIComponent
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testUIComponent():void {
        	var myPanel:MyPanel = new MyPanel();
        	UIImpersonator.addChild(myPanel);
        	var myPanel2:Panel = new MyPanel2();
			UIImpersonator.addChild(myPanel2);
			var myPanel3:Panel = new MyPanel3();
			UIImpersonator.addChild(myPanel3);
        	
        	Assert.assertTrue("MyPanel component", Tide.getInstance().isComponent("myPanel"));
        	Assert.assertStrictlyEquals("MyPanel", _ctx.myPanel, myPanel);
        	Assert.assertStrictlyEquals("MyPanel2", _ctx[Tide.internalUIComponentName(myPanel2)], myPanel2);
			Assert.assertStrictlyEquals("MyPanel3", _ctx[Tide.internalUIComponentName(myPanel3)], myPanel3);
        	
			UIImpersonator.removeChild(myPanel);
			UIImpersonator.removeChild(myPanel2);
			UIImpersonator.removeChild(myPanel3);
        }
    }
}
