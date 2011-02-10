package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentEventScope
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyCompSession, MyCompConversation]);
        }
        
        
        [Test]
        public function testComponentEventScope():void {
			Assert.assertNotNull("Session proxy", _ctx.myCompSession.eventService);
			
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("1", "start"));
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("2", "start"));
        	
        	var ctx1:BaseContext = Tide.getInstance().getContext("1", null, false);
        	var ctx2:BaseContext = Tide.getInstance().getContext("2", null, false);
        	
			Assert.assertNotNull("Conversation 1 proxy", ctx1.myCompConversation.eventService);
			
			Assert.assertFalse("Proxy session != conversation", ctx1.myCompConversation.eventService === _ctx.myCompSession.eventService) 
			Assert.assertFalse("Proxy conversation 1 != conversation 2", ctx1.myCompConversation.eventService === ctx2.myCompConversation.eventService) 
        }
    }
}
