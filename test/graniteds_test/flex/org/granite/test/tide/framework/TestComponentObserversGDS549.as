package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObserversGDS549
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentObserversGDS549():void {
        	_ctx["bla.component1"] = new MyComponentSubcontext1();
        	_ctx["bla.component1b"] = new MyComponentSubcontext1b();
        	
        	_ctx["bla.bla.component1b"] = new MyComponentSubcontext1b();
        	
        	_ctx["bla.component1"].dispatchEvent(new MyLocalEvent());
        	
        	Assert.assertTrue("Observer triggered in same subcontext", _ctx["bla.component1b"].localTriggered);
        	Assert.assertFalse("Observer not triggered in child subcontext", _ctx["bla.bla.component1b"].localTriggered);
        	
        	_ctx["bla.component1"].dispatchEvent(new MyEvent());
        	
        	Assert.assertTrue("Observer triggered in same subcontext", _ctx["bla.component1b"].localTriggered);
        	
        	_ctx["bla.component1"].triggered = false;
        	_ctx["bla.component1b"].localTriggered = false;
        	
        	_ctx["bla.bla.component1b"].dispatchEvent(new MyLocalEvent());
        	
        	Assert.assertTrue("Observer triggered in parent subcontext", _ctx["bla.component1b"].localTriggered);
        }
    }
}
