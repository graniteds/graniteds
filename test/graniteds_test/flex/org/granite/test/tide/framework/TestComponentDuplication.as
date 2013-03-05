package org.granite.test.tide.framework
{
    import mx.utils.ObjectProxy;
    
    import org.flexunit.Assert;
    import org.granite.ns.tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    use namespace tide;
    
    
    public class TestComponentDuplication
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testComponentDuplicationVariables():void {
			_ctx.var1 = true;
			_ctx.var2 = false;
			_ctx.var3 = true;
        	
			Assert.assertNotNull("var1", _ctx.var1);
			Assert.assertNotNull("var2", _ctx.var2);
			Assert.assertNotNull("var3", _ctx.var3);
        }
		
		[Test]
		public function testComponentDuplicationVariables2():void {
			_ctx.var1 = "A";
			_ctx.var2 = "B";
			_ctx.var3 = "A";
			
			Assert.assertEquals("var1", "A", _ctx.var1);
			Assert.assertEquals("var2", "B", _ctx.var2);
			Assert.assertEquals("var3", "A", _ctx.var3);
		}
		
		[Test]
		public function testComponentDuplicationComp():void {
			var comp1:MyPanel = new MyPanel();
			var comp2:MyPanel2 = new MyPanel2();
			
			_ctx.comp1 = comp1;
			_ctx.comp2 = comp2;
			_ctx.comp3 = comp1;
			
			Assert.assertNull("comp1", _ctx.comp1);
			Assert.assertStrictlyEquals("comp3", comp1, _ctx.comp3);
		}
    }
}
