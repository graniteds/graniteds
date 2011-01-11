package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontextsGDS627 extends TestCase
    {
        public function TestComponentSubcontextsGDS627() {
            super("testComponentSubcontextsGDS627");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            Tide.getInstance().setComponentGlobal("myEventTriggered", true);
            _ctx = Tide.getInstance().getContext();
            _ctx.myEventTriggered = 0;
            Tide.getInstance().addComponent("com.foo.bar.myComponentB", MyComponentSubcontextB);
            Tide.getInstance().addComponent("com.foo.myComponentA1", MyComponentSubcontextA1);
            Tide.getInstance().addComponent("com.foo.bar.myComponentB1", MyComponentSubcontextA1);
        }
        
        
        public function testComponentSubcontextsGDS627():void {
        	_ctx["com.foo.bar.myComponentB"].dispatchEvent(new MyEvent());
        	
        	assertEquals("Component A1 triggered", 2, _ctx["com.foo.myComponentA1"].triggered);
        	assertEquals("Component B1 triggered", 1, _ctx["com.foo.bar.myComponentB1"].triggered);
        }
    }
}
