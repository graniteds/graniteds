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
package org.granite.client.test.javafx.tide;


import static org.granite.client.tide.data.EntityManager.UpdateKind.PERSIST;
import static org.granite.client.tide.data.EntityManager.UpdateKind.REMOVE;
import static org.granite.client.tide.data.EntityManager.UpdateKind.UPDATE;
import static org.granite.client.tide.data.PersistenceManager.getEntityManager;

import java.util.Arrays;
import java.util.Collections;

import org.granite.client.javafx.tide.JavaFXApplication;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.test.tide.MockChannelFactory;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.ChangeMerger;
import org.granite.client.tide.data.ChangeSetBuilder;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.ServerSession;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestApplyRemoteChange {

    private SimpleContextManager contextManager;
    private Context ctx;
    @SuppressWarnings("unused")
    private ServerSession serverSession;
    private DataManager dataManager;
    private EntityManager entityManager;

    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXApplication());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext("");
        serverSession = new ServerSession();
        serverSession.setChannelFactoryClass(MockChannelFactory.class);
        serverSession.setRemoteAliasPackages(Collections.singleton(Person.class.getPackage().getName()));
        ctx.set(serverSession);
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
        ctx.set(new ChangeMerger());
        serverSession.start();
    }

    private static String personAlias = Person.class.getAnnotation(RemoteAlias.class).value();
    private static String patientAlias = Patient3.class.getAnnotation(RemoteAlias.class).value();
    private static String visitAlias = Visit.class.getAnnotation(RemoteAlias.class).value();


    @Test
    public void testApplyRemoteChange1() {
        Person person = new Person(1L, 0L, "P1", null, "Test");

        person = (Person)entityManager.mergeExternalData(person);
        entityManager.clearCache();

        Change change = new Change(personAlias, 1L, 1L, "P1");
        change.getChanges().put("lastName", "Toto");

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Collections.singletonList(UPDATE.of(new ChangeSet(change))));

        Assert.assertEquals("Last name changed", "Toto", person.getLastName());
        Assert.assertEquals("Version increased", Long.valueOf(1), person.getVersion());

        Person person0 = new Person(1L, false, "__ds__");
        Contact contact = new Contact(1L, 0L, "C1", person0, "toto@toto.net");

        change = new Change(personAlias, 1L, 2L, "P1");
        change.addCollectionChanges("contacts", new CollectionChange(1, null, contact));

        entityManager.handleUpdates(mergeContext, null, Collections.singletonList(UPDATE.of(new ChangeSet(change))));

        Assert.assertEquals("Contact added", 1, person.getContacts().size());
        Assert.assertTrue("Contact", person.getContacts().get(0) instanceof Contact);
        Assert.assertSame("Contact em", entityManager, getEntityManager(person.getContact(0)));
        Assert.assertEquals("Version increased", Long.valueOf(2), person.getVersion());

        Person person1 = new Person(1L, 3L, "P1", null, null);
        ((PersistentCollection)person1.getContacts()).uninitialize();
        Contact contact2 = new Contact(2L, 0L, "C2", person1, "tutu@tutu.net");

        change = new Change(personAlias, 1L, 3L, "P1");
        change.addCollectionChanges("contacts", new CollectionChange(1, null, contact2));

        entityManager.handleUpdates(mergeContext, null, Collections.singletonList(UPDATE.of(new ChangeSet(change))));

        Assert.assertEquals("Contact added", 2, person.getContacts().size());
        Assert.assertSame("Contact attached", person, person.getContacts().get(1).getPerson());
        Assert.assertSame("Person em", entityManager, getEntityManager(person));
        Assert.assertSame("Contact em", entityManager, getEntityManager(person.getContacts().get(1)));
    }


    @Test
    public void testApplyRemoteChange2() {
        Person person = new Person(1L, 0L, "P1", null, "Test");
        Contact contact1 = new Contact(1L, 0L, "C1", person, "toto@toto.net");
        person.getContacts().add(contact1);

        person = (Person)entityManager.mergeExternalData(person);
        entityManager.clearCache();

        Contact contact2 = new Contact(null, null, "C2", person, "toto2@toto.net");
        person.getContacts().add(contact2);

        Person personb = new Person(person.getId(), false, "__ds__");
        Contact contact2b = new Contact(2L, 0L, "C2", personb, "toto2@toto.net");

        Change change1 = new Change(personAlias, person.getId(), 0L, person.getUid());
        change1.addCollectionChanges("contacts", new CollectionChange(1, null, contact2b));

        Person personc = new Person(person.getId(), 0L, person.getUid(), null, "Test");
        Contact contact1c = new Contact(1L, 0L, "C1", personc, "toto@toto.net");
        personc.getContacts().add(contact1c);
        Contact contact2c = new Contact(2L, 0L, "C2", personc, "toto2@toto.net");
        personc.getContacts().add(contact2c);

        Change change2 = new Change(personAlias, person.getId(), 0L, person.getUid());
        change2.addCollectionChanges("contacts", new CollectionChange(1, null, contact2c));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(UPDATE.of(new ChangeSet(change1)), UPDATE.of(new ChangeSet(change2))));

        Assert.assertSame("Person em", entityManager, getEntityManager(person));
        Assert.assertEquals("Contact added", 2, person.getContacts().size());
        Assert.assertTrue("Contact", person.getContacts().get(0) instanceof Contact);

        Assert.assertFalse("Context not dirty", dataManager.isDirty());

        person.getContacts().remove(1);

        Assert.assertTrue("Context dirty", dataManager.isDirty());
    }

    @Test
    public void testApplyRemoteChange3() {
        Person person = new Person(1L, 0L, "P1", null, "Test");
        Contact contact1 = new Contact(1L, 0L, "C1", person, "toto@toto.net");
        person.addContact(contact1);

        person = (Person)entityManager.mergeExternalData(person);
        entityManager.clearCache();

        Contact contact2 = new Contact(null, null, "C2", person, "toto2@toto.net");
        person.addContact(contact2);


        Person personb = new Person(person.getId(), person.getVersion(), person.getUid(), null, null);
        ((PersistentCollection)personb.getContacts()).uninitialize();
        Contact contact2b = new Contact(2L, 0L, "C2", personb, "toto2@toto.net");

        Change change = new Change(personAlias, person.getId(), 0L, person.getUid());
        change.addCollectionChanges("contacts", new CollectionChange(1, null, contact2b));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Collections.singletonList(UPDATE.of(new ChangeSet(change))));

        Assert.assertSame("Person em", entityManager, getEntityManager(person));
        Assert.assertEquals("Contact added", 2, person.getContacts().size());
        Assert.assertTrue("Contact", person.getContacts().get(0) instanceof Contact);
        Assert.assertEquals("Contact id", contact2b.getId(), person.getContacts().get(1).getId());
    }

    @Test
    public void testApplyRemoteChangeLazy() {
        Person person = new Person(1L, 0L, "P1", null, "Test");
        ((PersistentCollection)person.getContacts()).uninitialize();

        person = (Person)entityManager.mergeExternalData(person);
        entityManager.clearCache();


        Person personb = new Person(person.getId(), person.getVersion(), person.getUid(), null, "Test");
        ((PersistentCollection)personb.getContacts()).uninitialize();
        Contact contact2b = new Contact(2L, 0L, "C2", personb, "toto2@toto.net");

        Change change = new Change(personAlias, person.getId(), 0L, person.getUid());
        change.addCollectionChanges("contacts", new CollectionChange(1, null, contact2b));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Collections.singletonList(UPDATE.of(new ChangeSet(change))));

        Assert.assertSame("Person em", entityManager, getEntityManager(person));
        Assert.assertFalse("Contacts uninitialized", ((PersistentCollection)person.getContacts()).wasInitialized());
    }

    @Test
    public void testApplyRemoteChangeSet() {
        Patient3 patient = new Patient3(1L, 0L, "P1", "Test");
        Visit visit = new Visit(1L, 0L, "V1", patient, "Test");
        patient.getVisits().add(visit);
        VisitTest test1 = new VisitTest(1L, 0L, "T1", patient, visit, "Test");
        patient.getTests().add(test1);
        visit.getTests().add(test1);

        patient = (Patient3)entityManager.mergeExternalData(patient);
        entityManager.clearCache();

        VisitTest test2 = new VisitTest(null, null, "T2", patient, visit, "Test2");
        patient.getTests().add(test2);
        visit.getTests().add(test2);

        Patient3 patientb = new Patient3(patient.getId(), false, "__ds__");
        Visit visitb = new Visit(visit.getId(), false, "__ds__");
        VisitTest test2b = new VisitTest(2L, 0L, "T2", patientb, visitb, "Test2");

        Change change1 = new Change(patientAlias, patient.getId(), 0L, patient.getUid());
        change1.addCollectionChanges("tests", new CollectionChange(1, null, test2b));

        Patient3 patientc = new Patient3(patient.getId(), false, "__ds__");
        Visit visitc = new Visit(visit.getId(), false, "__ds__");
        VisitTest test2c = new VisitTest(2L, 0L, "T2", patientc, visitc, "Test2");

        Change change2 = new Change(visitAlias, visit.getId(), 0L, visit.getUid());
        change2.addCollectionChanges("tests", new CollectionChange(1, null, test2c));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(UPDATE.of(new ChangeSet(change1)), UPDATE.of(new ChangeSet(change2))));

        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));
        Assert.assertEquals("Test added to patient", 2, patient.getTests().size());
        Assert.assertEquals("Test added to patient (id)", Long.valueOf(2), patient.getTests().get(1).getId());
        Assert.assertSame("Test added to patient (em)", entityManager, getEntityManager(patient.getTests().get(1)));
        Assert.assertEquals("Test added to visit", 2, visit.getTests().size());
        Assert.assertEquals("Test added to visit (id)", Long.valueOf(2), visit.getTests().get(1).getId());
        Assert.assertSame("Test added to visit (em)", entityManager, getEntityManager(visit.getTests().get(1)));

        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

    @Test
    public void testApplyRemoteChangeSet2() {
        Patient3 patient = new Patient3(1L, 0L, "P1", "Test");
        Visit visit = new Visit(1L, 0L, "V1", patient, "Test");
        patient.getVisits().add(visit);
        VisitTest test1 = new VisitTest(1L, 0L, "T1", patient, visit, "Test");
        patient.getTests().add(test1);
        visit.getTests().add(test1);

        patient = (Patient3)entityManager.mergeExternalData(patient);
        entityManager.clearCache();

        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));

        VisitTest test2 = new VisitTest(null, null, "T2", patient, visit, "Test2");
        patient.getTests().add(test2);
        visit.getTests().add(test2);


        Patient3 patientc = new Patient3(patient.getId(), patient.getVersion(), patient.getUid(), "Test");
        ((PersistentCollection)patientc.getVisits()).uninitialize();
        ((PersistentCollection)patientc.getTests()).uninitialize();
        Visit visitc = new Visit(visit.getId(), visit.getVersion(), visit.getUid(), patientc, "Test");
        ((PersistentCollection)visitc.getTests()).uninitialize();
        VisitTest test2c = new VisitTest(2L, 0L, "T2", patientc, visitc, "Test2");

        Change change1 = new Change(patientAlias, patient.getId(), 0L, patient.getUid());
        change1.addCollectionChanges("tests", new CollectionChange(1, null, test2c));
        Change change2 = new Change(visitAlias, visit.getId(), 0L, visit.getUid());
        change2.addCollectionChanges("tests", new CollectionChange(1, null, test2c));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(UPDATE.of(new ChangeSet(change1)), UPDATE.of(new ChangeSet(change2))));
        
        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));
        Assert.assertEquals("Test added to patient", 2, patient.getTests().size());
        Assert.assertEquals("Test added to patient (id)", Long.valueOf(2), patient.getTests().get(1).getId());
        Assert.assertEquals("Test added to visit", 2, visit.getTests().size());
        Assert.assertEquals("Test added to visit (id)", Long.valueOf(2), visit.getTests().get(1).getId());

        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

    @Test
    public void testApplyRemoteChangeSet3() {
    	Patient3 patient = new Patient3(1L, 0L, "P1", "Test");
    	Visit visit = new Visit(1L, 0L, "V1", patient, "Test");
    	patient.getVisits().add(visit);
    	Visit visit2 = new Visit(2L, 0L, "V2", patient, "Test2");
    	patient.getVisits().add(visit2);
    	
        patient = (Patient3)entityManager.mergeExternalData(patient);
        entityManager.clearCache();
        
        Patient3 patientc = new Patient3(patient.getId(), patient.getVersion(), patient.getUid(), "Test");
        ((PersistentCollection)patientc.getVisits()).uninitialize();
        Visit visitc = new Visit(3L, 0L, "V3", patientc, "Test3");
        Visit visitd = new Visit(4L, 0L, "V4", patientc, "Test4");
        
        Change change = new Change(patientAlias, patientc.getId(), patientc.getVersion(), patientc.getUid());
        change.addCollectionChanges("visits", 
        		new CollectionChange(1, null, visitc),
        		new CollectionChange(1, null, visitd),
        		new CollectionChange(-1, null, new ChangeRef(visitAlias, visit.getUid(), visit.getId())),
        		new CollectionChange(-1, null, new ChangeRef(visitAlias, visit2.getUid(), visit2.getId()))
        );

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(
        		PERSIST.of(visitc), PERSIST.of(visitd),
        		UPDATE.of(new ChangeSet(change)), 
        		REMOVE.of(new ChangeRef(visitAlias, visit.getUid(), visit.getId())),
        		REMOVE.of(new ChangeRef(visitAlias, visit2.getUid(), visit2.getId()))
        ));
        
        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));
        Assert.assertEquals("Patient visits", 2, patient.getVisits().size());
        Assert.assertEquals("Patient visit id", Long.valueOf(3), patient.getVisits().get(0).getId());
        
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

    @Test
    public void testApplyRemoteChangeList() {
    	Patient3 patient = new Patient3(1L, 0L, "P1", "Test");
    	Visit visit = new Visit(1L, 0L, "V1", patient, "Test");
    	patient.getVisits().add(visit);
    	VisitTest test1 = new VisitTest(1L, 0L, "T1", patient, visit, "Test");
    	patient.getTests().add(test1);
    	visit.getTests().add(test1);
    	
        patient = (Patient3)entityManager.mergeExternalData(patient);
        entityManager.clearCache();
        
        VisitTest test2 = new VisitTest(null, null, "T2", patient, visit, "Test2");
        patient.getTests().add(test2);
        // Don't add the item to the tests list of Visit so we check that both cases work
        // (with and without local change)
        // visit.getTests().add(test2);

        Patient3 patientb = new Patient3(patient.getId(), false, "__ds__");
        Visit visitb = new Visit(visit.getId(), false, "__ds__");
        VisitTest test2b = new VisitTest(2L, 0L, "T2", patientb, visitb, "Test2");
        
        Change change1 = new Change(patientAlias, patient.getId(), 0L, patient.getUid());
        change1.addCollectionChanges("tests", new CollectionChange(1, 1, test2b));

        Patient3 patientc = new Patient3(patient.getId(), false, "__ds__");
        Visit visitc = new Visit(visit.getId(), false, "__ds__");
        VisitTest test2c = new VisitTest(2L, 0L, "T2", patientc, visitc, "Test2");
        
        Change change2 = new Change(visitAlias, visit.getId(), 0L, visit.getUid());
        change2.addCollectionChanges("tests", new CollectionChange(1, 1, test2c));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(
        		UPDATE.of(new ChangeSet(change1)), UPDATE.of(new ChangeSet(change2))
        ));
        
        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));
        Assert.assertEquals("Test added to patient", 2, patient.getTests().size());
        Assert.assertEquals("Test added to patient (id)", Long.valueOf(2), patient.getTests().get(1).getId());
        Assert.assertEquals("Test added to visit", 2, visit.getTests().size());
        Assert.assertEquals("Test added to visit (id)", Long.valueOf(2), visit.getTests().get(1).getId());

        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

    @Test
    public void testApplyRemoteChangeList2() {
        Patient3 patient = new Patient3(1L, 0L, "P1", "Test");
        Visit visit = new Visit(1L, 0L, "V1", patient, "Test");
        patient.getVisits().add(visit);
        VisitTest test1 = new VisitTest(1L, 0L, "T1", patient, visit, "Test");
        patient.getTests().add(test1);
        visit.getTests().add(test1);
    	
        patient = (Patient3)entityManager.mergeExternalData(patient);
        entityManager.clearCache();

        VisitTest test2 = new VisitTest(null, null, "T2", patient, visit, "Test2");
        patient.getTests().add(test2);
        // Don't add the item to the tests list of Visit so we check that both cases work
        // (with and without local change)
        // visit.getTests().add(test2);

        VisitTest test3 = new VisitTest(null, null, "T3", patient, visit, "Test3");
        patient.getTests().add(1, test3);

        Patient3 patientb = new Patient3(patient.getId(), false, "__ds__");
        Visit visitb = new Visit(visit.getId(), false, "__ds__");
        VisitTest test2b = new VisitTest(2L, 0L, "T2", patientb, visitb, "Test2");

        Change change1 = new Change(patientAlias, patient.getId(), 0L, patient.getUid());
        change1.addCollectionChanges("tests", new CollectionChange(1, 1, test2b));

        Patient3 patientc = new Patient3(patient.getId(), false, "__ds__");
        Visit visitc = new Visit(visit.getId(), false, "__ds__");
        VisitTest test2c = new VisitTest(2L, 0L, "T2", patientc, visitc, "Test2");

        Change change2 = new Change(visitAlias, visit.getId(), 0L, visit.getUid());
        change2.addCollectionChanges("tests", new CollectionChange(1, 1, test2c));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(
        		UPDATE.of(new ChangeSet(change1)), UPDATE.of(new ChangeSet(change2))
        ));
        
        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));
        Assert.assertEquals("Test added to patient", 3, patient.getTests().size());
        Assert.assertEquals("Test added to patient (id)", Long.valueOf(2), patient.getTests().get(2).getId());

        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

    @Test
    public void testApplyRemoteChangeList3() {
        Patient3 patient = new Patient3(1L, 0L, "P1", "Test");
        Visit visit = new Visit(1L, 0L, "V1", patient, "Test");
        patient.getVisits().add(visit);
        VisitTest test1 = new VisitTest(1L, 0L, "T1", patient, visit, "Test");
        patient.getTests().add(test1);
        visit.getTests().add(test1);
    	
        patient = (Patient3)entityManager.mergeExternalData(patient);
        entityManager.clearCache();

        VisitTest test2 = new VisitTest(null, null, "T2", patient, visit, "Test2");
        patient.getTests().add(test2);
        // Don't add the item to the tests list of Visit so we check that both cases work
        // (with and without local change)
        // visit.getTests().add(test2);

        VisitTest test3 = new VisitTest(null, null, "T3", patient, visit, "Test3");
        patient.getTests().add(1, test3);
        
        
        Patient3 patientb = new Patient3(patient.getId(), false, "__ds__");
        Visit visitb = new Visit(visit.getId(), false, "__ds__");
        VisitTest test2b = new VisitTest(2L, 0L, "T2", patientb, visitb, "Test2");
        
        Change change1 = new Change(patientAlias, patient.getId(), 0L, patient.getUid());
        change1.addCollectionChanges("tests", new CollectionChange(1, 1, test2b));

        Patient3 patientc = new Patient3(patient.getId(), false, "__ds__");
        Visit visitc = new Visit(visit.getId(), false, "__ds__");
        VisitTest test2c = new VisitTest(2L, 0L, "T2", patientc, visitc, "Test2");

        Change change2 = new Change(visitAlias, visit.getId(), 0L, visit.getUid());
        change2.addCollectionChanges("tests", new CollectionChange(1, 1, test2c));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(
        		UPDATE.of(new ChangeSet(change1)), UPDATE.of(new ChangeSet(change2))
        ));

        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));
        Assert.assertEquals("Test added to patient", 3, patient.getTests().size());
        Assert.assertEquals("Test added to patient (id)", Long.valueOf(2), patient.getTests().get(2).getId());
        Assert.assertSame("Test em", entityManager, getEntityManager(patient.getTests().get(2)));

        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

   	@Test
    public void testApplyRemoteChangeList4() {
   		Patient4 patient = new Patient4(1L, 0L, "P1", "Test");
   		PatientStatus status = new PatientStatus(1L, 0L, "S1", patient, "Test");
   		patient.setStatus(status);
   		Diagnosis diagnosis = new Diagnosis(1L, 0L, "D1", patient, null, "Test");
   		patient.getDiagnosis().add(diagnosis);
   		patient.getStatus().getDeathCauses().add(diagnosis);

        patient = (Patient4)entityManager.mergeExternalData(patient);
        entityManager.clearCache();
        
   		Diagnosis diagnosis2 = new Diagnosis(null, null, "D2", patient, null, "Test2");
        patient.getStatus().getDeathCauses().add(diagnosis2);
        
        // Simulate remote call with local merge of arguments
        ChangeSet changeSet = new ChangeSetBuilder(ctx).buildChangeSet();
        entityManager.mergeExternalData(changeSet);
        entityManager.clearCache();

        
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

//    [Test]
//    public function testApplyRemoteChangeList5():void {
//        var patient:Patient4 = new Patient4();
//        patient.uid = "P1";
//        patient.id = 1;
//        patient.version = 0;
//        patient.name = "Test";
//        patient.diagnosis = new PersistentSet();
//        patient.visits = new PersistentSet();
//        var status:PatientStatus = new PatientStatus();
//        status.uid = "S1";
//        status.id = 1;
//        status.version = 0;
//        status.name = "Test";
//        status.patient = patient;
//        status.deathCauses = new PersistentList();
//        patient.status = status;
//        var visit:Visit2 = new Visit2();
//        visit.uid = "V1";
//        visit.id = 1;
//        visit.version = 0;
//        visit.name = "Test";
//        visit.patient = patient;
//        visit.assessments = new PersistentSet();
//        patient.visits.addItem(visit);
//        var diagnosis:Diagnosis = new Diagnosis();
//        diagnosis.uid = "D1";
//        diagnosis.id = 1;
//        diagnosis.version = 0;
//        diagnosis.name = "Test";
//        diagnosis.patient = patient;
//        diagnosis.visit = visit;
//        patient.diagnosis.addItem(diagnosis);
//        patient.status.deathCauses.addItem(diagnosis);
//        visit.assessments.addItem(diagnosis);
//
//        _ctx.patient = _ctx.meta_mergeExternalData(patient);
//        _ctx.meta_clearCache();
//
//        patient = _ctx.patient;
//
//        var diagnosis2:Diagnosis = new Diagnosis();
//        diagnosis2.uid = "D2";
//        diagnosis2.patient = patient;
//        patient.status.deathCauses.addItem(diagnosis2);
//
//        diagnosis2.name = "Test2";
//
//
//        // Simulate remote call with local merge of arguments
//        var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
//        _ctx.meta_mergeExternal(changeSet);
//        _ctx.meta_clearCache();
//
//
//        var patientb:Patient4 = new Patient4();
//        patientb.id = patient.id;
//        patientb.uid = patient.uid;
//        patientb.version = patient.version;
//        patientb.name = "Test";
//        patientb.diagnosis = new PersistentSet(false);
//        patientb.visits = new PersistentSet(false);
//        var statusb:PatientStatus = new PatientStatus();
//        statusb.id = status.id;
//        statusb.uid = status.uid;
//        statusb.version = status.version+1;
//        statusb.deathCauses = new PersistentSet(false);
//        patientb.status = statusb;
//        var visitb:Visit2 = new Visit2();
//        visitb.id = visit.id;
//        visitb.uid = visit.uid;
//        visitb.version = visit.version;
//        visitb.assessments = new PersistentSet(false);
//        visitb.patient = patientb;
//
//        var diagnosis2b:Diagnosis = new Diagnosis();
//        diagnosis2b.uid = "D2";
//        diagnosis2b.id = 2;
//        diagnosis2b.version = 0;
//        diagnosis2b.name = "Test2";
//        diagnosis2b.patient = patientb;
//        diagnosis2b.visit = visitb;
//
//
//        var change1:Change = new Change(Type.forClass(Patient4).name, patient.uid, patient.id, patientb.version);
//        var collChanges1:CollectionChanges = new CollectionChanges();
//        collChanges1.addChange(1, null, diagnosis2b);
//        change1.changes.diagnosis = collChanges1;
//
//        var change3:Change = new Change(Type.forClass(PatientStatus).name, status.uid, status.id, statusb.version+1);
//        var collChanges3:CollectionChanges = new CollectionChanges();
//        collChanges3.addChange(1, 1, diagnosis2b);
//        change3.changes.deathCauses = collChanges3;
//
//        _ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ], [ 'PERSIST', diagnosis2b ] , [ 'UPDATE', new ChangeSet([ change3 ]) ]]);
//
//        Assert.assertStrictlyEquals("Patient em", _ctx, patient.meta::entityManager);
//        Assert.assertStrictlyEquals("Diagnosis patient", patient, patient.diagnosis.getItemAt(1).patient);
//        Assert.assertStrictlyEquals("Diagnosis patient", patient, status.deathCauses.getItemAt(1).patient);
//        Assert.assertStrictlyEquals("Diagnosis em", _ctx, patient.diagnosis.getItemAt(1).meta::entityManager);
//        Assert.assertStrictlyEquals("Diagnosis em", _ctx, status.deathCauses.getItemAt(1).meta::entityManager);
//        Assert.assertEquals("Diagnosis added to patient", 2, patient.diagnosis.length);
//        Assert.assertEquals("Diagnosis added to patient (id)", 2, patient.diagnosis.getItemAt(1).id);
//        Assert.assertEquals("Diagnosis added to status", 2, status.deathCauses.length);
//        Assert.assertEquals("Diagnosis added to status (id)", 2, status.deathCauses.getItemAt(1).id);
//
//        Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
//    }
//
//    [Test]
//    public function testApplyRemoteChangeList6():void {
//        var list:Classification = new Classification();
//        list.uid = "P1";
//        list.id = 1;
//        list.version = 0;
//        list.subclasses = new PersistentList();
//        var c1:Classification = new Classification(1, 0, "C1");
//        var c2:Classification = new Classification(2, 0, "C2");
//        var c3:Classification = new Classification(3, 0, "C3");
//        var c4:Classification = new Classification(4, 0, "C4");
//        list.subclasses.addItem(c1);
//        list.subclasses.addItem(c2);
//        list.subclasses.addItem(c3);
//        list.subclasses.addItem(c4);
//        c1.superclasses = new PersistentSet();
//        c1.superclasses.addItem(list);
//        c2.superclasses = new PersistentSet();
//        c2.superclasses.addItem(list);
//        c3.superclasses = new PersistentSet();
//        c3.superclasses.addItem(list);
//        c4.superclasses = new PersistentSet();
//        c4.superclasses.addItem(list);
//
//        _ctx.list = _ctx.meta_mergeExternalData(list);
//        _ctx.meta_clearCache();
//
//        list = Classification(_ctx.list);
//
//        var c:Object = list.subclasses.removeItemAt(3);
//        list.subclasses.addItemAt(c, 2);
//
//        // Case where the server does not build the changes in the same order than the client
//        // Ex. Client: A B C => A C B :  remove C (2) => add C (1)
//        //     Server: A B C => A C B :  add C (1) => remove C (3)
//
//        var c4b:Classification = new Classification(4, 0, "C4");
//        c4b.subclasses = new PersistentList(false);
//        c4b.superclasses = new PersistentSet(false);
//
//        var change:Change = new Change(Type.forClass(Classification).name, list.uid, list.id, list.version+1);
//        var collChanges:CollectionChanges = new CollectionChanges();
//        collChanges.addChange(1, 2, c4b);
//        collChanges.addChange(-1, 4, c4b);
//        change.changes.subclasses = collChanges;
//
//        _ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
//
//        Assert.assertEquals("List size", 4, list.subclasses.length);
//        Assert.assertEquals("List elt 3", "C4", list.subclasses.getItemAt(2).uid);
//        Assert.assertEquals("List elt 4", "C3", list.subclasses.getItemAt(3).uid);
//        Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
//    }
//
//    [Test]
//    public function testApplyRemoteChangeList6b():void {
//        var list:Classification = new Classification();
//        list.uid = "P1";
//        list.id = 1;
//        list.version = 0;
//        list.subclasses = new PersistentList();
//        var c1:Classification = new Classification(1, 0, "C1");
//        var c2:Classification = new Classification(2, 0, "C2");
//        var c3:Classification = new Classification(3, 0, "C3");
//        var c4:Classification = new Classification(4, 0, "C4");
//        list.subclasses.addItem(c1);
//        list.subclasses.addItem(c2);
//        list.subclasses.addItem(c3);
//        list.subclasses.addItem(c4);
//        c1.superclasses = new PersistentSet();
//        c1.superclasses.addItem(list);
//        c2.superclasses = new PersistentSet();
//        c2.superclasses.addItem(list);
//        c3.superclasses = new PersistentSet();
//        c3.superclasses.addItem(list);
//        c4.superclasses = new PersistentSet();
//        c4.superclasses.addItem(list);
//
//        _ctx.list = _ctx.meta_mergeExternalData(list);
//        _ctx.meta_clearCache();
//
//        list = Classification(_ctx.list);
//
//        var c:Object = list.subclasses.removeItemAt(3);
//        list.subclasses.addItemAt(c, 2);
//
//        // Case where the server does not build the same permutation as the client
//        // Ex. Client: A B C => A C B :  remove C (2) => add C (1)
//        //     Server: A B C => A C B :  remove B (1) => add B (2)
//
//        var c3b:Classification = new Classification(3, 0, "C3");
//        c3b.subclasses = new PersistentList(false);
//        c3b.superclasses = new PersistentSet(false);
//
//        var change:Change = new Change(Type.forClass(Classification).name, list.uid, list.id, list.version+1);
//        var collChanges:CollectionChanges = new CollectionChanges();
//        collChanges.addChange(-1, 2, c3b);
//        collChanges.addChange(1, 3, c3b);
//        change.changes.subclasses = collChanges;
//
//        _ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
//
//        Assert.assertEquals("List size", 4, list.subclasses.length);
//        Assert.assertEquals("List elt 3", "C4", list.subclasses.getItemAt(2).uid);
//        Assert.assertEquals("List elt 4", "C3", list.subclasses.getItemAt(3).uid);
//        Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
//    }
//
//    [Test]
//    public function testApplyRemoteChangeList6c():void {
//        var list:Classification = new Classification();
//        list.uid = "P1";
//        list.id = 1;
//        list.version = 0;
//        list.subclasses = new PersistentList();
//        var c1:Classification = new Classification(1, 0, "C1");
//        var c2:Classification = new Classification(2, 0, "C2");
//        var c3:Classification = new Classification(3, 0, "C3");
//        var c4:Classification = new Classification(4, 0, "C4");
//        list.subclasses.addItem(c1);
//        list.subclasses.addItem(c2);
//        list.subclasses.addItem(c3);
//        list.subclasses.addItem(c4);
//        c1.superclasses = new PersistentSet();
//        c1.superclasses.addItem(list);
//        c2.superclasses = new PersistentSet();
//        c2.superclasses.addItem(list);
//        c3.superclasses = new PersistentSet();
//        c3.superclasses.addItem(list);
//        c4.superclasses = new PersistentSet();
//        c4.superclasses.addItem(list);
//
//        _ctx.list = _ctx.meta_mergeExternalData(list);
//        _ctx.meta_clearCache();
//
//        list = Classification(_ctx.list);
//
//        var c:Object = list.subclasses.removeItemAt(0);
//        list.subclasses.addItemAt(c, 1);
//        c = list.subclasses.removeItemAt(2);
//        list.subclasses.addItemAt(c, 1);
//
//        // Case where the server does not build the same permutation as the client
//        // Ex. Client: A B C D => B A C D => B C A D :  remove A (0) => add A (1) => remove C (2) => add C (1)
//        //     Server: A B C D => B C A D :  remove A (0) => add A (2)
//
//        var c3b:Classification = new Classification(3, 0, "C3");
//        c3b.subclasses = new PersistentList(false);
//        c3b.superclasses = new PersistentSet(false);
//
//        var change:Change = new Change(Type.forClass(Classification).name, list.uid, list.id, list.version+1);
//        var collChanges:CollectionChanges = new CollectionChanges();
//        collChanges.addChange(-1, 0, new ChangeRef(Type.forClass(Classification).name, "C1", 1));
//        collChanges.addChange(1, 2, new ChangeRef(Type.forClass(Classification).name, "C1", 2));
//        change.changes.subclasses = collChanges;
//
//        _ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
//
//        Assert.assertEquals("List size", 4, list.subclasses.length);
//        Assert.assertEquals("List elt 3", "C1", list.subclasses.getItemAt(2).uid);
//        Assert.assertEquals("List elt 4", "C4", list.subclasses.getItemAt(3).uid);
//        Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
//    }
//
//    [Test]
//    public function testApplyRemoteChangeMap():void {
//        var person:Person11 = new Person11();
//        person.uid = "P1";
//        person.id = 1;
//        person.version = 0;
//        person.lastName = "Test";
//        person.map = new PersistentMap();
//        var key:Key = new Key();
//        key.uid = "K1";
//        key.id = 1;
//        key.version = 0;
//        key.name = "Key";
//        var val:Value = new Value();
//        val.uid = "V1";
//        val.id = 1;
//        val.version = 0;
//        val.name = "Value";
//        person.map.put(key, val);
//
//        _ctx.person = _ctx.meta_mergeExternalData(person);
//
//        person = _ctx.person;
//
//        var key2:Key = new Key();
//        key2.uid = "K2";
//        key2.name = "Key2";
//        var val2:Value = new Value();
//        val2.uid = "V2";
//        val2.name = "Value2";
//        person.map.put(key2, val2);
//
//
//        var keyb:Key = new Key();
//        keyb.uid = "K2";
//        keyb.id = 2;
//        keyb.version = 0;
//        keyb.name = "Key2";
//        var valb:Value = new Value();
//        valb.uid = "V2";
//        valb.id = 2;
//        valb.version = 0;
//        valb.name = "Value2";
//
//        var change1:Change = new Change(Type.forClass(Person11).name, person.uid, person.id, 0);
//        var collChanges1:CollectionChanges = new CollectionChanges();
//        collChanges1.addChange(1, keyb, valb);
//        change1.changes.map = collChanges1;
//
//        _ctx.meta_handleUpdates(false, [[ 'UPDATE', new ChangeSet([ change1 ]) ]]);
//
//        Assert.assertStrictlyEquals("Patient em", _ctx, person.meta::entityManager);
//        Assert.assertEquals("Entry added to person.map", 2, person.map.length);
//        Assert.assertEquals("Entry key updated", 2, key2.id);
//        Assert.assertEquals("Entry value updated", 2, val2.id);
//
//        Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
//    }
//
//
//    private var _conflicts:Conflicts;
//
//    [Test]
//    public function testApplyRemoteChangeConflict():void {
//        var person:Person = new Person();
//        person.uid = "P1";
//        person.id = 1;
//        person.version = 0;
//        person.contacts = new PersistentSet(true);
//        person.lastName = "Test";
//
//        _ctx.person = _ctx.meta_mergeExternalData(person);
//        person = _ctx.person;
//
//        person.lastName = "Tutu";
//
//        var personAlias:String = Type.forClass(Person).name;
//
//        var change:Change = new Change(personAlias, "P1", 1, 1);
//        change.changes.lastName = "Toto";
//
//        _ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, dataConflictHandler, false, 0, true);
//        _ctx.meta_handleUpdates(true, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
//
//        Assert.assertEquals("Last name not changed", "Tutu", person.lastName);
//        Assert.assertEquals("Conflict detected", 1, _conflicts.conflicts.length);
//
//        _conflicts.acceptAllServer();
//
//        Assert.assertEquals("Last name changed", "Toto", person.lastName);
//    }
//
//    [Test]
//    public function testApplyRemoteChangeConflict2():void {
//        var person:Person = new Person();
//        person.uid = "P1";
//        person.id = 1;
//        person.version = 0;
//        person.contacts = new PersistentSet(true);
//        person.lastName = "Test";
//
//        _ctx.person = _ctx.meta_mergeExternalData(person);
//        person = _ctx.person;
//
//        person.lastName = "Tutu";
//
//        var personAlias:String = Type.forClass(Person).name;
//
//        var change:Change = new Change(personAlias, "P1", 1, 1);
//        change.changes.lastName = "Toto";
//
//        _ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, dataConflictHandler, false, 0, true);
//        _ctx.meta_handleUpdates(true, [[ 'UPDATE', new ChangeSet([ change ]) ]]);
//
//        Assert.assertEquals("Last name not changed", "Tutu", person.lastName);
//        Assert.assertEquals("Conflict detected", 1, _conflicts.conflicts.length);
//
//        _conflicts.acceptAllClient();
//
//        Assert.assertEquals("Last name changed", "Tutu", person.lastName);
//        Assert.assertEquals("Version increased", 1, person.version);
//    }
//
//    private function dataConflictHandler(event:TideDataConflictsEvent):void {
//        _conflicts = event.conflicts;
//    }
//
//    [Test]
//    public function testApplyChangeRef():void {
//        var person:Person = new Person();
//        person.uid = "P1";
//        person.id = 1;
//        person.version = 0;
//        person.contacts = new PersistentSet(true);
//        person.lastName = "Test";
//        var contact:Contact = new Contact();
//        contact.uid = "C1";
//        contact.id = 1;
//        contact.version = 0;
//        contact.person = person;
//        contact.email = "test@test.com";
//        person.contacts.addItem(contact);
//
//        _ctx.person = _ctx.meta_mergeExternalData(person);
//        person = _ctx.person;
//
//        person.contacts.removeItemAt(0);
//
//        var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
//
//        // Check serialization of change set
//        changeSet = ObjectUtil.copy(changeSet) as ChangeSet;
//
//        Managed.resetEntity(person);
//
//        Assert.assertEquals("Person has 1 contact", 1, person.contacts.length);
//
//        // Check that applied ChangeSet removes the contact
//
//        _ctx.meta_mergeExternalData(changeSet);
//
//        Assert.assertEquals("Person contact removed", 0, person.contacts.length);
//    }
//
//    [Test]
//    public function testApplyChangeBoolean():void {
//        _ctx.meta_uninitializeAllowed = false;
//
//        var patient:Patient4b = new Patient4b();
//        patient.id = 1;
//        patient.version = 0;
//        patient.uid = "P1";
//        patient.diagnosisAssessed = false;
//        patient.diagnosis = new PersistentSet(true);
//        patient.name = "Bla";
//
//        _ctx.patient = _ctx.meta_mergeExternalData(patient);
//        patient = _ctx.patient;
//
//        var diagnosis:Diagnosisb = new Diagnosisb();
//        diagnosis.uid = "D1";
//        diagnosis.patient = patient;
//        patient.diagnosis.addItem(diagnosis);
//        patient.diagnosisAssessed = true;
//
//        diagnosis.name = "bla";
//
//        Assert.assertTrue("Context dirty", _ctx.meta_dirty);
//
//        var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
//
//        // Simulate pre-remote call merge
//        changeSet = ChangeSet(_ctx.meta_mergeExternal(changeSet));
//
//        Assert.assertStrictlyEquals(patient, diagnosis.patient);
//
//        // Simulate serialization
//        changeSet = ChangeSet(ObjectUtil.copy(changeSet));
//
//        var c:Change = changeSet.changes[0];
//        var change:Change = new Change(c.className, c.uid, c.id, 1, false);
//        for (var p:String in c.changes)
//            change.changes[p] = c.changes[p];
//        var diag:Diagnosisb = c.changes.diagnosis.changes[0].value;
//        diag.id = 1;
//        diag.version = 0;
//        diag.patient.version = 1;
//        var updates:Array = [ [ 'PERSIST', diag ], [ 'UPDATE', change ] ];
//        _ctx.meta_handleUpdates(false, updates);
//
//        Assert.assertFalse("Context not dirty", _ctx.meta_dirty);
//        Assert.assertEquals("Patient version", 1, patient.version);
//        Assert.assertEquals("Diagnosis count", 1, patient.diagnosis.length);
//    }
}
