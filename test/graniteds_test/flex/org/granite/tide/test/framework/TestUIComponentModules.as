package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestUIComponentModules extends TestCase
    {
        public function TestUIComponentModules() {
            super("testUIComponentModules");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponent("module1.myComponent1", MyComponentModule1);
            Tide.getInstance().addComponent("module2.myComponent2", MyComponentModule2);
        }
        
        
        public function testUIComponentModules():void {
        	var myPanel2:MyPanel4 = new MyPanel4();
			_ctx['module1.myPanel'] = myPanel2;
        	_ctx.application.addChild(myPanel2);
        	
        	myPanel2.dispatchEvent(new MyEvent());
        	
        	assertTrue("Component module1 triggered", _ctx['module1.myComponent1'].triggered);
        	assertFalse("Component module2 not triggered", _ctx['module2.myComponent2'].triggered);
        	
        	_ctx.application.removeChild(myPanel2);
        }
    }
}
