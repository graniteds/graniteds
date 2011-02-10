package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.PersonService;
    
    
    public class TestTypedRemoteInjection
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedInjectedComponent2]);
        }
        
        
        [Test]
        public function testTypedRemoteInjection():void {
			var comp:Object = _ctx.myTypedInjectedComponent2;
			Assert.assertTrue(comp.personService is PersonService);
        }
    }
}
