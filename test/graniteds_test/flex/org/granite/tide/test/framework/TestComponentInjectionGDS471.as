package org.granite.tide.test.framework
{
	import mx.core.Application;
	
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentInjectionGDS471 extends TestCase
    {
        public function TestComponentInjectionGDS471() {
            super("testComponentInjectionGDS471");
        }
        
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Application;
            
  		[In(create="false")]
  		public var globalOutjectedVariable:String;
  		         
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentGlobalGDS471, MyComponentLocalGDS471]);
        	_ctx.test = this;
        }
        
        
        public function testComponentInjectionGDS471():void {
        	application.dispatchEvent(new TideUIEvent("step1Global"));
        	
        	application.dispatchEvent(new TideUIConversationEvent("someConv", "step1Conv"));
        	
        	assertEquals("Global", "global string", globalOutjectedVariable);
        	assertStrictlyEquals("Global", globalOutjectedVariable, Tide.getInstance().getContext("someConv").myComponentLocal.globalOutjectedVariable);
        	
        	application.dispatchEvent(new TideUIEvent("step2Global"));
        	
        	assertEquals("Global", "global 2 string", globalOutjectedVariable);
        	assertStrictlyEquals("Global 2", globalOutjectedVariable, Tide.getInstance().getContext("someConv").myComponentLocal.globalOutjectedVariable);
        }
    }
}
