package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.ns.tide;
    
    use namespace tide;
    
    
    public class TestComponentOutjection
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentOutjection():void {
        	Tide.getInstance().addComponents([MyComponentOutject, MyComponentNSOutject]);
        	
        	var contact2:Contact = new Contact();
        	_ctx.inoutContact2 = contact2;
        	
        	_ctx.raiseEvent("createContact");
        	Assert.assertStrictlyEquals("Contact created", _ctx.contact, _ctx.myComponentOutject.contact);
        	Assert.assertStrictlyEquals("Contact NS created", _ctx.nscontact, _ctx.myComponentNSOutject.tide::nscontact);
        	
        	Assert.assertNotNull("Contact inout 1", _ctx.inoutContact1);
        	Assert.assertStrictlyEquals("Contact inout 1", _ctx.inoutContact1, _ctx.myComponentOutject.inoutContact1);
        	
        	Assert.assertStrictlyEquals("Contact inout 2", _ctx.inoutContact2, contact2);
        	Assert.assertStrictlyEquals("Contact inout 2", _ctx.inoutContact2, _ctx.myComponentOutject.inoutContact2);
        	
        	Assert.assertNull("Contact inout 3", _ctx.inoutContact3);
        	Assert.assertNull("Contact inout 3", _ctx.myComponentOutject.inoutContact3);
        }
    }
}
