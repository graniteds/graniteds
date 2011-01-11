package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedLookup extends TestCase
    {
        public function TestTypedLookup() {
            super("testTypedLookup");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedComponent]);
        }
        
        public function testTypedLookup():void {
        	assertNotNull("ByType", _ctx.byType(IMyTypedComponent));
        	
        	_ctx.myComp = new MyTypedComponent2();
        	
        	assertEquals("AllByType", 2, _ctx.allByType(IMyTypedComponent, true).length);
        }
    }
}
