package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.spring.Spring;
    
    
    public class TestComponentConversations4
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Spring.resetInstance();
            _ctx = Spring.getInstance().getContext();
            Spring.getInstance().initApplication();
            Spring.getInstance().addComponents([ MyComponent, MyComponent2, MyComponentConversationGDS1012, MyComponentConversationGDS1012b ]);
        }
        
        
        [Test]
        public function testComponentConversations4():void {
        	var localCtx:BaseContext = Spring.getInstance().getContext("test");
        	
			Assert.assertStrictlyEquals("Global component [In]", _ctx.myComponent, localCtx.myComponentConversation.myComponent);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, localCtx.myComponentConversation.myComponentY);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, localCtx.myComponentConversation.myComponentZ);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, localCtx.myComponentConversationB.myComponent);
        }
		
		[Test]
		public function testComponentNestedConversations4():void {
			var localCtx:BaseContext = Spring.getInstance().getContext("test");
			var nestedCtx:BaseContext = Spring.getInstance().getContext("nested", localCtx.contextId);
			
			Assert.assertStrictlyEquals("Global component [In]", _ctx.myComponent, nestedCtx.myComponentConversation.myComponent);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, nestedCtx.myComponentConversation.myComponentY);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, nestedCtx.myComponentConversation.myComponentZ);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, nestedCtx.myComponentConversationB.myComponent);
		}
    }
}
