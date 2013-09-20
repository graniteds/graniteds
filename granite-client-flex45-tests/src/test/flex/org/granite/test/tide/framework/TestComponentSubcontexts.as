package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentSubcontexts
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
        }
        
        
        [Test]
        public function testComponentSubcontexts():void {
        	_ctx.application.dispatchEvent(new MyEvent());
        	
        	Assert.assertTrue("Subcontext 1 created", _ctx.module1 is Subcontext);
        	Assert.assertTrue("Subcontext 2 created", _ctx["com.foo.module2"] is Subcontext);
        	Assert.assertStrictlyEquals("Component in subcontext 1", _ctx['module1.myComponent1'], _ctx.module1.myComponent1);
        	Assert.assertStrictlyEquals("Component in subcontext 2", _ctx['com.foo.module2.myComponent2'], _ctx['com.foo.module2'].myComponent2);
        	Assert.assertStrictlyEquals("Component subcontext injection 1", _ctx.module1, _ctx.module1.myComponent1.context);
        	
        	var module1:Subcontext = _ctx.module1 as Subcontext;
        	var module2:Subcontext = _ctx['com.foo.module2'] as Subcontext;
        	
        	Assert.assertTrue("Component module2 triggered", module2.myComponent2.triggered);
        	Assert.assertTrue("Component module1 local triggered", module1.myComponent1b.localTriggered);
        	Assert.assertFalse("Component module2 not local triggered", module2.myComponent2.localTriggered);
        	
        	Assert.assertFalse("Component injections", module1.myComponent1.myVariable === module2.myComponent2.myVariable);
        	Assert.assertStrictlyEquals("Component module1 injection", module1.myComponent1.myVariable, module1.myVariable);
        	Assert.assertStrictlyEquals("Component module1 injection", module1.myComponent1b.myVariable, module1.myVariable);
        	Assert.assertStrictlyEquals("Component module2 injection", module2.myComponent2.myVariable, module2.myVariable);
        	
        	Assert.assertFalse("Component outjections", module1.myComponent1.outjectedVariable === module2.myComponent2.outjectedVariable);
        	Assert.assertStrictlyEquals("Component module1 outjection", module1.myComponent1.outjectedVariable, module1.outjectedVariable);
        	Assert.assertStrictlyEquals("Component module2 outjection", module2.myComponent2.outjectedVariable, module2.outjectedVariable);
        	
        	Assert.assertFalse("No global variable", Tide.getInstance().isComponent("myLocalVariable"));
        	Assert.assertFalse("Component global access", _ctx.myLocalVariable is Contact);
        	Assert.assertTrue("Component local access", module1.myLocalVariable is Contact);
        	Assert.assertStrictlyEquals("Component module1 variable injection", module1.myComponent1b.myLocalVariable, module1.myLocalVariable);
        	
        	Assert.assertStrictlyEquals("Component global variable", module1.myComponent1.globalVariable, _ctx.globalVariable);
        	Assert.assertStrictlyEquals("Component global variable", module2.myComponent2.globalVariable, _ctx.globalVariable);
        	
        	_ctx.module3.myComponentModule3;
        	Assert.assertNull("No local component proxy", _ctx.module3.myRemoteComponent);
        	Assert.assertTrue("Component proxy", _ctx.myRemoteComponent is IComponent);
        	
        	var contact:Contact = new Contact();
        	module1.myVariable = contact;
        	Assert.assertStrictlyEquals("Component module1 variable injection", contact, module1.myComponent1.myVariable);
        	Assert.assertStrictlyEquals("Component module1 variable injection", contact, module1.myComponent1b.myVariable);
        	Assert.assertFalse("Component module1 variable injection", module2.myComponent2.myVariable === contact);
        }
    }
}
