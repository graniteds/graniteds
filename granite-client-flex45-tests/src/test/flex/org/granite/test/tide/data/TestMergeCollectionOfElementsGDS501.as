package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person2;
    
    
    public class TestMergeCollectionOfElementsGDS501 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
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
        	
        	Assert.assertEquals("Collection merged", 2, person.names.length);
        }
    }
}
