package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObservers2 extends TestCase
    {
        public function TestComponentObservers2() {
            super("testComponentObservers2");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentObservers2():void {
        	var myComponentObserverNoCreate:MyComponentObserverNoCreate = new MyComponentObserverNoCreate();
        	_ctx.myComponentObserverNoCreate = myComponentObserverNoCreate;
        	
        	_ctx.raiseEvent("someEvent");
        	assertTrue("Observer no create", myComponentObserverNoCreate.triggered);        	
        	
        	myComponentObserverNoCreate.triggered = false;
        	_ctx.myComponentObserverNoCreate = null;
        	_ctx.raiseEvent("someEvent");
        	
        	assertFalse("Observer no create", myComponentObserverNoCreate.triggered);
        	
        	var panel2:MyPanel2 = new MyPanel2();
        	_ctx.application.addChild(panel2);
        	
        	_ctx.raiseEvent("someEvent2");
        	assertTrue("Observer UI triggered", panel2.triggered);
        	
        	panel2.triggered = false;
        	_ctx.application.removeChild(panel2);
        	
        	_ctx.raiseEvent("someEvent2");
        	assertFalse("Observer UI non triggered", panel2.triggered);
        }
    }
}
