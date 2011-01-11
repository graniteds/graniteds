package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.IComponent;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentModules extends TestCase
    {
        public function TestComponentModules() {
            super("testComponentModules");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponent("module1.myComponent1", MyComponentModule1);
            Tide.getInstance().addComponent("module1.myComponent1b", MyComponentModule1b);
            Tide.getInstance().addComponent("module2.myComponent2", MyComponentModule2);
            Tide.getInstance().addComponents([MyComponentModule3]);
        }
        
        
        public function testComponentModules():void {
        	_ctx.application.dispatchEvent(new MyEvent());
        	
        	assertTrue("Component module1 triggered", _ctx['module1.myComponent1'].triggered);
        	assertTrue("Component module2 triggered", _ctx['module2.myComponent2'].triggered);
        	assertTrue("Component module1 local triggered", _ctx['module1.myComponent1b'].localTriggered);
        	assertFalse("Component module2 not local triggered", _ctx['module2.myComponent2'].localTriggered);
        	
        	assertFalse("Component injections", _ctx['module1.myComponent1'].myVariable === _ctx['module2.myComponent2'].myVariable);
        	assertStrictlyEquals("Component module1 injection", _ctx['module1.myComponent1'].myVariable, _ctx['module1.myVariable']);
        	assertStrictlyEquals("Component module1 injection", _ctx['module1.myComponent1b'].myVariable, _ctx['module1.myVariable']);
        	assertStrictlyEquals("Component module2 injection", _ctx['module2.myComponent2'].myVariable, _ctx['module2.myVariable']);
        	
        	assertFalse("Component outjections", _ctx['module1.myComponent1'].outjectedVariable === _ctx['module2.myComponent2'].outjectedVariable);
        	assertStrictlyEquals("Component module1 outjection", _ctx['module1.myComponent1'].outjectedVariable, _ctx['module1.outjectedVariable']);
        	assertStrictlyEquals("Component module2 outjection", _ctx['module2.myComponent2'].outjectedVariable, _ctx['module2.outjectedVariable']);
        	
        	assertFalse("No global variable", Tide.getInstance().isComponent("myLocalVariable"));
        	assertFalse("Component global access", _ctx['myLocalVariable'] is Contact);
        	assertTrue("Component local access", _ctx['module1.myLocalVariable'] is Contact);
        	assertStrictlyEquals("Component module1 variable injection", _ctx['module1.myComponent1b'].myLocalVariable, _ctx['module1.myLocalVariable']);
        	
        	assertStrictlyEquals("Component global variable", _ctx['module1.myComponent1'].globalVariable, _ctx.globalVariable);
        	assertStrictlyEquals("Component global variable", _ctx['module2.myComponent2'].globalVariable, _ctx.globalVariable);
        	
        	_ctx['module3.myComponentModule3'];
        	assertNull("No local component proxy", _ctx['module3.myRemoteComponent']);
        	assertTrue("Component proxy", _ctx.myRemoteComponent is IComponent);
        }
    }
}
