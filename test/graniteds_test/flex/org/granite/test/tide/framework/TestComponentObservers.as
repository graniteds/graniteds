package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentObservers
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentObserver, MyComponentNSObserver, MyComponentObserverNoCreate]);
        }
        
        
        [Test]
        public function testComponentObservers():void {
        	_ctx.raiseEvent("someEvent");
        	Assert.assertNull("Observer no create", _ctx.myComponentObserverNoCreate);
        	
        	_ctx.application.dispatchEvent(new MyEvent());
        	Assert.assertEquals("Observer typed event", 1, _ctx.myComponentObserver.typedEvent);
        	Assert.assertEquals("Observer NS typed event", 1, _ctx.myComponentNSObserver.typedEvent);
        	
        	_ctx.dispatchEvent(new MyEvent());
        	Assert.assertEquals("Observer typed event from context", 2, _ctx.myComponentObserver.typedEvent);
        	Assert.assertEquals("Observer NS typed event from context", 2, _ctx.myComponentNSObserver.typedEvent);
        	
        	_ctx.application.dispatchEvent(new TideUIEvent("someEvent2"));
        	Assert.assertTrue("Observer untyped event with context", _ctx.myComponentObserver.untypedEventWithContext);
        	Assert.assertTrue("Observer NS untyped event with context", _ctx.myComponentNSObserver.untypedEventWithContext);
        	Assert.assertTrue("Multiple observer event 2", _ctx.myComponentObserver.multipleObserverEvent2);
        	Assert.assertFalse("Multiple observer event 3", _ctx.myComponentObserver.multipleObserverEvent3);  // Not yet triggered
        	
        	_ctx.application.dispatchEvent(new TideUIEvent("someEvent3", "toto", true));
        	Assert.assertTrue("Observer untyped event with args", _ctx.myComponentObserver.untypedEventWithArgs);
        	Assert.assertEquals("Observer untyped event arg1", "toto", _ctx.myComponentObserver.untypedEventArg1); 
        	Assert.assertEquals("Observer untyped event arg2", true, _ctx.myComponentObserver.untypedEventArg2); 
        	Assert.assertTrue("Observer NS untyped event with args", _ctx.myComponentNSObserver.untypedEventWithArgs);
        	Assert.assertEquals("Observer NS untyped event arg1", "toto", _ctx.myComponentNSObserver.untypedEventArg1); 
        	Assert.assertEquals("Observer NS untyped event arg2", true, _ctx.myComponentNSObserver.untypedEventArg2); 
        	Assert.assertTrue("Multiple observer event 3", _ctx.myComponentObserver.multipleObserverEvent3);
        }
    }
}
