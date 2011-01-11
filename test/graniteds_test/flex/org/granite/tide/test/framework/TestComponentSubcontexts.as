package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentSubcontexts extends TestCase
    {
        public function TestComponentSubcontexts() {
            super("testComponentSubcontexts");
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
        }
        
        
        public function testComponentSubcontexts():void {
        	_ctx.application.dispatchEvent(new MyEvent());
        	
        	assertTrue("Subcontext 1 created", _ctx.module1 is Subcontext);
        	assertTrue("Subcontext 2 created", _ctx["com.foo.module2"] is Subcontext);
        	assertStrictlyEquals("Component in subcontext 1", _ctx['module1.myComponent1'], _ctx.module1.myComponent1);
        	assertStrictlyEquals("Component in subcontext 2", _ctx['com.foo.module2.myComponent2'], _ctx['com.foo.module2'].myComponent2);
        	assertStrictlyEquals("Component subcontext injection 1", _ctx.module1, _ctx.module1.myComponent1.context);
        	
        	var module1:Subcontext = _ctx.module1 as Subcontext;
        	var module2:Subcontext = _ctx['com.foo.module2'] as Subcontext;
        	
        	assertTrue("Component module2 triggered", module2.myComponent2.triggered);
        	assertTrue("Component module1 local triggered", module1.myComponent1b.localTriggered);
        	assertFalse("Component module2 not local triggered", module2.myComponent2.localTriggered);
        	
        	assertFalse("Component injections", module1.myComponent1.myVariable === module2.myComponent2.myVariable);
        	assertStrictlyEquals("Component module1 injection", module1.myComponent1.myVariable, module1.myVariable);
        	assertStrictlyEquals("Component module1 injection", module1.myComponent1b.myVariable, module1.myVariable);
        	assertStrictlyEquals("Component module2 injection", module2.myComponent2.myVariable, module2.myVariable);
        	
        	assertFalse("Component outjections", module1.myComponent1.outjectedVariable === module2.myComponent2.outjectedVariable);
        	assertStrictlyEquals("Component module1 outjection", module1.myComponent1.outjectedVariable, module1.outjectedVariable);
        	assertStrictlyEquals("Component module2 outjection", module2.myComponent2.outjectedVariable, module2.outjectedVariable);
        	
        	assertFalse("No global variable", Tide.getInstance().isComponent("myLocalVariable"));
        	assertFalse("Component global access", _ctx.myLocalVariable is Contact);
        	assertTrue("Component local access", module1.myLocalVariable is Contact);
        	assertStrictlyEquals("Component module1 variable injection", module1.myComponent1b.myLocalVariable, module1.myLocalVariable);
        	
        	assertStrictlyEquals("Component global variable", module1.myComponent1.globalVariable, _ctx.globalVariable);
        	assertStrictlyEquals("Component global variable", module2.myComponent2.globalVariable, _ctx.globalVariable);
        	
        	_ctx.module3.myComponentModule3;
        	assertNull("No local component proxy", _ctx.module3.myRemoteComponent);
        	assertTrue("Component proxy", _ctx.myRemoteComponent is IComponent);
        	
        	var contact:Contact = new Contact();
        	module1.myVariable = contact;
        	assertStrictlyEquals("Component module1 variable injection", contact, module1.myComponent1.myVariable);
        	assertStrictlyEquals("Component module1 variable injection", contact, module1.myComponent1b.myVariable);
        	assertFalse("Component module1 variable injection", module2.myComponent2.myVariable === contact);
        }
    }
}
