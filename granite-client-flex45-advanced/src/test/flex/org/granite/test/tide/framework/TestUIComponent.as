package org.granite.test.tide.framework
{
    import mx.containers.Panel;
    
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestUIComponent
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testUIComponent():void {
        	var myPanel:MyPanel = new MyPanel();
        	UIImpersonator.addChild(myPanel);
        	var myPanel2:Panel = new MyPanel2();
			UIImpersonator.addChild(myPanel2);
			var myPanel3:Panel = new MyPanel3();
			UIImpersonator.addChild(myPanel3);
        	
        	Assert.assertTrue("MyPanel component", Tide.getInstance().isComponent("myPanel"));
        	Assert.assertStrictlyEquals("MyPanel", _ctx.myPanel, myPanel);
        	Assert.assertStrictlyEquals("MyPanel2", _ctx[Tide.internalUIComponentName(myPanel2)], myPanel2);
			Assert.assertStrictlyEquals("MyPanel3", _ctx[Tide.internalUIComponentName(myPanel3)], myPanel3);
        	
			UIImpersonator.removeChild(myPanel);
			UIImpersonator.removeChild(myPanel2);
			UIImpersonator.removeChild(myPanel3);
        }
    }
}
