package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Subcontext;
    import org.granite.tide.seam.Seam;
    
    
    public class TestComponentSubcontexts3
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Seam.resetInstance();
            _ctx = Seam.getInstance().getContext();
            Seam.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentSubcontexts3():void {
        	var sc:Subcontext = new Subcontext();
        	_ctx["subcontext"] = sc;
        	
        	sc.view = new MyComponentSubcontextView();
        	sc.control = new MyComponentSubcontextControl();
        	
        	Assert.assertStrictlyEquals("View injected subcontext", sc.view.subcontext, sc);
        	Assert.assertStrictlyEquals("View injected controller", sc.view.control, sc.control);
        	Assert.assertStrictlyEquals("Controller injected subcontext", sc.control.subcontext, sc);
        }
    }
}
