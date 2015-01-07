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
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentNestedConversations2
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentConversation, MyComponentConversation2, MyComponentConversation2b]);
        }
        
        
        [Test]
        public function testComponentNestedConversations2():void {        	
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("1", "start"));
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("2", "start"));

        	var ctx1:BaseContext = Tide.getInstance().getContext("1", null, false);
        	var ctx2:BaseContext = Tide.getInstance().getContext("2", null, false);
        	
        	Assert.assertNotNull("Context 1", ctx1.meta_getInstance("myComponent", false, true));
        	Assert.assertNotNull("Context 2", ctx2.meta_getInstance("myComponent", false, true));
        	Assert.assertFalse("Context 1/2 components", ctx1.myComponent === ctx2.myComponent);
        	
        	ctx1.myComponent.dispatchEvent(new TideUIConversationEvent("1.1", "next"));
        	ctx2.myComponent.dispatchEvent(new TideUIConversationEvent("2.1", "next"));
        	
        	var ctx11:BaseContext = Tide.getInstance().getContext("1.1", null, false);
        	var ctx21:BaseContext = Tide.getInstance().getContext("2.1", null, false);
        	
        	Assert.assertNotNull("Context 1.1 component 2", ctx11.meta_getInstance("myComponent2", false, true));
        	Assert.assertNotNull("Context 2.1 component 2", ctx21.meta_getInstance("myComponent2", false, true));
        	Assert.assertFalse("Context 1.1/2.1 components", ctx11.myComponent2 === ctx21.myComponent2);
        	
        	ctx1.myComponent2c = new MyComponentConversation2c();
        	ctx1.myComponent2d = new MyComponentConversation2b();
        	
        	ctx11.myComponent2.dispatchEvent(new TideUIEvent("renext"));
        	
        	Assert.assertTrue("Context 1.1 component 2b triggered", ctx11.myComponent2b.triggered);
        	Assert.assertFalse("Context 1 component 2d not triggered", ctx1.myComponent2c.triggered);
        	Assert.assertTrue("Context 1 component 2d triggered", ctx1.myComponent2d.triggered);
        	Assert.assertFalse("Context 1/1.1 components", ctx1.myComponent2b === ctx11.myComponent2b);
        	Assert.assertNull("Context 2.1 component 2b not triggered", ctx21.meta_getInstance("myComponent2b", false, true));
        }
    }
}
