package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedInjection2
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedInjectedComponent]);
        }
        
        
        [Test]
        public function testTypedInjection2():void {
			var comp:Object = _ctx.myTypedInjectedComponent;
			
			_ctx.blabla = new MyTypedComponent();
			Assert.assertStrictlyEquals("Injected by class", _ctx.blabla, comp.byClass);
			Assert.assertStrictlyEquals("Injected by interface", _ctx.blabla, comp.byInterface);
        }
    }
}
