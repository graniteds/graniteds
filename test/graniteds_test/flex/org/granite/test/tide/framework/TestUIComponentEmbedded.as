package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.test.tide.Contact;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Tide;
    
    
    public class TestUIComponentEmbedded
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testUIComponentEmbedded():void {
			var myView1:MyView1 = new MyView1();
        	UIImpersonator.addChild(myView1);
        	
        	_ctx.raiseEvent("testEvent");
			
			Assert.assertEquals("Component registered", 1, _ctx.allByType(MyView2).length);			
			Assert.assertEquals("Event triggered", 1, MyView2.count);
        }
		
		[Test]
		public function testUIComponentSparkEmbedded():void {
			var myView1:MySparkView1 = new MySparkView1();
			UIImpersonator.addChild(myView1);
			
			_ctx.raiseEvent("testEvent");
			
			Assert.assertEquals("Component registered", 1, _ctx.allByType(MySparkView2).length);			
			Assert.assertEquals("Event triggered", 1, MySparkView2.count);
		}
    }
}
