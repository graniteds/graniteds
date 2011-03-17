package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObservers4
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentManagedEvent1, MyComponentManagedEvent2, MyComponentObserver2]);
        }
        
        
        [Test]
        public function testComponentObservers4():void {
        	_ctx.raiseEvent("myInitEvent");
        	
        	Assert.assertTrue("Component 2 triggered", _ctx.myComponentManagedEvent2.triggered);
        }
		
		[Test]
		public function testComponentObserverGNLI6():void {
			_ctx.myComponentObserver2.dispatch();
			
			Assert.assertTrue("Untyped observer 1 triggered", _ctx.myComponentObserver2.triggered1);
			Assert.assertFalse("Untyped observer 2 not triggered", _ctx.myComponentObserver2.triggered2);
		}
    }
}
