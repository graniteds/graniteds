package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontexts4
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentSubcontexts4():void {
        	var sc:Subcontext = new Subcontext();
        	_ctx["subcontext"] = sc;
        	
        	var localComponent:MyComponentSimpleObserver = new MyComponentSimpleObserver();
        	sc.localComponent = localComponent;
        	
        	var globalComponent:MyComponentSimpleObserver = new MyComponentSimpleObserver();
        	_ctx.globalComponent = globalComponent;
        	
        	sc.raiseEvent("someEvent");
        	Assert.assertEquals("Untyped event subcontext", 1, localComponent.untypedEvent);
        	Assert.assertEquals("No untyped event global", 0, globalComponent.untypedEvent);
        	Assert.assertEquals("Typed event subcontext", 1, localComponent.typedEvent);
        	Assert.assertEquals("Typed event global", 1, globalComponent.typedEvent);
        }
    }
}
