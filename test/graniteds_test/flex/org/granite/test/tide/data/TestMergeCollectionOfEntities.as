package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
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
    }
}
