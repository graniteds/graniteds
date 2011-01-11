package org.granite.tide.test.framework
{
	import flexunit.framework.TestCase;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.seam.Seam;
    
    
    public class TestComponentInjectionGDS480 extends TestCase
    {
        public function TestComponentInjectionGDS480() {
            super("testComponentInjectionGDS480");
        }
        
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Application;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Seam.resetInstance();
            _ctx = Seam.getInstance().getContext();
            Seam.getInstance().initApplication();
        	_ctx.test = this;
        }
        
        
        public function testComponentInjectionGDS480():void {
        	// Create proxy
        	_ctx.myComponentInject2;
        	
        	assertTrue("Restricted", Seam.getInstance().isComponentRestrict("myComponentInject2"));
        	assertTrue("Remote", Seam.getInstance().getComponentRemoteSync("myComponentInject2") == Tide.SYNC_BIDIRECTIONAL);
        	
        	// Replace by real component
        	_ctx.myComponentInject2 = new MyComponentInject2();
        	
        	assertTrue("Instance", _ctx.myComponentInject2 is MyComponentInject2);
        	assertFalse("Restricted", Seam.getInstance().isComponentRestrict("myComponentInject2"));
        	assertFalse("Remote", Seam.getInstance().getComponentRemoteSync("myComponentInject2") == Tide.SYNC_BIDIRECTIONAL);
        }
    }
}
