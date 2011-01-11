package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentModules2 extends TestCase
    {
        public function TestComponentModules2() {
            super("testComponentModules2");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentModuleA1, MyComponentModuleA2]);
            Tide.getInstance().addComponent("module1.myComponentB", MyComponentModuleB);
            Tide.getInstance().addComponent("module2.myComponentB", MyComponentModuleB);
        }
        
        
        public function testComponentModules2():void {
        	_ctx.application.dispatchEvent(new MyEvent());
        	
        	assertTrue("Component module1 event1 triggered", _ctx['module1.myComponentB'].triggered1);
        	assertFalse("Component module1 event2 not triggered", _ctx['module1.myComponentB'].triggered2);
        	assertFalse("Component module2 event1 not triggered", _ctx['module2.myComponentB'].triggered1);
        	assertTrue("Component module2 event2 triggered", _ctx['module2.myComponentB'].triggered2);
        }
    }
}
