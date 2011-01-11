package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.ns.tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    use namespace tide;
    
    
    public class TestComponentInjection2 extends TestCase
    {
        public function TestComponentInjection2() {
            super("testComponentInjection2");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testComponentInjection2():void {
        	Tide.getInstance().addComponents([MyComponent1]);
        	
        	_ctx.myComponent1;
        	
        	assertNull("No interface implicit injection", _ctx.myComponent1.myComponent2);
        	
        	_ctx.myComponent2 = new MyComponent2();
        	
        	assertStrictlyEquals("Explicit interface injection", _ctx.myComponent2, _ctx.myComponent1.myComponent2);
        }
    }
}
