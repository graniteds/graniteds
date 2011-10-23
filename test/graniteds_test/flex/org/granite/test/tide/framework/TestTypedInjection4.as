package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedInjection4
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedComponent]);
        }
        
		[Test]
		public function testTypedInjection2():void {
			Assert.assertTrue("Injected by class", _ctx.byType(MyTypedComponent) is MyTypedComponent);
		}
		
		[Test]
		public function testTypedInjection3():void {
			Assert.assertTrue("Injected by interface", _ctx.byType(IMyTypedComponent) is MyTypedComponent);
		}
    }
}
