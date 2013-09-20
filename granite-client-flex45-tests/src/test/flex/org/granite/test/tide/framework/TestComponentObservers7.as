package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentObservers7
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentObserver2, MyComponentObserver4]);
        }
        
        
		[Test]
		public function testComponentObserverGDS1039():void {
			_ctx.myComponentObserver2.dispatchEvent(new TideUIEvent("myManagedEvent", new MyManagedEvent()));
			
			Assert.assertTrue("Untyped observer 1 triggered", _ctx.myComponentObserver2.triggered1);
			Assert.assertFalse("Untyped observer 2 not triggered", _ctx.myComponentObserver2.triggered2);
		}
		
		[Test]
		public function testComponentObserverGDS1039b():void {
			_ctx.myComponentObserver2.dispatchEvent(new TideUIEvent("myManagedEvent4", new MyManagedEvent()));
			
			Assert.assertTrue("Untyped observer 1 triggered", _ctx.myComponentObserver2.triggered1);
			Assert.assertFalse("Untyped observer 2 not triggered", _ctx.myComponentObserver2.triggered2);
			Assert.assertFalse("Untyped observer 3 triggered", _ctx.myComponentObserver4.triggered1);
			
			_ctx.myComponentObserver2.dispatchEvent(new TideUIEvent("myManagedEvent4", new MyManagedEvent("myManagedEvent4")));
			
			Assert.assertTrue("Untyped observer 3 triggered", _ctx.myComponentObserver4.triggered1);
		}
    }
}
