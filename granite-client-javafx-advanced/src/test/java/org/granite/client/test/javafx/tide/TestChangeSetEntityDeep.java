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

import java.util.Collections;

import org.granite.client.javafx.tide.JavaFXApplication;
import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.messages.Message;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.requests.InvocationMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.test.MockRemoteService;
import org.granite.client.test.ResponseBuilder;
import org.granite.client.test.tide.MockChannelFactory;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.test.tide.MockServiceFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.ChangeMerger;
import org.granite.client.tide.data.ChangeSetBuilder;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.data.impl.ObjectUtil;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChanges;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestChangeSetEntityDeep {

    private SimpleContextManager contextManager;
    private Context ctx;
    private ServerSession serverSession;
    private DataManager dataManager;
    private EntityManager entityManager;
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXApplication());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext("");
        serverSession = new ServerSession("/test", "localhost", 8080);
        serverSession.setChannelFactoryClass(MockChannelFactory.class);
        serverSession.setServiceFactory(new MockServiceFactory());
        serverSession.setRemoteAliasPackages(Collections.singleton(Person.class.getPackage().getName()));
        ctx.set(serverSession);
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
        ctx.set(new ChangeMerger());
        serverSession.start();
    }
    
    
    @Test
    public void testChangeSetEntityDeep() {
    	Patient patient = new Patient(1L, 0L, "P1", "Walter White");
    	Medication medication = new Medication(1L, 0L, "M1", patient, "Cancer");
    	Prescription prescription = new Prescription(1L, 0L, "PR1", medication, "Aspirin");
    	medication.getPrescriptionList().add(prescription);
    	patient.getMedicationList().add(medication);
    	
    	patient = (Patient)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();
		
    	Medication medication2 = new Medication(null, null, "M2", patient, "Flu");
    	Prescription prescription2 = new Prescription(null, null, "PR2", medication2, "Xanax");
    	medication2.getPrescriptionList().add(prescription2);
    	patient.getMedicationList().add(medication2);

		ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet with one add", 1, changeSet.size());
		Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.getChange(0).getCollectionChange("medicationList").size());
		Assert.assertEquals("ChangeSet contains an add", 1, changeSet.getChange(0).getCollectionChange("medicationList").getChangeType(0));
		Medication med = changeSet.getChange(0).getCollectionChange("medicationList").getChangeValue(0, Medication.class);
		Assert.assertEquals("ChangeSet contains the new entity", medication2.getUid(), med.getUid());
    }
	
	@Test
	public void testChangeSetEntityDeep2() {
		Patient2 patient = new Patient2(1L, 0L, "PA1", "Tom");
		Account2 account = new Account2(1L, 0L, "AC1", "Jerry");
		Alert2 alert = new Alert2(1L, 0L, "AL1", patient, account, "Alert");
		account.getAlerts().add(alert);
		patient.getAlerts().add(alert);
    	
    	patient = (Patient2)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();
		
    	Alert2 alert2 = new Alert2(null, null, "AL2", patient, account, "Alert 2");
		account.getAlerts().add(alert2);
		patient.getAlerts().add(alert2);

		ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet with one add", 2, changeSet.size());
		Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.getChange(0).getCollectionChange("alerts").size());
		Assert.assertEquals("ChangeSet contains an add", 1, changeSet.getChange(0).getCollectionChange("alerts").getChangeType(0));
		Assert.assertSame("ChangeSet same add in both collections", 
			changeSet.getChange(0).getCollectionChange("alerts").getChangeValue(0), 
			changeSet.getChange(1).getCollectionChange("alerts").getChangeValue(0)
		);
	}
		
	@Test
	public void testChangeSetEntityDeepMerge() throws Exception {
		Patient patient = new Patient(1L, 0L, "P1", "Walter White");
    	
    	patient = (Patient)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();

    	Medication medication = new Medication(null, null, "M1", patient, "Test");
    	patient.getMedicationList().add(medication);
    	
    	Prescription prescription = new Prescription(null, null, "PR1", medication, "Bla");
    	medication.getPrescriptionList().add(prescription);
    	
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		// Simulate remote call
        MockRemoteService.setResponseBuilder(new ResponseBuilder() {
            @Override
            public Message buildResponseMessage(RemoteService service, RequestMessage request) {
            	InvocationMessage invocation = (InvocationMessage)request;
            	
        		Assert.assertTrue("ChangeSet received", ((Object[])invocation.getParameters()[3])[0] instanceof ChangeSet);
        		
        		Object[][] updates = new Object[5][];
        		
        		Patient patient = (Patient)entityManager.getCachedObject(Patient.class.getName() + ":P1", true);
        		Medication medication = patient.getMedicationList().iterator().next();
        		Prescription prescription = medication.getPrescriptionList().iterator().next();
        		
        		Patient p1 = new Patient(patient.getId(), patient.getVersion(), patient.getUid(), patient.getName());
        		((PersistentCollection)p1.getMedicationList()).uninitialize();
        		
        		Medication m1 = new Medication(1L, 0L, medication.getUid(), p1, medication.getName());
        		((PersistentCollection)m1.getPrescriptionList()).uninitialize();
        		
        		Prescription pr1 = new Prescription(1L, 0L, prescription.getUid(), m1, prescription.getName());
        		
        		updates[0] = new Object[] { UpdateKind.PERSIST.name(), pr1 };

        		Patient p2 = new Patient(patient.getId(), patient.getVersion(), patient.getUid(), patient.getName());
        		Medication m2 = new Medication(1L, 0L, medication.getUid(), p2, medication.getName());
        		p2.getMedicationList().add(m2);
        		
        		updates[1] = new Object[] { UpdateKind.UPDATE.name(), p2 };
        		
        		Patient p3 = new Patient(patient.getId(), patient.getVersion(), patient.getUid(), patient.getName());
        		((PersistentCollection)p3.getMedicationList()).uninitialize();
        		
        		Medication m3 = new Medication(1L, 0L, medication.getUid(), p3, medication.getName());
        		Prescription pr3 = new Prescription(1L, 0L, prescription.getUid(), m3, prescription.getName());
        		m3.getPrescriptionList().add(pr3);
        		
        		updates[2] = new Object[] { UpdateKind.PERSIST.name(), m3 };

        		Patient p4 = new Patient(patient.getId(), patient.getVersion(), patient.getUid(), patient.getName());
        		Medication m4 = new Medication(1L, 0L, medication.getUid(), p4, medication.getName());
        		((PersistentCollection)m4.getPrescriptionList()).uninitialize();
        		p4.getMedicationList().add(m4);
        		
        		updates[3] = new Object[] { UpdateKind.UPDATE.name(), p4 };

        		Patient p5 = new Patient(patient.getId(), patient.getVersion(), patient.getUid(), patient.getName());
        		((PersistentCollection)p5.getMedicationList()).uninitialize();
        		
        		Medication m5 = new Medication(1L, 0L, medication.getUid(), p5, medication.getName());
        		Prescription pr5 = new Prescription(1L, 0L, prescription.getUid(), m5, prescription.getName());
        		m5.getPrescriptionList().add(pr5);
        		
        		updates[4] = new Object[] { UpdateKind.UPDATE.name(), m5 };

        		Patient p6 = new Patient(patient.getId(), patient.getVersion(), patient.getUid(), patient.getName());
        		Medication m6 = new Medication(1L, 0L, medication.getUid(), p5, medication.getName());
        		((PersistentCollection)m6.getPrescriptionList()).uninitialize();
        		p6.getMedicationList().add(m6);
        		
        		InvocationResult result = new InvocationResult(p6);
        		result.setUpdates(updates);
        		return new ResultMessage(request.getClientId(), request.getId(), result);
            }
        });
    	
    	Component tideService = ctx.set(new ComponentImpl(serverSession));
    	tideService.call("applyChanges", changeSet).get();
    	
		Assert.assertEquals("New medication id", Long.valueOf(1), medication.getId());
		Assert.assertEquals("New prescription id", Long.valueOf(1), prescription.getId());
		
		Assert.assertFalse("Context not dirty", dataManager.isDirty());
		
		prescription.setName("Truc");
		
		Assert.assertTrue("Context dirty", dataManager.isDirty());
	}
	
	
	@Test
	public void testChangeSetEntityDeepUninit() {
		Patient patient = new Patient(1L, 0L, "P1", "Walter White");
		Medication medication = new Medication(1L, 0L, "M1", patient, "Cancer");
		Prescription prescription = new Prescription(1L, 0L, "PR1", medication, "Aspirin");
		medication.getPrescriptionList().add(prescription);
		patient.getMedicationList().add(medication);
		
		Medication medication2 = new Medication(2L, 0L, "M2", patient, "Flu");
		Prescription prescription2 = new Prescription(2L, 0L, "PR2", medication2, "Xanax");
		medication2.getPrescriptionList().add(prescription2);
		patient.getMedicationList().add(medication2);
    	
    	patient = (Patient)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();
		
		Prescription prescription3 = new Prescription(null, null, "PR3", medication2, "Subutex");
		medication2.getPrescriptionList().add(prescription3);
    	
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet with one add", 1, changeSet.size());
		Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.getChange(0).getCollectionChange("prescriptionList").size());
		Assert.assertTrue("Change contains prescription", ObjectUtil.objectEquals(dataManager, prescription3, changeSet.getChange(0).getCollectionChange("prescriptionList").getChangeValue(0)));
		Assert.assertFalse("ChangeSet prescription list uninitialized", ((PersistentCollection)changeSet.getChange(0).getCollectionChange("prescriptionList").getChangeValue(0, Prescription.class).getMedication().getPrescriptionList()).wasInitialized());
		Assert.assertFalse("ChangeSet medication list uninitialized", ((PersistentCollection)changeSet.getChange(0).getCollectionChange("prescriptionList").getChangeValue(0, Prescription.class).getMedication().getPatient().getMedicationList()).wasInitialized()); 
	}
	
	@Test
	public void testChangeSetEntityDeepUninitB() {
		Patient patient = new Patient(1L, 0L, "P1", "Walter White");
		Medication medication = new Medication(1L, 0L, "M1", patient, "Cancer");
		Prescription prescription = new Prescription(1L, 0L, "PR1", medication, "Aspirin");
		medication.getPrescriptionList().add(prescription);
		patient.getMedicationList().add(medication);
		
		Medication medication2 = new Medication(2L, 0L, "M2", patient, "Flu");
		Prescription prescription2 = new Prescription(2L, 0L, "PR2", medication2, "Xanax");
		medication2.getPrescriptionList().add(prescription2);
		patient.getMedicationList().add(medication2);
    	
    	patient = (Patient)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();
		
		Medication medication3 = new Medication(null, null, "M3", patient, "Flu2");
		Prescription prescription3 = new Prescription(null, null, "PR3", medication3, "Subutex");
		medication3.getPrescriptionList().add(prescription3);
		patient.getMedicationList().add(medication3);
    	
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet with one add", 1, changeSet.size());
		Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.getChange(0).getCollectionChange("medicationList").size());
		Assert.assertTrue("Change contains prescription", ObjectUtil.objectEquals(dataManager, prescription3, changeSet.getChange(0).getCollectionChange("medicationList").getChangeValue(0, Medication.class).getPrescriptionList().iterator().next()));
		Assert.assertFalse("ChangeSet medication list uninitialized", ((PersistentCollection)changeSet.getChange(0).getCollectionChange("medicationList").getChangeValue(0, Medication.class).getPatient().getMedicationList()).wasInitialized()); 
	}
	
	@Test
	public void testChangeSetEntityDeepUninit2() {
		Patient patient = new Patient(1L, 0L, "P1", "Walter White");
		Medication medication = new Medication(1L, 0L, "M1", patient, "Cancer");
		Prescription prescription = new Prescription(1L, 0L, "PR1", medication, "Aspirin");
		medication.getPrescriptionList().add(prescription);
		patient.getMedicationList().add(medication);
    	
    	patient = (Patient)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();
		
		Medication medication2 = new Medication(null, null, "M2", patient, "Flu");
		Prescription prescription2 = new Prescription(null, null, "PR2", medication2, "Xanax");
		medication2.getPrescriptionList().add(prescription2);
		patient.getMedicationList().add(medication2);
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet with one add", 1, changeSet.size());
		Assert.assertEquals("ChangeSet contains the new entity", 1, changeSet.getChange(0).getCollectionChange("medicationList").size());
		Assert.assertTrue("ChangeSet collection with init elts", ((PersistentCollection)changeSet.getChange(0).getCollectionChange("medicationList").getChangeValue(0, Medication.class).getPrescriptionList()).wasInitialized());
	}
	
	@Test
	public void testChangeSetEntityDeepUninit3() {
		Patient patient = new Patient(1L, 0L, "P1", "Walter White");
		Medication medication = new Medication(1L, 0L, "M1", patient, "Cancer");
		Prescription prescription = new Prescription(1L, 0L, "PR1", medication, "Aspirin");
		medication.getPrescriptionList().add(prescription);
		patient.getMedicationList().add(medication);
    	
    	patient = (Patient)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();
		
		Medication medication2 = new Medication(null, null, "M2", patient, "Flu");
		patient.getMedicationList().add(medication2);
		Medication medication3 = new Medication(null, null, "M3", patient, "Flo");
		patient.getMedicationList().add(medication3);
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
    	
		Assert.assertEquals("ChangeSet with one add", 1, changeSet.size());
		Assert.assertEquals("ChangeSet contains the new entity", 2, changeSet.getChange(0).getCollectionChange("medicationList").size());
		Assert.assertTrue("ChangeSet new medication 1 prescriptions initialized", dataManager.isInitialized(changeSet.getChange(0).getCollectionChange("medicationList").getChangeValue(0, Medication.class).getPrescriptionList()));
		Assert.assertTrue("ChangeSet new medication 2 prescriptions initialized", dataManager.isInitialized(changeSet.getChange(0).getCollectionChange("medicationList").getChangeValue(1, Medication.class).getPrescriptionList()));
		Assert.assertFalse("ChangeSet patient medications not initialized", dataManager.isInitialized(changeSet.getChange(0).getCollectionChange("medicationList").getChangeValue(0, Medication.class).getPatient().getMedicationList()));
	}
	
    @Test
    public void testChangeSetEntityRemote() {
    	Person person = new Person(1L, 0L, "P1", null, null);
    	dataManager.initProxy(person, 1L, true, "bla");
    	((PersistentCollection)person.getContacts()).uninitialize();
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
		
    	person = new Person(1L, 0L, "P1", null, null);
    	dataManager.initProxy(person, 1L, true, "bla");
    	Contact contact = new Contact(1L, 0L, "C1", person, null);
    	person.getContacts().add(contact);
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet empty", 0, changeSet.size());
		
    	person.setLastName("toto");
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 1", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		
		person.getContacts().remove(0);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 2", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		CollectionChanges coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 1, coll.size());
		Assert.assertEquals("ChangeSet collection type", -1, coll.getChangeType(0));
		Assert.assertEquals("ChangeSet collection index", 0, coll.getChangeKey(0));
		Assert.assertEquals("ChangeSet collection value", "C1", coll.getChangeValue(0, ChangeRef.class).getUid());
		
    	Contact contact2a = new Contact(null, null, "C2a", person, "test@truc.net");
    	person.getContacts().add(contact2a);
    	Contact contact2b = new Contact(null, null, "C2b", person, "test@truc.com");
    	person.getContacts().add(contact2b);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 3", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 3, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(1));
		Assert.assertEquals("ChangeSet collection index", 0, coll.getChangeKey(1));
		Assert.assertFalse("ChangeSet collection element uninitialized", dataManager.isInitialized(coll.getChangeValue(1, Contact.class).getPerson()));
		Assert.assertEquals("ChangeSet collection element reference", coll.getChangeValue(1, Contact.class).getPerson().getId(), coll.getChangeValue(2, Contact.class).getPerson().getId());
		
    	Contact contact3 = new Contact(3L, 0L, "C3", person, "tutu@tutu.net");
    	contact3 = (Contact)entityManager.mergeExternalData(contact3);
    	entityManager.clearCache();
    	
    	person.getContacts().add(contact3);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 4", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 4, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(3));
		Assert.assertEquals("ChangeSet collection index", 2, coll.getChangeKey(3));
		Assert.assertTrue("ChangeSet collection value", coll.getChangeValue(3) instanceof ChangeRef);
		Assert.assertSame("ChangeSet collection value", contact3.getUid(), coll.getChangeValue(3, ChangeRef.class).getUid());
		
		changeSet = (ChangeSet)entityManager.mergeExternalData(changeSet);
    	entityManager.clearCache();
		
		Assert.assertEquals("ChangeSet count 4", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 4, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(3));
		Assert.assertEquals("ChangeSet collection index", 2, coll.getChangeKey(3));
		Assert.assertTrue("ChangeSet collection value", coll.getChangeValue(3) instanceof ChangeRef);
		Assert.assertSame("ChangeSet collection value", contact3.getUid(), coll.getChangeValue(3, ChangeRef.class).getUid());
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession, false).buildChangeSet();
        MergeContext mergeContext = entityManager.initMerge(serverSession);
		Object object = entityManager.mergeExternal(mergeContext, changeSet, null, null, null, false);
		
		Assert.assertSame("Local ChangeSet merge", person, object);
    }
}
