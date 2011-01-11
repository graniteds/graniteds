package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.seam.Seam;
    
    
    public class TestComponentInjectionForum7129 extends TestCase
    {
        public function TestComponentInjectionForum7129() {
            super("testComponentInjectionForum7129");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Seam.resetInstance();
            _ctx = Seam.getInstance().getContext();
            Seam.getInstance().initApplication();
        }
        
        
        public function testComponentInjectionForum7129():void {
        	Seam.getInstance().addComponents([MyComponent5, MyPanel5]);
        	
        	_ctx.raiseEvent("testEvent");
        	assertNotNull("Component injected in ctl", _ctx.myComponent5.myQuery);
        	assertNotNull("Panel injected in ctl", _ctx.myComponent5.myPanel5);
        	assertNotNull("Component injected in panel", _ctx.myPanel5.myQuery);
        	assertStrictlyEquals("Same Component", _ctx.myComponent5.myQuery, _ctx.myPanel5.myQuery);
        	
        	_ctx.application.removeChild(_ctx.myPanel5); 
        }
    }
}
