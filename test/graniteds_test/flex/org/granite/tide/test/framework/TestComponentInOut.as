package org.granite.tide.test.framework
{
	import flexunit.framework.TestCase;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.test.Contact;
    
    
    public class TestComponentInOut extends TestCase
    {
        public function TestComponentInOut() {
            super("testComponentInOut");
        }
        
        private var _ctx:BaseContext;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentInOut]);
        }
        
        
        public function testComponentInOut():void {
        	assertNotNull(_ctx.myComponentInOut.testContact);
        	assertNotNull(_ctx.testContact);
        	var testContact:Contact = _ctx.testContact;
        	
        	_ctx.myComponentInOut.reset();
        	
        	assertNotNull(_ctx.testContact);
        	assertFalse(_ctx.testContact === testContact);
        }
    }
}
