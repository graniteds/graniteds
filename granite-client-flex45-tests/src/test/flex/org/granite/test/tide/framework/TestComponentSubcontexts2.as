package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontexts2
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponent("module1.myComponent1", MyComponentSubcontext1);
            Tide.getInstance().addComponent("module1.myComponent1b", MyComponentSubcontext1b);
            Tide.getInstance().addComponent("com.foo.module2.myComponent2", MyComponentSubcontext2);
            Tide.getInstance().addComponents([MyComponentSubcontext3]);
            Tide.getInstance().addComponent("myComponent4", MyComponentSubcontext4);
        }
        
        
        [Test]
        public function testComponentSubcontexts2():void {
        	var module1:Subcontext = new Subcontext();
        	var module2:Subcontext = new Subcontext();
        	_ctx.module1 = module1;
        	_ctx["com.foo.module2"] = module2;
        	module1.dispatchEvent(new MyLocalEvent());
        	
        	Assert.assertTrue("Component module1 local triggered", module1.myComponent1b.localTriggered);
        	Assert.assertStrictlyEquals("Component module1 subcontext injection", module1, module1.myComponent1b.subcontext);
        	Assert.assertFalse("Component module2 not local triggered", module2.myComponent2.localTriggered);
        	
        	module2.dispatchEvent(new MyLocalEvent());
        	Assert.assertTrue("Component module2 local triggered", module2.myComponent2.localTriggered);
        	
        	Assert.assertTrue("Generic component created", module1.myComponent4 is MyComponentSubcontext4);
        	Assert.assertTrue("Generic component created", module2.myComponent4 is MyComponentSubcontext4);
        	
        	module1.dispatchEvent(new MyEvent());
        	Assert.assertFalse("Global not triggered", _ctx.myComponent4.triggered); 
        	Assert.assertTrue("Module 1 triggered", module1.myComponent4.triggered); 
        	Assert.assertFalse("Module 2 not triggered", module2.myComponent4.triggered); 
        }
    }
}
