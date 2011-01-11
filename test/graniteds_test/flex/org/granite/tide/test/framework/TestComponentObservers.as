package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentObservers extends TestCase
    {
        public function TestComponentObservers() {
            super("testComponentObservers");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentObserver, MyComponentNSObserver, MyComponentObserverNoCreate]);
        }
        
        
        public function testComponentObservers():void {
        	_ctx.raiseEvent("someEvent");
        	assertNull("Observer no create", _ctx.myComponentObserverNoCreate);
        	
        	_ctx.application.dispatchEvent(new MyEvent());
        	assertEquals("Observer typed event", 1, _ctx.myComponentObserver.typedEvent);
        	assertEquals("Observer NS typed event", 1, _ctx.myComponentNSObserver.typedEvent);
        	
        	_ctx.dispatchEvent(new MyEvent());
        	assertEquals("Observer typed event from context", 2, _ctx.myComponentObserver.typedEvent);
        	assertEquals("Observer NS typed event from context", 2, _ctx.myComponentNSObserver.typedEvent);
        	
        	_ctx.application.dispatchEvent(new TideUIEvent("someEvent2"));
        	assertTrue("Observer untyped event with context", _ctx.myComponentObserver.untypedEventWithContext);
        	assertTrue("Observer NS untyped event with context", _ctx.myComponentNSObserver.untypedEventWithContext);
        	assertTrue("Multiple observer event 2", _ctx.myComponentObserver.multipleObserverEvent2);
        	assertFalse("Multiple observer event 3", _ctx.myComponentObserver.multipleObserverEvent3);  // Not yet triggered
        	
        	_ctx.application.dispatchEvent(new TideUIEvent("someEvent3", "toto", true));
        	assertTrue("Observer untyped event with args", _ctx.myComponentObserver.untypedEventWithArgs);
        	assertEquals("Observer untyped event arg1", "toto", _ctx.myComponentObserver.untypedEventArg1); 
        	assertEquals("Observer untyped event arg2", true, _ctx.myComponentObserver.untypedEventArg2); 
        	assertTrue("Observer NS untyped event with args", _ctx.myComponentNSObserver.untypedEventWithArgs);
        	assertEquals("Observer NS untyped event arg1", "toto", _ctx.myComponentNSObserver.untypedEventArg1); 
        	assertEquals("Observer NS untyped event arg2", true, _ctx.myComponentNSObserver.untypedEventArg2); 
        	assertTrue("Multiple observer event 3", _ctx.myComponentObserver.multipleObserverEvent3);
        }
    }
}
