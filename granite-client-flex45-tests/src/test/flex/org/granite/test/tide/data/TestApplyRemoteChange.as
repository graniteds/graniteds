/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.data
{
    import mx.data.utils.Managed;
    import mx.utils.ObjectUtil;
    
    import org.flexunit.Assert;
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
    import org.granite.persistence.PersistentList;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.reflect.Type;
    import org.granite.test.tide.Classification;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IEntity;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Change;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    import org.granite.tide.data.Conflicts;
    import org.granite.tide.data.events.TideDataConflictsEvent;

    use namespace meta;


    public class TestApplyRemoteChange
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
			// _ctx.meta_uninitializeAllowed = false;
            Tide.getInstance().addComponents([ChangeMerger]);
        }
        

        [Test]
        public function testApplyRemoteChange1():void {
        	var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
            person.contacts = new PersistentSet(true);
            person.lastName = "Test";

        	_ctx.person = _ctx.meta_mergeExternalData(person);

        	person = _ctx.person;

            var alias:String = Type.forClass(Person).name;

            var change:Change = new Change(alias, "P1", 1, 1);
            change.changes.lastName = "Toto";

            _ctx.meta_handleUpdates(true, [[ 'UPDATE', new ChangeSet([ change ]) ]]);

			Assert.assertEquals("Last name changed", "Toto", person.lastName);
            Assert.assertEquals("Version increased", 1, person.version);

            var person0:Person = new Person();
            person0.meta::setInitialized(false);
            person0.id = 1;
            var contact:Contact = new Contact();
            contact.uid = "C1";
            contact.id = 1;
            contact.version = 0;
            contact.email = "toto@toto.net";
            contact.person = person0;

            change = new Change(alias, "P1", 1, 2);
            var ccs:CollectionChanges = new CollectionChanges();
            ccs.addChange(1, null, contact);
            change.changes.contacts = ccs;

            _ctx.meta_handleUpdates(true, [[ 'UPDATE', new ChangeSet([ change ]) ]]);

            Assert.assertEquals("Contact added", 1, person.contacts.length);
            Assert.assertTrue("Contact", person.contacts.getItemAt(0) is Contact);
			Assert.assertStrictlyEquals("Contact em", _ctx, person.contacts.getItemAt(0).meta::entityManager);
            Assert.assertEquals("Version increased", 2, person.version);

            var person1:Person = new Person();
            person1.id = 1;
            person1.uid = "P1";
            person1.version = 3;
            person1.contacts = new PersistentSet(false);
            var contact2:Contact = new Contact();
            contact2.uid = "C2";
            contact2.id = 2;
            contact2.version = 0;
            contact2.email = "tutu@tutu.net";
            contact2.person = person1;

            change = new Change(alias, "P1", 1, 3);
            ccs = new CollectionChanges();
            ccs.addChange(1, null, contact2);
            change.changes.contacts = ccs;

            _ctx.meta_handleUpdates(true, [[ 'UPDATE', new ChangeSet([ change ]) ]]);

            Assert.assertEquals("Contact added", 2, person.contacts.length);
            Assert.assertStrictlyEquals("Contact attached", person, person.contacts.getItemAt(1).person);
			Assert.assertStrictlyEquals("Person em", _ctx, person.meta::entityManager);
			Assert.assertStrictlyEquals("Contact em", _ctx, person.contacts.getItemAt(1).meta::entityManager);
		}
		
		
		[Test]
		public function testApplyRemoteChange2():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			person.lastName = "Test";
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.id = 1;
			contact1.person = person;
			contact1.email = "toto@toto.net";
			person.contacts.addItem(contact1);
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			_ctx.meta_clearCache();
			
			person = _ctx.person;
			
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.person = person;
			contact2.email = "toto2@toto.net";
			person.contacts.addItem(contact2);
			

			var personb:Person = new Person();
			personb.meta::setInitialized(false);
			personb.id = person.id;
			var contact2b:Contact = new Contact();
			contact2b.uid = "C2";
			contact2b.id = 2;
			contact2b.version = 0;
			contact2b.person = personb;
			contact2b.email = "toto2@toto.net";
			
			var change1:Change = new Change(Type.forClass(Person).name, person.uid, person.id, 0);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, null, contact2b);
			change1.changes.contacts = collChanges1;
			
			var personc:Person = new Person();
			personc.contacts = new PersistentSet();
			personc.uid = person.uid;
			personc.id = 1;
			personc.version = 0;
			var contact1c:Contact = new Contact();
			contact1c.uid = "C1";
			contact1c.id = 1;
			contact1c.version = 0;
			contact1c.person = personc;
			contact1c.email = "toto@toto.net";
			personc.contacts.addItem(contact1c);
			var contact2c:Contact = new Contact();
			contact2c.uid = "C2";
			contact2c.id = 2;
			contact2c.version = 0;
			contact2c.person = personc;
			contact2c.email = "toto2@toto.net";
			personc.contacts.addItem(contact2c);
			
			var change2:Change = new Change(Type.forClass(Person).name, person.uid, person.id, 0);
			var collChanges2:CollectionChanges = new CollectionChanges();
			collChanges2.addChange(1, null, contact2c);
			change2.changes.contacts = collChanges2;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'UPDATE', new ChangeSet([ change2 ]) ]]);
			
			Assert.assertStrictlyEquals("Person em", _ctx, person.meta::entityManager);
			Assert.assertEquals("Contact added", 2, person.contacts.length);
			Assert.assertTrue("Contact", person.contacts.getItemAt(0) is Contact);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			
			person.contacts.removeItemAt(1);
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChange3():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			person.lastName = "Test";
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.id = 1;
			contact1.person = person;
			contact1.email = "toto@toto.net";
			person.contacts.addItem(contact1);
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person = _ctx.person;
			
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.person = person;
			contact2.email = "toto2@toto.net";
			person.contacts.addItem(contact2);
			
			
			var personb:Person = new Person();
			personb.uid = person.uid;
			personb.version = person.version;
			personb.id = person.id;
			personb.contacts = new PersistentSet(false);
			var contact2b:Contact = new Contact();
			contact2b.uid = "C2";
			contact2b.id = 2;
			contact2b.version = 0;
			contact2b.person = personb;
			contact2b.email = "toto2@toto.net";
			
			var change:Change = new Change(Type.forClass(Person).name, person.uid, person.id, 0);
			var collChanges:CollectionChanges = new CollectionChanges();
			collChanges.addChange(1, null, contact2b);
			change.changes.contacts = collChanges;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
			
			Assert.assertStrictlyEquals("Person em", _ctx, person.meta::entityManager);
			Assert.assertEquals("Contact added", 2, person.contacts.length);
			Assert.assertTrue("Contact", person.contacts.getItemAt(0) is Contact);
			Assert.assertEquals("Contact id", contact2b.id, person.contacts.getItemAt(1).id);
		}
		
		[Test]
		public function testApplyRemoteChangeLazy():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(false);
			person.lastName = "Test";
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person = _ctx.person;
			
			
			var personb:Person = new Person();
			personb.uid = person.uid;
			personb.version = person.version;
			personb.id = person.id;
			personb.contacts = new PersistentSet(false);
			var contact2b:Contact = new Contact();
			contact2b.uid = "C2";
			contact2b.id = 2;
			contact2b.version = 0;
			contact2b.person = personb;
			contact2b.email = "toto2@toto.net";
			
			var change:Change = new Change(Type.forClass(Person).name, person.uid, person.id, 0);
			var collChanges:CollectionChanges = new CollectionChanges();
			collChanges.addChange(1, null, contact2b);
			change.changes.contacts = collChanges;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
			
			Assert.assertStrictlyEquals("Person em", _ctx, person.meta::entityManager);
			Assert.assertFalse("Contacts uninitialized", IPersistentCollection(person.contacts).isInitialized());
		}
		
		[Test]
		public function testApplyRemoteChangeSet():void {
			var patient:Patient3 = new Patient3();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.visits = new PersistentSet();
			patient.tests = new PersistentSet();
			var visit:Visit = new Visit();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			visit.patient = patient;
			visit.tests = new PersistentSet();
			patient.visits.addItem(visit);
			var test1:VisitTest = new VisitTest();
			test1.uid = "T1";
			test1.id = 1;
			test1.version = 0;
			test1.name = "Test";
			patient.tests.addItem(test1);
			visit.tests.addItem(test1);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			patient = _ctx.patient;
			
			var test2:VisitTest = new VisitTest();
			test2.name = "Test2";
			test2.uid = "T2";
			test2.patient = patient;
			test2.visit = visit;
			patient.tests.addItem(test2);
			visit.tests.addItem(test2);
			
			
			var patientb:Patient3 = new Patient3();
			patientb.meta::setInitialized(false);
			patientb.id = patient.id;
			var visitb:Visit = new Visit();
			visitb.meta::setInitialized(false);
			visitb.id = visit.id;
			var test2b:VisitTest = new VisitTest();
			test2b.uid = "T2";
			test2b.id = 2;
			test2b.version = 0;
			test2b.name = "Test2";
			test2b.patient = patientb;
			test2b.visit = visitb;
			
			var change1:Change = new Change(Type.forClass(Patient3).name, patient.uid, patient.id, 0);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, null, test2b);
			change1.changes.tests = collChanges1;
			
			var patientc:Patient3 = new Patient3();
			patientc.meta::setInitialized(false);
			patientc.id = patient.id;
			var visitc:Visit = new Visit();
			visitc.meta::setInitialized(false);
			visitc.id = visit.id;
			var test2c:VisitTest = new VisitTest();
			test2c.uid = "T2";
			test2c.id = 2;
			test2c.version = 0;
			test2c.name = "Test2";
			test2c.patient = patientc;
			test2c.visit = visitc;
			
			var change2:Change = new Change(Type.forClass(Visit).name, visit.uid, visit.id, 0);
			var collChanges2:CollectionChanges = new CollectionChanges();
			collChanges2.addChange(1, null, test2c);
			change2.changes.tests = collChanges2;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'UPDATE', new ChangeSet([ change2 ]) ]]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertEquals("Test added to patient", 2, patient.tests.length);
			Assert.assertEquals("Test added to patient (id)", 2, patient.tests.getItemAt(1).id);
			Assert.assertStrictlyEquals("Test added to patient (em)", _ctx, patient.tests.getItemAt(1).meta::entityManager);
			Assert.assertEquals("Test added to visit", 2, visit.tests.length);
			Assert.assertEquals("Test added to visit (id)", 2, visit.tests.getItemAt(1).id);
			Assert.assertStrictlyEquals("Test added to visit (em)", _ctx, visit.tests.getItemAt(1).meta::entityManager);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeSet2():void {
			var patient:Patient3 = new Patient3();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.visits = new PersistentSet();
			patient.tests = new PersistentSet();
			var visit:Visit = new Visit();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			visit.tests = new PersistentSet();
			patient.visits.addItem(visit);
			var test1:VisitTest = new VisitTest();
			test1.uid = "T1";
			test1.id = 1;
			test1.version = 0;
			test1.name = "Test";
			patient.tests.addItem(test1);
			visit.tests.addItem(test1);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			patient = _ctx.patient;
			
			var test2:VisitTest = new VisitTest();
			test2.name = "Test2";
			test2.uid = "T2";
			test2.patient = patient;
			test2.visit = visit;
			patient.tests.addItem(test2);
			visit.tests.addItem(test2);
			
			
			var patientc:Patient3 = new Patient3();
			patientc.uid = patient.uid;
			patientc.id = patient.id;
			patientc.version = patient.version;
            patientc.name = patient.name;
			patientc.visits = new PersistentSet(false);
			patientc.tests = new PersistentSet(false);
			var visitc:Visit = new Visit();
			visitc.uid = visit.uid;
			visitc.id = visit.id;
			visitc.version = visit.version;
            visitc.name = visit.name;
			visitc.patient = patientc;
			visitc.tests = new PersistentSet(false);
			var test2c:VisitTest = new VisitTest();
			test2c.uid = "T2";
			test2c.id = 2;
			test2c.version = 0;
			test2c.name = "Test2";
			test2c.patient = patientc;
			test2c.visit = visitc;
			
			var change1:Change = new Change(Type.forClass(Patient3).name, patient.uid, patient.id, 0);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, null, test2c);
			change1.changes.tests = collChanges1;
			var change2:Change = new Change(Type.forClass(Visit).name, visit.uid, visit.id, 0);
			var collChanges2:CollectionChanges = new CollectionChanges();
			collChanges2.addChange(1, null, test2c);
			change2.changes.tests = collChanges2;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'UPDATE', new ChangeSet([ change2 ]) ]]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertEquals("Test added to patient", 2, patient.tests.length);
			Assert.assertEquals("Test added to patient (id)", 2, patient.tests.getItemAt(1).id);
			Assert.assertEquals("Test added to visit", 2, visit.tests.length);
			Assert.assertEquals("Test added to visit (id)", 2, visit.tests.getItemAt(1).id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeSet3():void {
			var patient:Patient3 = new Patient3();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.visits = new PersistentSet();
			var visit:Visit = new Visit();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			patient.visits.addItem(visit);
			var visit2:Visit = new Visit();
			visit2.uid = "V2";
			visit2.id = 2;
			visit2.version = 0;
			visit2.name = "Test2";
			patient.visits.addItem(visit2);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			patient = _ctx.patient;
			
			var patientc:Patient3 = new Patient3();
			patientc.uid = patient.uid;
			patientc.id = patient.id;
			patientc.version = patient.version;
			patientc.name = "Test"
			patientc.visits = new PersistentSet(false);
			var visitc:Visit = new Visit();
			visitc.uid = "V3";
			visitc.id = 3;
			visitc.version = 0;
			visitc.patient = patientc;
			var visitd:Visit = new Visit();
			visitd.uid = "V4";
			visitd.id = 4;
			visitd.version = 0;
			visitd.patient = patientc;
			
			var change:Change = new Change(Type.forClass(Patient3).name, patientc.uid, patientc.id, patientc.version);
			var collChanges:CollectionChanges = new CollectionChanges();
			collChanges.addChange(1, null, visitc);
			collChanges.addChange(1, null, visitd);
			collChanges.addChange(-1, null, new ChangeRef(Type.forClass(Visit).name, visit.uid, visit.id));
			collChanges.addChange(-1, null, new ChangeRef(Type.forClass(Visit).name, visit2.uid, visit2.id));
			change.changes.visits = collChanges;
			
			_ctx.meta_handleUpdates(false, [
				[ 'PERSIST', visitc ],
				[ 'PERSIST', visitd ],
				[ 'UPDATE', new ChangeSet([ change ]) ], 
				[ 'REMOVE', new ChangeRef(Type.forClass(Visit).name, visit.uid, visit.id) ], 
				[ 'REMOVE', new ChangeRef(Type.forClass(Visit).name, visit2.uid, visit2.id) ]
			]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertEquals("Patient visits", 2, patient.visits.length);
			Assert.assertEquals("Patient visit id", 3, patient.visits.getItemAt(0).id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeList():void {
			var patient:Patient3 = new Patient3();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.visits = new PersistentList();
			patient.tests = new PersistentList();
			var visit:Visit = new Visit();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			visit.tests = new PersistentList();
			patient.visits.addItem(visit);
			var test1:VisitTest = new VisitTest();
			test1.uid = "T1";
			test1.id = 1;
			test1.version = 0;
			test1.name = "Test";
			patient.tests.addItem(test1);
			visit.tests.addItem(test1);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			patient = _ctx.patient;
			
			var test2:VisitTest = new VisitTest();
			test2.name = "Test2";
			test2.uid = "T2";
			test2.patient = patient;
			test2.visit = visit;
			patient.tests.addItem(test2);
			// Don't add the item to the tests list of Visit so we check that both cases work
			// (with and without local change)
			// visit.tests.addItem(test2);
			
			
			var patientb:Patient3 = new Patient3();
			patientb.meta::setInitialized(false);
			patientb.id = patient.id;
			var visitb:Visit = new Visit();
			visitb.meta::setInitialized(false);
			visitb.id = visit.id;
			var test2b:VisitTest = new VisitTest();
			test2b.uid = "T2";
			test2b.id = 2;
			test2b.version = 0;
			test2b.name = "Test2";
			test2b.patient = patientb;
			test2b.visit = visitb;
			
			var change1:Change = new Change(Type.forClass(Patient3).name, patient.uid, patient.id, 0);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, 1, test2b);
			change1.changes.tests = collChanges1;
			
			var patientc:Patient3 = new Patient3();
			patientc.meta::setInitialized(false);
			patientc.id = patient.id;
			var visitc:Visit = new Visit();
			visitc.meta::setInitialized(false);
			visitc.id = visit.id;
			var test2c:VisitTest = new VisitTest();
			test2c.uid = "T2";
			test2c.id = 2;
			test2c.version = 0;
			test2c.name = "Test2";
			test2c.patient = patientc;
			test2c.visit = visitc;
			
			var change2:Change = new Change(Type.forClass(Visit).name, visit.uid, visit.id, 0);
			var collChanges2:CollectionChanges = new CollectionChanges();
			collChanges2.addChange(1, 1, test2c);
			change2.changes.tests = collChanges2;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'UPDATE', new ChangeSet([ change2 ]) ]]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertEquals("Test added to patient", 2, patient.tests.length);
			Assert.assertEquals("Test added to patient (id)", 2, patient.tests.getItemAt(1).id);
			Assert.assertEquals("Test added to visit", 2, visit.tests.length);
			Assert.assertEquals("Test added to visit (id)", 2, visit.tests.getItemAt(1).id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeList2():void {
			var patient:Patient3 = new Patient3();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.visits = new PersistentList();
			patient.tests = new PersistentList();
			var visit:Visit = new Visit();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			visit.tests = new PersistentList();
			patient.visits.addItem(visit);
			var test1:VisitTest = new VisitTest();
			test1.uid = "T1";
			test1.id = 1;
			test1.version = 0;
			test1.name = "Test";
			patient.tests.addItem(test1);
			visit.tests.addItem(test1);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			patient = _ctx.patient;
			
			var test2:VisitTest = new VisitTest();
			test2.name = "Test2";
			test2.uid = "T2";
			test2.patient = patient;
			test2.visit = visit;
			patient.tests.addItem(test2);
			// Don't add the item to the tests list of Visit so we check that both cases work
			// (with and without local change)
			// visit.tests.addItem(test2);
			
			var test3:VisitTest = new VisitTest();
			test3.name = "Test3";
			test3.uid = "T3";
			test3.patient = patient;
			test3.visit = visit;
			patient.tests.addItemAt(test3, 1);
			
			
			var patientb:Patient3 = new Patient3();
			patientb.meta::setInitialized(false);
			patientb.id = patient.id;
			var visitb:Visit = new Visit();
			visitb.meta::setInitialized(false);
			visitb.id = visit.id;
			var test2b:VisitTest = new VisitTest();
			test2b.uid = "T2";
			test2b.id = 2;
			test2b.version = 0;
			test2b.name = "Test2";
			test2b.patient = patientb;
			test2b.visit = visitb;
			
			var change1:Change = new Change(Type.forClass(Patient3).name, patient.uid, patient.id, 0);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, 1, test2b);
			change1.changes.tests = collChanges1;
			
			var patientc:Patient3 = new Patient3();
			patientc.meta::setInitialized(false);
			patientc.id = patient.id;
			var visitc:Visit = new Visit();
			visitc.meta::setInitialized(false);
			visitc.id = visit.id;
			var test2c:VisitTest = new VisitTest();
			test2c.uid = "T2";
			test2c.id = 2;
			test2c.version = 0;
			test2c.name = "Test2";
			test2c.patient = patientc;
			test2c.visit = visitc;
			
			var change2:Change = new Change(Type.forClass(Visit).name, visit.uid, visit.id, 0);
			var collChanges2:CollectionChanges = new CollectionChanges();
			collChanges2.addChange(1, 1, test2c);
			change2.changes.tests = collChanges2;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'UPDATE', new ChangeSet([ change2 ]) ]]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertEquals("Test added to patient", 3, patient.tests.length);
			Assert.assertEquals("Test added to patient (id)", 2, patient.tests.getItemAt(2).id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeList3():void {
			var patient:Patient3 = new Patient3();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.visits = new PersistentList();
			patient.tests = new PersistentList();
			var visit:Visit = new Visit();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			visit.tests = new PersistentList();
			patient.visits.addItem(visit);
			var test1:VisitTest = new VisitTest();
			test1.uid = "T1";
			test1.id = 1;
			test1.version = 0;
			test1.name = "Test";
			patient.tests.addItem(test1);
			visit.tests.addItem(test1);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			patient = _ctx.patient;
			
			var test2:VisitTest = new VisitTest();
			test2.name = "Test2";
			test2.uid = "T2";
			test2.patient = patient;
			test2.visit = visit;
			patient.tests.addItem(test2);
			// Don't add the item to the tests list of Visit so we check that both cases work
			// (with and without local change)
			// visit.tests.addItem(test2);
			
			var test3:VisitTest = new VisitTest();
			test3.name = "Test3";
			test3.uid = "T3";
			test3.patient = patient;
			test3.visit = visit;
			patient.tests.addItemAt(test3, 1);
			
			
			var patientb:Patient3 = new Patient3();
			patientb.meta::setInitialized(false);
			patientb.id = patient.id;
			var visitb:Visit = new Visit();
			visitb.meta::setInitialized(false);
			visitb.id = visit.id;
			var test2b:VisitTest = new VisitTest();
			test2b.uid = "T2";
			test2b.id = 2;
			test2b.version = 0;
			test2b.name = "Test2";
			test2b.patient = patientb;
			test2b.visit = visitb;
			
			var change1:Change = new Change(Type.forClass(Patient3).name, patient.uid, patient.id, 0);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, 1, test2b);
			change1.changes.tests = collChanges1;
			
			var patientc:Patient3 = new Patient3();
			patientc.meta::setInitialized(false);
			patientc.id = patient.id;
			var visitc:Visit = new Visit();
			visitc.meta::setInitialized(false);
			visitc.id = visit.id;
			var test2c:VisitTest = new VisitTest();
			test2c.uid = "T2";
			test2c.id = 2;
			test2c.version = 0;
			test2c.name = "Test2";
			test2c.patient = patientc;
			test2c.visit = visitc;
			
			var change2:Change = new Change(Type.forClass(Visit).name, visit.uid, visit.id, 0);
			var collChanges2:CollectionChanges = new CollectionChanges();
			collChanges2.addChange(1, 1, test2c);
			change2.changes.tests = collChanges2;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'UPDATE', new ChangeSet([ change2 ]) ]]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertEquals("Test added to patient", 3, patient.tests.length);
			Assert.assertEquals("Test added to patient (id)", 2, patient.tests.getItemAt(2).id);
			Assert.assertStrictlyEquals("Test em", _ctx, patient.tests.getItemAt(2).meta::entityManager);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeList4():void {
			var patient:Patient4 = new Patient4();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.diagnosis = new PersistentSet();
			var status:PatientStatus = new PatientStatus();
			status.uid = "S1";
			status.id = 1;
			status.version = 0;
			status.name = "Test";
			status.patient = patient;
			status.deathCauses = new PersistentList();			
			patient.status = status;
			var diagnosis:Diagnosis = new Diagnosis();
			diagnosis.uid = "D1";
			diagnosis.id = 1;
			diagnosis.version = 0;
			diagnosis.name = "Test";
			diagnosis.patient = patient;
			patient.diagnosis.addItem(diagnosis);
			patient.status.deathCauses.addItem(diagnosis);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			
			patient = _ctx.patient;
			
			var diagnosis2:Diagnosis = new Diagnosis();
			diagnosis2.uid = "D2";
			diagnosis2.patient = patient;
			patient.status.deathCauses.addItem(diagnosis2);

			diagnosis2.name = "Test2";

			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			_ctx.meta_mergeExternal(changeSet);
			_ctx.meta_clearCache();

			
			var patientb:Patient4 = new Patient4();
			patientb.id = patient.id;
			patientb.uid = patient.uid;
			patientb.version = patient.version;
			patientb.name = "Test";
			patientb.diagnosis = new PersistentSet(false);
			var statusb:PatientStatus = new PatientStatus();
			statusb.id = status.id;
			statusb.uid = status.uid;
			statusb.version = status.version+1;
			statusb.deathCauses = new PersistentSet(false);
			patientb.status = statusb;
			
			var diagnosis2b:Diagnosis = new Diagnosis();
			diagnosis2b.uid = "D2";
			diagnosis2b.id = 2;
			diagnosis2b.version = 0;
			diagnosis2b.name = "Test2";
			diagnosis2b.patient = patientb;

			
			var change1:Change = new Change(Type.forClass(Patient4).name, patient.uid, patient.id, patientb.version);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, null, diagnosis2b);
			change1.changes.diagnosis = collChanges1;

			var change3:Change = new Change(Type.forClass(PatientStatus).name, status.uid, status.id, statusb.version+1);
			var collChanges3:CollectionChanges = new CollectionChanges();
			collChanges3.addChange(1, 1, diagnosis2b);
			change3.changes.deathCauses = collChanges3;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'PERSIST', diagnosis2b ] , [ 'UPDATE', new ChangeSet([ change3 ]) ]]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertStrictlyEquals("Diagnosis patient", patient, patient.diagnosis.getItemAt(1).patient);
			Assert.assertStrictlyEquals("Diagnosis patient", patient, status.deathCauses.getItemAt(1).patient);
			Assert.assertStrictlyEquals("Diagnosis em", _ctx, patient.diagnosis.getItemAt(1).meta::entityManager);
			Assert.assertStrictlyEquals("Diagnosis em", _ctx, status.deathCauses.getItemAt(1).meta::entityManager);
			Assert.assertEquals("Diagnosis added to patient", 2, patient.diagnosis.length);
			Assert.assertEquals("Diagnosis added to patient (id)", 2, patient.diagnosis.getItemAt(1).id);
			Assert.assertEquals("Diagnosis added to status", 2, status.deathCauses.length);
			Assert.assertEquals("Diagnosis added to status (id)", 2, status.deathCauses.getItemAt(1).id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeList5():void {
			var patient:Patient4 = new Patient4();
			patient.uid = "P1";
			patient.id = 1;
			patient.version = 0;
			patient.name = "Test";
			patient.diagnosis = new PersistentSet();
			patient.visits = new PersistentSet();
			var status:PatientStatus = new PatientStatus();
			status.uid = "S1";
			status.id = 1;
			status.version = 0;
			status.name = "Test";
			status.patient = patient;
			status.deathCauses = new PersistentList();			
			patient.status = status;
			var visit:Visit2 = new Visit2();
			visit.uid = "V1";
			visit.id = 1;
			visit.version = 0;
			visit.name = "Test";
			visit.patient = patient;
			visit.assessments = new PersistentSet();
			patient.visits.addItem(visit);
			var diagnosis:Diagnosis = new Diagnosis();
			diagnosis.uid = "D1";
			diagnosis.id = 1;
			diagnosis.version = 0;
			diagnosis.name = "Test";
			diagnosis.patient = patient;
			diagnosis.visit = visit;
			patient.diagnosis.addItem(diagnosis);
			patient.status.deathCauses.addItem(diagnosis);
			visit.assessments.addItem(diagnosis);
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			_ctx.meta_clearCache();
			
			patient = _ctx.patient;
			
			var diagnosis2:Diagnosis = new Diagnosis();
			diagnosis2.uid = "D2";
			diagnosis2.patient = patient;
			patient.status.deathCauses.addItem(diagnosis2);
			
			diagnosis2.name = "Test2";
			
			
			// Simulate remote call with local merge of arguments
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			_ctx.meta_mergeExternal(changeSet);
			_ctx.meta_clearCache();
			
			
			var patientb:Patient4 = new Patient4();
			patientb.id = patient.id;
			patientb.uid = patient.uid;
			patientb.version = patient.version;
			patientb.name = "Test";
			patientb.diagnosis = new PersistentSet(false);
			patientb.visits = new PersistentSet(false);
			var statusb:PatientStatus = new PatientStatus();
			statusb.id = status.id;
			statusb.uid = status.uid;
			statusb.version = status.version+1;
			statusb.deathCauses = new PersistentSet(false);
			patientb.status = statusb;
			var visitb:Visit2 = new Visit2();
			visitb.id = visit.id;
			visitb.uid = visit.uid;
			visitb.version = visit.version;
			visitb.assessments = new PersistentSet(false);
			visitb.patient = patientb;
			
			var diagnosis2b:Diagnosis = new Diagnosis();
			diagnosis2b.uid = "D2";
			diagnosis2b.id = 2;
			diagnosis2b.version = 0;
			diagnosis2b.name = "Test2";
			diagnosis2b.patient = patientb;
			diagnosis2b.visit = visitb;
			
			
			var change1:Change = new Change(Type.forClass(Patient4).name, patient.uid, patient.id, patientb.version);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, null, diagnosis2b);
			change1.changes.diagnosis = collChanges1;
			
			var change3:Change = new Change(Type.forClass(PatientStatus).name, status.uid, status.id, statusb.version+1);
			var collChanges3:CollectionChanges = new CollectionChanges();
			collChanges3.addChange(1, 1, diagnosis2b);
			change3.changes.deathCauses = collChanges3;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'PERSIST', diagnosis2b ] , [ 'UPDATE', new ChangeSet([ change3 ]) ]]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
			Assert.assertStrictlyEquals("Diagnosis patient", patient, patient.diagnosis.getItemAt(1).patient);
			Assert.assertStrictlyEquals("Diagnosis patient", patient, status.deathCauses.getItemAt(1).patient);
			Assert.assertStrictlyEquals("Diagnosis em", _ctx, patient.diagnosis.getItemAt(1).meta::entityManager);
			Assert.assertStrictlyEquals("Diagnosis em", _ctx, status.deathCauses.getItemAt(1).meta::entityManager);
			Assert.assertEquals("Diagnosis added to patient", 2, patient.diagnosis.length);
			Assert.assertEquals("Diagnosis added to patient (id)", 2, patient.diagnosis.getItemAt(1).id);
			Assert.assertEquals("Diagnosis added to status", 2, status.deathCauses.length);
			Assert.assertEquals("Diagnosis added to status (id)", 2, status.deathCauses.getItemAt(1).id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeList6():void {
			var list:Classification = new Classification();
			list.uid = "P1";
			list.id = 1;
			list.version = 0;
			list.subclasses = new PersistentList();
			var c1:Classification = new Classification(1, 0, "C1");
			var c2:Classification = new Classification(2, 0, "C2");
			var c3:Classification = new Classification(3, 0, "C3");
			var c4:Classification = new Classification(4, 0, "C4");
			list.subclasses.addItem(c1);
			list.subclasses.addItem(c2);
			list.subclasses.addItem(c3);
			list.subclasses.addItem(c4);
			c1.superclasses = new PersistentSet();
			c1.superclasses.addItem(list);
			c2.superclasses = new PersistentSet();
			c2.superclasses.addItem(list);
			c3.superclasses = new PersistentSet();
			c3.superclasses.addItem(list);
			c4.superclasses = new PersistentSet();
			c4.superclasses.addItem(list);
			
			_ctx.list = _ctx.meta_mergeExternalData(list);
			_ctx.meta_clearCache();
			
			list = Classification(_ctx.list);
			
			var c:Object = list.subclasses.removeItemAt(3);
			list.subclasses.addItemAt(c, 2);

			// Case where the server does not build the changes in the same order than the client
			// Ex. Client: A B C => A C B :  remove C (2) => add C (1)
			//     Server: A B C => A C B :  add C (1) => remove C (3)
			
			var c4b:Classification = new Classification(4, 0, "C4");
			c4b.subclasses = new PersistentList(false);
			c4b.superclasses = new PersistentSet(false);
			
			var change:Change = new Change(Type.forClass(Classification).name, list.uid, list.id, list.version+1);
			var collChanges:CollectionChanges = new CollectionChanges();
			collChanges.addChange(1, 2, c4b);
			collChanges.addChange(-1, 4, c4b);
			change.changes.subclasses = collChanges;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
			
			Assert.assertEquals("List size", 4, list.subclasses.length);
			Assert.assertEquals("List elt 3", "C4", list.subclasses.getItemAt(2).uid);
			Assert.assertEquals("List elt 4", "C3", list.subclasses.getItemAt(3).uid);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeList6b():void {
			var list:Classification = new Classification();
			list.uid = "P1";
			list.id = 1;
			list.version = 0;
			list.subclasses = new PersistentList();
			var c1:Classification = new Classification(1, 0, "C1");
			var c2:Classification = new Classification(2, 0, "C2");
			var c3:Classification = new Classification(3, 0, "C3");
			var c4:Classification = new Classification(4, 0, "C4");
			list.subclasses.addItem(c1);
			list.subclasses.addItem(c2);
			list.subclasses.addItem(c3);
			list.subclasses.addItem(c4);
			c1.superclasses = new PersistentSet();
			c1.superclasses.addItem(list);
			c2.superclasses = new PersistentSet();
			c2.superclasses.addItem(list);
			c3.superclasses = new PersistentSet();
			c3.superclasses.addItem(list);
			c4.superclasses = new PersistentSet();
			c4.superclasses.addItem(list);
			
			_ctx.list = _ctx.meta_mergeExternalData(list);
			_ctx.meta_clearCache();
			
			list = Classification(_ctx.list);
			
			var c:Object = list.subclasses.removeItemAt(3);
			list.subclasses.addItemAt(c, 2);
			
			// Case where the server does not build the same permutation as the client
			// Ex. Client: A B C => A C B :  remove C (2) => add C (1)
			//     Server: A B C => A C B :  remove B (1) => add B (2)
			
			var c3b:Classification = new Classification(3, 0, "C3");
			c3b.subclasses = new PersistentList(false);
			c3b.superclasses = new PersistentSet(false);
			
			var change:Change = new Change(Type.forClass(Classification).name, list.uid, list.id, list.version+1);
			var collChanges:CollectionChanges = new CollectionChanges();
			collChanges.addChange(-1, 2, c3b);
			collChanges.addChange(1, 3, c3b);
			change.changes.subclasses = collChanges;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
			
			Assert.assertEquals("List size", 4, list.subclasses.length);
			Assert.assertEquals("List elt 3", "C4", list.subclasses.getItemAt(2).uid);
			Assert.assertEquals("List elt 4", "C3", list.subclasses.getItemAt(3).uid);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeList6c():void {
			var list:Classification = new Classification();
			list.uid = "P1";
			list.id = 1;
			list.version = 0;
			list.subclasses = new PersistentList();
			var c1:Classification = new Classification(1, 0, "C1");
			var c2:Classification = new Classification(2, 0, "C2");
			var c3:Classification = new Classification(3, 0, "C3");
			var c4:Classification = new Classification(4, 0, "C4");
			list.subclasses.addItem(c1);
			list.subclasses.addItem(c2);
			list.subclasses.addItem(c3);
			list.subclasses.addItem(c4);
			c1.superclasses = new PersistentSet();
			c1.superclasses.addItem(list);
			c2.superclasses = new PersistentSet();
			c2.superclasses.addItem(list);
			c3.superclasses = new PersistentSet();
			c3.superclasses.addItem(list);
			c4.superclasses = new PersistentSet();
			c4.superclasses.addItem(list);
			
			_ctx.list = _ctx.meta_mergeExternalData(list);
			_ctx.meta_clearCache();
			
			list = Classification(_ctx.list);
			
			var c:Object = list.subclasses.removeItemAt(0);
			list.subclasses.addItemAt(c, 1);
			c = list.subclasses.removeItemAt(2);
			list.subclasses.addItemAt(c, 1);
			
			// Case where the server does not build the same permutation as the client
			// Ex. Client: A B C D => B A C D => B C A D :  remove A (0) => add A (1) => remove C (2) => add C (1)
			//     Server: A B C D => B C A D :  remove A (0) => add A (2)
			
			var c3b:Classification = new Classification(3, 0, "C3");
			c3b.subclasses = new PersistentList(false);
			c3b.superclasses = new PersistentSet(false);
			
			var change:Change = new Change(Type.forClass(Classification).name, list.uid, list.id, list.version+1);
			var collChanges:CollectionChanges = new CollectionChanges();
			collChanges.addChange(-1, 0, new ChangeRef(Type.forClass(Classification).name, "C1", 1));
			collChanges.addChange(1, 2, new ChangeRef(Type.forClass(Classification).name, "C1", 2));
			change.changes.subclasses = collChanges;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
			
			Assert.assertEquals("List size", 4, list.subclasses.length);
			Assert.assertEquals("List elt 3", "C1", list.subclasses.getItemAt(2).uid);
			Assert.assertEquals("List elt 4", "C4", list.subclasses.getItemAt(3).uid);
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}
		
		[Test]
		public function testApplyRemoteChangeMap():void {
			var person:Person11 = new Person11();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.lastName = "Test";
			person.map = new PersistentMap();
			var key:Key = new Key();
			key.uid = "K1";
			key.id = 1;
			key.version = 0;
			key.name = "Key";
			var val:Value = new Value();
			val.uid = "V1";
			val.id = 1;
			val.version = 0;
			val.name = "Value";
			person.map.put(key, val);
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			
			person = _ctx.person;
			
			var key2:Key = new Key();
			key2.uid = "K2";
			key2.name = "Key2";
			var val2:Value = new Value();
			val2.uid = "V2";
			val2.name = "Value2";
			person.map.put(key2, val2);
			
			
			var keyb:Key = new Key();
			keyb.uid = "K2";
			keyb.id = 2;
			keyb.version = 0;
			keyb.name = "Key2";
			var valb:Value = new Value();
			valb.uid = "V2";
			valb.id = 2;
			valb.version = 0;
			valb.name = "Value2";
			
			var change1:Change = new Change(Type.forClass(Person11).name, person.uid, person.id, 0);
			var collChanges1:CollectionChanges = new CollectionChanges();
			collChanges1.addChange(1, keyb, valb);
			change1.changes.map = collChanges1;
			
			_ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ]]);
			
			Assert.assertStrictlyEquals("Patient em", _ctx, person.meta::entityManager);
			Assert.assertEquals("Entry added to person.map", 2, person.map.length);
			Assert.assertEquals("Entry key updated", 2, key2.id);
			Assert.assertEquals("Entry value updated", 2, val2.id);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
		}


        private var _conflicts:Conflicts;

        [Test]
        public function testApplyRemoteChangeConflict():void {
            var person:Person = new Person();
            person.uid = "P1";
            person.id = 1;
            person.version = 0;
            person.contacts = new PersistentSet(true);
            person.lastName = "Test";

            _ctx.person = _ctx.meta_mergeExternalData(person);
            person = _ctx.person;

            person.lastName = "Tutu";

            var alias:String = Type.forClass(Person).name;

            var change:Change = new Change(alias, "P1", 1, 1);
            change.changes.lastName = "Toto";

            _ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, dataConflictHandler, false, 0, true);
            _ctx.meta_handleUpdates(true, [[ 'UPDATE', new ChangeSet([ change ]) ]]);

            Assert.assertEquals("Last name not changed", "Tutu", person.lastName);
            Assert.assertEquals("Conflict detected", 1, _conflicts.conflicts.length);

            _conflicts.acceptAllServer();

            Assert.assertEquals("Last name changed", "Toto", person.lastName);
        }

        [Test]
        public function testApplyRemoteChangeConflict2():void {
            var person:Person = new Person();
            person.uid = "P1";
            person.id = 1;
            person.version = 0;
            person.contacts = new PersistentSet(true);
            person.lastName = "Test";

            _ctx.person = _ctx.meta_mergeExternalData(person);
            person = _ctx.person;

            person.lastName = "Tutu";

            var alias:String = Type.forClass(Person).name;

            var change:Change = new Change(alias, "P1", 1, 1);
            change.changes.lastName = "Toto";

            _ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, dataConflictHandler, false, 0, true);
            _ctx.meta_handleUpdates(true, [[ 'UPDATE', new ChangeSet([ change ]) ]]);

            Assert.assertEquals("Last name not changed", "Tutu", person.lastName);
            Assert.assertEquals("Conflict detected", 1, _conflicts.conflicts.length);

            _conflicts.acceptAllClient();

            Assert.assertEquals("Last name changed", "Tutu", person.lastName);
            Assert.assertEquals("Version increased", 1, person.version);
        }

        private function dataConflictHandler(event:TideDataConflictsEvent):void {
            _conflicts = event.conflicts;
        }
		
		[Test]
		public function testApplyChangeRef():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			person.lastName = "Test";
			var contact:Contact = new Contact();
			contact.uid = "C1";
			contact.id = 1;
			contact.version = 0;
			contact.person = person;
			contact.email = "test@test.com";
			person.contacts.addItem(contact);
			
			_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			person.contacts.removeItemAt(0);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			// Check serialization of change set
			changeSet = ObjectUtil.copy(changeSet) as ChangeSet;
			
			Managed.resetEntity(IEntity(person));
			
			Assert.assertEquals("Person has 1 contact", 1, person.contacts.length);
			
			// Check that applied ChangeSet removes the contact 
			
			_ctx.meta_mergeExternalData(changeSet);
			
			Assert.assertEquals("Person contact removed", 0, person.contacts.length);
		}
		
		[Test]
		public function testApplyChangeBoolean():void {
			_ctx.meta_uninitializeAllowed = false;
			
			var patient:Patient4b = new Patient4b();
			patient.id = 1;
			patient.version = 0;
			patient.uid = "P1";
			patient.diagnosisAssessed = false;
			patient.diagnosis = new PersistentSet(true);
			patient.name = "Bla";
			
			_ctx.patient = _ctx.meta_mergeExternalData(patient);
			patient = _ctx.patient;
			
			var diagnosis:Diagnosisb = new Diagnosisb();
			diagnosis.uid = "D1";
			diagnosis.patient = patient;
			patient.diagnosis.addItem(diagnosis);
			patient.diagnosisAssessed = true;
			
			diagnosis.name = "bla";
			
			Assert.assertTrue("Context dirty", _ctx.meta_dirty);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			// Simulate pre-remote call merge
			changeSet = ChangeSet(_ctx.meta_mergeExternal(changeSet));
			
			Assert.assertStrictlyEquals(patient, diagnosis.patient);
			
			// Simulate serialization
			changeSet = ChangeSet(ObjectUtil.copy(changeSet));
			
			var c:Change = changeSet.changes[0];
			var change:Change = new Change(c.className, c.uid, c.id, 1, false);
			for (var p:String in c.changes)
				change.changes[p] = c.changes[p];
			var diag:Diagnosisb = c.changes.diagnosis.changes[0].value;
			diag.id = 1;
			diag.version = 0;
			diag.patient.version = 1;
			var updates:Array = [ [ 'PERSIST', diag ], [ 'UPDATE', change ] ];
			_ctx.meta_handleUpdates(false, updates);
			
			Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
			Assert.assertEquals("Patient version", 1, patient.version);
			Assert.assertEquals("Diagnosis count", 1, patient.diagnosis.length);
		}
    }
}
