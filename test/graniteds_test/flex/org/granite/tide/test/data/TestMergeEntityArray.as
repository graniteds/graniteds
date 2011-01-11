package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
	import mx.events.PropertyChangeEvent;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestMergeEntityArray extends TestCase
    {
        public function TestMergeEntityArray() {
            super("testMergeEntityArray");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testMergeEntityArray():void {
			var a1:EmbeddedAddress = new EmbeddedAddress();
			a1.address1 = "12 Main Street";
        	var p1:Person5 = new Person5();
			p1.id = 1;
			p1.uid = "P1";
			p1.version = 0;
			p1.address = [ a1 ];
			_ctx.meta_mergeExternalData(p1);
			
        	var p2:Person5 = new Person5();
			p2.id = 1;
			p2.uid = "P1";
			p2.version = 1;
			p2.address = null;
        	var p:Person5 = _ctx.meta_mergeExternalData(p2) as Person5;
			
			assertNull("Array address merged", p.address);
			
			var a2a:EmbeddedAddress = new EmbeddedAddress();
			a2a.address1 = "12 Main Street";
			var a2b:EmbeddedAddress = new EmbeddedAddress();
			a2b.address1 = "13 Main Street";
			var p3:Person5 = new Person5();
			p3.id = 1;
			p3.uid = "P1";
			p3.version = 2;
			p3.address = [ a2a, a2b ];
			_ctx.meta_mergeExternalData(p3);
			
			assertEquals("Array address merged 2", 2, p.address.length);
        }
    }
}
