package org.granite.test.tide.framework
{
	import org.flexunit.Assert;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.test.tide.Contact;
    
    
    public class TestComponentNoName
    {
        private var _ctx:BaseContext;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentNoName, MyComponentNoName2]);
        }
        
        
        [Test]
        public function testComponentNoName():void {
        	Assert.assertTrue("No name injected", _ctx.myComponentNoName2.injected is MyComponentNoName);
        }
    }
}
