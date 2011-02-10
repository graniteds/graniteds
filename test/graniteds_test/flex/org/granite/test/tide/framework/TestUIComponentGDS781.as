package org.granite.test.tide.framework
{
    import mx.containers.Panel;
    
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestUIComponentGDS781
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testUIComponentGDS781():void {
        	var myPanel1:MyPanel6 = new MyPanel6();
        	UIImpersonator.addChild(myPanel1);
        	var myPanel2:MyPanel6 = new MyPanel6();
			UIImpersonator.addChild(myPanel2);
			var myPanel3:MyPanel6 = new MyPanel6();
			UIImpersonator.addChild(myPanel3);
        	
			_ctx.application.dispatchEvent(new MyEvent());
			
        	Assert.assertTrue("MyPanel1 input1 triggered", myPanel1.input1.triggered);
			Assert.assertTrue("MyPanel1 input2 triggered", myPanel1.input2.triggered);
			Assert.assertTrue("MyPanel2 input1 triggered", myPanel2.input1.triggered);
			Assert.assertTrue("MyPanel2 input2 triggered", myPanel2.input2.triggered);
			Assert.assertTrue("MyPanel3 input1 triggered", myPanel3.input1.triggered);
			Assert.assertTrue("MyPanel3 input2 triggered", myPanel3.input2.triggered);
        	
			UIImpersonator.removeChild(myPanel1);
			UIImpersonator.removeChild(myPanel2);
			UIImpersonator.removeChild(myPanel3);
        }
    }
}
