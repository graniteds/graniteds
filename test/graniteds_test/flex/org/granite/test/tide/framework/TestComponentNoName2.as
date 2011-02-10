package org.granite.test.tide.framework
{
	import org.flexunit.Assert;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.test.tide.Contact;
	import org.granite.test.tide.framework.MyEvent;
    
    
    public class TestComponentNoName2
    {
        private var _ctx:BaseContext;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentNoNameA, MyComponentNoNameB]);
        }
        
        
        [Test]
        public function testComponentNoName2():void {
			_ctx.dispatchEvent(new MyEvent());
        	Assert.assertTrue("No name A triggered", _ctx.byType(MyComponentNoNameA).triggered);
			Assert.assertTrue("No name B not triggered", !_ctx.byType(MyComponentNoNameB).triggered);
        }
    }
}
