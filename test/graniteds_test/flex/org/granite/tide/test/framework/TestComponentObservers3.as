package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObservers3 extends TestCase
    {
        public function TestComponentObservers3() {
            super("testComponentObservers3");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentConversationA, MyComponentConversationB]);
        }
        
        
        public function testComponentObservers3():void {
        	var ctx:BaseContext = Tide.getInstance().getContext("convCtx");
        	
        	ctx.raiseEvent("start");
        	
        	assertNotNull("Component A created", ctx.myComponentA);
        	assertNull("Component B not created", ctx.meta_getInstance("myComponentB", false, true));
        }
    }
}
