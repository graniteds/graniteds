package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
	import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.data.Conflicts;
    import org.granite.tide.data.events.TideDataConflictsEvent;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeConflictEntityConversation 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
			Tide.getInstance().addComponents([MyComponentConflictConversation]);
        }
        
        
        private var _conflicts:Conflicts;
        
        
        [Test]
        public function testMergeConflictEntityConversation():void {
        	var person:Person = new Person();
        	person.id = 1; 
        	person.version = 0;
        	person.contacts = new ArrayCollection();
        	var contact:Contact = new Contact();
        	contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	_ctx.person = _ctx.meta_mergeExternalData(person, null, null);
        	person = _ctx.person;
			
			_ctx.dispatchEvent(new TideUIConversationEvent("person#1", "editPerson", person));
			
			var component:Object = Tide.getInstance().getContext("person#1").byType(MyComponentConflictConversation);
        	
        	Assert.assertTrue("Person dirty", component.context.meta_isEntityChanged(component.person));
        	
        	var person2:Person = new Person();
        	person2.contacts = new ArrayCollection();
        	person2.id = person.id;
        	person2.version = 1;
        	person2.uid = person.uid;
        	person2.lastName = "tutu";
        	var contact2:Contact = new Contact();
        	contact2.id = contact.id;
        	contact2.version = 1;
        	contact2.uid = contact.uid;
        	contact2.email = "test2";
        	contact2.person = person2;
        	person2.contacts.addItem(contact2);
        	
        	_ctx.meta_mergeExternalData(person2, null, "S2");
        	
        	Assert.assertEquals("Conflicts after merge", 1, component.conflicts.conflicts.length);
        	
			component.conflicts.conflicts[0].acceptClient();
        	
        	Assert.assertEquals("Person last name", "toto", component.person.lastName);
        	Assert.assertEquals("Person contacts", 2, component.person.contacts.length);
        	Assert.assertEquals("Person version", 1, component.person.version);
        	Assert.assertTrue("Person dirty", component.context.meta_isEntityChanged(component.person));
        	
        	component.context.meta_resetEntity(component.person);
        	
        	Assert.assertEquals("Person last name after cancel", "tutu", component.person.lastName);
        	Assert.assertEquals("Person contacts after cancel", 1, component.person.contacts.length);
        	
			component.person.lastName = "toto";
        	
        	var person3:Person = new Person();
        	person3.contacts = new ArrayCollection();
        	person3.id = person.id;
        	person3.version = 2;
        	person3.uid = person.uid;
        	person3.lastName = "titi";
        	var contact3:Contact = new Contact();
        	contact3.id = contact.id;
        	contact3.version = 1;
        	contact3.uid = contact.uid;
        	contact3.email = "test2";
        	contact3.person = person3;
        	person3.contacts.addItem(contact3);
        	var contact3b:Contact = new Contact();
        	contact3b.id = 3;
        	contact3b.version = 0;
        	contact3b.email = "test3";
        	contact3b.person = person3;
        	person3.contacts.addItem(contact3);
        	
        	_ctx.meta_mergeExternalData(person3, null, "S3");
        	
        	Assert.assertEquals("Conflicts after merge 2", 1, component.conflicts.conflicts.length);
        	
			component.conflicts.conflicts[0].acceptServer();
        	
        	Assert.assertEquals("Person last name", "titi", component.person.lastName);
        	Assert.assertEquals("Person version", 2, component.person.version);
        	Assert.assertEquals("Person contacts", 2, component.person.contacts.length);
        	Assert.assertFalse("Person not dirty", component.context.meta_isEntityChanged(component.person));
        }
    }
}
