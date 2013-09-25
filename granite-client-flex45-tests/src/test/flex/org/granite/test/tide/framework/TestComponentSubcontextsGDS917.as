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
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontextsGDS917
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            Tide.getInstance().setComponentGlobal("myEventTriggered", true);
            _ctx = Tide.getInstance().getContext();
            _ctx.myEventTriggered = 0;
			Tide.getInstance().addComponent("com.foo.myPanelB", MyPanelB);
            Tide.getInstance().addComponent("com.foo.bar.myPanelA1", MyPanelA1);
        }
        
        
        [Test]
        public function testComponentSubcontextsGDS917():void {
			var myPanelB:MyPanelB = _ctx["com.foo.myPanelB"] as MyPanelB;
			myPanelB.dispatchEvent(new MyEvent());
			
			Assert.assertEquals("Component foo.bar.A1 not triggered", 0, _ctx["com.foo.bar.myPanelA1"].triggered);
			
			_ctx["com.foo.bar.myPanelB"] = myPanelB;
			myPanelB.dispatchEvent(new MyEvent());
        	
        	Assert.assertEquals("Component foo.bar.A1 triggered", 1, _ctx["com.foo.bar.myPanelA1"].triggered);
        }
    }
}
