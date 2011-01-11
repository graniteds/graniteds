package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentScan extends TestCase
    {
        public function TestComponentScan() {
            super("testComponentScan");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentScan]);
        }
        
        
        public function testComponentScan():void {
        	assertTrue("String scanned", Tide.getInstance().isComponent("testString"));
        	// assertTrue("Array scanned", Tide.getInstance().isComponent("testArray"));
        }
    }
}
