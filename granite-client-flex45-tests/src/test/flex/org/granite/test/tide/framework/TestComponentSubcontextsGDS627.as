package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontextsGDS627
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            Tide.getInstance().setComponentGlobal("myEventTriggered", true);
            _ctx = Tide.getInstance().getContext();
            _ctx.myEventTriggered = 0;
            Tide.getInstance().addComponent("com.foo.bar.myComponentB", MyComponentSubcontextB);
            Tide.getInstance().addComponent("com.foo.myComponentA1", MyComponentSubcontextA1);
            Tide.getInstance().addComponent("com.foo.bar.myComponentB1", MyComponentSubcontextA1);
        }
        
        
        [Test]
        public function testComponentSubcontextsGDS627():void {
        	_ctx["com.foo.bar.myComponentB"].dispatchEvent(new MyEvent());
        	
        	Assert.assertEquals("Component A1 triggered", 2, _ctx["com.foo.myComponentA1"].triggered);
        	Assert.assertEquals("Component B1 triggered", 1, _ctx["com.foo.bar.myComponentB1"].triggered);
        }
    }
}
