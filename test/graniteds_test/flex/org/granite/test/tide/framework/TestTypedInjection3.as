package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestTypedInjection3
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			
			Tide.getInstance().addComponents([MyTypedInjectedComponent3]);
        }
        
        
        [Test]
        public function testTypedInjection3():void {
			var comp:Object = _ctx.myTypedInjectedComponent3;
			Assert.assertNull(comp.byInterface);
			
			_ctx.blabla = new MyTypedComponent();
			Assert.assertStrictlyEquals("Component injected", _ctx.blabla, comp.byInterface);
			
			_ctx.blabla = null;
			Assert.assertNull("Component disinjected", comp.byInterface);
			
			_ctx.bloblo = new MyOtherTypedComponent();
			Assert.assertStrictlyEquals("Component injected", _ctx.bloblo, comp.byInterface);
        }
    }
}
