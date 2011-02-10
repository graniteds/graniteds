package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentInjectionGDS471
    {
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Object;
            
  		[In(create="false")]
  		public var globalOutjectedVariable:String;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentGlobalGDS471, MyComponentLocalGDS471]);
        	_ctx.test = this;
        }
        
        
        [Test]
        public function testComponentInjectionGDS471():void {
        	application.dispatchEvent(new TideUIEvent("step1Global"));
        	
        	application.dispatchEvent(new TideUIConversationEvent("someConv", "step1Conv"));
        	
			Assert.assertEquals("Global", "global string", globalOutjectedVariable);
			Assert.assertStrictlyEquals("Global", globalOutjectedVariable, Tide.getInstance().getContext("someConv").myComponentLocal.globalOutjectedVariable);
        	
        	application.dispatchEvent(new TideUIEvent("step2Global"));
        	
			Assert.assertEquals("Global", "global 2 string", globalOutjectedVariable);
			Assert.assertStrictlyEquals("Global 2", globalOutjectedVariable, Tide.getInstance().getContext("someConv").myComponentLocal.globalOutjectedVariable);
        }
    }
}
