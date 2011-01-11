package org.granite.test
{
    import flexunit.framework.TestCase;
    
    import org.granite.collections.BasicMap;
    
    
    public class TestBasicMapGDS470 extends TestCase
    {
        public function TestBasicMapGDS470() {
            super("testBasicMapGDS470");
        }
        
        
        public override function setUp():void {
            super.setUp();
        }
        
        
        public function testBasicMapGDS470():void {
            var q:BasicMap = new BasicMap();
			q.put("key1", new Object());
			q.put("key2", new Object());
			q.put("key3", new Object());
			
			var i:int = 0;
			for (var p:String in q) {
				assertTrue(p == "key1" || p == "key2" || p == "key3");
				i++;
			}
			assertEquals("Map key iteration", 3, i);
			
			i = 0;
			for each (var v:Object in q) {
				i++;
			}
			assertEquals("Map value iteration", 3, i);
        }
    }
}
