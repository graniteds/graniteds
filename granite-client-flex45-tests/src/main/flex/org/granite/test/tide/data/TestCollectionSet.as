package org.granite.test.tide.data
{
    import mx.collections.ArrayCollection;
    import mx.utils.ObjectUtil;
    
    import org.flexunit.Assert;
    import org.granite.collections.BasicMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    
    
    public class TestCollectionSet 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testCollectionSet():void {
			var coll:ArrayCollection = new ArrayCollection();
			var person:Person = new Person(1, 0, "A1", "B1");
			var contact1:Contact = new Contact(1, 0, person, "C1");
			coll.addItem(contact1);
			coll.addItem(contact1);
			var contact2:Contact = new Contact(2, 0, person, "C2");
			coll.addItem(contact2);
			
			person.contacts = new PersistentCollection(person, "contacts", new PersistentSet());
			person.contacts.addItem(contact2);
			person.contacts.addItem(new Contact(3, 0, person, "C3"));
			person.contacts.addAllAt(coll, 1);
			
			Assert.assertEquals("Set length", 3, person.contacts.length);
			Assert.assertEquals("Set 0", 2, person.contacts.getItemAt(0).id);
			Assert.assertEquals("Set 1", 1, person.contacts.getItemAt(1).id);
			Assert.assertEquals("Set 2", 3, person.contacts.getItemAt(2).id);
        }
		
		[Test]
		public function testCollectionSet2():void {
			var coll:ArrayCollection = new ArrayCollection();
			var person:Person = new Person(1, 0, "A1", "B1");
			var contact1:Contact = new Contact(1, 0, person, "C1");
			coll.addItem(contact1);
			coll.addItem(contact1);
			var contact2:Contact = new Contact(2, 0, person, "C2");
			coll.addItem(contact2);
			
			person.contacts = new PersistentCollection(person, "contacts", new PersistentSet());
			person.contacts.addItem(new Contact(3, 0, person, "C3"));
			person.contacts.addAllAt(coll, 0);
			
			Assert.assertEquals("Set length", 3, person.contacts.length);
			Assert.assertEquals("Set 0", 1, person.contacts.getItemAt(0).id);
			Assert.assertEquals("Set 1", 2, person.contacts.getItemAt(1).id);
			Assert.assertEquals("Set 2", 3, person.contacts.getItemAt(2).id);
		}
		
		[Test]
		public function testCollectionSet3():void {
			var coll:ArrayCollection = new ArrayCollection();
			var person:Person = new Person(1, 0, "A1", "B1");
			var contact1:Contact = new Contact(1, 0, person, "C1");
			coll.addItem(contact1);
			coll.addItem(contact1);
			var contact2:Contact = new Contact(2, 0, person, "C2");
			coll.addItem(contact2);
			
			person.contacts = new PersistentCollection(person, "contacts", new PersistentSet());
			person.contacts.addItem(new Contact(3, 0, person, "C3"));
			person.contacts.addAll(coll);
			
			Assert.assertEquals("Set length", 3, person.contacts.length);
			Assert.assertEquals("Set 0", 3, person.contacts.getItemAt(0).id);
			Assert.assertEquals("Set 1", 1, person.contacts.getItemAt(1).id);
			Assert.assertEquals("Set 2", 2, person.contacts.getItemAt(2).id);
		}
    }
}
