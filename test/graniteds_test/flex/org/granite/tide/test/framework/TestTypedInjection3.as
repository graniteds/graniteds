package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedInjection3 extends TestCase
    {
        public function TestTypedInjection3() {
            super("testTypedInjection3");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedInjectedComponent3]);
        }
        
        
        public function testTypedInjection3():void {
			var comp:Object = _ctx.myTypedInjectedComponent3;
			assertNull(comp.byInterface);
			
			_ctx.blabla = new MyTypedComponent();
			assertStrictlyEquals("Component injected", _ctx.blabla, comp.byInterface);
			
			_ctx.blabla = null;
			assertNull("Component disinjected", comp.byInterface);
			
			_ctx.bloblo = new MyOtherTypedComponent();
			assertStrictlyEquals("Component injected", _ctx.bloblo, comp.byInterface);
        }
    }
}
