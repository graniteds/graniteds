package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.ns.tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    use namespace tide;
    
    
    public class TestComponentInjection
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testComponentInjection():void {
        	Tide.getInstance().addComponents([MyComponentInject, MyComponentNSInject]);
        	
			Assert.assertTrue(Tide.getInstance().isComponent("injectedCollection"));
			Assert.assertTrue(Tide.getInstance().isComponent("injectedCollectionCreate"));
			Assert.assertTrue(Tide.getInstance().isComponent("injectedEntity"));
			Assert.assertTrue(Tide.getInstance().isComponent("injectedEntityCreate"));
        	
        	var instance:MyComponentInject = _ctx.myComponentInject;
        	var instance2:MyComponentNSInject = _ctx.myComponentNSInject;
        	
			Assert.assertNull("Injected collection", instance.injectedCollection);
			Assert.assertNotNull("Injected entity create", instance.injectedEntityCreate);
			Assert.assertNotNull("Injected collection create", instance.injectedCollectionCreate);
			Assert.assertStrictlyEquals("Injected collection create", instance.injectedCollectionCreate, _ctx.injectedCollectionCreate);
			Assert.assertStrictlyEquals("Injected collection alias", instance.injectedCollectionAlias, _ctx.injectedCollectionCreate);
			Assert.assertNull("Injected entity no create", instance.injectedEntity);
			Assert.assertStrictlyEquals("Injected entity in context", instance.injectedEntityCreate, _ctx.injectedEntityCreate);
			Assert.assertStrictlyEquals("Injected component", instance.myComponent, _ctx.myComponent);
			Assert.assertStrictlyEquals("Injected component create", instance.myComponentAutoCreate, _ctx.myComponentAutoCreate);
			Assert.assertNull("Injected component no create", instance.myComponentNoCreate);
			Assert.assertNull("Injected component no create in context", _ctx.myComponentNoCreate);
			Assert.assertTrue("Post constructor called", instance.constructed);
        	
			Assert.assertNull("Injected NS collection", instance2.injectedCollection);
			Assert.assertStrictlyEquals("Injected NS collection create", instance2.injectedCollectionCreate, _ctx.injectedCollectionCreate);
			Assert.assertStrictlyEquals("Injected NS collection alias", instance2.injectedCollectionAlias, _ctx.injectedCollectionCreate);
			Assert.assertNull("Injected NS entity no create", instance2.injectedEntity);
			Assert.assertStrictlyEquals("Injected NS entity in context", instance2.injectedEntityCreate, _ctx.injectedEntityCreate);
			Assert.assertStrictlyEquals("Injected NS component", instance2.myComponent, _ctx.myComponent);
			Assert.assertStrictlyEquals("Injected NS component create", instance2.myComponentAutoCreate, _ctx.myComponentAutoCreate);
			Assert.assertNull("Injected NS component no create", instance2.myComponentNoCreate);
			Assert.assertNull("Injected NS component no create in context", _ctx.myComponentNoCreate);
        	
        	_ctx.injectedEntityCreate = new Contact();
			Assert.assertStrictlyEquals("Injected entity in context after change", instance.injectedEntityCreate, _ctx.injectedEntityCreate);
			Assert.assertStrictlyEquals("Injected NS entity in context after change", instance2.injectedEntityCreate, _ctx.injectedEntityCreate);
        }
    }
}
