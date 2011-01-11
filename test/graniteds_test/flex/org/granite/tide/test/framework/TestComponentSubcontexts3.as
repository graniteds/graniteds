package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.seam.Seam;
    
    
    public class TestComponentSubcontexts3 extends TestCase
    {
        public function TestComponentSubcontexts3() {
            super("testComponentSubcontexts3");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Seam.resetInstance();
            _ctx = Seam.getInstance().getContext();
            Seam.getInstance().initApplication();
        }
        
        
        public function testComponentSubcontexts3():void {
        	var sc:Subcontext = new Subcontext();
        	_ctx["subcontext"] = sc;
        	
        	sc.view = new MyComponentSubcontextView();
        	sc.control = new MyComponentSubcontextControl();
        	
        	assertStrictlyEquals("View injected subcontext", sc.view.subcontext, sc);
        	assertStrictlyEquals("View injected controller", sc.view.control, sc.control);
        	assertStrictlyEquals("Controller injected subcontext", sc.control.subcontext, sc);
        }
    }
}
