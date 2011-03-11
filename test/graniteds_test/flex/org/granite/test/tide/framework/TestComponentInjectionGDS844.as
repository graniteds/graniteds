package org.granite.test.tide.framework
{
	import __AS3__.vec.Vector;
	
	import org.flexunit.Assert;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.test.tide.Contact;
    
    
    public class TestComponentInjectionGDS844
    {
        private var _ctx:BaseContext;
        
  		
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
			Tide.getInstance().addComponent("myComponentSession", MyComponentInjectGDS844, false);
			Tide.getInstance().addComponent("myComponentConversation", MyComponentInjectGDS844, true);
        }
        
        
        [Test]
        public function testComponentInjectionGDS844():void {
			Assert.assertStrictlyEquals("Session component injected", _ctx.byType(MyService), _ctx.myComponentSession.service);
			_ctx.dispatchEvent(new MyConversationEvent(null));
			var ctx:BaseContext = Tide.getInstance().getContext("test");
			Assert.assertStrictlyEquals("Conversation component injected", ctx.byType(MyService), ctx.myComponentConversation.service);
			ctx.myComponentConversation.end();
			Assert.assertNotNull("Session component disinjected", ctx.myComponentSession.service);
        }
    }
}
