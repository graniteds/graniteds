package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentOutjectionGDS481 extends TestCase
    {
        public function TestComponentOutjectionGDS481() {
            super("testComponentOutjectionGDS481");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentOutjectionGDS481():void {
        	Tide.getInstance().addComponents([MyComponentSubcontextGDS481]);
        	
        	_ctx.subcontext.dispatchEvent(new MyEvent());
        	
        	assertNull("No global outject", _ctx.myComponentOutjected);
        	assertStrictlyEquals("Subcontext outject", _ctx.subcontext.myComponentSubcontext.myComponentOutjected, _ctx.subcontext.myComponentOutjected);
        }
    }
}
