package org.granite.tide.test.data
{
    import flash.utils.ByteArray;
    
    import flexunit.framework.TestCase;
    
    import org.granite.collections.BasicMap;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Person0;
    
    
    public class TestMergeMap2 extends TestCase
    {
        public function TestMergeMap2() {
            super("testMergeMap2");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testMergeMap2():void {
        	var ba:ByteArray = new ByteArray();
        	ba.writeObject([Salutation.Dr, Salutation.Mr, Salutation.Ms]);
        	ba.position = 0;
        	var s:Array = ba.readObject() as Array;
        	
        	var map:BasicMap = new BasicMap();
        	map.put(Salutation.Dr, new Person0(1, "A1", "B1"));
        	map.put(Salutation.Mr, new Person0(2, "A2", "B2"));
        	map.put(Salutation.Ms, new Person0(3, "A3", "B3"));
        	_ctx.meta_mergeExternalData(map);
        	
        	var map2:BasicMap = new BasicMap();
        	map2.put(s[0], new Person0(1, "A1", "B1"));
        	map2.put(s[1], new Person0(3, "A3", "B3"));
        	map2.put(s[2], new Person0(4, "A4", "B4"));
        	_ctx.meta_mergeExternalData(map2, map);
        	
        	assertEquals("Size", 3, map.length);
        	assertEquals("Element Dr", 1, map.get(Salutation.Dr).id);
        	assertEquals("Element Mr", 3, map.get(Salutation.Mr).id);
        	assertEquals("Element Ms", 4, map.get(Salutation.Ms).id);
        }
    }
}
