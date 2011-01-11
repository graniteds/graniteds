package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import mx.containers.Panel;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestUIComponentGDS781b extends TestCase
    {
        public function TestUIComponentGDS781b() {
            super("testUIComponentGDS781b");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testUIComponentGDS781b():void {
        	var myPanel1:MyPanel7 = new MyPanel7();
        	_ctx.application.addChild(myPanel1);
        	var myPanel2:MyPanel7 = new MyPanel7();
        	_ctx.application.addChild(myPanel2);
			var myPanel3:MyPanel7 = new MyPanel7();
			_ctx.application.addChild(myPanel3);
        	
			_ctx.application.dispatchEvent(new MyEvent());
			
        	assertTrue("MyPanel1 input1 triggered", Object(myPanel1.getChildAt(0)).triggered);
			assertTrue("MyPanel1 input2 triggered", Object(myPanel1.getChildAt(1)).triggered);
			assertTrue("MyPanel2 input1 triggered", Object(myPanel2.getChildAt(0)).triggered);
			assertTrue("MyPanel2 input2 triggered", Object(myPanel2.getChildAt(1)).triggered);
			assertTrue("MyPanel3 input1 triggered", Object(myPanel3.getChildAt(0)).triggered);
			assertTrue("MyPanel3 input2 triggered", Object(myPanel3.getChildAt(1)).triggered);
        	
        	_ctx.application.removeChild(myPanel1);
        	_ctx.application.removeChild(myPanel2);
			_ctx.application.removeChild(myPanel3);
        }
    }
}
