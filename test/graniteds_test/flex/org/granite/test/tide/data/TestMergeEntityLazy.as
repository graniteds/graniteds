package org.granite.test.tide.data
{
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.math.Long;
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
		
		[Test]
		public function testMergeEntityLazyGDS997a():void {
			var p1:Person9 = new Person9();
			p1.id = 1;
			p1.meta::defineProxy3();
			var c1:Contact3 = new Contact3();
			c1.id = 1;
			c1.uid = "C1";
			c1.version = 0;
			c1.email = "test@test.com";
			c1.person = p1;
			c1 = Contact3(_ctx.meta_mergeExternalData(c1));
			
			var p2:Person9 = new Person9();
			p2.id = 1;
			p2.uid = "P1";
			p2.version = 0;
			p2.lastName = "Test";
			var c2:Contact3 = new Contact3();
			c2.id = 1;
			c2.uid = "C1";
			c2.version = 0;
			c2.email = "test@test.com";
			c2.person = p2;
			
			_ctx.meta_mergeExternalData(c2);
			
			Assert.assertEquals("Proxy merged", "P1", p1.uid);
		}
		
		[Test]
		public function testMergeEntityLazyGDS997b():void {
			var p1:Person9b = new Person9b();
			p1.id = new Long(1);
			p1.meta::defineProxy3();
			var c1:Contact3b = new Contact3b();
			c1.id = new Long(1);
			c1.uid = "C1";
			c1.version = new Long(0);
			c1.email = "test@test.com";
			c1.person = p1;
			c1 = Contact3b(_ctx.meta_mergeExternalData(c1));
			
			var p2:Person9b = new Person9b();
			p2.id = new Long(1);
			p2.uid = "P1";
			p2.version = new Long(0);
			p2.lastName = "Test";
			var c2:Contact3b = new Contact3b();
			c2.id = new Long(1);
			c2.uid = "C1";
			c2.version = new Long(0);
			c2.email = "test@test.com";
			c2.person = p2;

			_ctx.meta_mergeExternalData(c2);
			
			Assert.assertEquals("Proxy merged", "P1", p1.uid);
		}
		
		[Test]
		public function testMergeEntityLazyGDS997c():void {
			
			var p1:PersonNoVersion = new PersonNoVersion();
			p1.id = 1;
			p1.uid = "P1";
			p1.lastName = "Test";
			var c1:ContactNoVersion = new ContactNoVersion();
			c1.id = 1;
			c1.uid = "C1";
			c1.email = "test@test.com";
			c1.person = p1;
			c1 = ContactNoVersion(_ctx.meta_mergeExternalData(c1));		
			
			var p2:PersonNoVersion = new PersonNoVersion();
			p2.id = 1;
			p2.meta::defineProxy3();
			var c2:ContactNoVersion = new ContactNoVersion();
			c2.id = 1;
			c2.uid = "C1";
			c2.email = "test@test.com";
			c2.person = p2;
			_ctx.meta_mergeExternalData(c2);
			
			Assert.assertTrue("Proxy ignored", p1.meta::isInitialized());
		}
    }

}
