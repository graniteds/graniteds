package org.granite.test.tide.framework
{
    import mx.core.Application;
    
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.seam.Seam;
    
    
    public class TestComponentInjectionForum7129
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Seam.resetInstance();
            _ctx = Seam.getInstance().getContext();
            Seam.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentInjectionForum7129():void {
        	Seam.getInstance().addComponents([MyComponent5, MyPanel5]);
        	
        	_ctx.raiseEvent("testEvent");
			Assert.assertNotNull("Component injected in ctl", _ctx.myComponent5.myQuery);
			Assert.assertNotNull("Panel injected in ctl", _ctx.myComponent5.myPanel5);
			Assert.assertNotNull("Component injected in panel", _ctx.myPanel5.myQuery);
			Assert.assertStrictlyEquals("Same Component", _ctx.myComponent5.myQuery, _ctx.myPanel5.myQuery);
        	
			UIImpersonator.removeChild(_ctx.myPanel5); 
        }
    }
}
