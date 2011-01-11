package org.granite.test
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.utils.ObjectUtil;
    
    import org.granite.collections.BasicMap;
    
    
    public class TestCollectionEnumExtGDS588 extends TestCase
    {
        public function TestCollectionEnumExtGDS588() {
            super("testCollectionEnumExtGDS588");
        }
        
        
        public override function setUp():void {
            super.setUp();
        }
        
        
        public function testCollectionEnumExtGDS588():void {
        	var sal1:Salutation = Salutation.Mr;
        	var sal2:Salutation = Salutation.Ms;
			
			var sal:Salutation;
			
			var c:ArrayCollection = new ArrayCollection([sal1, sal2]);
			var d:ArrayCollection = ObjectUtil.copy(c) as ArrayCollection;
			for each (sal in d)
				assertTrue("Salutation " + sal.name, Salutation.constants.indexOf(sal) >= 0);
				
            var q:BasicMap = new BasicMap();
			q.put(sal1, "Zozo");
			q.put(sal2, "Zuzu");
			var r:BasicMap = ObjectUtil.copy(q) as BasicMap;
			for each (sal in r.keySet)
				assertTrue("Salutation " + sal.name, Salutation.constants.indexOf(sal) >= 0);
        }
    }
}
