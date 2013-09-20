package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeCollection3 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var _collEvents:Array = new Array();
        
        
        [Test]
        public function testMergeCollection3():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B2"));
        	coll.addItem(new Person0(3, "A3", "B3"));
        	coll.addItem(new Person0(4, "A4", "B4"));
        	_ctx.meta_mergeExternalData(coll);
        	
        	coll.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangeHandler);
        	
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(new Person0(1, "A1", "B1"));
        	coll2.addItem(new Person0(5, "A5", "B5"));
        	coll2.addItem(new Person0(2, "A2", "B2"));
        	coll2.addItem(new Person0(4, "A4", "B4"));
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	Assert.assertEquals("Element 1", 5, coll.getItemAt(1).id);
        	Assert.assertEquals("Element 2", 2, coll.getItemAt(2).id);
        	Assert.assertEquals("Events", 2, _collEvents.length);
        }
        
        private function collectionChangeHandler(event:CollectionEvent):void {
        	_collEvents.push(event);
        }
    }
}
