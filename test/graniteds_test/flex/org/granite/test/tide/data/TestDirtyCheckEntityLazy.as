package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.collections.IPersistentCollection;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Classification;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityLazy
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testDirtyCheckEntityLazy():void {
        	var parent:Classification = new Classification();
			parent.id = 1;
			parent.version = 0;
			parent.uid = "P1";
			parent.subclasses = new PersistentSet(true);
			parent.superclasses = new PersistentSet(true);
			var child:Classification = new Classification();
			child.id = 2;
			child.version = 0;
			child.uid = "C1";
			child.subclasses = new PersistentSet(true);
			child.superclasses = new PersistentSet(true);
			parent.subclasses.addItem(child);
			child.superclasses.addItem(parent);
			
			_ctx.parent = _ctx.meta_mergeExternal(parent);
			parent = Classification(_ctx.parent);
			
			Assert.assertFalse("Classification not dirty", _ctx.meta_dirty);
			
			IPersistentCollection(child.superclasses).uninitialize();
			
			Assert.assertFalse("Classification not dirty after uninit", _ctx.meta_dirty);
        }
    }
}
