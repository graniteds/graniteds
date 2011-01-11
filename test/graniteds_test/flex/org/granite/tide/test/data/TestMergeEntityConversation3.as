package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.test.Person;
    
    
    public class TestMergeEntityConversation3 extends TestCase
    {
        public function TestMergeEntityConversation3() {
            super("testMergeEntityConversation3");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();            
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([EntityConversation]);
        }
        
        
        public function testMergeEntityConversation3():void {
        	var person:Person = new Person();
        	person.id = 1;
        	person.version = 0;
        	person.uid = "P1";
        	person.lastName = "Jojo";
        	person.contacts = new ArrayCollection();
        	_ctx.person = _ctx.meta_mergeExternalData(person, null);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "startConversation", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#2", "startConversation", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#1", "endMergeConversation"));
        	
        	assertEquals("global person not updated", "Jojo", person.lastName);
        	assertEquals("person 2 not updated", "Jojo", Tide.getInstance().getContext("Test#2").person.lastName);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#2", "endMergeConversation2"));
        	
        	assertEquals("global person updated", "Juju", person.lastName);
        }
    }
}
