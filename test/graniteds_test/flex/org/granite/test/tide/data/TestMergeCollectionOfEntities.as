package org.granite.test.tide.data
{
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
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
    }
}
