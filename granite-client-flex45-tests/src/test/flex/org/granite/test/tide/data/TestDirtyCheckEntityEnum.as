package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityEnum 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        [Test]
        public function testDirtyCheckEntityEnum():void {
        	var person:Person3 = new Person3();
        	person.version = 0;
        	person.salutation = Salutation.Dr;
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	_ctx.person = _ctx.meta_mergeExternalData(person);
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	
        	person.salutation = Salutation.Mr;
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", ctxDirty);
        	
        	person.salutation = Salutation.Dr;
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", ctxDirty);
        	
        	person.salutation = null;
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", ctxDirty);
        	
        	person.salutation = Salutation.Dr;
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", ctxDirty);
        }
    }
}
