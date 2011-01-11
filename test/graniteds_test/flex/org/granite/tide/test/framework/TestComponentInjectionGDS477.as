package org.granite.tide.test.framework
{
	import flexunit.framework.TestCase;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.test.Contact;
    
    
    public class TestComponentInjectionGDS477 extends TestCase
    {
        public function TestComponentInjectionGDS477() {
            super("testComponentInjectionGDS477");
        }
        
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Application;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentInjectGDS477]);
        	_ctx.test = this;
        }
        
        
        public function testComponentInjectionGDS477():void {
        	
        	assertStrictlyEquals("Injected", _ctx, _ctx.myComponentInject.context);
        }
    }
}
