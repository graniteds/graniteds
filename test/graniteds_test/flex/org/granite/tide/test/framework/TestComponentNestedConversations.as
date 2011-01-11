package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestComponentNestedConversations extends TestCase
    {
        public function TestComponentNestedConversations() {
            super("testComponentNestedConversations");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentConversation, MyComponentConversation2]);
        }
        
        
        public function testComponentNestedConversations():void {
        	var ctx1:BaseContext = Tide.getInstance().getContext("1", null, true);
        	var ctx2:BaseContext = Tide.getInstance().getContext("2", null, true);
        	var ctx11:BaseContext = Tide.getInstance().getContext("1.1", "1", true);
        	var ctx12:BaseContext = Tide.getInstance().getContext("1.2", "1", true);
        	var ctx21:BaseContext = Tide.getInstance().getContext("2.1", "2", true);
        	var ctx22:BaseContext = Tide.getInstance().getContext("2.2", "2", true);
        	
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("1", "start"));
        	
        	assertNotNull("Context 1 component", ctx1.myComponent);
        	assertStrictlyEquals("Context 1.1 component", ctx1.myComponent, ctx11.myComponent);
        	
        	assertNotNull("Context 2.1 component", ctx21.myComponent);
        	assertFalse("Context 2 component", ctx2.myComponent === ctx21.myComponent);
        	
        	ctx1.myComponent.dispatchEvent(new TideUIConversationEvent("1.1", "next"));
        	
        	assertTrue("Context 1.1 component 2 triggered", ctx11.myComponent2.triggered);
        	assertFalse("Context 1.2 component 2 not created", ctx12.meta_getInstance("myComponent2", false, true));
        	
        	ctx2.myComponent.dispatchEvent(new TideUIConversationEvent("2.3", "next"));
        	
        	var ctx23:BaseContext = Tide.getInstance().getContext("2.3", null, false);
        	assertNotNull("Context 2.3 created", ctx23);
        	assertStrictlyEquals("Context 2.3 parent", ctx2, ctx23.meta_parentContext);
        	
        	assertTrue("Context 2.3 component 2 triggered", ctx23.myComponent2.triggered);
        	assertFalse("Context 2.1 component 2 not triggered", ctx21.myComponent2.triggered);
        }
    }
}
