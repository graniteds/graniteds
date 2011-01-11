package org.granite.tide.test.framework
{
    import flexunit.framework.TestCase;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    
    
    public class TestAnnotatedComponent extends TestCase
    {
        public function TestAnnotatedComponent() {
            super("testAnnotatedComponent");
        }
        
        private var _ctx:BaseContext;
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
                
        public function testAnnotatedComponent():void {
        	var contact:Contact = new Contact();
        	assertNotNull("uid not null", contact.uid);
        	
        	Tide.getInstance().addComponents([MyComponent, MyComponentNoCreate]);
        	assertTrue("autoCreate component", _ctx.myComponent is MyComponent);
        	assertTrue("non autoCreate component", _ctx.myComponentNoCreate == null);
        }
    }
}
