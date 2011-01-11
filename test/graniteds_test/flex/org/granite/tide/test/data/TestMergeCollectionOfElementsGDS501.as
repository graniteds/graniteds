package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Person2;
    
    
    public class TestMergeCollectionOfElementsGDS501 extends TestCase
    {
        public function TestMergeCollectionOfElementsGDS501() {
            super("testMergeCollectionOfElementsGDS501");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testMergeCollectionOfElementsGDS501():void {
        	var person:Person2 = new Person2();
        	person.version = 0;
        	person.names = new ArrayCollection();
        	person.names.addItem("Jacques");
        	person.names.addItem("Nicolas");
        	_ctx.person = person;
        	
        	var person2:Person2 = new Person2();
        	person2.version = 1;
        	person2.names = new ArrayCollection();
        	person2.names.addItem("Jacques");
        	person2.names.addItem("Nicolas");
        	
        	_ctx.meta_mergeExternal(person2, person);
        	
        	assertEquals("Collection merged", 2, person.names.length);
        }
    }
}
