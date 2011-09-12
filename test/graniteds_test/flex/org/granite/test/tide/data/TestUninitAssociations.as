package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    import org.granite.meta;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;


    public class TestUninitAssociations {

        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function TestUninitializeAssociations():void {
        	var person:Person = new Person();
            person.id = 1;
            person.version = 0;
            person.uid = "P1";
            person.contacts = new PersistentSet(false);

            person = Person(_ctx.meta_mergeExternalData(person));

            var contact:Contact = new Contact();
            contact.id = 1;
            contact.version = 0;
            contact.uid = "C1";
            contact.person = new Person();
            contact.person.meta::defineProxy(1);

            _ctx.meta_mergeExternalData(contact);

        	var person2:Person = new Person();
            person2.id = 1;
        	person2.version = 0;
            person2.uid = "P1";
            person2.contacts = new PersistentSet(true);
        	var contact2:Contact = new Contact();
            contact2.id = 1;
        	contact2.version = 0;
            contact2.uid = "C1";
        	contact2.person = person2;
        	person2.contacts.addItem(contact2);

        	person2 = Person(_ctx.meta_mergeExternalData(person2));
            contact2 = Contact(person2.contacts.getItemAt(0));

            var tmpctx:BaseContext = _ctx.newTemporaryContext();
            var p:Person = Person(tmpctx.meta_mergeFromContext(_ctx, person2, false, true));

        	Assert.assertFalse("Contacts coll uninitialized", Object(p.contacts).isInitialized());

            tmpctx = _ctx.newTemporaryContext();
            var c:Contact = Contact(tmpctx.meta_mergeFromContext(_ctx, contact2, false, true));

        	Assert.assertFalse("Person assoc proxied", c.meta::isInitialized("person"));
		}
    }
}
