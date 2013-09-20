package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentOutjectionGDS427
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentOutjectionGDS427():void {
        	Tide.getInstance().addComponent("injected1", MyComponentInject2);
        	Tide.getInstance().addComponent("injected2", MyComponentInject2);
        	
        	Tide.getInstance().addComponents([MyComponentOutject2]);
        	
        	var injected1:MyComponentInject2 = _ctx.injected1;
        	var injected2:MyComponentInject2 = _ctx.injected2;
        	
        	_ctx.raiseEvent("createContact");
        	Assert.assertStrictlyEquals("Contact created", injected1.contact, _ctx.myComponentOutject2.contact);
        	Assert.assertStrictlyEquals("Contact created", injected2.contact, _ctx.myComponentOutject2.contact);
        	
        	_ctx.raiseEvent("updateContact");
        	Assert.assertStrictlyEquals("Contact updated", injected1.contact, _ctx.myComponentOutject2.contact);
        	Assert.assertStrictlyEquals("Contact updated", injected2.contact, _ctx.myComponentOutject2.contact);
        	
        	_ctx.raiseEvent("updateContact");
        	Assert.assertStrictlyEquals("Contact updated 2", injected2.contact, _ctx.myComponentOutject2.contact);
        	Assert.assertStrictlyEquals("Contact updated 2", injected2.contact, _ctx.myComponentOutject2.contact);
        }
    }
}
