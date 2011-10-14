package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
import org.fluint.uiImpersonation.UIImpersonator;
import org.granite.test.tide.*;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    public class TestComponentObservers6
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentObserver3]);
        }
        
        
		[Test]
		public function testComponentObservers6():void {
            var ui:MyUIComponent = new MyUIComponent();
            UIImpersonator.addChild(ui);
            ui.dispatch();
			Assert.assertTrue("Observer triggered", _ctx.myComponentObserver3.triggered1);
		}
		
    }
}
