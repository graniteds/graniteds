package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIEvent;
	import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestComponentObservers5
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentConversationObserver]);
        }
        
        
        [Test]
        public function testComponentObserversGDS842():void {
        	_ctx.dispatchEvent(new TideUIConversationEvent("test", "someEvent"));			
			var ctx:BaseContext = Tide.getInstance().getContext("test");					        	
        	Assert.assertEquals("Observer untyped event", 1, ctx.myComponentObserver.untypedEvent);
        	
        	_ctx.dispatchEvent(new MyEvent());			
        	Assert.assertEquals("Observer typed event ", 1, _ctx.myComponentObserver.typedEvent);
        	
			_ctx.dispatchEvent(new TideUIConversationEvent("test", "someEvent2", callback));			
        }
		
		private function callback():void {
		}
    }
}
