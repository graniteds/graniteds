package org.granite.test.tide.framework
{
    import mx.utils.ObjectProxy;
    
    import org.flexunit.Assert;
    import org.granite.ns.tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    use namespace tide;
    
    
    public class TestComponentInjectionProxy
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testComponentInjectionProxy():void {
			_ctx.myComponent1 = new MyComponentInjectProxy();
			_ctx.myComponent2 = new MyComponentInjectProxy();
        	
			var p:Person = new Person();
			p.id = 1;
			_ctx.myComponent1.change(p);
			
			Assert.assertEquals("Proxy injection", 1, _ctx.myComponent2.objectProxy.id);
        }
    }
}
