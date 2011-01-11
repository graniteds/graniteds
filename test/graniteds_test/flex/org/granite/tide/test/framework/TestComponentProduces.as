package org.granite.tide.test.framework
{
	import flash.utils.describeType;
	
	import flexunit.framework.TestCase;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
    
    
    public class TestComponentProduces extends TestCase
    {
        public function TestComponentProduces() {
            super("testComponentProduces");
        }
        
        private var _ctx:BaseContext;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentNoName2, MyComponentNoName3]);
        }
        
        
        public function testComponentProduces():void {
        	assertTrue("Injected by producer method", _ctx.myComponentNoName2.injected is MyComponentNoName); 
        }
    }
}
