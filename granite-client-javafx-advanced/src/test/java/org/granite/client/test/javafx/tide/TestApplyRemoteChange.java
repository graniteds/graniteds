/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.test.javafx.tide;


import static org.granite.client.tide.data.EntityManager.UpdateKind.PERSIST;
import static org.granite.client.tide.data.EntityManager.UpdateKind.REMOVE;
import static org.granite.client.tide.data.EntityManager.UpdateKind.UPDATE;
import static org.granite.client.tide.data.PersistenceManager.getEntityManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.granite.client.javafx.platform.JavaFXReflection;
import org.granite.client.javafx.tide.JavaFXApplication;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.DefaultClientSharedContext;
import org.granite.client.messaging.jmf.ext.ClientEntityCodec;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.test.tide.MockChannelFactory;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.ChangeMerger;
import org.granite.client.tide.data.ChangeSetBuilder;
import org.granite.client.tide.data.Conflicts;
import org.granite.client.tide.data.DataConflictListener;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.ServerSession;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.reflect.Reflection;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.CollectionChanges;
import org.granite.util.JMFAMFUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestApplyRemoteChange {

    private SimpleContextManager contextManager;
    private Context ctx;
    private ServerSession serverSession;
    private DataManager dataManager;
    private EntityManager entityManager;
    
    private ClientSharedContext clientSharedContext;
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXApplication());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext("");
        serverSession = new ServerSession("/test", "localhost", 8080);
        serverSession.setChannelFactoryClass(MockChannelFactory.class);
        serverSession.setRemoteAliasPackages(Collections.singleton(Person.class.getPackage().getName()));
        ctx.set(serverSession);
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
        ctx.set(new ChangeMerger());
        serverSession.start();
        
		List<ExtendedObjectCodec> clientExtendedCodecs = new ArrayList<ExtendedObjectCodec>(Arrays.asList(new ClientEntityCodec()));
		List<String> defaultStoredStrings = new ArrayList<String>(JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS);
		Reflection clientReflection = new JavaFXReflection(getClass().getClassLoader());
		clientSharedContext = new DefaultClientSharedContext(new DefaultCodecRegistry(clientExtendedCodecs), defaultStoredStrings, clientReflection, serverSession.getAliasRegistry());
    }
    
    private static String personAlias = Person.class.getAnnotation(RemoteAlias.class).value();
    private static String patientAlias = Patient3.class.getAnnotation(RemoteAlias.class).value();
    private static String visitAlias = Visit.class.getAnnotation(RemoteAlias.class).value();


    @SuppressWarnings("cast")
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


    @SuppressWarnings("cast")
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

    @SuppressWarnings("cast")
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

    
    private static String patient4Alias = Patient4.class.getAnnotation(RemoteAlias.class).value();
    private static String statusAlias = PatientStatus.class.getAnnotation(RemoteAlias.class).value();
    
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
        ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
        entityManager.mergeExternalData(changeSet);
        entityManager.clearCache();

        Patient4 patientb = new Patient4(patient.getId(), patient.getVersion(), patient.getUid(), "Test");
        ((PersistentCollection)patientb.getDiagnosis()).uninitialize();
        PatientStatus statusb = new PatientStatus(status.getId(), status.getVersion()+1, status.getUid(), patientb, status.getName());
        ((PersistentCollection)statusb.getDeathCauses()).uninitialize();
        patientb.setStatus(statusb);
        
        Diagnosis diagnosis2b = new Diagnosis(2L, 0L, "D2", patientb, null, "Test2");
        
        Change change1 = new Change(patient4Alias, patientb.getId(), patientb.getVersion(), patientb.getUid());
        change1.addCollectionChanges("diagnosis", new CollectionChange(1, null, diagnosis2b));
        
        Change change3 = new Change(statusAlias, statusb.getId(), statusb.getVersion(), statusb.getUid());
        change3.addCollectionChanges("deathCauses", new CollectionChange(1, 1, diagnosis2b));
        
        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(
        		UPDATE.of(new ChangeSet(change1)), PERSIST.of(diagnosis2b), UPDATE.of(new ChangeSet(change3))
        ));
        
        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));
        Assert.assertTrue("Diagnosis patient", patient.getDiagnosis().contains(diagnosis));
        Assert.assertTrue("Diagnosis patient", patient.getStatus().getDeathCauses().contains(diagnosis));
        for (Diagnosis d : patient.getDiagnosis())
        	Assert.assertSame("Diagnosis em", entityManager, getEntityManager(d));
        for (Diagnosis d : patient.getStatus().getDeathCauses())
        	Assert.assertSame("Diagnosis em", entityManager, getEntityManager(d));
        Assert.assertEquals("Diagnosis added to patient", 2, patient.getDiagnosis().size());
        boolean found = false;
        for (Diagnosis d : patient.getDiagnosis()) {
        	if (d.getId().equals(2L))
        		found = true;
        }
        Assert.assertTrue("Diagnosis added to patient (id)", found);
        found = false;
        for (Diagnosis d : patient.getStatus().getDeathCauses()) {
        	if (d.getId().equals(2L))
        		found = true;
        }
        Assert.assertTrue("Diagnosis added to status (id)", found);
        
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

    @Test
    public void testApplyRemoteChangeList5() {
        Patient4 patient = new Patient4(1L, 0L, "P1", "Test");
        PatientStatus status = new PatientStatus(1L, 0L, "S1", patient, "Test");
        patient.setStatus(status);
        Visit2 visit = new Visit2(1L, 0L, "V1", patient, "Test");
        patient.getVisits().add(visit);
        Diagnosis diagnosis = new Diagnosis(1L, 0L, "D1", patient, visit, "Test");
        patient.getDiagnosis().add(diagnosis);
        patient.getStatus().getDeathCauses().add(diagnosis);
        visit.getAssessments().add(diagnosis);
        
        patient = (Patient4)entityManager.mergeExternalData(patient);
        entityManager.clearCache();
        
        Diagnosis diagnosis2 = new Diagnosis(null, null, "D2", patient, null, null);
        patient.getStatus().getDeathCauses().add(diagnosis2);
        
        diagnosis2.setName("Test2");
        
        // Simulate remote call with local merge of arguments
        ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
        entityManager.mergeExternalData(changeSet);
        entityManager.clearCache();

        
        Patient4 patientb = new Patient4(patient.getId(), patient.getVersion(), patient.getUid(), "Test");
        ((PersistentCollection)patientb.getDiagnosis()).uninitialize();
        ((PersistentCollection)patientb.getVisits()).uninitialize();
        PatientStatus statusb = new PatientStatus(status.getId(), status.getVersion()+1, status.getUid(), patientb, "Test");
        ((PersistentCollection)statusb.getDeathCauses()).uninitialize();
        patientb.setStatus(statusb);
        Visit2 visitb = new Visit2(visit.getId(), visit.getVersion(), visit.getUid(), patientb, "Test");
        ((PersistentCollection)visitb.getAssessments()).uninitialize();

        Diagnosis diagnosis2b = new Diagnosis(2L, 0L, "D2", patientb, visitb, "Test2");

        Change change1 = new Change(patient4Alias, patientb.getId(), patientb.getVersion(), patientb.getUid());
        change1.addCollectionChanges("diagnosis", new CollectionChange(1, null, diagnosis2b));
        
        Change change3 = new Change(statusAlias, statusb.getId(), statusb.getVersion(), statusb.getUid());
        change3.addCollectionChanges("deathCauses", new CollectionChange(1, 1, diagnosis2b));
        
        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(
        		UPDATE.of(new ChangeSet(change1)), PERSIST.of(diagnosis2b), UPDATE.of(new ChangeSet(change3))
        ));
        
        Assert.assertSame("Patient em", entityManager, getEntityManager(patient));
        Assert.assertTrue("Diagnosis patient", patient.getDiagnosis().contains(diagnosis));
        Assert.assertTrue("Diagnosis patient", patient.getStatus().getDeathCauses().contains(diagnosis));
        for (Diagnosis d : patient.getDiagnosis())
        	Assert.assertSame("Diagnosis em", entityManager, getEntityManager(d));
        for (Diagnosis d : patient.getStatus().getDeathCauses())
        	Assert.assertSame("Diagnosis em", entityManager, getEntityManager(d));
        Assert.assertEquals("Diagnosis added to patient", 2, patient.getDiagnosis().size());
        boolean found = false;
        for (Diagnosis d : patient.getDiagnosis()) {
        	if (d.getId().equals(2L))
        		found = true;
        }
        Assert.assertTrue("Diagnosis added to patient (id)", found);
        found = false;
        for (Diagnosis d : patient.getStatus().getDeathCauses()) {
        	if (d.getId().equals(2L))
        		found = true;
        }
        Assert.assertTrue("Diagnosis added to status (id)", found);
        
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }
    
    
    private static String classificationAlias = Classification.class.getAnnotation(RemoteAlias.class).value();
    
    @Test
    public void testApplyRemoteChangeList6() {
        Classification list = new Classification(1L, 0L, "P1", "List");
        Classification c1 = new Classification(1L, 0L, "C1", "1");
        Classification c2 = new Classification(2L, 0L, "C2", "2");
        Classification c3 = new Classification(3L, 0L, "C3", "3");
        Classification c4 = new Classification(4L, 0L, "C4", "4");
        list.getSubclasses().addAll(c1, c2, c3, c4);
        c1.getSuperclasses().add(list);
        c2.getSuperclasses().add(list);
        c3.getSuperclasses().add(list);
        c4.getSuperclasses().add(list);
        
        list = (Classification)entityManager.mergeExternalData(list);
        entityManager.clearCache();

        Classification c = list.getSubclasses().remove(3);
        list.getSubclasses().add(2, c);

        // Case where the server does not build the changes in the same order than the client
        // Ex. Client: A B C => A C B :  remove C (2) => add C (1)
        //     Server: A B C => A C B :  add C (1) => remove C (3)

        Classification c4b = new Classification(4L, 0L, "C4", "4");
        ((PersistentCollection)c4b.getSubclasses()).uninitialize();
        ((PersistentCollection)c4b.getSuperclasses()).uninitialize();

        Change change = new Change(classificationAlias, list.getId(), list.getVersion()+1, list.getUid());
        change.addCollectionChanges("subclasses", new CollectionChange(1, 2, c4b), new CollectionChange(-1, 4, c4b));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(UPDATE.of(new ChangeSet(change))));

        Assert.assertEquals("List size", 4, list.getSubclasses().size());
        Assert.assertEquals("List elt 3", "C4", list.getSubclasses().get(2).getUid());
        Assert.assertEquals("List elt 4", "C3", list.getSubclasses().get(3).getUid());
        
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

   	@Test
    public void testApplyRemoteChangeList6b() {
        Classification list = new Classification(1L, 0L, "P1", "List");
        Classification c1 = new Classification(1L, 0L, "C1", "1");
        Classification c2 = new Classification(2L, 0L, "C2", "2");
        Classification c3 = new Classification(3L, 0L, "C3", "3");
        Classification c4 = new Classification(4L, 0L, "C4", "4");
        list.getSubclasses().addAll(c1, c2, c3, c4);
        c1.getSuperclasses().add(list);
        c2.getSuperclasses().add(list);
        c3.getSuperclasses().add(list);
        c4.getSuperclasses().add(list);
        
        list = (Classification)entityManager.mergeExternalData(list);
        entityManager.clearCache();

        Classification c = list.getSubclasses().remove(3);
        list.getSubclasses().add(2, c);

        // Case where the server does not build the same permutation as the client
        // Ex. Client: A B C => A C B :  remove C (2) => add C (1)
        //     Server: A B C => A C B :  remove B (1) => add B (2)

        Classification c3b = new Classification(3L, 0L, "C3", "Test");
        ((PersistentCollection)c3b.getSubclasses()).uninitialize();
        ((PersistentCollection)c3b.getSuperclasses()).uninitialize();

        Change change = new Change(classificationAlias, list.getId(), list.getVersion()+1, list.getUid());
        change.addCollectionChanges("subclasses", new CollectionChange(-1, 2, c3b), new CollectionChange(1, 3, c3b));

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(UPDATE.of(new ChangeSet(change))));

        Assert.assertEquals("List size", 4, list.getSubclasses().size());
        Assert.assertEquals("List elt 3", "C4", list.getSubclasses().get(2).getUid());
        Assert.assertEquals("List elt 4", "C3", list.getSubclasses().get(3).getUid());
        
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

   	@Test
    public void testApplyRemoteChangeList6c() {
        Classification list = new Classification(1L, 0L, "P1", "List");
        Classification c1 = new Classification(1L, 0L, "C1", "1");
        Classification c2 = new Classification(2L, 0L, "C2", "2");
        Classification c3 = new Classification(3L, 0L, "C3", "3");
        Classification c4 = new Classification(4L, 0L, "C4", "4");
        list.getSubclasses().addAll(c1, c2, c3, c4);
        c1.getSuperclasses().add(list);
        c2.getSuperclasses().add(list);
        c3.getSuperclasses().add(list);
        c4.getSuperclasses().add(list);
        
        list = (Classification)entityManager.mergeExternalData(list);
        entityManager.clearCache();

        Classification c = list.getSubclasses().remove(0);
        list.getSubclasses().add(1, c);
        c = list.getSubclasses().remove(2);
        list.getSubclasses().add(1, c);
        
        // Case where the server does not build the same permutation as the client
        // Ex. Client: A B C D => B A C D => B C A D :  remove A (0) => add A (1) => remove C (2) => add C (1)
        //     Server: A B C D => B C A D :  remove A (0) => add A (2)
        
        Classification c3b = new Classification(3L, 0L, "C3", "3");
        ((PersistentCollection)c3b.getSubclasses()).uninitialize();
        ((PersistentCollection)c3b.getSuperclasses()).uninitialize();
        
        Change change = new Change(classificationAlias, list.getId(), list.getVersion()+1, list.getUid());
        change.addCollectionChanges("subclasses", 
        		new CollectionChange(-1, 0, new ChangeRef(classificationAlias, "C1", 1L)),
        		new CollectionChange(1, 2, new ChangeRef(classificationAlias, "C2", 2L))
        );

        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(UPDATE.of(new ChangeSet(change))));

        Assert.assertEquals("List size", 4, list.getSubclasses().size());
        Assert.assertEquals("List elt 3", "C1", list.getSubclasses().get(2).getUid());
        Assert.assertEquals("List elt 4", "C4", list.getSubclasses().get(3).getUid());
        
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }
   	
   	
    private static String person11Alias = Person11.class.getAnnotation(RemoteAlias.class).value();
    
    @Test
    public void testApplyRemoteChangeMap() {
    	Person11 person = new Person11(1L, 0L, "P1", "Test", "Test");
    	SimpleEntity key = new SimpleEntity(null, null, "K1", "Key");
    	SimpleEntity2 value = new SimpleEntity2(1L, 0L, "V1", "Value");
    	person.getMap().put(key, value);
        
        person = (Person11)entityManager.mergeExternalData(person);
        entityManager.clearCache();
        
        SimpleEntity key2 = new SimpleEntity(null, null, "K2", "Key2");
        SimpleEntity2 val2 = new SimpleEntity2(null, null, "V2", "Value2");
        person.getMap().put(key2, val2);
        
    	SimpleEntity key2b = new SimpleEntity(2L, 0L, "K2", "Key2");
    	SimpleEntity2 val2b = new SimpleEntity2(2L, 0L, "V2", "Value2");
        person.getMap().put(key2b, val2b);
        
        Change change1 = new Change(person11Alias, person.getId(), 0L, person.getUid());
        change1.addCollectionChanges("map", new CollectionChange(1, key2b, val2b));
        
        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(UPDATE.of(new ChangeSet(change1))));

        Assert.assertSame("Person em", entityManager, getEntityManager(person));
        Assert.assertEquals("Entry added to person.map", 2, person.getMap().size());
        Assert.assertEquals("Entry key updated", Long.valueOf(2), key2.getId());
        Assert.assertEquals("Entry value updated", Long.valueOf(2), val2.getId());
        
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }

    
    @Test
    public void testApplyRemoteChangeConflict() {
    	Person person = new Person(1L, 0L, "P1", "Test", "Test");
        
        person = (Person)entityManager.mergeExternalData(person);
        entityManager.clearCache();

        person.setLastName("Tutu");

        Change change = new Change(personAlias, 1L, 1L, "P1");
        change.getChanges().put("lastName", "Toto");
        
        final Conflicts[] conflicts = new Conflicts[1];
        entityManager.addListener(new DataConflictListener() {			
			@Override
			public void onConflict(EntityManager entityManager, Conflicts c) {
				conflicts[0] = c;
			}
		});
        
        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, "EXTUSER", Arrays.asList(UPDATE.of(new ChangeSet(change))));
        
        Assert.assertEquals("Last name not changed", "Tutu", person.getLastName());
        Assert.assertEquals("Conflict detected", 1, conflicts[0].getConflicts().size());
        
        conflicts[0].acceptAllServer();
        
        Assert.assertEquals("Last name changed", "Toto", person.getLastName());
    }

    @Test
    public void testApplyRemoteChangeConflict2() {
        Person person = new Person(1L, 0L, "P1", "Test", "Test");
        
        person = (Person)entityManager.mergeExternalData(person);
        entityManager.clearCache();

        person.setLastName("Tutu");

        Change change = new Change(personAlias, 1L, 1L, "P1");
        change.getChanges().put("lastName", "Toto");

        final Conflicts[] conflicts = new Conflicts[1];
        entityManager.addListener(new DataConflictListener() {			
			@Override
			public void onConflict(EntityManager entityManager, Conflicts c) {
				conflicts[0] = c;
			}
		});
        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, "EXTUSER", Arrays.asList(UPDATE.of(new ChangeSet(change))));

        Assert.assertEquals("Last name not changed", "Tutu", person.getLastName());
        Assert.assertEquals("Conflict detected", 1, conflicts[0].getConflicts().size());
        
        conflicts[0].acceptAllClient();
        
        Assert.assertEquals("Last name changed", "Tutu", person.getLastName());
        Assert.assertEquals("Version increased", Long.valueOf(1), person.getVersion());
    }

    
    @SuppressWarnings("unchecked")
	private <T> T serializeCopy(T object) throws Exception {
        // Simulate serialization of change set
        ByteArrayOutputStream buf = new ByteArrayOutputStream(1000);
        JMFSerializer ser = new JMFSerializer(buf, clientSharedContext);
        ser.writeObject(object);
        ser.close();
        JMFDeserializer deser = new JMFDeserializer(new ByteArrayInputStream(buf.toByteArray()), clientSharedContext);
        object = (T)deser.readObject();
        deser.close();
        return object;
    }
    
    @Test
    public void testApplyChangeRef() throws Exception {
        Person person = new Person(1L, 0L, "P1", "Test", "Test");
        Contact contact = new Contact(1L, 0L, "C1", person, "test@test.com");
        person.getContacts().add(contact);
        
        person = (Person)entityManager.mergeExternalData(person);
        entityManager.clearCache();
        
        person.getContacts().remove(0);
        
        ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
        
        changeSet = serializeCopy(changeSet);
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Person has 1 contact", 1, person.getContacts().size());
        
        // Check that applied ChangeSet removes the contact
        
        MergeContext mergeContext = entityManager.initMerge(serverSession);
        entityManager.mergeExternal(mergeContext, changeSet, null, null, null, false);

        Assert.assertEquals("Person contact removed", 0, person.getContacts().size());
    }

    @Test
    public void testApplyChangeBoolean() throws Exception {
    	entityManager.setUninitializeAllowed(false);
    	
    	Patient4b patient = new Patient4b(1L, 0L, "P1", "Bla", false);
        
        patient = (Patient4b)entityManager.mergeExternalData(patient);
        entityManager.clearCache();

        Diagnosisb diagnosis = new Diagnosisb(null, null, "D1", patient, null, null);
        patient.getDiagnosis().add(diagnosis);
        patient.setDiagnosisAssessed(true);

        diagnosis.setName("bla");

        Assert.assertTrue("Context dirty", dataManager.isDirty());
        
        ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
        
        // Simulate pre-remote call merge
        MergeContext mergeContext = entityManager.initMerge(serverSession);
        changeSet = (ChangeSet)entityManager.mergeExternal(mergeContext, changeSet, null, null, null, false);
        
        Assert.assertSame(patient, diagnosis.getPatient());
        
        MergeContext.destroy(entityManager);
        
        // Simulate serialization
        changeSet = serializeCopy(changeSet);
        
        Change c = changeSet.getChanges()[0];
        Change change = new Change(c.getClassName(), c.getId(), 1L, c.getUid(), false);
        change.getChanges().putAll(c.getChanges());
        Diagnosisb diag = (Diagnosisb)((CollectionChanges)c.getChanges().get("diagnosis")).getChanges()[0].getValue();
        diag.resetId(1L, 0L);
        diag.getPatient().resetId(diag.getPatient().getId(), 1L);
        
        mergeContext = entityManager.initMerge(serverSession);
        entityManager.handleUpdates(mergeContext, null, Arrays.asList(PERSIST.of(diag), UPDATE.of(new ChangeSet(change))));
        
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
        Assert.assertEquals("Patient version", Long.valueOf(1), patient.getVersion());
        Assert.assertEquals("Diagnosis count", 1, patient.getDiagnosis().size());
    }
}
