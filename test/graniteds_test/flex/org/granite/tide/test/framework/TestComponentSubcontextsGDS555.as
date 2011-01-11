package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontextsGDS555 extends TestCase
    {
        public function TestComponentSubcontextsGDS555() {
            super("testComponentSubcontextsGDS555");
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
            Tide.getInstance().addComponent("com.foo.bar.myComponentB1", MyComponentSubcontextA1);
            Tide.getInstance().addComponent("com.foo.bar.myComponentB2", MyComponentSubcontextA2);
            Tide.getInstance().addComponent("com.foo.myComponentA1", MyComponentSubcontextA1);
            Tide.getInstance().addComponent("com.foo.myComponentA2", MyComponentSubcontextA2);
        }
        
        
        public function testComponentSubcontextsGDS555():void {
        	_ctx["com.foo.bar.myComponentB"].dispatchEvent(new MyEvent());
        	
        	assertTrue("Component A1 triggered", _ctx["com.foo.myComponentA1"].triggered > 0);
        	assertFalse("Component A2 not triggered", _ctx["com.foo.myComponentA2"].triggered > 0);
        	assertTrue("Component B1 triggered", _ctx["com.foo.bar.myComponentB1"].triggered > 0);
        	assertTrue("Component B2 triggered", _ctx["com.foo.bar.myComponentB2"].triggered > 0);
        }
    }
}
