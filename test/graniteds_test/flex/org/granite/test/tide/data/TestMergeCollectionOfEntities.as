package org.granite.test.tide.data
{
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.meta;
    import org.granite.persistence.PersistentList;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    
    
    public class TestMergeCollectionOfEntities 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeCollectionOfEntities():void {
        	var person:Person = new Person();
        	person.uid = "P01";
        	person.version = 0;
        	person.contacts = new ArrayCollection();
        	var c1:Contact = new Contact();
        	c1.uid = "C01";
        	c1.version = 0;
        	c1.person = person;
        	c1.email = "toto@toto.com";
        	person.contacts.addItem(c1);
        	var c2:Contact = new Contact();
        	c2.uid = "C02";
        	c2.version = 0;
        	c2.person = person;
        	c2.email = "toto@toto.net";
        	person.contacts.addItem(c2);
        	_ctx.person = _ctx.meta_mergeExternal(person);
        	
        	var person2:Person = new Person();
        	person2.uid = "P01";
        	person2.version = 0;
        	person2.contacts = new ArrayCollection();
        	var c21:Contact = new Contact();
        	c21.uid = "C01";
        	c21.version = 0;
        	c21.person = person;
        	c21.email = "toto@toto.com";
        	person2.contacts.addItem(c21);
        	var c22:Contact = new Contact();
        	c22.uid = "C02";
        	c22.version = 0;
        	c22.person = person;
        	c22.email = "toto@toto.net";
        	person2.contacts.addItem(c22);
        	var c23:Contact = new Contact();
        	c23.uid = "C03";
        	c23.version = 0;
        	c23.person = person;
        	c23.email = "toto@toto.org";
        	person2.contacts.addItem(c23);
        	
        	_ctx.meta_mergeExternal(person2, person);
        	
        	Assert.assertEquals("Collection merged", 3, person.contacts.length);
        }
		
		
		[Test]
		public function testMergeCollectionOfEntitiesRemove():void {
			var person0:Person = new Person();
			person0.uid = "P01";
			person0.contacts = new ArrayCollection();
			_ctx.person = person0;
			var c01:Contact = new Contact();
			c01.uid = "C01";
			c01.person = person0;
			c01.email = "toto@toto.com";
			person0.contacts.addItem(c01);
			var c02:Contact = new Contact();
			c02.uid = "C02";
			c02.person = person0;
			c02.email = "toto@toto.net";
			person0.contacts.addItem(c02);

			var person:Person = new Person();
			person.uid = "P01";
			person.version = 0;
			person.contacts = new PersistentSet();
			var c1:Contact = new Contact();
			c1.uid = "C01";
			c1.version = 0;
			c1.person = person;
			c1.email = "toto@toto.com";
			person.contacts.addItem(c1);
			var c2:Contact = new Contact();
			c2.uid = "C02";
			c2.version = 0;
			c2.person = person;
			c2.email = "toto@toto.net";
			person.contacts.addItem(c2);
			_ctx.meta_mergeExternalData(person);
			_ctx.meta_clearCache();	// clear context cache to simulate remote call
			
			var person2:Person = new Person();
			person2.uid = "P01";
			person2.version = 0;
			person2.contacts = new PersistentSet(false);
			var c21:Contact = new Contact();
			c21.uid = "C01";
			c21.version = 0;
			c21.person = person2;
			c21.email = "toto@toto.com";
			
			_ctx.meta_mergeExternalData(person2, null, null, [ c21 ]);
			
			Assert.assertEquals("Removals merged", 1, person.contacts.length);
		}
		
		
		[Test]
		public function testMergeCollectionOfEntities2():void {
			_ctx.meta_uninitializeAllowed = false;
			
			var p:Person = new Person();
			p.uid = "P1";
			p.id = 1;
			p.version = 0;
			p.contacts = new PersistentList();
			var c1:Contact = new Contact();
			c1.uid = "C1";
			c1.id = 1;
			c1.version = 0;
			c1.person = p;
			c1.email = "toto@toto.com";
			p.contacts.addItem(c1);
			var c2:Contact = new Contact();
			c2.uid = "C2";
			c2.id = 2;
			c2.version = 0;
			c2.person = p;
			c2.email = "toto@toto.net";
			p.contacts.addItem(c2);
			_ctx.person = _ctx.meta_mergeExternalData(p);
			p = _ctx.person;
			
			var pb:Person = new Person();
			pb.uid = "P1";
			pb.id = 1;
			pb.version = 0;
			pb.contacts = new PersistentList();
			var pc:Person = new Person();
			pc.uid = "P1";
			pc.id = 1;
			pc.version = 0;
			pc.contacts = new PersistentList(false);
			var c1b:Contact = new Contact();
			c1b.uid = "C1";
			c1b.id = 1;
			c1b.version = 0;
			c1b.person = pb;
			c1b.email = "toto@toto.com";
			pb.contacts.addItem(c1b);
			var c2b:Contact = new Contact();
			c2b.uid = "C2";
			c2b.id = 2;
			c2b.version = 0;
			c2b.person = pb;
			c2b.email = "toto@toto.net";
			pb.contacts.addItem(c2b);
			var c3b:Contact = new Contact();
			c3b.uid = "C3";
			c3b.version = 0;
			c3b.person = pb;
			c3b.email = "toto@toto.org";
			pb.contacts.addItem(c3b);
			var c3c:Contact = new Contact();
			c3c.uid = "C3";
			c3c.version = 0;
			c3c.person = pc;
			c3c.email = "toto@toto.org";
			pb.contacts.addItem(c3c);
			
			_ctx.meta_mergeExternal(pb, p);
			
			Assert.assertStrictlyEquals("Person em", _ctx, p.meta::entityManager);
			for each (var c:Contact in p.contacts)
				Assert.assertStrictlyEquals("Contact attached", p, c.person);
		}
    }
}
