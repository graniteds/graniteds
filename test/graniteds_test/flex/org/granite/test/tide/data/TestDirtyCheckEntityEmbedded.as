package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityEmbedded 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
		[Ignore("GDS-819 TODO")]
        [Test]
        public function testDirtyCheckEntityEmbedded():void {
        	var person:Person4 = new Person4();
        	var person2:Person4 = new Person4(); 
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	person.version = 0;
			person.address = new EmbeddedAddress();
			person.address.address1 = "toto";
			
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.address.address1 = "tutu";
        	
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	
        	person.address.address1 = "toto";
        	
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        }
    }
}
