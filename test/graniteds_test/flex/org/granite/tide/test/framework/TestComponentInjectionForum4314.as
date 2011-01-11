package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestComponentInjectionForum4314 extends TestCase
    {
        public function TestComponentInjectionForum4314() {
            super("testComponentInjectionForum4314");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentInjectionForum4314():void {
        	Tide.getInstance().addComponents([MyComponent3, MyComponentConversation3]);
        	
        	_ctx.raiseEvent("testGlobal");
        	_ctx.myComponent3a.dispatchEvent(new TideUIConversationEvent("Test", "test"));
        	
        	assertEquals("Inject", "test", Tide.getInstance().getContext("Test").myComponent3b.injected);
        	
        	_ctx.myComponent3a.testInjectGlobal = "test2";
        	
        	assertEquals("Inject 2", "test2", Tide.getInstance().getContext("Test").myComponent3b.testInjectGlobal);
        }
    }
}
