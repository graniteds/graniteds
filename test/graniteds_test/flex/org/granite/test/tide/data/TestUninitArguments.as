package org.granite.test.tide.data
{
import mx.data.utils.Managed;

    import org.flexunit.Assert;
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
import org.granite.persistence.PersistentMap;
import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Address;
import org.granite.test.tide.Contact;
import org.granite.test.tide.data.Contact4;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.EntityGraphUninitializer;


    public class TestUninitArguments {

        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function TestUninitializeArguments():void {
        	var person:Person11 = new Person11();
            person.id = 1;
            person.version = 0;
            person.uid = "P1";
            person.contacts = new PersistentSet(true);
            var contact:Contact4 = new Contact4();
            contact.id = 1;
            contact.version = 0;
            contact.uid = "C1";
            var address:Address = new Address();
            address.id = 1;
            address.version = 0;
            address.uid = "A1";
            contact.address = address;
            contact.person = person;
            person.contacts.addItem(contact);
            person.map = new PersistentMap(true);

            person = Person11(_ctx.meta_mergeExternalData(person));
            contact = person.contacts.getItemAt(0) as Contact4;

            var p:Person11 = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(person) as Person11;

            Assert.assertFalse("Contacts coll uninitialized", p.meta::isInitialized("contacts"));
            Assert.assertFalse("Map uninitialized", p.meta::isInitialized("map"));

            var c:Contact4 = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(contact) as Contact4;

            Assert.assertFalse("Contact associations uninitialized", c.person.meta::isInitialized());
            Assert.assertFalse("Contact associations uninitialized", c.address.meta::isInitialized());

            contact.email = "test@test.com";

            p = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(person) as Person11;

            Assert.assertTrue("Contacts coll initialized", p.meta::isInitialized("contacts"));
            c = p.contacts.getItemAt(0) as Contact4;
            Assert.assertStrictlyEquals("Contact person", p, c.person);
            Assert.assertFalse("Contact address uninitialized", c.address.meta::isInitialized());

            c = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(contact) as Contact4;

            Assert.assertFalse("Contact associations uninitialized", c.person.meta::isInitialized());
            Assert.assertFalse("Contact associations uninitialized", c.address.meta::isInitialized());

            person.lastName = "Test";

            c = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(contact) as Contact4;

            Assert.assertFalse("Contact associations uninitialized", c.address.meta::isInitialized());
            Assert.assertTrue("Contact person association initialized", c.person.meta::isInitialized());
            Assert.assertFalse("Person contacts coll uninitialized", c.person.meta::isInitialized("contacts"));
		}

        [Test]
        public function TestUninitializeArguments2():void {
        	var person:Person11 = new Person11();
            person.id = 1;
            person.version = 0;
            person.uid = "P1";
            person.contacts = new PersistentSet(true);
            var contact:Contact4 = new Contact4();
            contact.id = 1;
            contact.version = 0;
            contact.uid = "C1";
            var address:Address = new Address();
            address.id = 1;
            address.version = 0;
            address.uid = "A1";
            contact.address = address;
            contact.person = person;
            person.contacts.addItem(contact);
            person.map = new PersistentMap(true);

            person = Person11(_ctx.meta_mergeExternalData(person));
            contact = person.contacts.getItemAt(0) as Contact4;

            contact.address.homeAddress = "Test";

            var p:Person11 = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(person) as Person11;

            Assert.assertTrue("Contacts coll initialized", p.meta::isInitialized("contacts"));
            var c:Contact4 = p.contacts.getItemAt(0) as Contact4;
            Assert.assertStrictlyEquals("Contact person", p, c.person);
            Assert.assertTrue("Contact address initialized", c.address.meta::isInitialized());
            Assert.assertFalse("Map uninitialized", p.meta::isInitialized("map"));

            c = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(contact) as Contact4;

            Assert.assertFalse("Contact associations uninitialized", c.person.meta::isInitialized());
            Assert.assertTrue("Contact associations uninitialized", c.address.meta::isInitialized());
        }


        [Test]
        public function TestUninitializeArguments3():void {
        	var person:Person11 = new Person11();
            person.id = 1;
            person.version = 0;
            person.uid = "P1";
            person.contacts = new PersistentSet(true);
            var contact:Contact4 = new Contact4();
            contact.id = 1;
            contact.version = 0;
            contact.uid = "C1";
            var address:Address = new Address();
            address.id = 1;
            address.version = 0;
            address.uid = "A1";
            contact.address = address;
            contact.person = person;
            person.contacts.addItem(contact);
            person.map = new PersistentMap(true);

            person = Person11(_ctx.meta_mergeExternalData(person));
            contact = person.contacts.getItemAt(0) as Contact4;

            var contact2:Contact4 = new Contact4();
            contact2.uid = "C2";
            contact2.address = contact.address;
            contact2.person = person;

            person.contacts.addItem(contact2);

            var p:Person11 = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(person) as Person11;

            Assert.assertTrue("Contacts coll initialized", p.meta::isInitialized("contacts"));
            var c:Contact4 = p.contacts.getItemAt(1) as Contact4;
            Assert.assertTrue("Contact address initialized", c.address.meta::isInitialized());
            Assert.assertFalse("Map uninitialized", p.meta::isInitialized("map"));
		}

        [Test]
        public function TestUninitializeArguments4():void {
        	var person:Person11 = new Person11();
            person.id = 1;
            person.version = 0;
            person.uid = "P1";
            person.contacts = new PersistentSet(true);
            var contact:Contact4 = new Contact4();
            contact.id = 1;
            contact.version = 0;
            contact.uid = "C1";
            var address:Address = new Address();
            address.id = 1;
            address.version = 0;
            address.uid = "A1";
            contact.address = address;
            contact.person = person;
            person.contacts.addItem(contact);
            person.map = new PersistentMap(true);
            var key:Key = new Key();
            key.id = 1;
            key.version = 0;
            key.uid = "K1";
            var value:Value = new Value();
            value.id = 1;
            value.version = 0;
            value.uid = "V1";
            person.map.put(key, value);

            person = Person11(_ctx.meta_mergeExternalData(person));
            value = person.map.get(key);

            var p:Person11 = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(person) as Person11;

            Assert.assertFalse("Contacts coll uninitialized", p.meta::isInitialized("contacts"));
            Assert.assertFalse("Map uninitialized", p.meta::isInitialized("map"));

            value.name = "Test";

            p = new EntityGraphUninitializer(_ctx).uninitializeEntityGraph(person) as Person11;

            Assert.assertFalse("Contacts coll initialized", p.meta::isInitialized("contacts"));
            Assert.assertTrue("Map initialized", p.meta::isInitialized("map"));
		}
    }
}
