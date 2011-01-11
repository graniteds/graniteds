package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentTideModules extends TestCase
    {
        public function TestComponentTideModules() {
            super("testComponentTideModules");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			Tide.getInstance().addModule(MyModule1);
        }
        
        
        public function testComponentTideModules():void {
        	_ctx.application.dispatchEvent(new MyEvent());
        	
        	assertTrue("Component module1 event1 triggered", _ctx['module1.myComponentB'].triggered1);
        	assertFalse("Component module1 event2 not triggered", _ctx['module1.myComponentB'].triggered2);
        	assertFalse("Component module2 event1 not triggered", _ctx['module2.myComponentB'].triggered1);
        	assertTrue("Component module2 event2 triggered", _ctx['module2.myComponentB'].triggered2);
        }
    }
}
