/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.granite.client.javafx.tide.JavaFXApplication;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextManager;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.ServerSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("unchecked")
public class TestResetEntity {

    private ContextManager contextManager;
    private Context ctx;
	private DataManager dataManager;
    private EntityManager entityManager;
    private ServerSession serverSession;
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXApplication());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext("");
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
        serverSession = new ServerSession();
        ctx.set(serverSession);
    }
    
    @Test
    public void testResetEntityGDS920() {
        Person person = new Person(1L, 0L, "P1", null, null);
        Contact contact = new Contact(1L, 0L, "C1", null);
        contact.setPerson(person);
        person.getContacts().add(contact);

        Person person2 = new Person(2L, 0L, "P2", null, null);
        Contact contact2 = new Contact(2L, 0L, "C2", null);
        contact2.setPerson(person2);
        person2.getContacts().add(contact2);
        
        List<Person> p = (List<Person>)entityManager.mergeExternalData(Arrays.asList(person, person2));
        person = p.get(0);
        person2 = p.get(1);

        person.setLastName("test");
        person2.getContacts().remove(0);
        
        Assert.assertTrue("Person dirty", dataManager.isDirtyEntity(person));
        Assert.assertTrue("Person2 dirty", dataManager.isDirtyEntity(person2));
         
        entityManager.resetAllEntities();
         
        Assert.assertFalse("Person not dirty", dataManager.isDirtyEntity(person));
        Assert.assertFalse("Person2 not dirty", dataManager.isDirtyEntity(person2));
        Assert.assertFalse("Context not dirty", dataManager.isDirty());
    }
    
    @Test
    public void testResetEntityBigNumber() {
        PersonBigNum person = new PersonBigNum(1L, 0L, "P1", null, null);
        person.setBigInt(BigInteger.valueOf(100L));
        person.getBigInts().add(BigInteger.valueOf(200L));
        person = (PersonBigNum)entityManager.mergeExternalData(person);
        
        person.setBigInt(null);
        entityManager.resetEntity(person);
         
        Assert.assertEquals("Person reset", BigInteger.valueOf(100L), person.getBigInt());
        
        person.setBigInt(BigInteger.valueOf(300L));
        entityManager.resetEntity(person);
         
        Assert.assertEquals("Person reset 2", BigInteger.valueOf(100L), person.getBigInt());
         
        person.getBigInts().set(0, BigInteger.valueOf(300L));
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Person reset coll", 1, person.getBigInts().size());
        Assert.assertEquals("Person reset coll", BigInteger.valueOf(200L), person.getBigInts().get(0));
    }
    
    @Test
    public void testResetEntityEnum() {
        PersonEnum person = new PersonEnum(1L, 0L, "P1", null, null);
        person.setSalutation(Salutation.Mr);
        person.getSalutations().addAll(Salutation.Dr);
        person = (PersonEnum)entityManager.mergeExternalData(person);
        
        person.setSalutation(null);
        entityManager.resetEntity(person);
         
        Assert.assertSame("Person reset", Salutation.Mr, person.getSalutation());
         
        person.setSalutation(Salutation.Dr);
        entityManager.resetEntity(person);
         
        Assert.assertSame("Person reset 2", Salutation.Mr, person.getSalutation());
         
        person.getSalutations().remove(0);
        person.getSalutations().add(Salutation.Mr);
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Person reset coll", 1, person.getSalutations().size());
        Assert.assertSame("Person reset coll", Salutation.Dr, person.getSalutations().get(0));
        
        person.getSalutations().set(0, Salutation.Ms);
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Person reset coll", 1, person.getSalutations().size());
        Assert.assertSame("Person reset coll", Salutation.Dr, person.getSalutations().get(0));
    }
    
    @Test
    public void testResetEntityGDS453() {
        Person person = new Person(1L, 0L, "P1", null, null);
        Contact contact = new Contact(1L, 0L, "C1", null);
        contact.setPerson(person);
        contact = (Contact)entityManager.mergeExternalData(contact);
        serverSession.handleResult(ctx, null, null, null, null, null);
        
        contact.setPerson(new Person());
        entityManager.resetEntity(contact);
         
        Assert.assertSame("Entity reset", person, contact.getPerson());
         
        Person p = new Person(2L, 0L, "P2", null, null);
        Contact c = new Contact(2L, 0L, "C2", null);
        c.setPerson(p);
        p.getContacts().add(c);
        person = (Person)entityManager.mergeExternalData(p);
        serverSession.handleResult(ctx, null, null, null, null, null);
        
        person.getContacts().remove(0);
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Person contact collection restored", 1, person.getContacts().size());
        Assert.assertSame("Person contact restored", c, person.getContacts().get(0));
    }
    
    @Test
    public void testResetEntityGDS667() {
        Person p = new Person(1L, 0L, "P1", null, null);
        Contact c = new Contact(1L, 0L, "C1", null);
        c.setPerson(p);
        p.getContacts().add(c);
        Person person = (Person)entityManager.mergeExternalData(p);
        
        person.getContacts().clear();
         
        entityManager.resetEntity(person);
         
        Assert.assertEquals("Person contact collection restored", 1, person.getContacts().size());
        Assert.assertSame("Person contact restored", c, person.getContacts().get(0));
    }
    
    @Test
    public void testResetEntityGDS668() {
        Person person = new Person(1L, 0L, "P1", null, null);
        Contact contact = new Contact(1L, 0L, "C1", null);
        contact.setPerson(person);
        contact = (Contact)entityManager.mergeExternalData(contact);
        
        contact.setPerson(null);
        entityManager.resetEntity(contact);
        
        Assert.assertSame("Entity reset", person, contact.getPerson());
    }
    
    @Test
    public void testResetEntityMap() {
    	PersonMap person = new PersonMap(1L, 0L, "P1", null, "Toto");
        person = (PersonMap)entityManager.mergeExternalData(person);
        
        person.getMapSimple().put(2, "toto");
        
        Assert.assertTrue("Person dirty", dataManager.isDirty());
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Person map reset", 0, person.getMapSimple().size());
    }
    
    @Test
    public void testResetEntityMap2() {
        PersonMap person = new PersonMap(1L, 0L, "P1", null, "Toto");
        person.getMapSimple().put(2, "toto");
        person = (PersonMap)entityManager.mergeExternalData(person);
         
        person.getMapSimple().put(2, "tutu");
         
        Assert.assertTrue("Person dirty", entityManager.isDirty());
         
        entityManager.resetEntity(person);
         
        Assert.assertEquals("Person map reset", 1, person.getMapSimple().size());
        Assert.assertEquals("Person map value", "toto", person.getMapSimple().get(2));
    }
     
    @Test
    public void testResetEntityMap3() {
        PersonMap person = new PersonMap(1L, 0L, "P1", null, "Toto");
        SimpleEntity value = new SimpleEntity(null, null, "E1", "toto");
        person.getMapEntity().put("2", value);
        person = (PersonMap)entityManager.mergeExternalData(person);
        value = person.getMapEntity().get("2");
         
        value.setName("tutu");
         
        Assert.assertTrue("Person dirty", entityManager.isDirty());
         
        entityManager.resetEntity(person);
         
        Assert.assertEquals("Person map reset", 1, person.getMapEntity().size());
        Assert.assertEquals("Person map value", "toto", person.getMapEntity().get("2").getName());
    }
    
    @Test
    public void testResetEntityMap4() {
        PersonMap person = new PersonMap(1L, 0L, "P1", null, "Toto");
        SimpleEntity value = new SimpleEntity(null, null, "E1", "toto");
        person.getMapEntity().put("2", value);
        person = (PersonMap)entityManager.mergeExternalData(person);
     
        person.getMapEntity().put("2", new SimpleEntity(null, null, "E2", "tutu"));
        person.getMapEntity().put("3", new SimpleEntity(null, null, "E3", "tata"));
         
        Assert.assertTrue("Person dirty", entityManager.isDirty());
        Assert.assertEquals("Person map reset", 2, person.getMapEntity().size());
        Assert.assertEquals("Person map value", "tutu", person.getMapEntity().get("2").getName());
        
        entityManager.resetEntity(person);
         
        Assert.assertEquals("Person map reset", 1, person.getMapEntity().size());
        Assert.assertEquals("Person map value", "toto", person.getMapEntity().get("2").getName());
    }
     
    @Test
    public void testResetEntityMap5() {
        PersonMap person = new PersonMap(1L, 0L, "P1", null, "Toto");
        SimpleEntity value = new SimpleEntity(null, null, "E1", "toto");
        person.getMapEntity().put("2", value);
        person = (PersonMap)entityManager.mergeExternalData(person);
        value = person.getMapEntity().get("2");
         
        person.getMapEntity().put("2", new SimpleEntity(null, null, "E2", "tutu"));
         
        Assert.assertTrue("Person dirty", entityManager.isDirty());
        Assert.assertEquals("Person map reset", 1, person.getMapEntity().size());
        Assert.assertEquals("Person map value", "tutu", person.getMapEntity().get("2").getName());
         
        person.getMapEntity().put("2", new SimpleEntity(null, null, "E3", "tata"));
        person.getMapEntity().put("2", value);
        
        Assert.assertFalse("Person dirty", entityManager.isDirty());
        
        person.getMapEntity().put("2", new SimpleEntity(null, null, "E4", "tata"));
        Assert.assertTrue("Person dirty", entityManager.isDirty());
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Person map value", "toto", person.getMapEntity().get("2").getName());
        Assert.assertFalse("Person dirty", entityManager.isDirty());
    }
    
}
