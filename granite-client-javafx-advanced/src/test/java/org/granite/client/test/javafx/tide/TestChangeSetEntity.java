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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.granite.client.javafx.tide.JavaFXApplication;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.UnsafePersistentCollection;
import org.granite.client.test.tide.MockChannelFactory;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.test.tide.data.TestDataUtils;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.ChangeMerger;
import org.granite.client.tide.data.ChangeSetBuilder;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.ServerSession;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.CollectionChanges;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestChangeSetEntity {

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
        serverSession.setRemoteAliasPackages(Collections.singleton(Person.class.getPackage().getName()));
        ctx.set(serverSession);
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
        ctx.set(new ChangeMerger());
        serverSession.start();
    }

    @Test
    public void testChangeSetEntity() {
    	Person person = new Person(1L, 0L, "P1", null, null);
    	dataManager.initProxy(person, 1L, true, "bla");
    	((PersistentCollection)person.getContacts()).uninitialize();
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
    	
    	person = new Person(1L, 0L, "P1", null, null);
    	dataManager.initProxy(person, 1L, true, "bla");
		Contact contact = new Contact(1L, 0L, "C1", person, null);
		person.addContact(contact);
    	
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
		Assert.assertTrue("ChangeSet collection value", coll.getChangeValue(0) instanceof ChangeRef);
		Assert.assertEquals("ChangeSet collection value", "C1", coll.getChangeValue(0, ChangeRef.class).getUid());
		
		Contact contact2a = new Contact(null, null, null, person, "test@truc.net");
		person.addContact(contact2a);
		Contact contact2b = new Contact(null, null, null, person, "test@truc.com");
		person.addContact(contact2b);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 3", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 3, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(1));
		Assert.assertEquals("ChangeSet collection index", 0, coll.getChangeKey(1));
		Assert.assertFalse("ChangeSet collection element uninitialized", dataManager.isInitialized(coll.getChangeValue(1, Contact.class).getPerson()));
		Assert.assertEquals("ChangeSet collection element reference", coll.getChangeValue(1, Contact.class).getPerson().getId(), coll.getChangeValue(2, Contact.class).getPerson().getId());
		
		Contact contact3 = new Contact(3L, 0L, "C3", null, "tutu@tutu.net");
    	
		contact3 = (Contact)entityManager.mergeExternalData(contact3);
    	entityManager.clearCache();
		
		person.addContact(contact3);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 4", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 4, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(3));
		Assert.assertEquals("ChangeSet collection index", 2, coll.getChangeKey(3));
		Assert.assertTrue("ChangeSet collection value", coll.getChangeValue(3) instanceof ChangeRef);
		Assert.assertSame("ChangeSet collection value", contact3.getUid(), coll.getChangeValue(3, ChangeRef.class).getUid());
    }
    
    @Test
    public void testLocalChangeSetEntity() {
    	Person person = new Person(1L, 0L, "P1", null, null);
    	dataManager.initProxy(person, 1L, true, "bla");
    	((PersistentCollection)person.getContacts()).uninitialize();
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
    	
    	person = new Person(1L, 0L, "P1", null, null);
    	dataManager.initProxy(person, 1L, true, "bla");
		Contact contact = new Contact(1L, 0L, "C1", person, null);
		person.addContact(contact);
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildEntityChangeSet(person);
    	
		Assert.assertEquals("ChangeSet empty", 1, changeSet.size());
        Assert.assertTrue("ChangeSet empty", changeSet.getChange(0).isEmpty());    // Only version property

    	person.setLastName("toto");

		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildEntityChangeSet(person);

		Assert.assertEquals("ChangeSet count 1", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));

		person.getContacts().remove(0);

		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildEntityChangeSet(person);
		
		Assert.assertEquals("ChangeSet count after remove contact", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		CollectionChanges coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 1, coll.size());
		Assert.assertEquals("ChangeSet collection type", -1, coll.getChangeType(0));
		Assert.assertEquals("ChangeSet collection index", 0, coll.getChangeKey(0));
		Assert.assertEquals("ChangeSet collection value", "C1", coll.getChangeValue(0, ChangeRef.class).getUid());
		
		Contact contact2a = new Contact(null, null, null, person, "test@truc.net");
		person.addContact(contact2a);
		Contact contact2b = new Contact(null, null, null, person, "test@truc.com");
		person.addContact(contact2b);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 3", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 3, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(1));
		Assert.assertEquals("ChangeSet collection index", 0, coll.getChangeKey(1));
		Assert.assertFalse("ChangeSet collection element uninitialized", dataManager.isInitialized(coll.getChangeValue(1, Contact.class).getPerson()));
		Assert.assertEquals("ChangeSet collection element reference", coll.getChangeValue(1, Contact.class).getPerson().getId(), coll.getChangeValue(2, Contact.class).getPerson().getId());
		
		Contact contact3 = new Contact(3L, 0L, "C3", null, "tutu@tutu.net");
    	
		contact3 = (Contact)entityManager.mergeExternalData(contact3);
    	entityManager.clearCache();
		
		person.addContact(contact3);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 4", 1, changeSet.size());
		Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).getChange("lastName"));
		coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 4, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(3));
		Assert.assertEquals("ChangeSet collection index", 2, coll.getChangeKey(3));
		Assert.assertTrue("ChangeSet collection value", coll.getChangeValue(3) instanceof ChangeRef);
		Assert.assertSame("ChangeSet collection value", contact3.getUid(), coll.getChangeValue(3, ChangeRef.class).getUid());
    }
    
    private static final String personAlias = Person.class.getAnnotation(RemoteAlias.class).value();
    private static final String contactAlias = Contact.class.getAnnotation(RemoteAlias.class).value();

    @Test
    public void testLocalChangeSetEntity2() {
    	Person person = new Person(1L, 0L, "P1", null, null);
    	Contact contact = new Contact(1L, 0L, "C1", person, null);
    	person.addContact(contact);
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
        contact = person.getContact(0);

        contact.setEmail("test@test.com");

		ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildEntityChangeSet(person);
		
		Assert.assertEquals("ChangeSet count 1", 2, changeSet.size());
        Assert.assertEquals("ChangeSet type", personAlias, changeSet.getChange(0).getClassName());
        Assert.assertTrue("ChangeSet no property", changeSet.getChange(0).isEmpty());
        Assert.assertEquals("ChangeSet type", contactAlias, changeSet.getChange(1).getClassName());
		Assert.assertEquals("ChangeSet property value", "test@test.com", changeSet.getChange(1).getChange("email"));

        person.setLastName("Toto");

        changeSet = new ChangeSetBuilder(entityManager, serverSession).buildEntityChangeSet(person);
        
        Assert.assertEquals("ChangeSet count 2", 2, changeSet.size());
        Assert.assertEquals("ChangeSet type", personAlias, changeSet.getChange(0).getClassName());
        Assert.assertEquals("ChangeSet property value", "Toto", changeSet.getChange(0).getChange("lastName"));
        Assert.assertEquals("ChangeSet type", contactAlias, changeSet.getChange(1).getClassName());
        Assert.assertEquals("ChangeSet property value", "test@test.com", changeSet.getChange(1).getChange("email"));
        
		person.getContacts().remove(0);

		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildEntityChangeSet(person);
		
		Assert.assertEquals("ChangeSet count after remove contact", 1, changeSet.size());
		Assert.assertEquals("ChangeSet type", personAlias, changeSet.getChange(0).getClassName());
		CollectionChanges coll = changeSet.getChange(0).getCollectionChange("contacts");
		Assert.assertEquals("ChangeSet collection", 1, coll.size());
		Assert.assertEquals("ChangeSet collection type", -1, coll.getChangeType(0));
		Assert.assertEquals("ChangeSet collection index", 0, coll.getChangeKey(0));
		Assert.assertEquals("ChangeSet collection value", "C1", coll.getChangeValue(0, ChangeRef.class).getUid());
    }
    
    private static final String person9Alias = Person9.class.getAnnotation(RemoteAlias.class).value();
    private static final String contact3Alias = Contact3.class.getAnnotation(RemoteAlias.class).value();
    private static final String keyAlias = SimpleEntity.class.getAnnotation(RemoteAlias.class).value();

    @Test
    public void testLocalChangeSetEntity3() {
    	Person9 person = new Person9(1L, 0L, "P1", null, null);
    	dataManager.initProxy(person, 1L, true, "bla");
    	Contact3 contact = new Contact3(1L, 0L, "C1", person, null);
        person.addContact(contact);
    	
    	person = (Person9)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
        contact = person.getContact(0);
        
        SimpleEntity key = new SimpleEntity(1L, 0L, "K1", null);
    	key = (SimpleEntity)entityManager.mergeExternalData(key);
    	entityManager.clearCache();

        person.getTestMap().put(key, contact);
        
        ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildEntityChangeSet(person);
        
		Assert.assertEquals("ChangeSet count 1", 1, changeSet.size());
        Assert.assertEquals("ChangeSet type", person9Alias, changeSet.getChange(0).getClassName());
        Assert.assertTrue("ChangeSet map", changeSet.getChange(0).getChange("testMap") instanceof CollectionChanges);
        CollectionChanges ccs = changeSet.getChange(0).getCollectionChange("testMap");
        Assert.assertEquals("ChangeSet add", 1, ccs.getChangeType(0));
        Assert.assertTrue("ChangeSet add key", ccs.getChangeKey(0) instanceof ChangeRef);
        Assert.assertEquals("ChangeSet add key", keyAlias, ccs.getChangeKey(0, ChangeRef.class).getClassName());
		Assert.assertTrue("ChangeSet add value", ccs.getChangeValue(0) instanceof ChangeRef);
        Assert.assertEquals("ChangeSet add value", contact3Alias, ccs.getChangeValue(0, ChangeRef.class).getClassName());
        
        Contact3 contact2 = new Contact3(null, null, "C2", person, "test@test.com");
        SimpleEntity key2 = new SimpleEntity(null, null, "K2", null);

        person.getTestMap().put(key2, contact2);
        
        changeSet = new ChangeSetBuilder(entityManager, serverSession).buildEntityChangeSet(person);
        
        Assert.assertEquals("ChangeSet count 2", 1, changeSet.size());
        Assert.assertEquals("ChangeSet type", person9Alias, changeSet.getChange(0).getClassName());
        Assert.assertTrue("ChangeSet map", changeSet.getChange(0).getChange("testMap") instanceof CollectionChanges);
        ccs = changeSet.getChange(0).getCollectionChange("testMap");
        Assert.assertEquals("ChangeSet add", 1, ccs.getChangeType(0));
        Assert.assertEquals("ChangeSet add", 1, ccs.getChangeType(1));
        CollectionChange cc = null;
        for (int i = 0; i < ccs.size(); i++) {
        	Object k = ccs.getChangeKey(i);
        	String uid = k instanceof ChangeRef ? ((ChangeRef)k).getUid() : ((AbstractEntity)k).getUid();
            if (uid.equals("K2")) {
                cc = ccs.getChange(i);
                break;
            }
        }
        Assert.assertNotNull("ChangeSet add key K2", cc);
        Assert.assertEquals("ChangeSet add value", "test@test.com", ((Contact3)cc.getValue()).getEmail());
        Assert.assertFalse("ChangeSet add value uninit", dataManager.isInitialized(((Contact3)cc.getValue()).getPerson()));
    }
	
	
	@Test
	public void testChangeSetEntityCollectionSort() {
		Person person = new Person(1L, 0L, "P1", null, null);
		Contact contact1 = new Contact(1L, 0L, "C1", null);
		person.addContact(contact1);
		Contact contact2 = new Contact(2L, 0L, "C2", null);
		person.addContact(contact2);
		Contact contact3 = new Contact(3L, 0L, "C3", null);
		person.addContact(contact3);
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();

        List<Contact> collSnapshot = new ArrayList<Contact>(person.getContacts());
		
        Contact c = person.getContacts().remove(0);
        person.getContacts().add(2, c);
        c = person.getContacts().remove(1);
        person.getContacts().add(0, c);
		
		ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count", 1, changeSet.size());
		CollectionChanges collChanges = changeSet.getChange(0).getCollectionChange("contacts");
		
        TestDataUtils.checkListChangeSet(entityManager, serverSession.getAliasRegistry(), person.getContacts(), collChanges, collSnapshot);
	}
	
	
	@Test
	public void testChangeSetEntityCollectionSort2() {
		Person person = new Person(1L, 0L, "P1", null, null);
		Contact contact1 = new Contact(1L, 0L, "C1", null);
		person.addContact(contact1);
		Contact contact2 = new Contact(2L, 0L, "C2", null);
		person.addContact(contact2);
		Contact contact3 = new Contact(3L, 0L, "C3", null);
		person.addContact(contact3);
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();

        List<Contact> collSnapshot = new ArrayList<Contact>(person.getContacts());
        
        Contact contact4 = new Contact(4L, 0L, "C4", person, null);
        person.addContact(contact4);
		
		Contact c = person.getContacts().remove(2);
		person.getContacts().add(1, c);
		c = person.getContacts().remove(1);
		person.getContacts().add(0, c);
		
		ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count", 1, changeSet.size());
		CollectionChanges collChanges = changeSet.getChange(0).getCollectionChange("contacts");
		
        TestDataUtils.checkListChangeSet(entityManager, serverSession.getAliasRegistry(), person.getContacts(), collChanges, collSnapshot);
	}
	
    @Test
    public void testChangeSetEntity2() {
    	Person9 person = new Person9(1L, 0L, "P1", null, null);
    	SimpleEntity key = new SimpleEntity(1L, 0L, "K1", null);
    	Contact3 value = new Contact3(1L, 0L, "V1", null);
    	person.getTestMap().put(key, value);
    	
    	person = (Person9)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet empty", 0, changeSet.size());
		
    	SimpleEntity key2 = new SimpleEntity(2L, 0L, "K2", null);
		key2 = (SimpleEntity)entityManager.mergeExternalData(key2);
		
    	Contact3 value2 = new Contact3(null, null, "V2", "V2");
		person.getTestMap().put(key2, value2);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count", 1, changeSet.size());
		CollectionChanges coll = changeSet.getChange(0).getCollectionChange("testMap");
		Assert.assertEquals("ChangeSet collection", 1, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(0));
		Assert.assertTrue("ChangeSet collection key", coll.getChangeKey(0) instanceof ChangeRef);
		Assert.assertEquals("ChangeSet collection key", key2.getUid(), coll.getChangeKey(0, ChangeRef.class).getUid());
		Assert.assertEquals("ChangeSet collection value", value2.getUid(), coll.getChangeValue(0, Contact3.class).getUid());
		
    	Contact3 value3 = new Contact3(3L, 0L, "V3", "V3");
		value3 = (Contact3)entityManager.mergeExternalData(value3);
		person.getTestMap().put(key2, value3);
					
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count", 1, changeSet.size());
		coll = changeSet.getChange(0).getCollectionChange("testMap");
		Assert.assertEquals("ChangeSet collection", 1, coll.size());
		Assert.assertEquals("ChangeSet collection type", 1, coll.getChangeType(0));
		Assert.assertTrue("ChangeSet collection key", coll.getChangeKey(0) instanceof ChangeRef);
		Assert.assertEquals("ChangeSet collection key", key2.getUid(), coll.getChangeKey(0, ChangeRef.class).getUid());
		Assert.assertTrue("ChangeSet collection value", coll.getChangeValue(0) instanceof ChangeRef);
		Assert.assertEquals("ChangeSet collection value", value3.getUid(), coll.getChangeValue(0, ChangeRef.class).getUid());
	}
    
    @Test
    public void testChangeSetEntity3() {
    	PersonEmbed person = new PersonEmbed(1L, 0L, "P1", null, null);
		person.setAddress(new EmbeddedAddress());
    	
    	person = (PersonEmbed)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet empty", 0, changeSet.size());
		
		person.setSalutation(Salutation.Mr);
		person.getAddress().setAddress("1 rue des Bleuets");
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 1", 1, changeSet.size());
		Assert.assertEquals("ChangeSet salutation value", Salutation.Mr, changeSet.getChange(0).getChange("salutation"));
		Assert.assertEquals("ChangeSet address value", "1 rue des Bleuets", changeSet.getChange(0).getChange("address", EmbeddedAddress.class).getAddress());
	}

    @Test
    public void testChangeSetEntity4() {
    	PersonEmbed person = new PersonEmbed(1L, 0L, "P1", null, null);
    	person.setSalutation(Salutation.Dr);
    	person.setAddress(new EmbeddedAddress());
    	
    	person = (PersonEmbed)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet empty", 0, changeSet.size());
		
		person.setLastName("zozo");
		
		Contact contact = new Contact(null, null, null, "zozo@zozo.net");
		entityManager.attach(contact);
		person.getContacts().add(contact);
		
		changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet count 1", 1, changeSet.size());
		Assert.assertEquals("ChangeSet lastName value", "zozo", changeSet.getChange(0).getChange("lastName"));
		Assert.assertEquals("ChangeSet contacts value", "zozo@zozo.net", changeSet.getChange(0).getCollectionChange("contacts").getChangeValue(0, Contact.class).getEmail());
	}
	
	
	@Test
	public void testChangeSetEntity5() {
		Patient3 patient = new Patient3(1L, 0L, "P1", null);
		Visit visit = new Visit(1L, 0L, "V1", patient, null);
		patient.getVisits().add(visit);
    	
    	patient = (Patient3)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();
    	
    	VisitTest test = new VisitTest(null, null, null, patient, visit, null);
		VisitObservation vo1 = new VisitObservation(null, null, null, test, null);
		VisitObservation vo2 = new VisitObservation(null, null, null, test, null);
		test.getObservations().add(vo1);
		test.getObservations().add(vo2);
		visit.getTests().add(test);
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		
		Assert.assertEquals("ChangeSet 1", 1, changeSet.size());
		
		changeSet = (ChangeSet)entityManager.mergeExternalData(changeSet);
		
		VisitTest t = changeSet.getChange(0).getCollectionChange("tests").getChangeValue(0, VisitTest.class);
		
		Assert.assertEquals("ChangeSet count 1", 1, changeSet.size());
		Assert.assertSame("ChangeSet test", t, t.getObservations().get(0).getTest());
	}
	
	@Test
	public void testChangeSetEntity6() {
		Patient5 patient = new Patient5(1L, 0L, "P1", null);
    	
    	patient = (Patient5)entityManager.mergeExternalData(patient);
    	entityManager.clearCache();
		
    	Consent consent = new Consent(null, null, "CO1", patient, null);
    	patient.getConsents().add(consent);
    	
    	DocumentList documentList = new DocumentList();
    	consent.setDocumentList(documentList);
    	Document document = new Document();
    	document.getDocumentLists().add(documentList);
    	document.setPayload(new DocumentPayload());
    	documentList.getDocuments().add(document);
		
    	ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		@SuppressWarnings("unchecked")
		UnsafePersistentCollection<PersistentCollection> documents = (UnsafePersistentCollection<PersistentCollection>)changeSet.getChange(0).getCollectionChange("consents").getChangeValue(0, Consent.class).getDocumentList().getDocuments();
		Assert.assertNotSame("Different set", documentList.getDocuments(), documents.internalPersistentCollection());
		
		Assert.assertEquals("ChangeSet 1", 1, changeSet.size());
		
		Assert.assertTrue("Document list initialized", ((PersistentCollection)documentList.getDocuments()).wasInitialized());
	}
    
    @Test
    public void testChangeSetEntityAssociation() {
		Person person = new Person(1L, 0L, "P1", null, null);
		Contact contact = new Contact(1L, 0L, "C1", person, null);
		person.getContacts().add(contact);
		Contact contact2 = new Contact(2L, 0L, "C2", person, null);
		person.getContacts().add(contact2);
    	
    	person = (Person)entityManager.mergeExternalData(person);
    	entityManager.clearCache();
		
    	person.setLastName("Zozo");
		contact.setEmail("zozo@zozo.net");
    	
    	Assert.assertTrue("Person dirty", dataManager.isDirtyEntity(contact.getPerson()));
		
		ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		Assert.assertEquals("Changes", 2, changeSet.size());
		Change change = null;
		for (Change ch : changeSet.getChanges()) {
			if (ch.getClassName().equals(contactAlias)) {
				change = ch;
				break;
			}
		}
		int count = 0;
		for (String c : change.getChanges().keySet()) {
			if (!c.equals("version"))
				count++;
		}
		Assert.assertEquals("Person change", 1, count);
    }
	
	@Test
	public void testChangeSetEntityProxy() {		
		Classification cl2 = new Classification(2L, 0L, "CL2", null);
		dataManager.initProxy(cl2, 2L, true, "CL2DS");
    	
    	cl2 = (Classification)entityManager.mergeExternalData(cl2);
    	entityManager.clearCache();
		
		Classification cl1 = new Classification(null, null, "CL1", null);
		cl1.getSubclasses().add(cl2);
		cl2.getSuperclasses().add(cl1);
		
		ChangeSet changeSet = new ChangeSetBuilder(entityManager, serverSession).buildChangeSet();
		Assert.assertEquals("Changes", 1, changeSet.size());
		CollectionChange cc = changeSet.getChange(0).getCollectionChange("superclasses").getChange(0);
		Assert.assertEquals("Add", 1, cc.getType());
		Assert.assertTrue("Classification", cc.getValue() instanceof Classification);
		Assert.assertEquals("Classification uid", "CL1", ((Classification)cc.getValue()).getUid());
		Classification cl = ((Classification)cc.getValue()).getSubclasses().get(0);
		Assert.assertFalse("Child class uninitialized", dataManager.isInitialized(cl));
		Assert.assertEquals("Child class detached state", "CL2DS", dataManager.getDetachedState(cl));
	}
}
