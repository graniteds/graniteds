package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeEntityConversation3 
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
        	
        	Assert.assertEquals("global person not updated", "Jojo", person.lastName);
        	Assert.assertEquals("person 2 not updated", "Jojo", Tide.getInstance().getContext("Test#2").person.lastName);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("Test#2", "endMergeConversation2"));
        	
        	Assert.assertEquals("global person updated", "Juju", person.lastName);
        }
    }
}
