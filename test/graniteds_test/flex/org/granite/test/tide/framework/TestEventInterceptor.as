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
			Assert.assertNull("After type", interceptor.after);
			Assert.assertNull("After ctx", interceptor.afterCtx);
			
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
