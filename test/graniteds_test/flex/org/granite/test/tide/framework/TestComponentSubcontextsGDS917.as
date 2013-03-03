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
			Tide.getInstance().addComponent("com.foo.myComponentB", MyComponentSubcontextB);
            Tide.getInstance().addComponent("com.foo.bar.myComponentA1", MyComponentSubcontextA1);
        }
        
        
        [Test]
        public function testComponentSubcontextsGDS917():void {
			var myComponentB:MyComponentSubcontextB = _ctx["com.foo.myComponentB"] as MyComponentSubcontextB;
			myComponentB.dispatchEvent(new MyEvent());
			
			Assert.assertEquals("Component foo.bar.A1 not triggered", 0, _ctx["com.foo.bar.myComponentA1"].triggered);
			
			_ctx["com.foo.bar.myComponentB"] = myComponentB;
			myComponentB.dispatchEvent(new MyEvent());
        	
        	Assert.assertEquals("Component foo.bar.A1 triggered", 1, _ctx["com.foo.bar.myComponentA1"].triggered);
        }
    }
}
