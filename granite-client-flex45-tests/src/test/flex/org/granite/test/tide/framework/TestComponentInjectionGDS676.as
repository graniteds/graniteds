package org.granite.test.tide.framework
{
	import __AS3__.vec.Vector;
	
	import org.flexunit.Assert;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.test.tide.Contact;
    
    
    public class TestComponentInjectionGDS676
    {
        private var _ctx:BaseContext;
        
  		
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			Tide.getInstance().addComponents([MyComponentInjectGDS676]);
        }
        
        
        [Test]
        public function testComponentInjectionGDS676():void {
			_ctx.testVector = Vector.<Contact>([ new Contact() ]);
        	Assert.assertStrictlyEquals("Test vector injected", _ctx.testVector, _ctx.myComponentInject.testVector);
        	Assert.assertNotNull("Test vector.<*> created", _ctx.myComponentInject.testVectorWild);
        }
    }
}
