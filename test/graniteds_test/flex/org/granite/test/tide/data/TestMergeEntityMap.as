package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import org.granite.collections.BasicMap;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestMergeEntityMap 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
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
			
			Assert.assertNotNull("Map merged", p.testMap);
			Assert.assertEquals("Map size", 2, p.testMap.length);
			
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
			
			Assert.assertNotNull("Map merged", p.testMap);
			Assert.assertEquals("Map size", 1, p.testMap.length);
        }
    }
}
