package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.Component;
    
    
    public class TestSimpleComponent
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testSimpleComponent():void {
        	Assert.assertNull("No default component", _ctx.myDefaultComponent);
        	Assert.assertFalse(Tide.getInstance().isComponent("myDefaultComponent"));
        	
        	Tide.getInstance().addComponent("myComponent", ArrayCollection);
        	Assert.assertTrue("autoCreate component", _ctx.myComponent is ArrayCollection);
        	Tide.getInstance().addComponent("myComponentNoCreate", ArrayCollection, false, false);
        	Assert.assertTrue("non autoCreate component", _ctx.myComponentNoCreate == null);
        }
    }
}
