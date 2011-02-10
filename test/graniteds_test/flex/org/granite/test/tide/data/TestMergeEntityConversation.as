package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeEntityConversation 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();            
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([EntityConversation]);
        }
        
        
        [Test]
        public function testMergeEntityConversation():void {
        	var person:Person = new Person();
        	person.id = 1;
        	person.version = 0;
        	person.uid = "P1";
        	person.lastName = "Jojo";
        	person.contacts = new PersistentSet();
        	var contact:Contact = new Contact();
        	contact.person = person;
        	contact.id = 1;
        	contact.version = 0;
        	contact.uid = "C1";
        	contact.email = "jojo@jojo.net";
        	person.contacts.addItem(contact);
        	_ctx.person = _ctx.meta_mergeExternalData(person, null);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "startConversation", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#2", "startConversation", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "updateConversation"));
        	
        	Assert.assertEquals("global person not updated", 1, person.contacts.length);
        	Assert.assertEquals("person 1 updated", 2, Tide.getInstance().getContext("Test#1").person.contacts.length);
        	Assert.assertEquals("person 2 not updated", 1, Tide.getInstance().getContext("Test#2").person.contacts.length);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "endConversation"));
        	
        	Assert.assertEquals("global person updated", 2, person.contacts.length);
        	Assert.assertEquals("person 2 updated", 2, Tide.getInstance().getContext("Test#2").person.contacts.length);
        }
    }
}
