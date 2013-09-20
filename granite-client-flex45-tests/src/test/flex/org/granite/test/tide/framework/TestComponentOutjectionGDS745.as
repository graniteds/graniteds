package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentOutjectionGDS745
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentOutjectionGDS745():void {
        	Tide.getInstance().addComponents([MyComponentOutjectGDS745]);
        	
        	_ctx.myComponentOutject745;
        	
        	Assert.assertTrue("Number outjected", isNaN(_ctx.value));
        	
        	_ctx.raiseEvent("defineValue");
        	
        	Assert.assertEquals("Number outjected 2", 12, _ctx.value);
        }
    }
}
