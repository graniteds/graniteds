package org.granite.tide.test.framework
{
	import flexunit.framework.TestCase;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.test.Contact;
    
    
    public class TestComponentInjectionGDS482 extends TestCase
    {
        public function TestComponentInjectionGDS482() {
            super("testComponentInjectionGDS482");
        }
        
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Application;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentInjectGDS482]);
        	_ctx.test = this;
        }
        
        
        public function testComponentInjectionGDS482():void {
        	var contact:Contact = new Contact();
        	
        	_ctx.subcontext.injectedSetter = contact;
        	
        	_ctx.subcontext.myComponentInject;	// Initialize component
        	
        	assertStrictlyEquals("Injected 1", contact, _ctx.subcontext.myComponentInject.injectedSetter);
        	
        	var contact2:Contact = new Contact();
        	
        	_ctx.subcontext.injectedSetter = contact2;
        	
        	assertStrictlyEquals("Injected 2", contact2, _ctx.subcontext.myComponentInject.injectedSetter);
        }
    }
}
