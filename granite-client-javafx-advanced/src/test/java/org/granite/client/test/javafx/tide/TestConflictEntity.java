/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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

import java.util.Arrays;

import org.granite.client.javafx.tide.JavaFXApplication;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.Conflicts;
import org.granite.client.tide.data.DataConflictListener;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.Update;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.impl.SimpleContextManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestConflictEntity {

    private SimpleContextManager contextManager;
    private Context ctx;
    @SuppressWarnings("unused")
	private DataManager dataManager;
    private EntityManager entityManager;
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXApplication());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext("");
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
    }
    
    @Test
    public void testEntityCollectionRefs() {
        Person p = new Person(1L, 0L, "P01", null, null);
        Contact c1 = new Contact(1L, 0L, "C01", null);
        c1.setPerson(p);
        p.getContacts().add(c1);
        p = (Person)entityManager.mergeExternalData(p);

        Person np = new Person(1L, 0L, "P01", null, null);
        Contact nc = new Contact(1L, 0L, "C01", null);
        nc.setPerson(np);

        MergeContext mergeContext = entityManager.initMerge(null);
        entityManager.handleUpdates(mergeContext, "SID", Arrays.asList(new Update(UpdateKind.REMOVE, nc)));

        Assert.assertEquals("Person contacts empty", 0, p.getContacts().size());
    }

    @Test
    public void testEntityCollectionMultiRefs() {
        PersonUniDir p1 = new PersonUniDir(1L, 0L, "P01", null, null);
        Contact2 c1 = new Contact2(1L, 0L, "C01", null);
        p1.getContacts().add(c1);
        p1 = (PersonUniDir)entityManager.mergeExternalData(p1);
        
        PersonUniDir p2 = new PersonUniDir(2L, 0L, "P02", null, null);
        Contact2 c1b = new Contact2(1L, 0L, "C01", null);
        p2.getContacts().add(c1b);
        Contact2 c2b = new Contact2(2L, 0L, "C02", null);
        p2.getContacts().add(c2b);
        p2 = (PersonUniDir)entityManager.mergeExternalData(p2);

        Contact2 nc = new Contact2(1L, 0L, "C01", null);

        MergeContext mergeContext = entityManager.initMerge(null);
        entityManager.handleUpdates(mergeContext, "SID", Arrays.asList(new Update(UpdateKind.REMOVE, nc)));

        Assert.assertEquals("Person 1 contacts empty", 0, p1.getContacts().size());
        Assert.assertEquals("Person 2 one contact left", 1, p2.getContacts().size());
    }


     @Test
     public void testEntityCollectionRemoveConflictServer() {
         Person p = new Person(1L, 0L, "P01", null, null);
         Contact c1 = new Contact(1L, 0L, "C01", p, null);
         p.addContact(c1);
         p = (Person)entityManager.mergeExternalData(p);

         // Contact is locally modified
         c1.setEmail("toto@toto.org");

         Person np = new Person(1L, 0L, "P01", null, null);
         Contact nc = new Contact(1L, 0L, "C01", np, null);
         
         final Conflicts[] conflicts = new Conflicts[1];

         entityManager.addListener(new DataConflictListener() {           
             @Override
             public void onConflict(EntityManager em, Conflicts cs) {
                 conflicts[0] = cs;
             }
         });
         
         // Receive an external removal event
         MergeContext mergeContext = entityManager.initMerge(null);
         entityManager.handleUpdates(mergeContext, "SID", Arrays.asList(new Update(UpdateKind.REMOVE, nc)));

         Assert.assertEquals("Conflict detected", 1, conflicts[0].getConflicts().size());

         conflicts[0].acceptAllServer();

         Assert.assertEquals("Person contacts empty", 0, p.getContacts().size());
     }

     @Test
     public void testEntityCollectionRemoveConflictClient() {
         Person p = new Person(1L, 0L, "P01", null, null);
         Contact c1 = new Contact(1L, 0L, "C01", p, null);
         p.addContact(c1);
         p = (Person)entityManager.mergeExternalData(p);

         // Contact is locally modified
         c1.setEmail("toto@toto.org");

         Person np = new Person(1L, 0L, "P01", null, null);
         Contact nc = new Contact(1L, 0L, "C01", np, null);
         
         final Conflicts[] conflicts = new Conflicts[1];

         entityManager.addListener(new DataConflictListener() {           
             @Override
             public void onConflict(EntityManager em, Conflicts cs) {
                 conflicts[0] = cs;
             }
         });
         
         // Receive an external removal event
         MergeContext mergeContext = entityManager.initMerge(null);
         entityManager.handleUpdates(mergeContext, "SID", Arrays.asList(new Update(UpdateKind.REMOVE, nc)));

         Assert.assertEquals("Conflict detected", 1, conflicts[0].getConflicts().size());

         conflicts[0].acceptAllClient();

         Assert.assertEquals("Person contacts not empty", 1, p.getContacts().size());
     }
     
     @Test
     public void testMergeConflictEntity() {
     	Person person = new Person(1L, 0L, "P01", null, null);
     	Contact contact = new Contact(1L, 0L, "C01", person, null);
     	person.addContact(contact);
     	person = (Person)entityManager.mergeExternalData(person);
     	
     	person.setLastName("toto");
     	Contact addedContact = new Contact(null, null, "C02", person, "test");
     	person.addContact(addedContact);
     	
     	Assert.assertTrue("Person dirty", entityManager.isDirtyEntity(person));
     	
     	Person person2 = new Person(1L, 1L, "P01", null, "tutu");
     	Contact contact2 = new Contact(1L, 1L, "C01", person2, "test2");
     	person2.addContact(contact2);
     	
        final Conflicts[] conflicts = new Conflicts[1];

        entityManager.addListener(new DataConflictListener() {           
            @Override
            public void onConflict(EntityManager em, Conflicts cs) {
                conflicts[0] = cs;
            }
        });
     	
     	entityManager.mergeExternalData(person2, null, "S2", null, null);
     	
     	Assert.assertNotNull("Conflicts after merge", conflicts[0]);
		Assert.assertTrue("Person still dirty after merge", entityManager.isDirtyEntity(person));
     	
     	conflicts[0].acceptAllClient();
     	conflicts[0] = null;
     	
     	Assert.assertEquals("Person last name", "toto", person.getLastName());
     	Assert.assertEquals("Person contacts", 2, person.getContacts().size());
     	Assert.assertEquals("Person version", 1L, person.getVersion().longValue());
     	Assert.assertTrue("Person dirty", entityManager.isDirtyEntity(person));
     	
     	entityManager.resetEntity(person);
     	
     	Assert.assertEquals("Person last name after cancel", "tutu", person.getLastName());
     	Assert.assertEquals("Person contacts after cancel", 1, person.getContacts().size());
     	
     	person.setLastName("toto");
     	
     	Person person3 = new Person(1L, 2L, "P01", null, "titi");
     	Contact contact3 = new Contact(1L, 1L, "C01", person3, "test2");
     	person3.addContact(contact3);
     	Contact contact3b = new Contact(2L, 0L, "C02", person3, "test3");
     	person3.addContact(contact3b);
     	
     	entityManager.mergeExternalData(person3, null, "S2", null, null);
     	
     	Assert.assertNotNull("Conflicts after merge 2", conflicts[0]);
     	
     	conflicts[0].acceptAllServer();
     	
     	Assert.assertEquals("Person last name", "titi", person.getLastName());
     	Assert.assertEquals("Person version", 2L, person.getVersion().longValue());
     	Assert.assertEquals("Person contacts", 2, person.getContacts().size());
     	Assert.assertFalse("Person not dirty", entityManager.isDirtyEntity(person));
     }
	
	@Test
	public void testMergeConflictEntity2() {
		Person person = new Person(1L, 0L, "P01", null, null);
		Contact contact = new Contact(1L, 0L, "C01", person, null);
		person.addContact(contact);
		person = (Person)entityManager.mergeExternalData(person);
		
		person.setLastName("toto");
		Contact addedContact = new Contact(null, null, "C02", person, null);
		person.addContact(addedContact);
		contact.setEmail("test@test.com");
		
		Assert.assertTrue("Person dirty", entityManager.isDirtyEntity(person));
		
		Person person2 = new Person(person.getId(), 1L, person.getUid(), null, "toto");
		Contact contact2a = new Contact(contact.getId(), 0L, contact.getUid(), person2, null);
		person2.addContact(contact2a);
		Contact contact2b = new Contact(addedContact.getId(), 0L, addedContact.getUid(), person2, null);
		person2.addContact(contact2b);
		
        final Conflicts[] conflicts = new Conflicts[1];

        entityManager.addListener(new DataConflictListener() {           
            @Override
            public void onConflict(EntityManager em, Conflicts cs) {
                conflicts[0] = cs;
            }
        });
		
		entityManager.mergeExternalData(person2, null, "S2", null, null);
		
		Assert.assertNull("No conflict after merge", conflicts[0]);
		Assert.assertFalse("Person not dirty after merge", entityManager.isDirtyEntity(person));
	}
		
	@Test
	public void testMergeConflictEntity3() {
		Person person = new Person(1L, 0L, "P01", "toto", "toto");
		Contact contact = new Contact(1L, 0L, "C01", person, null);
		person.getContacts().add(contact);
		person = (Person)entityManager.mergeExternalData(person);
		
		person.setLastName("tutu");
		
		Assert.assertTrue("Person dirty", entityManager.isDirtyEntity(person));
		
		Person person2 = new Person(person.getId(), 1L, person.getUid(), "tutu", "toto");
		Contact contact2 = new Contact(contact.getId(), 0L, contact.getUid(), person2, null);
		person2.addContact(contact2);
		
        entityManager.addListener(new DataConflictListener() {           
            @Override
            public void onConflict(EntityManager em, Conflicts cs) {
                cs.acceptAllServer();
            }
        });
		
		entityManager.mergeExternalData(new Object[] { person2 }, null, "S2", null, null);

		Assert.assertFalse("Person dirty after merge", entityManager.isDirtyEntity(person));
		Assert.assertEquals("Person firstName", "tutu", person.getFirstName());
		Assert.assertEquals("Person lastName", "toto", person.getLastName());
	}

     @Test
     public void testMergeConflictEntity4() {
    	 Person person = new Person(1L, 0L, "P01", "toto", "toto");
    	 Contact contact = new Contact(1L, 0L, "C01", person, null);
    	 person.addContact(contact);
         person = (Person)entityManager.mergeExternalData(person);
         contact = person.getContact(0);

         contact.setEmail("test@test.com");

         Assert.assertTrue("Person dirty", entityManager.isDirtyEntity(contact));

         Person person2 = new Person(person.getId(), 1L, person.getUid(), "toto", "toto");
         Contact contact2 = new Contact(contact.getId(), 1L, contact.getUid(), person2, "toto@toto.net");
         person2.addContact(contact2);

         entityManager.addListener(new DataConflictListener() {           
             @Override
             public void onConflict(EntityManager em, Conflicts cs) {
            	 cs.acceptAllServer();
             }
         });

         entityManager.mergeExternalData(new Object[] { person2 }, null, "S2", null, null);

         Assert.assertFalse("Contact not dirty after merge", entityManager.isDirtyEntity(contact));
         Assert.assertEquals("Contact email", "toto@toto.net", contact.getEmail());
     }

	@Test
	public void testMergeConflictEntity5() {
		Person person = new Person(1L, 0L, "P01", "toto", "toto");
		Contact contact = new Contact(1L, 0L, "C01", person, null);
		person.addContact(contact);
		person = (Person)entityManager.mergeExternalData(person);
		
		person.setLastName("tutu");
		
		Assert.assertTrue("Person dirty", entityManager.isDirtyEntity(person));
		
		Person person2 = new Person(person.getId(), 1L, person.getUid(), "tata", "tata");
		Contact contact2 = new Contact(contact.getId(), 0L, contact.getUid(), person2, null);
		person2.addContact(contact2);
		
		entityManager.mergeExternalData(new Object[] { person2 }, null, "S2", null, null);
		
		Assert.assertTrue("Person dirty after merge", entityManager.isDirtyEntity(person));
		
		Assert.assertEquals("Person firstName", "toto", person.getFirstName());
		Assert.assertEquals("Person lastName", "tutu", person.getLastName());
		
		entityManager.resetEntity(person);
		
		Assert.assertEquals("Person firstName", "tata", person.getFirstName());
		Assert.assertEquals("Person lastName", "tata", person.getLastName());
	}
	
	@Test
	public void testMergeConflictEntity6() {
		Person person = new Person(1L, 0L, "P1", "toto", "toto");
		Contact contact = new Contact(1L, 0L, "C1", person, null);
		person.addContact(contact);
		person = (Person)entityManager.mergeExternalData(person);
		contact = person.getContact(0);
		
		Contact contact2 = new Contact(null, null, "C2", person, null);
		person.addContact(contact2);
		
		Assert.assertTrue("Person dirty", entityManager.isDirtyEntity(person));
		
		Person personb = new Person(person.getId(), 1L, person.getUid(), "toto", "toto");
		Contact contactb = new Contact(3L, 0L, "C3", personb, "toto@toto.net");
		personb.addContact(contactb);
		
        final Conflicts[] conflicts = new Conflicts[1];

        entityManager.addListener(new DataConflictListener() {           
            @Override
            public void onConflict(EntityManager em, Conflicts cs) {
                conflicts[0] = cs;
            }
        });
		
		entityManager.mergeExternalData(new Object[] { personb }, null, "S2", null, null);
		
		Assert.assertTrue("Person dirty after merge", entityManager.isDirtyEntity(person));
		Assert.assertNotNull("Conflict", conflicts[0]);
		
		Assert.assertEquals("Contacts count", 2, person.getContacts().size());
		
		entityManager.resetEntity(person);
		
		Assert.assertEquals("Contacts count", 1, person.getContacts().size());
		Assert.assertEquals("Contact", 3L, person.getContact(0).getId().longValue());
	}
    
    @Test
    public void testMergeConflictEntitySet() {
       PersonSet person = new PersonSet(1L, 0L, "P01", null, null);
       ContactSet contact = new ContactSet(1L, 0L, "C01", person, null);
       person.addContact(contact);
       person = (PersonSet)entityManager.mergeExternalData(person);
       
       person.setLastName("toto");
       ContactSet addedContactSet = new ContactSet(null, null, "C02", person, "test");
       person.addContact(addedContactSet);
       
       Assert.assertTrue("PersonSet dirty", entityManager.isDirtyEntity(person));
       
       PersonSet person2 = new PersonSet(1L, 1L, "P01", null, "tutu");
       ContactSet contact2 = new ContactSet(1L, 1L, "C01", person2, "test2");
       person2.addContact(contact2);
       
       final Conflicts[] conflicts = new Conflicts[1];

       entityManager.addListener(new DataConflictListener() {           
           @Override
           public void onConflict(EntityManager em, Conflicts cs) {
               conflicts[0] = cs;
           }
       });
       
       entityManager.mergeExternalData(person2, null, "S2", null, null);
       
       Assert.assertNotNull("Conflicts after merge", conflicts[0]);
       Assert.assertTrue("PersonSet still dirty after merge", entityManager.isDirtyEntity(person));
       
       conflicts[0].acceptAllClient();
       conflicts[0] = null;
       
       Assert.assertEquals("PersonSet last name", "toto", person.getLastName());
       Assert.assertEquals("PersonSet contacts", 2, person.getContacts().size());
       Assert.assertEquals("PersonSet version", 1L, person.getVersion().longValue());
       Assert.assertTrue("PersonSet dirty", entityManager.isDirtyEntity(person));
       
       entityManager.resetEntity(person);
       
       Assert.assertEquals("PersonSet last name after cancel", "tutu", person.getLastName());
       Assert.assertEquals("PersonSet contacts after cancel", 1, person.getContacts().size());
       
       person.setLastName("toto");
       
       PersonSet person3 = new PersonSet(1L, 2L, "P01", null, "titi");
       ContactSet contact3 = new ContactSet(1L, 1L, "C01", person3, "test2");
       person3.addContact(contact3);
       ContactSet contact3b = new ContactSet(2L, 0L, "C02", person3, "test3");
       person3.addContact(contact3b);
       
       entityManager.mergeExternalData(person3, null, "S2", null, null);
       
       Assert.assertNotNull("Conflicts after merge 2", conflicts[0]);
       
       conflicts[0].acceptAllServer();
       
       Assert.assertEquals("PersonSet last name", "titi", person.getLastName());
       Assert.assertEquals("PersonSet version", 2L, person.getVersion().longValue());
       Assert.assertEquals("PersonSet contacts", 2, person.getContacts().size());
       Assert.assertFalse("PersonSet not dirty", entityManager.isDirtyEntity(person));
    }
}
