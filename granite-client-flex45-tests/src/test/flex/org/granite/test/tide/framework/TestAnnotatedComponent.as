package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    
    
    public class TestAnnotatedComponent
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
                
        [Test]
        public function testAnnotatedComponent():void {
        	var contact:Contact = new Contact();
        	Assert.assertNotNull("uid not null", contact.uid);
        	
        	Tide.getInstance().addComponents([MyComponent, MyComponentNoCreate]);
			Assert.assertTrue("autoCreate component", _ctx.myComponent is MyComponent);
			Assert.assertTrue("non autoCreate component", _ctx.myComponentNoCreate == null);
        }
    }
}
