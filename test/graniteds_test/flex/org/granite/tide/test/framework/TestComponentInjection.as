package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.ns.tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    use namespace tide;
    
    
    public class TestComponentInjection extends TestCase
    {
        public function TestComponentInjection() {
            super("testComponentInjection");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testComponentInjection():void {
        	Tide.getInstance().addComponents([MyComponentInject, MyComponentNSInject]);
        	
        	assertTrue(Tide.getInstance().isComponent("injectedCollection"));
        	assertTrue(Tide.getInstance().isComponent("injectedCollectionCreate"));
        	assertTrue(Tide.getInstance().isComponent("injectedEntity"));
        	assertTrue(Tide.getInstance().isComponent("injectedEntityCreate"));
        	
        	var instance:MyComponentInject = _ctx.myComponentInject;
        	var instance2:MyComponentNSInject = _ctx.myComponentNSInject;
        	
        	assertNull("Injected collection", instance.injectedCollection);
        	assertNotNull("Injected entity create", instance.injectedEntityCreate);
        	assertNotNull("Injected collection create", instance.injectedCollectionCreate);
        	assertStrictlyEquals("Injected collection create", instance.injectedCollectionCreate, _ctx.injectedCollectionCreate);
        	assertStrictlyEquals("Injected collection alias", instance.injectedCollectionAlias, _ctx.injectedCollectionCreate);
        	assertNull("Injected entity no create", instance.injectedEntity);
        	assertStrictlyEquals("Injected entity in context", instance.injectedEntityCreate, _ctx.injectedEntityCreate);
        	assertStrictlyEquals("Injected component", instance.myComponent, _ctx.myComponent);
        	assertStrictlyEquals("Injected component create", instance.myComponentAutoCreate, _ctx.myComponentAutoCreate);
        	assertNull("Injected component no create", instance.myComponentNoCreate);
        	assertNull("Injected component no create in context", _ctx.myComponentNoCreate);
        	assertTrue("Post constructor called", instance.constructed);
        	
        	assertNull("Injected NS collection", instance2.injectedCollection);
        	assertStrictlyEquals("Injected NS collection create", instance2.injectedCollectionCreate, _ctx.injectedCollectionCreate);
        	assertStrictlyEquals("Injected NS collection alias", instance2.injectedCollectionAlias, _ctx.injectedCollectionCreate);
        	assertNull("Injected NS entity no create", instance2.injectedEntity);
        	assertStrictlyEquals("Injected NS entity in context", instance2.injectedEntityCreate, _ctx.injectedEntityCreate);
        	assertStrictlyEquals("Injected NS component", instance2.myComponent, _ctx.myComponent);
        	assertStrictlyEquals("Injected NS component create", instance2.myComponentAutoCreate, _ctx.myComponentAutoCreate);
        	assertNull("Injected NS component no create", instance2.myComponentNoCreate);
        	assertNull("Injected NS component no create in context", _ctx.myComponentNoCreate);
        	
        	_ctx.injectedEntityCreate = new Contact();
        	assertStrictlyEquals("Injected entity in context after change", instance.injectedEntityCreate, _ctx.injectedEntityCreate);
        	assertStrictlyEquals("Injected NS entity in context after change", instance2.injectedEntityCreate, _ctx.injectedEntityCreate);
        }
    }
}
