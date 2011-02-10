package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.ns.tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    use namespace tide;
    
    
    public class TestComponentInjection2
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testComponentInjection2():void {
        	Tide.getInstance().addComponents([MyComponent1]);
        	
        	_ctx.myComponent1;
        	
        	Assert.assertNull("No interface implicit injection", _ctx.myComponent1.myComponent2);
        	
        	_ctx.myComponent2 = new MyComponent2();
        	
			Assert.assertStrictlyEquals("Explicit interface injection", _ctx.myComponent2, _ctx.myComponent1.myComponent2);
        }
    }
}
