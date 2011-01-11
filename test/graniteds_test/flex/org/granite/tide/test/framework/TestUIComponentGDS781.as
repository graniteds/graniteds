package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import mx.containers.Panel;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestUIComponentGDS781 extends TestCase
    {
        public function TestUIComponentGDS781() {
            super("testUIComponentGDS781");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testUIComponentGDS781():void {
        	var myPanel1:MyPanel6 = new MyPanel6();
        	_ctx.application.addChild(myPanel1);
        	var myPanel2:MyPanel6 = new MyPanel6();
        	_ctx.application.addChild(myPanel2);
			var myPanel3:MyPanel6 = new MyPanel6();
			_ctx.application.addChild(myPanel3);
        	
			_ctx.application.dispatchEvent(new MyEvent());
			
        	assertTrue("MyPanel1 input1 triggered", myPanel1.input1.triggered);
			assertTrue("MyPanel1 input2 triggered", myPanel1.input2.triggered);
			assertTrue("MyPanel2 input1 triggered", myPanel2.input1.triggered);
			assertTrue("MyPanel2 input2 triggered", myPanel2.input2.triggered);
			assertTrue("MyPanel3 input1 triggered", myPanel3.input1.triggered);
			assertTrue("MyPanel3 input2 triggered", myPanel3.input2.triggered);
        	
        	_ctx.application.removeChild(myPanel1);
        	_ctx.application.removeChild(myPanel2);
			_ctx.application.removeChild(myPanel3);
        }
    }
}
