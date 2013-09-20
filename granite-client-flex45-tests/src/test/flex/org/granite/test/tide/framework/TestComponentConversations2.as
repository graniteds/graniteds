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
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.spring.Spring;
    import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestComponentConversations2
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Spring.resetInstance();
            _ctx = Spring.getInstance().getContext();
            Spring.getInstance().initApplication();
            Spring.getInstance().addComponent("myCompGlobal", MyComponentRemote);
            Spring.getInstance().addComponent("myCompLocal", MyComponentRemote, true);
        }
        
        
        [Test]
        public function testComponentConversations2():void {
        	var localCtx:BaseContext = Spring.getInstance().getContext("test");
        	
			Assert.assertTrue("Remote component global", _ctx.remoteComponent is Component);
			Assert.assertTrue("Remote component local", localCtx.remoteComponent is Component);
        	
        	// Behaviour for 2.0 final
        	// assertTrue("Different remote components", _ctx.remoteComponent !== localCtx.remoteComponent); 
        }
    }
}
