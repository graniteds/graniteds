package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentSubcontexts4 extends TestCase
    {
        public function TestComponentSubcontexts4() {
            super("testComponentSubcontexts4");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        public function testComponentSubcontexts4():void {
        	var sc:Subcontext = new Subcontext();
        	_ctx["subcontext"] = sc;
        	
        	var localComponent:MyComponentSimpleObserver = new MyComponentSimpleObserver();
        	sc.localComponent = localComponent;
        	
        	var globalComponent:MyComponentSimpleObserver = new MyComponentSimpleObserver();
        	_ctx.globalComponent = globalComponent;
        	
        	sc.raiseEvent("someEvent");
        	assertEquals("Untyped event subcontext", 1, localComponent.untypedEvent);
        	assertEquals("No untyped event global", 0, globalComponent.untypedEvent);
        	assertEquals("Typed event subcontext", 1, localComponent.typedEvent);
        	assertEquals("Typed event global", 1, globalComponent.typedEvent);
        }
    }
}
