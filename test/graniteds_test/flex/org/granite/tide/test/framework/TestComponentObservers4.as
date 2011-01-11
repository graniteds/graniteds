package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObservers4 extends TestCase
    {
        public function TestComponentObservers4() {
            super("testComponentObservers4");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentManagedEvent1, MyComponentManagedEvent2]);
        }
        
        
        public function testComponentObservers4():void {
        	_ctx.raiseEvent("myInitEvent");
        	
        	assertTrue("Component 2 triggered", _ctx.myComponentManagedEvent2.triggered);
        }
    }
}
