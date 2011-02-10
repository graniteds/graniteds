package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObservers3
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentConversationA, MyComponentConversationB]);
        }
        
        
        [Test]
        public function testComponentObservers3():void {
        	var ctx:BaseContext = Tide.getInstance().getContext("convCtx");
        	
        	ctx.raiseEvent("start");
        	
        	Assert.assertNotNull("Component A created", ctx.myComponentA);
        	Assert.assertNull("Component B not created", ctx.meta_getInstance("myComponentB", false, true));
        }
    }
}
