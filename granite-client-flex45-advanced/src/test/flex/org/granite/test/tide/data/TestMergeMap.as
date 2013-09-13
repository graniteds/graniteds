package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import org.granite.collections.BasicMap;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeMap 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeMap():void {
        	var map:BasicMap = new BasicMap();
        	map.put("p1", new Person0(1, "A1", "B1"));
        	map.put("p2", new Person0(2, "A2", "B2"));
        	map.put("p3", new Person0(3, "A3", "B3"));
        	_ctx.meta_mergeExternalData(map);
        	
        	var map2:BasicMap = new BasicMap();
        	map2.put("p1", new Person0(1, "A1", "B1"));
        	map2.put("p3", new Person0(3, "A3", "B3"));
        	map2.put("p4", new Person0(4, "A4", "B4"));
        	map2.put("p5", new Person0(5, "A5", "B5"));
        	_ctx.meta_mergeExternalData(map2, map);
        	
        	Assert.assertEquals("Size", 4, map.length);
        	Assert.assertEquals("Element 3", 3, map.get("p3").id);
        	Assert.assertEquals("Element 4", 4, map.get("p4").id);
        	Assert.assertEquals("Element 5", 5, map.get("p5").id);
        }
    }
}
