package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Person0;
    
    
    public class TestMergeCollection2 extends TestCase
    {
        public function TestMergeCollection2() {
            super("testMergeCollection2");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testMergeCollection2():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B2"));
        	coll.addItem(new Person0(3, "A3", "B3"));
        	coll.addItem(new Person0(4, "A4", "B4"));
        	_ctx.meta_mergeExternalData(coll);
        	
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(new Person0(5, "A5", "B5"));
        	coll2.addItem(new Person0(4, "A4", "B4"));
        	coll2.addItem(new Person0(3, "A3", "B3"));
        	coll2.addItem(new Person0(2, "A2", "B2"));
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	assertEquals("Element 0", 5, coll.getItemAt(0).id);
        	assertEquals("Element 2", 3, coll.getItemAt(2).id);
        	assertEquals("Element 3", 2, coll.getItemAt(3).id);
        }
    }
}
