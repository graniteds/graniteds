package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.PersonService;
    
    
    public class TestTypedRemoteInjection extends TestCase
    {
        public function TestTypedRemoteInjection() {
            super("testTypedRemoteInjection");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedInjectedComponent2]);
        }
        
        
        public function testTypedRemoteInjection():void {
			var comp:Object = _ctx.myTypedInjectedComponent2;
			assertTrue(comp.personService is PersonService);
        }
    }
}
