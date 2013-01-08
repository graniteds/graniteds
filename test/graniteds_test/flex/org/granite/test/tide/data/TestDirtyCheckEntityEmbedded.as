package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
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
        
        [Test("GDS-819")]
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
			Assert.assertFalse("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty", personDirty);
        }
		
		[Test("GDS-819 Nested")]
		public function testDirtyCheckEntityNestedEmbedded():void {
			var person:Person4b = new Person4b();
			var person2:Person4b = new Person4b(); 
			
			BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
			BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
			
			person.version = 0;
			person.address = new EmbeddedAddress2();
			person.address.location = new EmbeddedLocation();
			person.address.address1 = "toto";
			person.address.location.city = "test";
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.address.location.city = "truc";
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertTrue("Person dirty 2", personDirty);
			
			person.address.location.city = "test";
			
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
			Assert.assertFalse("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertFalse("Person not dirty", personDirty);
		}
		
		[Test]
		public function testDirtyCheckEntityEmbedded2():void {
			var person:Person4 = new Person4();
			
			BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
			BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
			
			person.version = 0;
			person.id = 1;
			person.uid = "P1";
			person.address = new EmbeddedAddress();
			person.address.address1 = "toto";
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.address.address1 = "tutu";
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertTrue("Person dirty 2", personDirty);
			
			var person2:Person4 = new Person4(); 
			person2.id = 1;
			person2.uid = "P1";
			person2.version = 1;
			person2.address = new EmbeddedAddress();
			person2.address.address1 = "tutu";
			
			_ctx.meta_mergeExternalData(person2);
			
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
			Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertFalse("Person not dirty 2", personDirty);
		}
		
		[Test]
		public function testDirtyCheckEntityEmbedded3():void {
			var person:Person4 = new Person4();
			
			BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
			BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
			
			person.version = 0;
			person.id = 1;
			person.uid = "P1";
			person.address = new EmbeddedAddress();
			person.address.address1 = "toto";
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.address.address1 = "tutu";
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertTrue("Person dirty 2", personDirty);
			
			var person2:Person4 = new Person4(); 
			person2.id = 1;
			person2.uid = "P1";
			person2.version = 0;
			person2.address = new EmbeddedAddress();
			person2.address.address1 = "tutu";
			
			_ctx.meta_mergeExternalData(person2);
			
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
			Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
			Assert.assertFalse("Person not dirty", personDirty);
		}
    }
}
