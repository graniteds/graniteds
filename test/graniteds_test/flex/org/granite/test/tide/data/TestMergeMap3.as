package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    
    import org.granite.collections.BasicMap;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeMap3 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var _mapEvents:Array = new Array();
        
        
        [Test]
        public function testMergeMap3():void {
        	var map:BasicMap = new BasicMap();
        	map.put("p1", new Person0(1, "A1", "B1"));
        	map.put("p2", new Person0(2, "A2", "B2"));
        	map.put("p3", new Person0(3, "A3", "B3"));
        	map.put("p4", new Person0(4, "A4", "B4"));
        	_ctx.meta_mergeExternalData(map);
        	
        	map.addEventListener(CollectionEvent.COLLECTION_CHANGE, mapChangeHandler);
        	
        	var map2:BasicMap = new BasicMap();
        	map2.put("p1", new Person0(1, "A1", "B1"));
        	map2.put("p5", new Person0(5, "A5", "B5"));
        	map2.put("p2", new Person0(2, "A2", "B2"));
        	map2.put("p4", new Person0(4, "A4", "B4"));
        	_ctx.meta_mergeExternalData(map2, map);
        	
        	Assert.assertEquals("Element p5", 5, map.get("p5").id);
        	Assert.assertEquals("Element p2", 2, map.get("p2").id);
        	Assert.assertEquals("Events", 2, _mapEvents.length);
        }
        
        private function mapChangeHandler(event:CollectionEvent):void {
        	if (event.kind == CollectionEventKind.ADD || event.kind == CollectionEventKind.REMOVE)
        		_mapEvents.push(event);
        }
    }
}
