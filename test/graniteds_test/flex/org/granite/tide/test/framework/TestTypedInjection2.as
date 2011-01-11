package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedInjection2 extends TestCase
    {
        public function TestTypedInjection2() {
            super("testTypedInjection2");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedInjectedComponent]);
        }
        
        
        public function testTypedInjection2():void {
			var comp:Object = _ctx.myTypedInjectedComponent;
			
			_ctx.blabla = new MyTypedComponent();
			assertStrictlyEquals("Injected by class", _ctx.blabla, comp.byClass);
			assertStrictlyEquals("Injected by interface", _ctx.blabla, comp.byInterface);
        }
    }
}
