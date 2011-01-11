package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.ns.tide;
    
    use namespace tide;
    
    
    public class TestComponentOutjection extends TestCase
    {
        public function TestComponentOutjection() {
            super("testComponentOutjection");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentOutjection():void {
        	Tide.getInstance().addComponents([MyComponentOutject, MyComponentNSOutject]);
        	
        	var contact2:Contact = new Contact();
        	_ctx.inoutContact2 = contact2;
        	
        	_ctx.raiseEvent("createContact");
        	assertStrictlyEquals("Contact created", _ctx.contact, _ctx.myComponentOutject.contact);
        	assertStrictlyEquals("Contact NS created", _ctx.nscontact, _ctx.myComponentNSOutject.nscontact);
        	
        	assertNotNull("Contact inout 1", _ctx.inoutContact1);
        	assertStrictlyEquals("Contact inout 1", _ctx.inoutContact1, _ctx.myComponentOutject.inoutContact1);
        	
        	assertStrictlyEquals("Contact inout 2", _ctx.inoutContact2, contact2);
        	assertStrictlyEquals("Contact inout 2", _ctx.inoutContact2, _ctx.myComponentOutject.inoutContact2);
        	
        	assertNull("Contact inout 3", _ctx.inoutContact3);
        	assertNull("Contact inout 3", _ctx.myComponentOutject.inoutContact3);
        }
    }
}
