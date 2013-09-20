package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestUIComponentModules
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponent("module1.myComponent1", MyComponentModule1);
            Tide.getInstance().addComponent("module2.myComponent2", MyComponentModule2);
        }
        
        
        [Test]
        public function testUIComponentModules():void {
        	var myPanel2:MyPanel4 = new MyPanel4();
			_ctx['module1.myPanel'] = myPanel2;
        	UIImpersonator.addChild(myPanel2);
        	
        	myPanel2.dispatchEvent(new MyEvent());
        	
        	Assert.assertTrue("Component module1 triggered", _ctx['module1.myComponent1'].triggered);
        	Assert.assertFalse("Component module2 not triggered", _ctx['module2.myComponent2'].triggered);
        	
			UIImpersonator.removeChild(myPanel2);
        }
    }
}
