package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeCollection4 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeCollection4():void {
        	var p1:Person0 = new Person0(1, "A1", "B1");
        	var p2:Person0 = new Person0(2, "A2", "B2");
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(p1);
        	coll.addItem(p2);
        	coll.addItem(p2);
        	coll.addItem(p1);
        	_ctx.meta_mergeExternalData(coll);
        	
        	var p1b:Person0 = new Person0(1, "A1", "B1");
        	var p2b:Person0 = new Person0(2, "A2", "B2");
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(p1b);
        	coll2.addItem(p2b);
        	coll2.addItem(p2b);
        	coll2.addItem(p1b);
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	Assert.assertEquals("Element 0", 1, coll.getItemAt(0).id);
        	Assert.assertEquals("Element 1", 2, coll.getItemAt(1).id);
        	Assert.assertEquals("Element 2", 2, coll.getItemAt(2).id);
        	Assert.assertEquals("Element 3", 1, coll.getItemAt(3).id);
        }
    }
}
