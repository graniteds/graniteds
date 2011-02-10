package org.granite.test.tide.framework
{
	import flash.utils.describeType;
	
	import org.flexunit.Assert;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
    
    
    public class TestComponentProduces
    {
        private var _ctx:BaseContext;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentNoName2, MyComponentNoName3]);
        }
        
        
        [Test]
        public function testComponentProduces():void {
        	Assert.assertTrue("Injected by producer method", _ctx.myComponentNoName2.injected is MyComponentNoName); 
        }
    }
}
