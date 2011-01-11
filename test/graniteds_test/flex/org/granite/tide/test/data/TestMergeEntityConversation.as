package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestMergeEntityConversation extends TestCase
    {
        public function TestMergeEntityConversation() {
            super("testMergeEntityConversation");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();            
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([EntityConversation]);
        }
        
        
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
        	
        	assertEquals("global person not updated", 1, person.contacts.length);
        	assertEquals("person 1 updated", 2, Tide.getInstance().getContext("Test#1").person.contacts.length);
        	assertEquals("person 2 not updated", 1, Tide.getInstance().getContext("Test#2").person.contacts.length);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "endConversation"));
        	
        	assertEquals("global person updated", 2, person.contacts.length);
        	assertEquals("person 2 updated", 2, Tide.getInstance().getContext("Test#2").person.contacts.length);
        }
    }
}
