package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontextsGDS917
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            Tide.getInstance().setComponentGlobal("myEventTriggered", true);
            _ctx = Tide.getInstance().getContext();
            _ctx.myEventTriggered = 0;
			Tide.getInstance().addComponent("com.foo.myPanelB", MyPanelB);
            Tide.getInstance().addComponent("com.foo.bar.myPanelA1", MyPanelA1);
        }
        
        
        [Test]
        public function testComponentSubcontextsGDS917():void {
			var myPanelB:MyPanelB = _ctx["com.foo.myPanelB"] as MyPanelB;
			myPanelB.dispatchEvent(new MyEvent());
			
			Assert.assertEquals("Component foo.bar.A1 not triggered", 0, _ctx["com.foo.bar.myPanelA1"].triggered);
			
			_ctx["com.foo.bar.myPanelB"] = myPanelB;
			myPanelB.dispatchEvent(new MyEvent());
        	
        	Assert.assertEquals("Component foo.bar.A1 triggered", 1, _ctx["com.foo.bar.myPanelA1"].triggered);
        }
    }
}
