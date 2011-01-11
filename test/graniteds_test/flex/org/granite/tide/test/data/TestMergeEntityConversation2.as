package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.persistence.PersistentSet;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.test.Person;
    
    
    public class TestMergeEntityConversation2 extends TestCase
    {
        public function TestMergeEntityConversation2() {
            super("testMergeEntityConversation2");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();            
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([EntityConversation]);
        }
        
        
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
        	
        	assertFalse("global person not updated", IPersistentCollection(person.contacts).isInitialized());
        	assertTrue("person 1 updated", Tide.getInstance().getContext("Test#1").person.contacts.isInitialized());
        	assertFalse("person 2 not updated", Tide.getInstance().getContext("Test#2").person.contacts.isInitialized());
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "endConversation"));
        	
        	assertTrue("global person updated", IPersistentCollection(person.contacts).isInitialized());
        	assertTrue("person 2 updated", Tide.getInstance().getContext("Test#2").person.contacts.isInitialized());
        }
    }
}
