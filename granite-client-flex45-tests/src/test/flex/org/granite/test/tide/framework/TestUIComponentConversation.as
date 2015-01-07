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
    import mx.events.FlexEvent;
    
    import org.flexunit.Assert;
    import org.flexunit.async.Async;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.test.tide.Contact;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestUIComponentConversation
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([ MyComponentConversationView ]);
        }
        
        
        [Test(async)]
		public function testUIComponentSparkConversation():void {
			_ctx.dispatchEvent(new TideUIConversationEvent("testConv", "initConv"));
			var convCtx:BaseContext = Tide.getInstance().getContext("testConv");
			var controller:MyComponentConversationView = convCtx.byType(MyComponentConversationView) as MyComponentConversationView;
			
			Assert.assertTrue("View1 in conv by name", Tide.getInstance().isComponentInConversation("view1"));			
			Assert.assertTrue("View1 in conv registered", convCtx.meta_listensTo(controller.view1));
			
			Async.handleEvent(this, controller.view1, FlexEvent.CREATION_COMPLETE, viewCreated);
		}
		
		private function viewCreated(event:FlexEvent, pass:Object = null):void {
			var convCtx:BaseContext = Tide.getInstance().getContext("testConv");
			var controller:MyComponentConversationView = convCtx.byType(MyComponentConversationView) as MyComponentConversationView;
			
			Assert.assertTrue("View2 in conv by name", Tide.getInstance().isComponentInConversation("view2"));			
			Assert.assertTrue("View2 in conv registered", convCtx.meta_listensTo(controller.view1.view2));
			
			Assert.assertTrue("View3 in conv registered", convCtx.meta_listensTo(controller.view1.view3));
			
			var view4:MySparkViewConv4 = convCtx.byType(MySparkViewConv4) as MySparkViewConv4;
			Assert.assertTrue("View4 in conv registered", convCtx.meta_listensTo(view4));
			
			Assert.assertTrue("View5 in conv registered", convCtx.meta_listensTo(controller.view1.view5));			
		}
    }
}
