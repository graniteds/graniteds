package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedLookup
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedComponent]);
        }
        
        [Test]
        public function testTypedLookup():void {
        	Assert.assertNotNull("ByType", _ctx.byType(IMyTypedComponent));
        	
        	_ctx.myComp = new MyTypedComponent2();
        	
        	Assert.assertEquals("AllByType", 2, _ctx.allByType(IMyTypedComponent, true).length);
        }
    }
}
