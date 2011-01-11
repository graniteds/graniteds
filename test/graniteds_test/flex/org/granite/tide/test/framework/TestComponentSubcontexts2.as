package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontexts2 extends TestCase
    {
        public function TestComponentSubcontexts2() {
            super("testComponentSubcontexts2");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponent("module1.myComponent1", MyComponentSubcontext1);
            Tide.getInstance().addComponent("module1.myComponent1b", MyComponentSubcontext1b);
            Tide.getInstance().addComponent("com.foo.module2.myComponent2", MyComponentSubcontext2);
            Tide.getInstance().addComponents([MyComponentSubcontext3]);
            Tide.getInstance().addComponent("myComponent4", MyComponentSubcontext4);
        }
        
        
        public function testComponentSubcontexts2():void {
        	var module1:Subcontext = new Subcontext();
        	var module2:Subcontext = new Subcontext();
        	_ctx.module1 = module1;
        	_ctx["com.foo.module2"] = module2;
        	module1.dispatchEvent(new MyLocalEvent());
        	
        	assertTrue("Component module1 local triggered", module1.myComponent1b.localTriggered);
        	assertStrictlyEquals("Component module1 subcontext injection", module1, module1.myComponent1b.subcontext);
        	assertFalse("Component module2 not local triggered", module2.myComponent2.localTriggered);
        	
        	module2.dispatchEvent(new MyLocalEvent());
        	assertTrue("Component module2 local triggered", module2.myComponent2.localTriggered);
        	
        	assertTrue("Generic component created", module1.myComponent4 is MyComponentSubcontext4);
        	assertTrue("Generic component created", module2.myComponent4 is MyComponentSubcontext4);
        	
        	module1.dispatchEvent(new MyEvent());
        	assertFalse("Global not triggered", _ctx.myComponent4.triggered); 
        	assertTrue("Module 1 triggered", module1.myComponent4.triggered); 
        	assertFalse("Module 2 not triggered", module2.myComponent4.triggered); 
        }
    }
}
