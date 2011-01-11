package org.granite.tide.test.framework
{
	import flash.utils.describeType;
	
	import flexunit.framework.TestCase;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
    
    
    public class TestComponentProduces2 extends TestCase
    {
        public function TestComponentProduces2() {
            super("testComponentProduces2");
        }
        
        private var _ctx:BaseContext;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentNoName2, MyComponentNoName4]);
        }
        
        
        public function testComponentProduces2():void {
        	assertTrue("Injected by producer property", _ctx.myComponentNoName2.injected is MyComponentNoName);
        }
    }
}
