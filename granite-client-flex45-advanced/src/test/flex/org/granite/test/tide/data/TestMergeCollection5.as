package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeCollection5 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var events:Object = { add: 0, remove: 0, update: 0, replace: 0, move: 0, refresh: 0, reset: 0 };
        
        [Test]
        public function testMergeCollection5():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B2"));
        	coll.addItem(new Person0(3, "A3", "B3"));
        	coll = _ctx.meta_mergeExternalData(coll) as ArrayCollection;
        	
        	coll.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangeHandler, false, 0, true);
        	
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(new Person0(4, "A4", "B4"));
        	coll2.addItem(new Person0(1, "A1", "B1"));
        	coll2.addItem(new Person0(2, "A2", "B2"));
        	coll2.addItem(new Person0(3, "A3", "B3"));
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	Assert.assertEquals("Element 1", 4, coll.getItemAt(0).id);
        	Assert.assertEquals("Element 2", 1, coll.getItemAt(1).id);
        	Assert.assertEquals("Element 3", 2, coll.getItemAt(2).id);
        	Assert.assertEquals("Element 4", 3, coll.getItemAt(3).id);
        	
        	Assert.assertEquals("Event add count", 1, events.add); 
        	Assert.assertEquals("Event remove count", 0, events.remove); 
        }
        
        private function collectionChangeHandler(event:CollectionEvent):void {
        	events[event.kind]++;
        }
    }
}
