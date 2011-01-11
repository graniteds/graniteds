package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.spring.Spring;
    import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestComponentConversations2 extends TestCase
    {
        public function TestComponentConversations2() {
            super("testComponentConversations2");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Spring.resetInstance();
            _ctx = Spring.getInstance().getContext();
            Spring.getInstance().initApplication();
            Spring.getInstance().addComponent("myCompGlobal", MyComponentRemote);
            Spring.getInstance().addComponent("myCompLocal", MyComponentRemote, true);
        }
        
        
        public function testComponentConversations2():void {
        	var localCtx:BaseContext = Spring.getInstance().getContext("test");
        	
        	assertTrue("Remote component global", _ctx.remoteComponent is Component);
        	assertTrue("Remote component local", localCtx.remoteComponent is Component);
        	
        	// Behaviour for 2.0 final
        	// assertTrue("Different remote components", _ctx.remoteComponent !== localCtx.remoteComponent); 
        }
    }
}
