package org.granite.tide.test.framework
{
	import flexunit.framework.TestCase;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.test.Contact;
    
    
    public class TestComponentNoName extends TestCase
    {
        public function TestComponentNoName() {
            super("testComponentNoName");
        }
        
        private var _ctx:BaseContext;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentNoName, MyComponentNoName2]);
        }
        
        
        public function testComponentNoName():void {
        	assertTrue("No name injected", _ctx.myComponentNoName2.injected is MyComponentNoName);
        }
    }
}
