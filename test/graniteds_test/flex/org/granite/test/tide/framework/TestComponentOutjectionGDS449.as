package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentOutjectionGDS449
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentOutjectionGDS449():void {
        	Tide.getInstance().addComponents([MyComponentOutjectGDS449]);
        	
        	_ctx.raiseEvent("defineBean");
        	Assert.assertStrictlyEquals("Bean defined", _ctx.bean, _ctx.myComponentOutject449.bean);
        	
        	_ctx.raiseEvent("defineBean");
        	Assert.assertStrictlyEquals("Bean defined 2", _ctx.bean, _ctx.myComponentOutject449.bean);
        	
        	_ctx.raiseEvent("defineBean");
        	Assert.assertStrictlyEquals("Bean defined 3", _ctx.bean, _ctx.myComponentOutject449.bean);
        }
    }
}
