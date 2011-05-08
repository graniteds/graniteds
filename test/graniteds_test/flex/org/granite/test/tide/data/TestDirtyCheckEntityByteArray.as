package org.granite.test.tide.data
{
    import flash.utils.ByteArray;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityByteArray
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        [Test("GDS-854")]
        public function testDirtyCheckEntityByteArray():void {
        	var person:Person10 = new Person10();
        	var person2:Person10 = new Person10();
			
			var pic1:ByteArray = new ByteArray();
			pic1.writeMultiByte("JKDEK", "UTF-8");
			pic1.position = 0;
			var pic2:ByteArray = new ByteArray();
			pic2.writeMultiByte("FSDLKZJH", "UTF-8");
			pic2.position = 0;
			var pic3:ByteArray = new ByteArray();
			pic3.writeMultiByte("JKDEK", "UTF-8");
			pic3.position = 0;
			
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	person.version = 0;
			person.picture = pic1;
			
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person.picture = pic2;
        	
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			Assert.assertTrue("Person dirty 1", personDirty);
        	Assert.assertTrue("Person dirty 2", _ctx.meta_isEntityChanged(person));
        	
        	person.picture = pic3;
        	
			Assert.assertFalse("Context dirty", _ctx.meta_dirty);
			Assert.assertFalse("Person not dirty 1", personDirty);
        	Assert.assertFalse("Person not dirty 2", _ctx.meta_isEntityChanged(person));
        }
    }
}
