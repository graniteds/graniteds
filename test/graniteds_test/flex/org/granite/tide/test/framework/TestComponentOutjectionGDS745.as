package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentOutjectionGDS745 extends TestCase
    {
        public function TestComponentOutjectionGDS745() {
            super("testComponentOutjectionGDS745");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentOutjectionGDS745():void {
        	Tide.getInstance().addComponents([MyComponentOutjectGDS745]);
        	
        	_ctx.myComponentOutject745;
        	
        	assertTrue("Number outjected", isNaN(_ctx.value));
        	
        	_ctx.raiseEvent("defineValue");
        	
        	assertEquals("Number outjected 2", 12, _ctx.value);
        }
    }
}
