package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentOutjectionGDS427 extends TestCase
    {
        public function TestComponentOutjectionGDS427() {
            super("testComponentOutjectionGDS427");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentOutjectionGDS427():void {
        	Tide.getInstance().addComponent("injected1", MyComponentInject2);
        	Tide.getInstance().addComponent("injected2", MyComponentInject2);
        	
        	Tide.getInstance().addComponents([MyComponentOutject2]);
        	
        	var injected1:MyComponentInject2 = _ctx.injected1;
        	var injected2:MyComponentInject2 = _ctx.injected2;
        	
        	_ctx.raiseEvent("createContact");
        	assertStrictlyEquals("Contact created", injected1.contact, _ctx.myComponentOutject2.contact);
        	assertStrictlyEquals("Contact created", injected2.contact, _ctx.myComponentOutject2.contact);
        	
        	_ctx.raiseEvent("updateContact");
        	assertStrictlyEquals("Contact updated", injected1.contact, _ctx.myComponentOutject2.contact);
        	assertStrictlyEquals("Contact updated", injected2.contact, _ctx.myComponentOutject2.contact);
        	
        	_ctx.raiseEvent("updateContact");
        	assertStrictlyEquals("Contact updated 2", injected2.contact, _ctx.myComponentOutject2.contact);
        	assertStrictlyEquals("Contact updated 2", injected2.contact, _ctx.myComponentOutject2.contact);
        }
    }
}
