package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeEntityConversation4 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();            
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([BaseEntityConversation, EntityConversation]);
        }
        
        
        [Test]
        public function testMergeEntityConversation4():void {
        	var person:Person = new Person();
        	person.id = 1;
        	person.version = 0;
        	person.uid = "P1";
        	person.lastName = "Jojo";
        	person.contacts = new ArrayCollection();
        	_ctx.person = _ctx.meta_mergeExternalData(person, null);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("1", "start", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("2", "start", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("3", "start", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("1", "endMerge"));
        	
        	Assert.assertEquals("global person not updated", "Jojo", person.lastName);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("2", "endMerge2"));
        	
        	Assert.assertEquals("global person updated", "Juju", person.lastName);
        	Assert.assertEquals("intermediate 3 person updated", "Juju", Tide.getInstance().getContext("3").person.lastName);
        	Assert.assertEquals("conversation 3.1 person updated", "Juju", Tide.getInstance().getContext("3.1").person.lastName);
        }
    }
}
