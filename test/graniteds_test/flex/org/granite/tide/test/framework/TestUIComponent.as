package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import mx.containers.Panel;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestUIComponent extends TestCase
    {
        public function TestUIComponent() {
            super("testUIComponent");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testUIComponent():void {
        	var myPanel:MyPanel = new MyPanel();
        	_ctx.application.addChild(myPanel);
        	var myPanel2:Panel = new MyPanel2();
        	_ctx.application.addChild(myPanel2);
			var myPanel3:Panel = new MyPanel3();
			_ctx.application.addChild(myPanel3);
        	
        	assertTrue("MyPanel component", Tide.getInstance().isComponent("myPanel"));
        	assertStrictlyEquals("MyPanel", _ctx.myPanel, myPanel);
        	assertStrictlyEquals("MyPanel2", _ctx[Tide.internalUIComponentName(myPanel2)], myPanel2);
			assertStrictlyEquals("MyPanel3", _ctx[Tide.internalUIComponentName(myPanel3)], myPanel3);
        	
        	_ctx.application.removeChild(myPanel);
        	_ctx.application.removeChild(myPanel2);
			_ctx.application.removeChild(myPanel3);
        }
    }
}
