package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.seam.Context;
    import org.granite.tide.seam.Seam;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentOutjectionForum3853
    {
        private var _ctx:Context;
        
        
        [Before]
        public function setUp():void {
            Seam.resetInstance();
            _ctx = Seam.getInstance().getSeamContext();
            Seam.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentOutjectionForum3853():void {
        	Seam.getInstance().addComponents([MyComponentOutject3]);
        	
        	_ctx.myComponentOutject3.doSomething();
        	
        	Assert.assertEquals("Outject remote var", 1, _ctx.meta_updates.length);
        }
    }
}
