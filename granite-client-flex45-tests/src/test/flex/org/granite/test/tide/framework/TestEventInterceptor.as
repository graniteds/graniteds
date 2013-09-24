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
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestEventInterceptor
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyEventInterceptor]);
        }
        
        
        [Test("GDS-831")]
        public function testEventInterceptorGDS831():void {
			var interceptor:Object = _ctx.byType(MyEventInterceptor);
			
        	_ctx.raiseEvent("someEvent");
			
			Assert.assertEquals("Before type", "someEvent", interceptor.before);
			Assert.assertStrictlyEquals("Before ctx", _ctx, interceptor.beforeCtx);
			Assert.assertEquals("After type", "someEvent", interceptor.after);
			Assert.assertStrictlyEquals("After ctx", _ctx, interceptor.afterCtx);
			
			_ctx.myComponentObserverNoCreate = new MyComponentObserverNoCreate();
			
			interceptor.before = null;
			interceptor.beforeCtx = null;
			interceptor.after = null;
			interceptor.afterCtx = null;
			
			_ctx.raiseEvent("someEvent");
			
			Assert.assertEquals("Before type observed", "someEvent", interceptor.before);
			Assert.assertStrictlyEquals("Before ctx observed", _ctx, interceptor.beforeCtx);
			Assert.assertEquals("After type observed", "someEvent", interceptor.after);
			Assert.assertStrictlyEquals("After ctx observed", _ctx, interceptor.afterCtx);
			
			interceptor.before = null;
			interceptor.beforeCtx = null;
			interceptor.after = null;
			interceptor.afterCtx = null;
			
			var ctx:BaseContext = Tide.getInstance().getContext("myConversation");
			ctx.raiseEvent("someConversationEvent");
			
			Assert.assertEquals("Before type conv", "someConversationEvent", interceptor.before);
			Assert.assertStrictlyEquals("Before ctx conv", ctx, interceptor.beforeCtx);
        }
    }
}
