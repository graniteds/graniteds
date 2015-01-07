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
    import mx.containers.Panel;
    
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestUIComponentGDS781b
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testUIComponentGDS781b():void {
        	var myPanel1:MyPanel7 = new MyPanel7();
        	UIImpersonator.addChild(myPanel1);
        	var myPanel2:MyPanel7 = new MyPanel7();
			UIImpersonator.addChild(myPanel2);
			var myPanel3:MyPanel7 = new MyPanel7();
			UIImpersonator.addChild(myPanel3);
        	
			_ctx.application.dispatchEvent(new MyEvent());
			
        	Assert.assertTrue("MyPanel1 input1 triggered", Object(myPanel1.getChildAt(0)).triggered);
			Assert.assertTrue("MyPanel1 input2 triggered", Object(myPanel1.getChildAt(1)).triggered);
			Assert.assertTrue("MyPanel2 input1 triggered", Object(myPanel2.getChildAt(0)).triggered);
			Assert.assertTrue("MyPanel2 input2 triggered", Object(myPanel2.getChildAt(1)).triggered);
			Assert.assertTrue("MyPanel3 input1 triggered", Object(myPanel3.getChildAt(0)).triggered);
			Assert.assertTrue("MyPanel3 input2 triggered", Object(myPanel3.getChildAt(1)).triggered);
        	
			UIImpersonator.removeChild(myPanel1);
			UIImpersonator.removeChild(myPanel2);
			UIImpersonator.removeChild(myPanel3);
        }
    }
}
