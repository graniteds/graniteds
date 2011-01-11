package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentEventScope extends TestCase
    {
        public function TestComponentEventScope() {
            super("testComponentEventScope");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyCompSession, MyCompConversation]);
        }
        
        
        public function testComponentEventScope():void {
			assertNotNull("Session proxy", _ctx.myCompSession.eventService);
			
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("1", "start"));
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("2", "start"));
        	
        	var ctx1:BaseContext = Tide.getInstance().getContext("1", null, false);
        	var ctx2:BaseContext = Tide.getInstance().getContext("2", null, false);
        	
			assertNotNull("Conversation 1 proxy", ctx1.myCompConversation.eventService);
			
			assertFalse("Proxy session != conversation", ctx1.myCompConversation.eventService === _ctx.myCompSession.eventService) 
			assertFalse("Proxy conversation 1 != conversation 2", ctx1.myCompConversation.eventService === ctx2.myCompConversation.eventService) 
        }
    }
}
