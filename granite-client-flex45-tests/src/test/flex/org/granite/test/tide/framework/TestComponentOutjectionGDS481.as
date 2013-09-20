package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentOutjectionGDS481
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentOutjectionGDS481():void {
        	Tide.getInstance().addComponents([MyComponentSubcontextGDS481]);
        	
        	_ctx.subcontext.dispatchEvent(new MyEvent());
        	
        	Assert.assertNull("No global outject", _ctx.myComponentOutjected);
        	Assert.assertStrictlyEquals("Subcontext outject", _ctx.subcontext.myComponentSubcontext.myComponentOutjected, _ctx.subcontext.myComponentOutjected);
        }
    }
}
