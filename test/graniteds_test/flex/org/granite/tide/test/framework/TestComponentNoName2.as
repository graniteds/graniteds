package org.granite.tide.test.framework
{
	import flexunit.framework.TestCase;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.test.Contact;
	import org.granite.tide.test.framework.MyEvent;
    
    
    public class TestComponentNoName2 extends TestCase
    {
        public function TestComponentNoName2() {
            super("testComponentNoName2");
        }
        
        private var _ctx:BaseContext;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentNoNameA, MyComponentNoNameB]);
        }
        
        
        public function testComponentNoName2():void {
			_ctx.dispatchEvent(new MyEvent());
        	assertTrue("No name A triggered", _ctx.byType(MyComponentNoNameA).triggered);
			assertTrue("No name B not triggered", !_ctx.byType(MyComponentNoNameB).triggered);
        }
    }
}
