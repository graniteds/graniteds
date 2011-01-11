package org.granite.tide.test.data
{
    import flash.events.TimerEvent;
    import flash.system.System;
    import flash.utils.Timer;
    
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestEntityRefs extends TestCase
    {
        public function TestEntityRefs() {
            super("testEntityRefs");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var contactsNew:ArrayCollection;
        
        public function testEntityRefs():void {
        	Tide.getInstance().setComponentRemoteSync("person", Tide.SYNC_BIDIRECTIONAL);
        	
        	var person:Person = new Person();
        	person.id = 1;
        	person.uid = "P01"; 
        	person.version = 0;
        	var contacts:ArrayCollection = new ArrayCollection();
        	person.contacts = contacts;
        	var contact:Contact = new Contact();
        	contact.id = 1;
        	contact.uid = "C01";
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	_ctx.person = _ctx.meta_mergeExternalData(person);
        	person = _ctx.person;
        	
        	assertEquals("Person bound", "person", _ctx.meta_getReference(person).path);
			assertEquals("Contacts bound", "person", _ctx.meta_getReference(contacts).path);
			assertEquals("Contact bound", "person", _ctx.meta_getReference(contact).path);
			
			contacts = null;
			contactsNew = new ArrayCollection();
			person.contacts = contactsNew;
        	
        	assertEquals("Person still bound", "person", _ctx.meta_getReference(person).path);
			assertNull("Contacts unbound", _ctx.meta_getReference(contacts));
			assertNull("Contact unbound", _ctx.meta_getReference(contact));
			
			contact = null;
			
			var person1:Person = new Person();
        	person1.id = 1; 
        	person1.uid = person.uid;
        	person1.version = 1;
			var contactsNew1:ArrayCollection = new ArrayCollection();
			person1.contacts = contactsNew1;
			_ctx.meta_mergeExternal(person1);	// Clear dirty cache of person
			
			System.gc();	// Force gc to clear refs on contact, works only in debug player
			
			var timer:Timer = new Timer(100);
			timer.addEventListener(TimerEvent.TIMER, addAsync(nextPart, 1000));
			timer.start();
        }
        
        private function nextPart(event:Object):void {			
			var person2:Person = new Person();
			person2.id = 1;
			person2.uid = "P01";
			person2.version = 2;
			var contacts2:ArrayCollection = new ArrayCollection();
			person2.contacts = contacts2;
			var contact2:Contact = new Contact();
			contact2.id = 1;
			contact2.uid = "C01";
			contact2.version = 1;
			contact2.person = person2;
			person2.contacts.addItem(contact2);
			
			_ctx.meta_mergeExternalData(person2);
			
			assertEquals("Contacts rebound", "person", _ctx.meta_getReference(contactsNew).path);
			assertEquals("Contact rebound", "person", _ctx.meta_getReference(contact2).path);
			
			var person3:Person = new Person();
			person3.id = 1;
			person3.uid = "P01";
			person3.version = 3;
			var contacts3:ArrayCollection = new ArrayCollection();
			person3.contacts = contacts3;
			_ctx.meta_mergeExternal(person3);
			
			assertEquals("Contacts bound", "person", _ctx.meta_getReference(contactsNew).path);
			assertEquals("Contact unbound", "person", _ctx.meta_getReference(contact2).path);
        }
    }
}
