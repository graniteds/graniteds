package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.test.Person;
    
    
    public class TestMergeEntityConversation4 extends TestCase
    {
        public function TestMergeEntityConversation4() {
            super("testMergeEntityConversation4");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();            
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([BaseEntityConversation, EntityConversation]);
        }
        
        
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
        	
        	assertEquals("global person not updated", "Jojo", person.lastName);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("2", "endMerge2"));
        	
        	assertEquals("global person updated", "Juju", person.lastName);
        	assertEquals("intermediate 3 person updated", "Juju", Tide.getInstance().getContext("3").person.lastName);
        	assertEquals("conversation 3.1 person updated", "Juju", Tide.getInstance().getContext("3.1").person.lastName);
        }
    }
}
