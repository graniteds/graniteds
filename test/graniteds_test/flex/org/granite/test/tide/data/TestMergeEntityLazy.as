package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;

    import org.granite.meta;

    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;


	public class TestMergeEntityLazy
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeEntityLazy():void {
        	var p1:Person9 = new Person9();
			p1.id = 1;
			p1.uid = "P1";
			p1.version = 0;
			p1.lastName = "Test";
			p1 = Person9(_ctx.meta_mergeExternalData(p1));
			
			var c1:Contact3 = new Contact3();
			c1.id = 1;
			c1.uid = "C1";
			c1.version = 0;
			c1.email = "test@test.com";
			var p2:Person9 = new Person9();
			p2.id = 1;
			p2.meta::defineProxy3();
			c1.person = p2;
			var coll:ArrayCollection = new ArrayCollection([c1]);
			var coll2:ArrayCollection = ArrayCollection(_ctx.meta_mergeExternalData(coll));
			
			Assert.assertStrictlyEquals("Contact attached to person", p1, coll2.getItemAt(0).person);
        }
    }

}
