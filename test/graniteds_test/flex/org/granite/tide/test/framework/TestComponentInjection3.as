package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.ns.tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    use namespace tide;
    
    
    public class TestComponentInjection3 extends TestCase
    {
        public function TestComponentInjection3() {
            super("testComponentInjection3");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testComponentInjection3():void {
        	Tide.getInstance().addComponents([MyComponent1, MyComponent2]);
        	
        	_ctx.myComponent1;
        	
        	assertStrictlyEquals("Interface injection", _ctx.myComponent2, _ctx.myComponent1.myComponent2);
        }
    }
}
