package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.binding.utils.ChangeWatcher;
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Change;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.CollectionChanges;
    
    
    public class TestChangeSetEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        [Test]
        public function testChangeSetEntity():void {
        	var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;        	
        	person.contacts = new ArrayCollection();
        	var contact:Contact = new Contact();
			contact.uid = "C1";
			contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	
        	_ctx.person = _ctx.meta_mergeExternal(person);

        	person = _ctx.person;
			
			var changeSet:ChangeSet = _ctx.meta_buildChangeSet();
			
			Assert.assertEquals("ChangeSet empty", 0, changeSet.length);
			
        	person.lastName = 'toto';
			
			changeSet = _ctx.meta_buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			
			person.contacts.removeItemAt(0);
			
			changeSet = _ctx.meta_buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 2", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			var coll:CollectionChanges = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 1, coll.length);
			Assert.assertEquals("ChangeSet collection type", -1, coll.getChange(0).type);
			Assert.assertEquals("ChangeSet collection index", 0, coll.getChange(0).key);
			Assert.assertEquals("ChangeSet collection value", "C1", coll.getChange(0).value.uid);
			
			var contact2:Contact = new Contact();
			contact2.email = "test@truc.net";
			contact2.person = person;
			person.contacts.addItem(contact2);
			
			changeSet = _ctx.meta_buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 3", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			coll = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 2, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(1).type);
			Assert.assertEquals("ChangeSet collection index", 0, coll.getChange(1).key);
			Assert.assertStrictlyEquals("ChangeSet collection value", contact2, coll.getChange(1).value);
			
			var contact3:Contact = new Contact();
			contact3.email = "tutu@tutu.net";
			contact3.version = 0;
			contact3.uid = "C3";
			contact3.id = 3;
			_ctx.contact3 = _ctx.meta_mergeExternal(contact3);
			contact3 = _ctx.contact3;
			
			person.contacts.addItem(contact3);
			
			changeSet = _ctx.meta_buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 4", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			coll = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 3, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(2).type);
			Assert.assertEquals("ChangeSet collection index", 1, coll.getChange(2).key);
			Assert.assertTrue("ChangeSet collection value", coll.getChange(2).value is ChangeRef);
			Assert.assertStrictlyEquals("ChangeSet collection value", contact3.uid, coll.getChange(2).value.uid);
        }
    }
}
