package org.granite.tide.test.framework
{
	import __AS3__.vec.Vector;
	
	import flexunit.framework.TestCase;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.test.Contact;
    
    
    public class TestComponentInjectionGDS676 extends TestCase
    {
        public function TestComponentInjectionGDS676() {
            super("testComponentInjectionGDS676");
        }
        
        private var _ctx:BaseContext;
        
  		
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			Tide.getInstance().addComponents([MyComponentInjectGDS676]);
        }
        
        
        public function testComponentInjectionGDS676():void {
			_ctx.testVector = Vector.<Contact>([ new Contact() ]);
        	assertStrictlyEquals("Test vector injected", _ctx.testVector, _ctx.myComponentInject.testVector);
        	assertNotNull("Test vector.<*> created", _ctx.myComponentInject.testVectorWild);
        }
    }
}
