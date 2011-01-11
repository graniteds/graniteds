package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.Component;
    
    
    public class TestSimpleComponent extends TestCase
    {
        public function TestSimpleComponent() {
            super("testSimpleComponent");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testSimpleComponent():void {
        	assertNull("No default component", _ctx.myDefaultComponent);
        	assertFalse(Tide.getInstance().isComponent("myDefaultComponent"));
        	
        	Tide.getInstance().addComponent("myComponent", ArrayCollection);
        	assertTrue("autoCreate component", _ctx.myComponent is ArrayCollection);
        	Tide.getInstance().addComponent("myComponentNoCreate", ArrayCollection, false, false);
        	assertTrue("non autoCreate component", _ctx.myComponentNoCreate == null);
        }
    }
}
