package org.granite.test.tide.framework
{
    import mx.containers.Panel;
    
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestUIComponentGDS781b
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            Tide.getInstance().initApplication();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testUIComponentGDS781b():void {
        	var myPanel1:MyPanel7 = new MyPanel7();
        	UIImpersonator.addChild(myPanel1);
        	var myPanel2:MyPanel7 = new MyPanel7();
			UIImpersonator.addChild(myPanel2);
			var myPanel3:MyPanel7 = new MyPanel7();
			UIImpersonator.addChild(myPanel3);
        	
			_ctx.application.dispatchEvent(new MyEvent());
			
        	Assert.assertTrue("MyPanel1 input1 triggered", Object(myPanel1.getChildAt(0)).triggered);
			Assert.assertTrue("MyPanel1 input2 triggered", Object(myPanel1.getChildAt(1)).triggered);
			Assert.assertTrue("MyPanel2 input1 triggered", Object(myPanel2.getChildAt(0)).triggered);
			Assert.assertTrue("MyPanel2 input2 triggered", Object(myPanel2.getChildAt(1)).triggered);
			Assert.assertTrue("MyPanel3 input1 triggered", Object(myPanel3.getChildAt(0)).triggered);
			Assert.assertTrue("MyPanel3 input2 triggered", Object(myPanel3.getChildAt(1)).triggered);
        	
			UIImpersonator.removeChild(myPanel1);
			UIImpersonator.removeChild(myPanel2);
			UIImpersonator.removeChild(myPanel3);
        }
    }
}
