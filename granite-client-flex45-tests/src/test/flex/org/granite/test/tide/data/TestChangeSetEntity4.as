package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.binding.utils.ChangeWatcher;
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    
    import org.flexunit.Assert;
    import org.granite.collections.BasicMap;
    import org.granite.collections.IPersistentCollection;
    import org.granite.persistence.PersistentList;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Change;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    
    
    public class TestChangeSetEntity4
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
			Tide.getInstance().addComponents([ ChangeMerger ]);
        }
        
        
        [Test]
        public function testChangeSetEntity4():void {
        	var person:Person4 = new Person4();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
        	person.salutation = Salutation.Dr;
			person.address = new EmbeddedAddress();
        	
        	_ctx.person = _ctx.meta_mergeExternal(person);

        	person = _ctx.person;
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet empty", 0, changeSet.changes.length);
			
			person.lastName = "zozo";
			
			var contact:Contact = new Contact();			
			_ctx.contact = contact;
			contact.email = "zozo@zozo.net";
			
			person.contacts = new ArrayCollection();
			person.contacts.addItem(contact);
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.changes.length);
			Assert.assertEquals("ChangeSet lastName value", "zozo", changeSet.changes[0].changes.lastName);
			Assert.assertEquals("ChangeSet contacts value", "zozo@zozo.net", changeSet.changes[0].changes.contacts.changes[0].value.email);
		}
		
		
		[Test]
		public function testChangeSetEntity5():void {
			var patient:Patient3 = new Patient3();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.tests = new PersistentSet();
			patient.visits = new PersistentSet();
			var visit:Visit = new Visit();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.patient = patient;
			visit.tests = new PersistentSet();
			patient.visits.addItem(visit);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient = _ctx.patient;
			
			var test:VisitTest = new VisitTest();
			test.visit = visit;
			test.patient = patient;
			test.observations = new ArrayCollection();
			
			var vo1:VisitObservation = new VisitObservation();
			vo1.test = test;
			var vo2:VisitObservation = new VisitObservation();
			vo2.test = test;
			test.observations.addItem(vo1);
			test.observations.addItem(vo2);
			visit.tests.addItem(test);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet 1", 1, changeSet.changes.length);
			
			changeSet = _ctx.meta_mergeExternalData(changeSet) as ChangeSet;
			
			var v:VisitTest = changeSet.getChange(0).changes.tests.changes[0].value as VisitTest;
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.changes.length);
			Assert.assertStrictlyEquals("ChangeSet test", v, v.observations.getItemAt(0).test);
		}
		
		[Test]
		public function testChangeSetEntity6():void {
			var patient:Patient5 = new Patient5();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.consents = new PersistentSet();
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient = _ctx.patient as Patient5;
			
			var consent:Consent = new Consent();
			consent.patient = patient;
			patient.consents.addItem(consent);
			
			var documentList:DocumentList = new DocumentList();
			consent.documentList = documentList;
			var document:Document = new Document();
			document.documentLists.addItem(document);
			
			documentList.documents = new PersistentSet();
			document.payload = new DocumentPayload();
			documentList.documents.addItem(document);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertFalse("Different set", documentList.documents === changeSet.changes[0].changes.consents.changes[0].value.documentList.documents.object);
			
			Assert.assertEquals("ChangeSet 1", 1, changeSet.changes.length);
			
			Assert.assertTrue("Document list initialized", IPersistentCollection(documentList.documents).isInitialized());
		}
    }
}
