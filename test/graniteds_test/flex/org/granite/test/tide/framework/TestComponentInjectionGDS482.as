package org.granite.test.tide.framework
{
	import org.flexunit.Assert;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.test.tide.Contact;
    
    
    public class TestComponentInjectionGDS482
    {
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Object;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentInjectGDS482]);
        	_ctx.test = this;
        }
        
        
        [Test]
        public function testComponentInjectionGDS482():void {
        	var contact:Contact = new Contact();
        	
        	_ctx.subcontext.injectedSetter = contact;
        	
        	_ctx.subcontext.myComponentInject;	// Initialize component
        	
        	Assert.assertStrictlyEquals("Injected 1", contact, _ctx.subcontext.myComponentInject.injectedSetter);
        	
        	var contact2:Contact = new Contact();
        	
        	_ctx.subcontext.injectedSetter = contact2;
        	
        	Assert.assertStrictlyEquals("Injected 2", contact2, _ctx.subcontext.myComponentInject.injectedSetter);
        }
    }
}
