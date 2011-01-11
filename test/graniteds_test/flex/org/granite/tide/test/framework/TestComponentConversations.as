package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentConversations extends TestCase
    {
        public function TestComponentConversations() {
            super("testComponentConversations");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentConversation, MyComponentConversation2]);
        }
        
        
        public function testComponentConversations():void {
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("1", "start"));
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("2", "start"));
        	
        	var ctx1:BaseContext = Tide.getInstance().getContext("1", null, false);
        	var ctx2:BaseContext = Tide.getInstance().getContext("2", null, false);
        	
        	assertNull("No global", _ctx.myComponent);
        	assertNotNull("Context 1 created", ctx1);
        	assertNotNull("Context 2 created", ctx2);
        	
        	assertNotNull("Context 1 component", ctx1.myComponent);
        	assertNotNull("Context 2 component", ctx2.myComponent);
        	assertFalse("Component instances", ctx1.myComponent === ctx2.myComponent);
        	
        	ctx1.raiseEvent("next");
        	assertTrue("Component 1 triggered", ctx1.myComponent2.triggered);
        	assertFalse("Component 2 triggered", ctx2.myComponent2.triggered);
        }
    }
}
