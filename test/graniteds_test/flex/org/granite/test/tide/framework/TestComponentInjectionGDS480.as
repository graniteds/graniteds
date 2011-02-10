package org.granite.test.tide.framework
{
	import org.flexunit.Assert;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.seam.Seam;
    
    
    public class TestComponentInjectionGDS480
    {
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Object;
  		         
        
        [Before]
        public function setUp():void {
            Seam.resetInstance();
            _ctx = Seam.getInstance().getContext();
            Seam.getInstance().initApplication();
        	_ctx.test = this;
        }
        
        
        [Test]
        public function testComponentInjectionGDS480():void {
        	// Create proxy
        	_ctx.myComponentInject2;
        	
        	Assert.assertTrue("Restricted", Seam.getInstance().isComponentRestrict("myComponentInject2"));
        	Assert.assertTrue("Remote", Seam.getInstance().getComponentRemoteSync("myComponentInject2") == Tide.SYNC_BIDIRECTIONAL);
        	
        	// Replace by real component
        	_ctx.myComponentInject2 = new MyComponentInject2();
        	
        	Assert.assertTrue("Instance", _ctx.myComponentInject2 is MyComponentInject2);
        	Assert.assertFalse("Restricted", Seam.getInstance().isComponentRestrict("myComponentInject2"));
        	Assert.assertFalse("Remote", Seam.getInstance().getComponentRemoteSync("myComponentInject2") == Tide.SYNC_BIDIRECTIONAL);
        }
    }
}
