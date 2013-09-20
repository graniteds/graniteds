package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestComponentInjectionForum4314
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentInjectionForum4314():void {
        	Tide.getInstance().addComponents([MyComponent3, MyComponentConversation3]);
        	
        	_ctx.raiseEvent("testGlobal");
        	_ctx.myComponent3a.dispatchEvent(new TideUIConversationEvent("Test", "test"));
        	
			Assert.assertEquals("Inject", "test", Tide.getInstance().getContext("Test").myComponent3b.injected);
        	
        	_ctx.myComponent3a.testInjectGlobal = "test2";
        	
			Assert.assertEquals("Inject 2", "test2", Tide.getInstance().getContext("Test").myComponent3b.testInjectGlobal);
        }
		
		[Test]
		public function testComponentInjectionGlobal():void {
			Tide.getInstance().addComponents([MyComponentConversation3]);
			
			_ctx.dispatchEvent(new TideUIConversationEvent("Test", "test"));
			
			Assert.assertNull("Inject", Tide.getInstance().getContext("Test").myComponent3b.testInjectGlobal);
			
			_ctx.testInjectGlobal = "test";
			
			Assert.assertEquals("Inject 2", _ctx.testInjectGlobal, Tide.getInstance().getContext("Test").myComponent3b.testInjectGlobal);
		}
		
		[Test]
		public function testComponentInjectionGlobal2():void {
			Tide.getInstance().addComponents([MyComponentConversation4]);
			
			_ctx.dispatchEvent(new TideUIConversationEvent("Test", "test"));
			
			Assert.assertNull("Inject", Tide.getInstance().getContext("Test").myComponent4b.testInjectGlobal);
			
			_ctx.testInjectGlobal = new Person();
			
			Assert.assertStrictlyEquals("Inject 2", _ctx.testInjectGlobal, Tide.getInstance().getContext("Test").myComponent4b.testInjectGlobal);
		}
    }
}
