package org.granite.test.tide.framework
{
	import org.flexunit.Assert;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.test.tide.Contact;
    
    
    public class TestComponentInOut
    {
        private var _ctx:BaseContext;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentInOut]);
        }
        
        
        [Test]
        public function testComponentInOut():void {
        	Assert.assertNotNull(_ctx.myComponentInOut.testContact);
        	Assert.assertNotNull(_ctx.testContact);
        	var testContact:Contact = _ctx.testContact;
        	
        	_ctx.myComponentInOut.reset();
        	
        	Assert.assertNotNull(_ctx.testContact);
        	Assert.assertFalse(_ctx.testContact === testContact);
        }
    }
}
