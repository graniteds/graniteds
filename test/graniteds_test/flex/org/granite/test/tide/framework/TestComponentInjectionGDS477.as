package org.granite.test.tide.framework
{
	import org.flexunit.Assert;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.test.tide.Contact;
    
    
    public class TestComponentInjectionGDS477
    {
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Object;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentInjectGDS477]);
        	_ctx.test = this;
        }
        
        
        [Test]
        public function testComponentInjectionGDS477():void {        	
        	Assert.assertStrictlyEquals("Injected", _ctx, _ctx.myComponentInject.context);
        }
    }
}
