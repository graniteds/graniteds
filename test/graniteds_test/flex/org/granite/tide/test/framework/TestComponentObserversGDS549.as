package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObserversGDS549 extends TestCase
    {
        public function TestComponentObserversGDS549() {
            super("testComponentObserversGDS549");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentObserversGDS549():void {
        	_ctx["bla.component1"] = new MyComponentSubcontext1();
        	_ctx["bla.component1b"] = new MyComponentSubcontext1b();
        	
        	_ctx["bla.bla.component1b"] = new MyComponentSubcontext1b();
        	
        	_ctx["bla.component1"].dispatchEvent(new MyLocalEvent());
        	
        	assertTrue("Observer triggered in same subcontext", _ctx["bla.component1b"].localTriggered);
        	assertFalse("Observer not triggered in child subcontext", _ctx["bla.bla.component1b"].localTriggered);
        	
        	_ctx["bla.component1"].dispatchEvent(new MyEvent());
        	
        	assertTrue("Observer triggered in same subcontext", _ctx["bla.component1b"].localTriggered);
        	
        	_ctx["bla.component1"].triggered = false;
        	_ctx["bla.component1b"].localTriggered = false;
        	
        	_ctx["bla.bla.component1b"].dispatchEvent(new MyLocalEvent());
        	
        	assertTrue("Observer triggered in parent subcontext", _ctx["bla.component1b"].localTriggered);
        }
    }
}
