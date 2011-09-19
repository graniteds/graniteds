package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.ns.tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    use namespace tide;
    
    
    public class TestComponentInjection4
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testComponentInjection3():void {
        	_ctx.myComponent = new MyTypedComponent3();
        	
			Assert.assertTrue("Service injection", _ctx.myComponent.service is MyService);
        }
    }
}
