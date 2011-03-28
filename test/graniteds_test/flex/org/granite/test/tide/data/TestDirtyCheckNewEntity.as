package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestDirtyCheckNewEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
		[Ignore("Test failed, should consider pull request from A. Busch")]
        [Test]
        public function testDirtyCheckNewEntity():void {
        	var person:Person = new Person();
        	person.firstName = "toto";			
			_ctx.person = person;
			
			var person2:Person = new Person();
			person2.id = 1;
			person2.uid = person.uid;
			person2.version = 0;
			person.firstName = "toto";
			
			_ctx.meta_mergeExternalData(person2);
			
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
        }
    }
}
