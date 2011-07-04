package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Classification;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityCircularRef
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testDirtyCheckEntityCircularRef():void {
        	var parent:Classification = new Classification();
			parent.id = 1;
			parent.version = 0;
			parent.uid = "P1";
			parent.subclasses = new PersistentSet(true);
			parent.superclasses = new PersistentSet(true);
			
			_ctx.parent = _ctx.meta_mergeExternal(parent);
			parent = Classification(_ctx.parent);
        	
			var child:Classification = new Classification();
			child.id = 2;
			child.version = 0;
			child.uid = "C1";
			child.subclasses = new PersistentSet(true);
			child.superclasses = new PersistentSet(true);
			_ctx.child = _ctx.meta_mergeExternal(child);
			child = Classification(_ctx.child);
			
			Assert.assertFalse("Classification not dirty", _ctx.meta_dirty);
			
			parent.subclasses.addItem(child);
			child.superclasses.addItem(parent);
			
			Assert.assertTrue("Classification dirty", _ctx.meta_dirty);
			
			var parent2:Classification = new Classification();
			parent2.id = 1;
			parent2.version = 1;
			parent2.uid = "P1";
			parent2.subclasses = new PersistentSet(true);
			parent2.superclasses = new PersistentSet(true);
			var child2:Classification = new Classification();
			child2.id = 2;
			child2.version = 1;
			child2.uid = "C1";
			child2.subclasses = new PersistentSet(true);
			child2.superclasses = new PersistentSet(true);
			parent2.subclasses.addItem(child2);
			parent2.superclasses.addItem(parent2);
			
			var res:ArrayCollection = new ArrayCollection();
			res.addItem(parent2);
			res.addItem(child2);
			
			_ctx.meta_mergeExternal(res);
			
			Assert.assertFalse("Classification merged not dirty", _ctx.meta_dirty);
        }
    }
}
