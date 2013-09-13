package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    import org.granite.meta;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Contact2;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Conflicts;
    import org.granite.tide.data.events.TideDataConflictsEvent;

	use namespace meta;	
    
    
    public class TestEntityCollectionRefs
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
			_ctx.meta_uninitializeAllowed = false;
        }
        
        
        [Test]
        public function testEntityCollectionRefs():void {
        	var p:Person = new Person();
        	p.id = 1;
        	p.uid = "P01";
        	p.version = 0;
        	p.contacts = new PersistentSet(true);
        	var c1:Contact = new Contact();
        	c1.id = 1;
        	c1.uid = "C01";
        	c1.version = 0;
        	c1.person = p;
        	p.contacts.addItem(c1);
        	_ctx.person = _ctx.meta_mergeExternalData(p);
        	p = Person(_ctx.person);

            var np:Person = new Person();
            np.id = 1;
            np.uid = "P01";
            np.version = 0;
            np.contacts = new PersistentSet(false);
            var nc:Contact = new Contact();
            nc.id = 1;
            nc.uid = "C01";
            nc.version = 0;
            nc.person = np;

            _ctx.meta_handleUpdates("SID", [[ 'REMOVE', nc ]]);

            Assert.assertEquals("Person contacts empty", 0, p.contacts.length);
        }
		
		[Test("GDS-1112")]
		public function testEntityCollectionRemove():void {
			var p:Person = new Person();
			p.id = 1;
			p.uid = "P01";
			p.version = 0;
			p.contacts = new PersistentSet(true);
			var c1:Contact = new Contact();
			c1.id = 1;
			c1.uid = "C01";
			c1.version = 0;
			c1.person = p;
			p.contacts.addItem(c1);
			_ctx.person = _ctx.meta_mergeExternalData(p);
			p = Person(_ctx.person);
			
			var np:Person = new Person();
			np.id = 1;
			np.uid = "P01";
			np.version = 0;
			np.contacts = new PersistentSet(false);
			var nc:Contact = new Contact();
			nc.id = 2;
			nc.uid = "C02";
			nc.version = 0;
			nc.person = np;
			
			_ctx.meta_handleUpdates("SID", [[ 'REMOVE', nc ]]);
			
			Assert.assertEquals("Person contacts length", 1, p.contacts.length);
		}


        [Test]
        public function testEntityCollectionMultiRefs():void {
        	var p1:Person = new Person();
        	p1.id = 1;
        	p1.uid = "P01";
        	p1.version = 0;
        	p1.contacts = new PersistentSet(true);
        	var c1:Contact2 = new Contact2();
        	c1.id = 1;
        	c1.uid = "C01";
        	c1.version = 0;
        	p1.contacts.addItem(c1);
        	_ctx.person1 = _ctx.meta_mergeExternalData(p1);
        	p1 = Person(_ctx.person1);

            var p2:Person = new Person();
            p2.id = 2;
            p2.uid = "P02";
            p2.version = 0;
            p2.contacts = new PersistentSet(true);
            var c1b:Contact2 = new Contact2();
            c1b.id = 1;
            c1b.uid = "C01";
            c1b.version = 0;
            p2.contacts.addItem(c1b);
            var c2b:Contact2 = new Contact2();
            c2b.id = 2;
            c2b.uid = "C02";
            c2b.version = 0;
            p2.contacts.addItem(c2b);
            _ctx.person2 = _ctx.meta_mergeExternalData(p2);
            p2 = Person(_ctx.person2);

            var nc:Contact2 = new Contact2();
            nc.id = 1;
            nc.uid = "C01";
            nc.version = 0;

            _ctx.meta_handleUpdates("SID", [[ 'REMOVE', nc ]]);

            Assert.assertEquals("Person 1 contacts empty", 0, p1.contacts.length);
            Assert.assertEquals("Person 2 one contact left", 1, p2.contacts.length);
        }


        private var _conflicts:Conflicts;

        [Test]
        public function testEntityCollectionRemoveConflictServer():void {
            var p:Person = new Person();
            p.id = 1;
            p.uid = "P01";
            p.version = 0;
            p.contacts = new PersistentSet(true);
            var c1:Contact = new Contact();
            c1.id = 1;
            c1.uid = "C01";
            c1.version = 0;
            c1.person = p;
            p.contacts.addItem(c1);
            _ctx.person = _ctx.meta_mergeExternalData(p);
            p = Person(_ctx.person);

            // Contact is locally modified
            c1.email = "toto@toto.org";

            var np:Person = new Person();
            np.id = 1;
            np.uid = "P01";
            np.version = 0;
            np.contacts = new PersistentSet(false);
            var nc:Contact = new Contact();
            nc.id = 1;
            nc.uid = "C01";
            nc.version = 0;
            nc.person = np;

            _ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, dataConflictsHandler, false, 0, true);

            // Receive an external removal event
            _ctx.meta_handleUpdates("SID", [[ 'REMOVE', nc ]]);

            Assert.assertEquals("Conflict detected", 1, _conflicts.conflicts.length);

            _conflicts.acceptAllServer();

            Assert.assertEquals("Person contacts empty", 0, p.contacts.length);
        }

        [Test]
        public function testEntityCollectionRemoveConflictClient():void {
            var p:Person = new Person();
            p.id = 1;
            p.uid = "P01";
            p.version = 0;
            p.contacts = new PersistentSet(true);
            var c1:Contact = new Contact();
            c1.id = 1;
            c1.uid = "C01";
            c1.version = 0;
            c1.person = p;
            p.contacts.addItem(c1);
            _ctx.person = _ctx.meta_mergeExternalData(p);
            p = Person(_ctx.person);

            // Contact is locally modified
            c1.email = "toto@toto.org";

            var np:Person = new Person();
            np.id = 1;
            np.uid = "P01";
            np.version = 0;
            np.contacts = new PersistentSet(false);
            var nc:Contact = new Contact();
            nc.id = 1;
            nc.uid = "C01";
            nc.version = 0;
            nc.person = np;

            _ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, dataConflictsHandler, false, 0, true);

            // Receive an external removal event
            _ctx.meta_handleUpdates("SID", [[ 'REMOVE', nc ]]);

            Assert.assertEquals("Conflict detected", 1, _conflicts.conflicts.length);

            _conflicts.acceptAllClient();

            Assert.assertEquals("Person contacts empty", 1, p.contacts.length);
        }

        private function dataConflictsHandler(event:TideDataConflictsEvent):void {
            _conflicts = event.conflicts;
        }
    }
}
