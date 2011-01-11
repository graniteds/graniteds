package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import org.granite.collections.BasicMap;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestMergeEntityMap extends TestCase
    {
        public function TestMergeEntityMap() {
            super("testMergeEntityMap");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testMergeEntityMap():void {
        	var p1:Person9 = new Person9();
			p1.id = 1;
			p1.uid = "P1";
			p1.version = 0;
			p1.firstName = "Toto";
			p1.testMap = null;
			_ctx.meta_mergeExternalData(p1);
			
			p1.lastName = "Test";
			
        	var p2:Person9 = new Person9();
			p2.id = 1;
			p2.uid = "P1";
			p2.version = 0;
			p2.firstName = "Toto2";
			var m:BasicMap = new BasicMap();
			m["test"] = new EmbeddedAddress();
			m["test"].address1 = "test";
			m["toto"] = new EmbeddedAddress();
			m["toto"].address2 = "toto";
			p2.testMap = m;
        	var p:Person9 = _ctx.meta_mergeExternalData(p2) as Person9;
			
			assertNotNull("Map merged", p.testMap);
			assertEquals("Map size", 2, p.testMap.length);
			
        	var p3:Person9 = new Person9();
			p3.id = 1;
			p3.uid = "P1";
			p3.version = 0;
			p3.firstName = "Toto3";
			var m2:BasicMap = new BasicMap();
			m2["test"] = new EmbeddedAddress();
			m2["test"].address1 = "test";
			p3.testMap = m2;
        	p = _ctx.meta_mergeExternalData(p3) as Person9;
			
			assertNotNull("Map merged", p.testMap);
			assertEquals("Map size", 1, p.testMap.length);
        }
    }
}
