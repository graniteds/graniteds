/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.test.tide.Contact;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Tide;
    
    
    public class TestUIComponentEmbedded
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testUIComponentEmbedded():void {
			var myView1:MyView1 = new MyView1();
        	UIImpersonator.addChild(myView1);
        	
        	_ctx.raiseEvent("testEvent");
			
			Assert.assertEquals("Component registered", 1, _ctx.allByType(MyView2).length);			
			Assert.assertEquals("Event triggered", 1, MyView2.count);
        }
		
		[Test]
		public function testUIComponentSparkEmbedded():void {
			var myView1:MySparkView1 = new MySparkView1();
			UIImpersonator.addChild(myView1);
			
			_ctx.raiseEvent("testEvent");
			
			Assert.assertEquals("Component registered", 1, _ctx.allByType(MySparkView2).length);			
			Assert.assertEquals("Event triggered", 1, MySparkView2.count);
		}
    }
}
