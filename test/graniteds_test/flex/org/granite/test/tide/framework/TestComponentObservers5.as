package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIEvent;
	import org.granite.tide.events.TideUIConversationEvent;
    
	import org.granite.test.tide.*;
    
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
		public function testComponentObservers():void {
			_ctx.dispatchEvent(new TideUIConversationEvent("test", "someEvent"));			
			var ctx:BaseContext = Tide.getInstance().getContext("test");					        	
			Assert.assertEquals("Observer untyped event", 1, ctx.myComponentObserver.untypedEvent);
			
			_ctx.dispatchEvent(new MyConversationEvent("toto"));			
			Assert.assertEquals("Observer typed event ", 1, ctx.myComponentObserver.typedEvent);			
		}
		
        [Test]
        public function testComponentObserversGDS840():void {
			_ctx.dispatchEvent(new TideUIConversationEvent("test", "someEvent2", callback));
        }
		
		private function callback():void {
		}
		
		[Test]
		public function testComponentObserversGDS842():void {
			var person:Person0 = new Person0(10, "Toto", "Tutu");
			_ctx.person = person;
			_ctx.dispatchEvent(new TideUIConversationEvent("test", "someEvent3", [person]));			
			var ctx:BaseContext = Tide.getInstance().getContext("test");
			Assert.assertFalse("Person copied in conversation context", person === ctx.myComponentObserver.arg);
			Assert.assertStrictlyEquals("Person uid copied in conversation context", person.uid, ctx.myComponentObserver.arg.uid);
		}
    }
}
