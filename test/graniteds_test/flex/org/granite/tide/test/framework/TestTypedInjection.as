package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedInjection extends TestCase
    {
        public function TestTypedInjection() {
            super("testTypedInjection");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedInjectedComponent, MyTypedComponent]);
        }
        
        public function testTypedInjection():void {
			var comp:Object = _ctx.myTypedInjectedComponent;
			assertStrictlyEquals("Injected by class", _ctx.byType(MyTypedComponent), comp.byClass);
			assertStrictlyEquals("Injected by interface", _ctx.byType(MyTypedComponent), comp.byInterface);
			assertStrictlyEquals("Injected context", _ctx, comp.context);
        }
    }
}
