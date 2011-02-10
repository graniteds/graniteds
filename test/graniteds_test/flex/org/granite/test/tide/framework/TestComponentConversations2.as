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
