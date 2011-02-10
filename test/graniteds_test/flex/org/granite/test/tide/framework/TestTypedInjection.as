package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedInjection
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedInjectedComponent, MyTypedComponent]);
        }
        
        [Test]
        public function testTypedInjection():void {
			var comp:Object = _ctx.myTypedInjectedComponent;
			Assert.assertStrictlyEquals("Injected by class", _ctx.byType(MyTypedComponent), comp.byClass);
			Assert.assertStrictlyEquals("Injected by interface", _ctx.byType(MyTypedComponent), comp.byInterface);
			Assert.assertStrictlyEquals("Injected context", _ctx, comp.context);
        }
    }
}
