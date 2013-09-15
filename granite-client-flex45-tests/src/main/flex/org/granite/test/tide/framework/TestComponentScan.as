package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentScan
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentScan]);
        }
        
        
        [Test]
        public function testComponentScan():void {
        	Assert.assertTrue("String scanned", Tide.getInstance().isComponent("testString"));
        	// assertTrue("Array scanned", Tide.getInstance().isComponent("testArray"));
        }
    }
}
