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
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestComponentInjectionForum4314
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentInjectionForum4314():void {
        	Tide.getInstance().addComponents([MyComponent3, MyComponentConversation3]);
        	
        	_ctx.raiseEvent("testGlobal");
        	_ctx.myComponent3a.dispatchEvent(new TideUIConversationEvent("Test", "test"));
        	
			Assert.assertEquals("Inject", "test", Tide.getInstance().getContext("Test").myComponent3b.injected);
        	
        	_ctx.myComponent3a.testInjectGlobal = "test2";
        	
			Assert.assertEquals("Inject 2", "test2", Tide.getInstance().getContext("Test").myComponent3b.testInjectGlobal);
        }
		
		[Test]
		public function testComponentInjectionGlobal():void {
			Tide.getInstance().addComponents([MyComponentConversation3]);
			
			_ctx.dispatchEvent(new TideUIConversationEvent("Test", "test"));
			
			Assert.assertNull("Inject", Tide.getInstance().getContext("Test").myComponent3b.testInjectGlobal);
			
			_ctx.testInjectGlobal = "test";
			
			Assert.assertEquals("Inject 2", _ctx.testInjectGlobal, Tide.getInstance().getContext("Test").myComponent3b.testInjectGlobal);
		}
		
		[Test]
		public function testComponentInjectionGlobal2():void {
			Tide.getInstance().addComponents([MyComponentConversation4]);
			
			_ctx.dispatchEvent(new TideUIConversationEvent("Test", "test"));
			
			Assert.assertNull("Inject", Tide.getInstance().getContext("Test").myComponent4b.testInjectGlobal);
			
			_ctx.testInjectGlobal = new Person();
			
			Assert.assertStrictlyEquals("Inject 2", _ctx.testInjectGlobal, Tide.getInstance().getContext("Test").myComponent4b.testInjectGlobal);
		}
    }
}
