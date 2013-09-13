package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.spring.Spring;
    
    
    public class TestComponentConversations3
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Spring.resetInstance();
            _ctx = Spring.getInstance().getContext();
            Spring.getInstance().initApplication();
            Spring.getInstance().addComponent("myComponent", MyComponent);
			Spring.getInstance().addComponent("myComponent2", MyComponent2);
            Spring.getInstance().addComponent("myConversationComponent", MyComponentConversationGDS1012, true);
			Spring.getInstance().addComponent("myConversationComponentB", MyComponentConversationGDS1012b, true);
        }
        
        
        [Test]
        public function testComponentConversations3():void {
        	var localCtx:BaseContext = Spring.getInstance().getContext("test");
        	
			Assert.assertStrictlyEquals("Global component [In]", _ctx.myComponent, localCtx.myConversationComponent.myComponent);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, localCtx.myConversationComponent.myComponentY);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, localCtx.myConversationComponent.myComponentZ);
			Assert.assertStrictlyEquals("Global component [Inject]", _ctx.myComponent2, localCtx.myConversationComponentB.myComponent);
        }
    }
}
