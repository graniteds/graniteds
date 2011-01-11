package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentOutjectionGDS449 extends TestCase
    {
        public function TestComponentOutjectionGDS449() {
            super("testComponentOutjectionGDS449");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentOutjectionGDS449():void {
        	Tide.getInstance().addComponents([MyComponentOutjectGDS449]);
        	
        	_ctx.raiseEvent("defineBean");
        	assertStrictlyEquals("Bean defined", _ctx.bean, _ctx.myComponentOutject449.bean);
        	
        	_ctx.raiseEvent("defineBean");
        	assertStrictlyEquals("Bean defined 2", _ctx.bean, _ctx.myComponentOutject449.bean);
        	
        	_ctx.raiseEvent("defineBean");
        	assertStrictlyEquals("Bean defined 3", _ctx.bean, _ctx.myComponentOutject449.bean);
        }
    }
}
