package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObservers2
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentObservers2():void {
        	var myComponentObserverNoCreate:MyComponentObserverNoCreate = new MyComponentObserverNoCreate();
        	_ctx.myComponentObserverNoCreate = myComponentObserverNoCreate;
        	
        	_ctx.raiseEvent("someEvent");
        	Assert.assertTrue("Observer no create", myComponentObserverNoCreate.triggered);        	
        	
        	myComponentObserverNoCreate.triggered = false;
        	_ctx.myComponentObserverNoCreate = null;
        	_ctx.raiseEvent("someEvent");
        	
        	Assert.assertFalse("Observer no create", myComponentObserverNoCreate.triggered);
        	
        	var panel2:MyPanel2 = new MyPanel2();
        	UIImpersonator.addChild(panel2);
        	
        	_ctx.raiseEvent("someEvent2");
        	Assert.assertTrue("Observer UI triggered", panel2.triggered);
        	
        	panel2.triggered = false;
			UIImpersonator.removeChild(panel2);
        	
        	_ctx.raiseEvent("someEvent2");
        	Assert.assertFalse("Observer UI non triggered", panel2.triggered);
        }
    }
}
