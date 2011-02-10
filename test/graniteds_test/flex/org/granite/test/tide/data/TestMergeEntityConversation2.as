package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeEntityConversation2 
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
        public function testMergeEntityConversation2():void {
        	var person:Person = new Person();
        	person.id = 1;
        	person.version = 0;
        	person.uid = "P1";
        	person.lastName = "Jojo";
        	person.contacts = new PersistentSet(false);
        	_ctx.person = _ctx.meta_mergeExternalData(person, null);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "startConversation", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#2", "startConversation", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "updateConversation"));
        	
        	Assert.assertFalse("global person not updated", IPersistentCollection(person.contacts).isInitialized());
        	Assert.assertTrue("person 1 updated", Tide.getInstance().getContext("Test#1").person.contacts.isInitialized());
        	Assert.assertFalse("person 2 not updated", Tide.getInstance().getContext("Test#2").person.contacts.isInitialized());
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "endConversation"));
        	
        	Assert.assertTrue("global person updated", IPersistentCollection(person.contacts).isInitialized());
        	Assert.assertTrue("person 2 updated", Tide.getInstance().getContext("Test#2").person.contacts.isInitialized());
        }
    }
}
