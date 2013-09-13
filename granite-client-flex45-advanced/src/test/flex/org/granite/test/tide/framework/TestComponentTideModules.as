package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentTideModules
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			Tide.getInstance().addModule(MyModule1);
        }
        
        
        [Test]
        public function testComponentTideModules():void {
        	_ctx.application.dispatchEvent(new MyEvent());
        	
        	Assert.assertTrue("Component module1 event1 triggered", _ctx['module1.myComponentB'].triggered1);
        	Assert.assertFalse("Component module1 event2 not triggered", _ctx['module1.myComponentB'].triggered2);
        	Assert.assertFalse("Component module2 event1 not triggered", _ctx['module2.myComponentB'].triggered1);
        	Assert.assertTrue("Component module2 event2 triggered", _ctx['module2.myComponentB'].triggered2);
        }
    }
}
