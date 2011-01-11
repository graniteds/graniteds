package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.seam.Context;
    import org.granite.tide.seam.Seam;
    import org.granite.tide.test.Contact;
    
    
    public class TestComponentOutjectionForum3853 extends TestCase
    {
        public function TestComponentOutjectionForum3853() {
            super("testComponentOutjectionForum3853");
        }
        
        private var _ctx:Context;
        
        
        public override function setUp():void {
            super.setUp();
            
            Seam.resetInstance();
            _ctx = Seam.getInstance().getSeamContext();
            Seam.getInstance().initApplication();
        }
        
        
        public function testComponentOutjectionForum3853():void {
        	Seam.getInstance().addComponents([MyComponentOutject3]);
        	
        	_ctx.myComponentOutject3.doSomething();
        	
        	assertEquals("Outject remote var", 1, _ctx.meta_updates.length);
        }
    }
}
