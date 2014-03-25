/**
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
package org.granite.test.tide.hibernate4.data;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.granite.hibernate4.ProxyFactory;
import org.granite.test.tide.data.Address;
import org.granite.test.tide.data.Classification;
import org.granite.test.tide.data.Contact2;
import org.granite.test.tide.data.LineItemList2;
import org.granite.test.tide.data.Medication;
import org.granite.test.tide.data.Order2;
import org.granite.test.tide.data.Patient;
import org.granite.test.tide.data.Person2;
import org.granite.test.tide.data.Phone2;
import org.granite.test.tide.data.Prescription;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.ChangeSetApplier;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.CollectionChanges;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.granite.tide.data.TidePersistenceAdapter;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractTestChangeSetApplier {
	
	protected abstract void initPersistence();
	
	protected abstract void open();
	protected abstract <T> T find(Class<T> entityClass, Serializable id);
	protected abstract <T> T save(T entity);
	protected abstract void flush();
	protected abstract void close();
	protected abstract TidePersistenceAdapter newPersistenceAdapter();
	protected abstract <T> T newProxy(Class<T> entityClass, Serializable id);

    private static final String GRANITE_CONFIG_PATH = "org/granite/test/tide/hibernate4/data/granite-config-hibernate4-changeset.xml";
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChanges() throws Exception {
		initPersistence();
		
		Person1 p = new Person1(null, null, "P1");
		p.setFirstName("test");
		p.setLastName("test");
		open();
		p = save(p);
		flush();
		Long personId = p.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		ChangeSet changeSet = new ChangeSet();
		Change change = new Change(Person1.class.getName(), personId, 0L, "P1");
		change.getChanges().put("firstName", "zozo");
		changeSet.setChanges(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		p = find(Person1.class, personId);
		Assert.assertEquals("Property applied", "zozo", p.getFirstName());
		close();
		
		open();
		
		Person1 p0 = new Person1(personId, p.getVersion(), "P1");
		p0.setFirstName("zozo");
		p0.setLastName("test");
		p0.setContacts(new PersistentSet(null, new HashSet<Contact1>()));
		Address a0 = new Address(null, null, "A1");
		a0.setCity("New York City");
		Contact1 c0 = new Contact1(null, null, "C1");
		c0.setPerson(p0);
		c0.setAddress(a0);
		c0.setEmail("zozo@zozo.net");
		p0.getContacts().add(c0);
		
		ChangeSet changeSet2 = new ChangeSet();
		Change change2 = new Change(Person1.class.getName(), personId, p.getVersion(), "P1");
		CollectionChanges collChanges2 = new CollectionChanges();
		CollectionChange collChange2 = new CollectionChange(1, null, c0);
		collChanges2.setChanges(new CollectionChange[] { collChange2 });
		change2.getChanges().put("contacts", collChanges2);
		changeSet2.setChanges(new Change[] { change2 });
		
		Object[] changes = new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet2);
		Assert.assertEquals("Changes", 1, changes.length);
		Assert.assertTrue("Changed person", changes[0] instanceof Person1);
		Person1 pc = (Person1)changes[0];
		Assert.assertEquals("Changed person coll", 1, pc.getContacts().size());
		Assert.assertEquals("Changed person coll element", pc, pc.getContacts().iterator().next().getPerson());
		
		flush();		
		
		p = find(Person1.class, personId);
		
		Assert.assertEquals("Collection added", 1, p.getContacts().size());
		Contact1 contact = p.getContacts().iterator().next();
		Long contactId = contact.getId();
		Long addressId = contact.getAddress().getId();
		Assert.assertEquals("Element property", "zozo@zozo.net", contact.getEmail());
		close();
		
		open();
		
		Person1 p1 = new Person1(personId, p.getVersion(), "P1");
		p1.setFirstName("zozo");
		p1.setLastName("zozo");
		p1.setContacts(new PersistentSet(null, new HashSet<Contact1>()));
		Address a1 = new Address(addressId, 0L, "A1");
		a1.setCity("New York City");
		Contact1 c1 = new Contact1(contactId, 0L, "C1");
		c1.setPerson(p1);
		c1.setAddress(a1);
		c1.setEmail("zozo@zozo.net");
		p1.getContacts().add(c1);
		Address a2 = new Address(null, null, "A2");
		a2.setCity("Paris");
		Contact1 c2 = new Contact1(null, null, "C2");
		c2.setPerson(p1);
		c2.setAddress(a2);
		c2.setEmail("zozo@zozo.fr");
		p1.getContacts().add(c2);
		
		ChangeSet changeSet3 = new ChangeSet();
		Change change3 = new Change(Person1.class.getName(), personId, p.getVersion(), "P1");
		CollectionChanges collChanges3 = new CollectionChanges();
		CollectionChange collChange3 = new CollectionChange(1, null, c2);
		collChanges3.setChanges(new CollectionChange[] { collChange3 });
		change3.getChanges().put("contacts", collChanges3);
		change3.getChanges().put("lastName", "zozo");
		changeSet3.setChanges(new Change[] { change3 });
		
		Object[] changes2 = new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet3);
		Assert.assertEquals("Changes 2", 1, changes2.length);
		Assert.assertTrue("Changed person 2", changes2[0] instanceof Person1);
		Person1 pc2 = (Person1)changes2[0];
		Assert.assertEquals("Changed person 2 coll", 2, pc2.getContacts().size());
		for (Contact1 c : pc2.getContacts())
			Assert.assertEquals("Changed person 2 coll element", pc2, c.getPerson());
		
		flush();		
		
		p = find(Person1.class, personId);
		
		Assert.assertEquals("Person 2 property", "zozo", p.getLastName());
		Assert.assertEquals("Collection 2 added", 2, p.getContacts().size());
		close();
		
		open();
				
		Person1 p2 = newProxy(Person1.class, personId);
		Address a3 = new Address(null, null, "A3");
		a3.setCity("London");
		Contact1 c3 = new Contact1(null, null, "C3");
		c3.setPerson(p2);
		c3.setAddress(a3);
		c3.setEmail("zozo@zozo.co.uk");
		
		ChangeSet changeSet4 = new ChangeSet();
		Change change4 = new Change(Person1.class.getName(), personId, p.getVersion(), "P1");
		CollectionChanges collChanges4 = new CollectionChanges();
		CollectionChange collChange4 = new CollectionChange(1, null, c3);
		collChanges4.setChanges(new CollectionChange[] { collChange4 });
		change4.getChanges().put("contacts", collChanges4);
		changeSet4.setChanges(new Change[] { change4 });
		
		Object[] changes3 = new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet4);
		Assert.assertEquals("Changes 3", 1, changes3.length);
		Assert.assertTrue("Changed person 3", changes3[0] instanceof Person1);
		Person1 pc3 = (Person1)changes3[0];
		Assert.assertEquals("Changed person 3 coll", 3, pc3.getContacts().size());
		for (Contact1 c : pc3.getContacts())
			Assert.assertSame("Changed person 3 coll element", pc3, c.getPerson());
		
		flush();		
		
		p = find(Person1.class, personId);
		
		Assert.assertEquals("Person 3 property", "zozo", p.getLastName());
		Assert.assertEquals("Collection 3 added", 3, p.getContacts().size());
		for (Contact1 c : p.getContacts())
			Assert.assertSame("Associations", p, c.getPerson());
		close();
	}

    @Test
    public void testSimpleChangesGDS1232() throws Exception {
        initPersistence();

        Person1 p = new Person1(null, null, "P1");
        p.setFirstName("test");
        p.setLastName("test");
        p.setSalutation(Person1.Salutation.Mr);
        open();
        p = save(p);
        Contact1 c = new Contact1(null, null, "C1");
        c.setEmail("test@test.com");
        Address a = new Address(null, null, "A1");
        c.setAddress(a);
        c.setPerson(p);
        c = save(c);
        flush();
        Long personId = p.getId();
        Long contactId = c.getId();
        close();

        open();

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/data/enterprise/granite-config.xml");
        GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
        ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
        SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());

        ChangeSet changeSet = new ChangeSet();
        Change change = new Change(Person1.class.getName(), personId, 0L, "P1");
        // Force order of changes: null first
        Field f = Change.class.getDeclaredField("changes");
        f.setAccessible(true);
        f.set(change, new LinkedHashMap<String, Object>());
        change.getChanges().put("salutation", null);
        change.getChanges().put("mainContact", new ChangeRef(Contact1.class.getName(), "C1", contactId));
        changeSet.setChanges(new Change[] { change });

        new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
        flush();

        p = find(Person1.class, personId);
        c = find(Contact1.class, contactId);
        Assert.assertSame("Property applied", c, p.getMainContact());
        Assert.assertNull("Property applied", p.getSalutation());
        close();
    }

	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChangesList() throws Exception {
		initPersistence();
		
		open();
		
		Order2 o = new Order2(null, null, "O1");
		o.setDescription("zozo");
		o.setLineItemsList(new PersistentList(null, new ArrayList<LineItemList2>()));
		LineItemList2 i1 = new LineItemList2(null, null, "LI1");
		i1.setDescription("item 1");
		o.getLineItemsList().add(i1);

		open();
		o = save(o);
		flush();
		Long orderId = o.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		ChangeSet changeSet = new ChangeSet();
		Change change = new Change(Order2.class.getName(), orderId, o.getVersion(), "O1");
		LineItemList2 i2u = new LineItemList2(null, null, "LI2");
		i2u.setDescription("item 2");
		change.getChanges().put("lineItemsList", new CollectionChanges(new CollectionChange[] { new CollectionChange(1, 1, i2u) }));
		changeSet.setChanges(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		o = find(Order2.class, orderId);
		
		Assert.assertEquals("List updated", 2, o.getLineItemsList().size());
		Assert.assertEquals("List elt 1", "item 1", o.getLineItemsList().get(0).getDescription());
		Assert.assertEquals("List elt 2", "item 2", o.getLineItemsList().get(1).getDescription());
		close();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChangesList2() throws Exception {
		initPersistence();
		
		open();
		Phone2 ph1 = new Phone2(null, null, "PH1");
		ph1.setPhone("01");
		ph1 = save(ph1);
		Phone2 ph2 = new Phone2(null, null, "PH2");
		ph2.setPhone("02");
		ph2 = save(ph2);
		Phone2 ph3 = new Phone2(null, null, "PH3");
		ph3.setPhone("03");
		ph3 = save(ph3);
		Phone2 ph4 = new Phone2(null, null, "PH4");
		ph4.setPhone("04");
		ph4 = save(ph4);
		flush();
		close();
		
		Person2 p = new Person2(null, null, "P1");
		p.setFirstName("test");
		p.setLastName("test");
		p.setContacts(new PersistentList(null, new ArrayList<Contact2>()));
		Contact2 c1 = new Contact2(null, null, "C1");
		c1.setPerson(p);
		p.getContacts().add(c1);
		c1.setPhones(new PersistentList(null, new ArrayList<Phone2>()));
		c1.getPhones().add(ph1);
		c1.getPhones().add(ph2);

		open();
		p = save(p);
		flush();
		Long personId = p.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		ChangeSet changeSet = new ChangeSet();
		Change change = new Change(Person2.class.getName(), personId, p.getVersion(), "P1");
		Person2 p2 = new Person2(personId, p.getVersion(), "P1");
		p2.setFirstName("test");
		p2.setLastName("test");
		p2.setContacts(new PersistentList());
		Contact2 c2 = new Contact2(null, null, "C2");
		c2.setPerson(p2);
		c2.setPhones(new PersistentList(null, new ArrayList<Phone2>()));
		ph3 = find(Phone2.class, ph3.getId());
		ph4 = find(Phone2.class, ph4.getId());
		c2.getPhones().add(ph3);
		c2.getPhones().add(ph4);
		change.getChanges().put("contacts", new CollectionChanges(new CollectionChange[] { new CollectionChange(1, 1, c2) }));
		changeSet.setChanges(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		p = find(Person2.class, personId);
		
		Assert.assertEquals("List updated", 2, p.getContacts().size());
		Assert.assertEquals("List elt 2 phones", 2, p.getContacts().get(1).getPhones().size());
		Assert.assertEquals("List elt 2 phone 2", "04", p.getContacts().get(1).getPhones().get(1).getPhone());
		close();
	}
	
	@Test
	public void testCascadingChanges() throws Exception {
		initPersistence();
		
		Person1 p = new Person1(null, null, "P1");
		p.setFirstName("test");
		p.setLastName("test");
		Address a = new Address(null, null, "A1");		
		Contact1 c = new Contact1(null, null, "C1");
		c.setEmail("toto");
		c.setPerson(p);
		c.setAddress(a);
		Phone ph1 = new Phone(null, null, "PH1");
		ph1.setPhone("01");
		ph1.setContact(c);
		Phone ph2 = new Phone(null, null, "PH2");
		ph2.setPhone("01");
		ph2.setContact(c);
		c.getPhones().add(ph1);
		c.getPhones().add(ph2);
		p.getContacts().add(c);
		open();
		p = save(p);
		flush();
		Long personId = p.getId();
		Long personVersion = p.getVersion();
		Contact1 contact = p.getContacts().iterator().next();
		Long contactId = contact.getId();
		Long contactVersion = contact.getVersion();
		Phone phone = contact.getPhones().iterator().next();
		Long phoneId = phone.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		ChangeSet changeSet = new ChangeSet();
		Change change1 = new Change(Person1.class.getName(), personId, personVersion, "P1");
		CollectionChanges collChanges1 = new CollectionChanges();
		CollectionChange collChange1 = new CollectionChange(-1, null, new ChangeRef(Contact1.class.getName(), "C1", contactId));
		collChanges1.setChanges(new CollectionChange[] { collChange1 });
		change1.getChanges().put("contacts", collChanges1);
		Change change2 = new Change(Contact1.class.getName(), contactId, contactVersion, "C1");
		CollectionChanges collChanges2 = new CollectionChanges();
		CollectionChange collChange2 = new CollectionChange(-1, null, new ChangeRef(Phone.class.getName(), "PH2", phoneId));
		collChanges2.setChanges(new CollectionChange[] { collChange2 });
		change2.getChanges().put("phones", collChanges2);
		changeSet.setChanges(new Change[] { change1, change2 });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		p = find(Person1.class, personId);
		Assert.assertEquals("Collection change applied", 0, p.getContacts().size());
		close();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testProxyChanges() throws Exception {
	    initPersistence();
	    
	    open();
	    
	    Classification cl2 = new Classification(null, null, "CL2");
	    cl2.setCode("CL2");
	    cl2 = save(cl2);
	    
	    flush();
	    
	    Long id2 = cl2.getId();
	    Long v2 = cl2.getVersion();

        close();
        
        ProxyFactory pf = new ProxyFactory(JavassistLazyInitializer.class.getName());
        cl2 = (Classification)pf.getProxyInstance(Classification.class.getName(), Classification.class.getName(), id2);
        
        open();
        
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
        GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
        ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
        SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
        
        Classification cl1 = new Classification(null, null, "CL1");
        cl1.setCode("CL1");
        cl1.setSubClassifications(new PersistentList(null, new ArrayList<Classification>()));
        cl1.setSuperClassifications(new PersistentSet(null, new HashSet<Classification>()));
        cl1.getSubClassifications().add(cl2);
        
        ChangeSet changeSet = new ChangeSet();
        Change change1 = new Change(Classification.class.getName(), id2, v2, "CL2");
        CollectionChanges collChanges1 = new CollectionChanges();
        CollectionChange collChange1 = new CollectionChange(1, null, cl1);
        collChanges1.setChanges(new CollectionChange[] { collChange1 });
        change1.getChanges().put("superClassifications", collChanges1);
        changeSet.setChanges(new Change[] { change1 });
        
        new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
        flush();
        close();
        
        open();

        cl2 = find(Classification.class, id2);
        Assert.assertEquals("Collection change applied", 1, cl2.getSuperClassifications().size());
        Assert.assertSame("Collection change applied 2", 
                cl2, cl2.getSuperClassifications().iterator().next().getSubClassifications().get(0));
        close();
	}
	
	@Test
	public void testCascadingChangesSoftDelete() throws Exception {
		initPersistence();
		
		PersonSoftDelete p = new PersonSoftDelete(null, null, "P1");
		p.setFirstName("test");
		p.setLastName("test");
		AddressSoftDelete a = new AddressSoftDelete(null, null, "A1");		
		ContactSoftDelete c = new ContactSoftDelete(null, null, "C1");
		c.setEmail("toto");
		c.setPerson(p);
		c.setAddress(a);
		PhoneSoftDelete ph1 = new PhoneSoftDelete(null, null, "PH1");
		ph1.setPhone("01");
		ph1.setContact(c);
		PhoneSoftDelete ph2 = new PhoneSoftDelete(null, null, "PH2");
		ph2.setPhone("01");
		ph2.setContact(c);
		c.getPhones().add(ph1);
		c.getPhones().add(ph2);
		p.getContacts().add(c);
		open();
		p = save(p);
		flush();
		Long personId = p.getId();
		Long personVersion = p.getVersion();
		ContactSoftDelete contact = p.getContacts().iterator().next();
		Long contactId = contact.getId();
		Long contactVersion = contact.getVersion();
		PhoneSoftDelete phone = contact.getPhones().iterator().next();
		Long phoneId = phone.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		ChangeSet changeSet = new ChangeSet();
		Change change1 = new Change(PersonSoftDelete.class.getName(), personId, personVersion, "P1");
		CollectionChanges collChanges1 = new CollectionChanges();
		CollectionChange collChange1 = new CollectionChange(-1, null, new ChangeRef(ContactSoftDelete.class.getName(), "C1", contactId));
		collChanges1.setChanges(new CollectionChange[] { collChange1 });
		change1.getChanges().put("contacts", collChanges1);
		Change change2 = new Change(ContactSoftDelete.class.getName(), contactId, contactVersion, "C1");
		CollectionChanges collChanges2 = new CollectionChanges();
		CollectionChange collChange2 = new CollectionChange(-1, null, new ChangeRef(PhoneSoftDelete.class.getName(), "PH2", phoneId));
		collChanges2.setChanges(new CollectionChange[] { collChange2 });
		change2.getChanges().put("phones", collChanges2);
		changeSet.setChanges(new Change[] { change1, change2 });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		p = find(PersonSoftDelete.class, personId);
		Assert.assertEquals("Collection change applied", 0, p.getContacts().size());
		close();
	}
	
//	@Test
//	public void testCascadingChangesSoftDelete2() throws Exception {
//		initPersistence();
//		
//		PatientSoftDelete p = new PatientSoftDelete(null, null, "P1");
//		p.setFirstName("test");
//		p.setLastName("test");
//		AddressSoftDelete a = new AddressSoftDelete(null, null, "A1");		
//		ContactSoftDelete c = new ContactSoftDelete(null, null, "C1");
//		c.setEmail("toto");
//		c.setPerson(p);
//		c.setAddress(a);
//		PhoneSoftDelete ph1 = new PhoneSoftDelete(null, null, "PH1");
//		ph1.setPhone("01");
//		ph1.setContact(c);
//		PhoneSoftDelete ph2 = new PhoneSoftDelete(null, null, "PH2");
//		ph2.setPhone("01");
//		ph2.setContact(c);
//		c.getPhones().add(ph1);
//		c.getPhones().add(ph2);
//		p.getContacts().add(c);
//		open();
//		p = save(p);
//		flush();
//		Long personId = p.getId();
//		Long personVersion = p.getVersion();
//		ContactSoftDelete contact = p.getContacts().iterator().next();
//		Long contactId = contact.getId();
//		Long contactVersion = contact.getVersion();
//		PhoneSoftDelete phone = contact.getPhones().iterator().next();
//		Long phoneId = phone.getId();
//		close();
//
//		open();
//		
//		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/data/granite-config.xml");
//		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
//		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
//		SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//		
//		ChangeSet changeSet = new ChangeSet();
//		Change change1 = new Change(PersonSoftDelete.class.getName(), personId, personVersion, "P1");
//		CollectionChanges collChanges1 = new CollectionChanges();
//		CollectionChange collChange1 = new CollectionChange(-1, null, new ChangeRef(ContactSoftDelete.class.getName(), "C1", contactId));
//		collChanges1.setChanges(new CollectionChange[] { collChange1 });
//		change1.getChanges().put("contacts", collChanges1);
//		Change change2 = new Change(ContactSoftDelete.class.getName(), contactId, contactVersion, "C1");
//		CollectionChanges collChanges2 = new CollectionChanges();
//		CollectionChange collChange2 = new CollectionChange(-1, null, new ChangeRef(PhoneSoftDelete.class.getName(), "PH2", phoneId));
//		collChanges2.setChanges(new CollectionChange[] { collChange2 });
//		change2.getChanges().put("phones", collChanges2);
//		changeSet.setChanges(new Change[] { change1, change2 });
//		
//		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
//		flush();
//
//		p = find(PersonSoftDelete.class, personId);
//		Assert.assertEquals("Collection change applied", 0, p.getContacts().size());
//		close();
//	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChangesListAdd() throws Exception {
		initPersistence();
		
		open();
		
		Patient p = new Patient(null, null, "P1");
		p.setName("Chuck Norris");
		p.setMedicationList(new PersistentSet(null, new HashSet<Medication>()));
		Medication m = new Medication(null, null, "M1");
		m.setName("Aspirin");
		m.setPatient(p);
		m.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		p.getMedicationList().add(m);
		Prescription pr = new Prescription(null, null, "PR1");
		pr.setName("500 mg");
		pr.setMedication(m);
		m.getPrescriptionList().add(pr);

		open();
		p = save(p);
		flush();
		Long patientId = p.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		DataContext.init(null, null, PublishMode.MANUAL);
		
		ChangeSet changeSet = new ChangeSet();
		Change change = new Change(Patient.class.getName(), patientId, p.getVersion(), "P1");
		Patient p2 = new Patient(p.getId(), p.getVersion(), "P1");
		p2.setName(p.getName());
		p2.setMedicationList(new PersistentSet());
		Medication m2 = new Medication(null, null, "M2");
		m2.setName("Xanax");
		m2.setPatient(p2);
		m2.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		Prescription pr2 = new Prescription(null, null, "PR2");
		pr2.setName("1 kg");
		pr2.setMedication(m2);
		m2.getPrescriptionList().add(pr2);
		change.getChanges().put("medicationList", new CollectionChanges(new CollectionChange[] { new CollectionChange(1, 1, m2) }));
		changeSet.setChanges(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		Patient p3 = find(Patient.class, patientId);
		
		Assert.assertEquals("List updated", 2, p3.getMedicationList().size());
		boolean found = false;
		for (Medication m3 : p3.getMedicationList()) {
			if (m3.getUid().equals("M2"))
				found = true;
		}
		Assert.assertTrue("Medication 2", found);
		
		Object[][] updates = DataContext.get().getUpdates();
		
		Assert.assertNotNull("Updates", updates);
		
		close();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChangesListAdd2() throws Exception {
		initPersistence();
		
		open();
		
		Patient p = new Patient(null, null, "P1");
		p.setName("Chuck Norris");
		p.setMedicationList(new PersistentSet(null, new HashSet<Medication>()));
		Medication m = new Medication(null, null, "M1");
		m.setName("Aspirin");
		m.setPatient(p);
		m.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		p.getMedicationList().add(m);
		Prescription pr = new Prescription(null, null, "PR1");
		pr.setName("500 mg");
		pr.setMedication(m);
		m.getPrescriptionList().add(pr);

		open();
		p = save(p);
		flush();
		Long patientId = p.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		DataContext.init(null, null, PublishMode.MANUAL);
		
		ChangeSet changeSet = new ChangeSet();
		Change change = new Change(Patient.class.getName(), patientId, p.getVersion(), "P1");
		Patient p2 = new Patient(p.getId(), p.getVersion(), "P1");
		p2.setName(p.getName());
		p2.setMedicationList(new PersistentSet(null, new HashSet<Medication>()));
		Medication m1 = new Medication(m.getId(), m.getVersion(), "M1");
		m1.setName(m.getName());
		m1.setPatient(p2);
		m1.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		Prescription pr1 = new Prescription(pr.getId(), pr.getVersion(), "PR1");
		pr1.setName(pr.getName());
		pr1.setMedication(m1);
		p2.getMedicationList().add(m1);
		Medication m2 = new Medication(null, null, "M2");
		m2.setName("Xanax");
		m2.setPatient(p2);
		m2.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		Prescription pr2 = new Prescription(null, null, "PR2");
		pr2.setName("1 kg");
		pr2.setMedication(m2);
		m2.getPrescriptionList().add(pr2);
		p2.getMedicationList().add(m2);
		change.getChanges().put("medicationList", new CollectionChanges(new CollectionChange[] { new CollectionChange(1, 1, m2) }));
		changeSet.setChanges(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		Patient p3 = find(Patient.class, patientId);
		
		Assert.assertEquals("List updated", 2, p3.getMedicationList().size());
		boolean found = false;
		for (Medication m3 : p3.getMedicationList()) {
			if (m3.getUid().equals("M2"))
				found = true;
		}
		Assert.assertTrue("Medication 2", found);
		
		Object[][] updates = DataContext.get().getUpdates();
		
		Assert.assertNotNull("Updates", updates);
		
		close();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChangesListAdd3() throws Exception {
		initPersistence();
		
		open();
		
		Patient p = new Patient(null, null, "P1");
		p.setName("Chuck Norris");
		p.setMedicationList(new PersistentSet(null, new HashSet<Medication>()));
		Medication m = new Medication(null, null, "M1");
		m.setName("Aspirin");
		m.setPatient(p);
		m.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		p.getMedicationList().add(m);
		Prescription pr = new Prescription(null, null, "PR1");
		pr.setName("500 mg");
		pr.setMedication(m);
		m.getPrescriptionList().add(pr);

		open();
		p = save(p);
		flush();
		Long patientId = p.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		DataContext.init(null, null, PublishMode.MANUAL);
		
		ChangeSet changeSet = new ChangeSet();
		Change change = new Change(Patient.class.getName(), patientId, p.getVersion(), "P1");
		Patient p2 = new Patient(p.getId(), p.getVersion(), "P1");
		p2.setName(p.getName());
		p2.setMedicationList(new PersistentSet(null, new HashSet<Medication>()));
		Medication m1 = new Medication(m.getId(), m.getVersion(), "M1");
		m1.setName(m.getName());
		m1.setPatient(p2);
		m1.setPrescriptionList(new PersistentSet());
		p2.getMedicationList().add(m1);
		Medication m2 = new Medication(null, null, "M2");
		m2.setName("Xanax");
		m2.setPatient(p2);
		m2.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		Prescription pr2 = new Prescription(null, null, "PR2");
		pr2.setName("1 kg");
		pr2.setMedication(m2);
		m2.getPrescriptionList().add(pr2);
		p2.getMedicationList().add(m2);
		change.getChanges().put("medicationList", new CollectionChanges(new CollectionChange[] { new CollectionChange(1, 1, m2) }));
		changeSet.setChanges(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		Patient p3 = find(Patient.class, patientId);
		
		Assert.assertEquals("List updated", 2, p3.getMedicationList().size());
		boolean found = false;
		for (Medication m3 : p3.getMedicationList()) {
			if (m3.getUid().equals("M2"))
				found = true;
		}
		Assert.assertTrue("Medication 2", found);
		
		Object[][] updates = DataContext.get().getUpdates();
		
		Assert.assertNotNull("Updates", updates);
		
		close();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChangesListAdd5() throws Exception {
		initPersistence();
		
		open();
		
		Patient p = new Patient(null, null, "P1");
		p.setName("Chuck Norris");
		p.setMedicationList(new PersistentSet(null, new HashSet<Medication>()));
		Medication m = new Medication(null, null, "M1");
		m.setName("Aspirin");
		m.setPatient(p);
		m.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		p.getMedicationList().add(m);
		Prescription pr = new Prescription(null, null, "PR1");
		pr.setName("500 mg");
		pr.setMedication(m);
		m.getPrescriptionList().add(pr);

		open();
		p = save(p);
		flush();
		Long patientId = p.getId();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		DataContext.init(null, null, PublishMode.MANUAL);
		
		ChangeSet changeSet = new ChangeSet();
		Change change = new Change(Patient.class.getName(), patientId, p.getVersion(), "P1");
		Patient p2 = new Patient(p.getId(), p.getVersion(), "P1");
		p2.setName(p.getName());
		p2.setMedicationList(new PersistentSet());
		Medication m2 = new Medication(null, null, "M2");
		m2.setName("Xanax");
		m2.setPatient(p2);
		m2.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		Prescription pr2 = new Prescription(null, null, "PR2");
		pr2.setName("1 kg");
		pr2.setMedication(m2);
		m2.getPrescriptionList().add(pr2);
		change.getChanges().put("medicationList", new CollectionChanges(new CollectionChange[] { new CollectionChange(1, 1, m2) }));
		changeSet.setChanges(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		Patient p3 = find(Patient.class, patientId);
		
		Assert.assertEquals("List updated", 2, p3.getMedicationList().size());
		Medication m3 = null;
		for (Medication mm : p3.getMedicationList()) {
			if (mm.getUid().equals("M2")) {
				m3 = mm;
				break;
			}
		}
		Assert.assertNotNull("Medication 2", m3);
		Assert.assertEquals("Medication prescriptions", 1, m3.getPrescriptionList().size());
		
		close();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChangesListAdd6() throws Exception {
		initPersistence();
		
		open();
		
		Patient p = new Patient(null, null, "P1");
		p.setName("Chuck Norris");
		p.setMedicationList(new PersistentSet(null, new HashSet<Medication>()));
		Medication m = new Medication(null, null, "M1");
		m.setName("Aspirin");
		m.setPatient(p);
		m.setPrescriptionList(new PersistentSet(null, new HashSet<Prescription>()));
		p.getMedicationList().add(m);
		Prescription pr = new Prescription(null, null, "PR1");
		pr.setName("500 mg");
		pr.setMedication(m);
		m.getPrescriptionList().add(pr);

		open();
		p = save(p);
		flush();
		Long patientId = p.getId();
		m = p.getMedicationList().iterator().next();
		close();

		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		DataContext.init(null, null, PublishMode.MANUAL);
		
		ChangeSet changeSet = new ChangeSet();
		Change change = new Change(Medication.class.getName(), m.getId(), m.getVersion(), m.getUid());
		Patient p2 = new Patient(p.getId(), p.getVersion(), p.getUid());
		p2.setName(p.getName());
		p2.setMedicationList(new PersistentSet());
		Medication m2 = new Medication(m.getId(), m.getVersion(), m.getUid());
		m2.setName("Aspirin");
		m2.setPatient(p2);
		m2.setPrescriptionList(new PersistentSet());
		Prescription pr2 = new Prescription(null, null, "PR2");
		pr2.setName("1 kg");
		pr2.setMedication(m2);
		change.getChanges().put("prescriptionList", new CollectionChanges(new CollectionChange[] { new CollectionChange(1, 1, pr2) }));
		changeSet.setChanges(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();

		Patient p3 = find(Patient.class, patientId);
		Medication m3 = p3.getMedicationList().iterator().next();
		
		Assert.assertEquals("List updated", 2, m3.getPrescriptionList().size());
		Prescription pr3 = null;
		for (Prescription prr : m3.getPrescriptionList()) {
			if (prr.getUid().equals("PR2")) {
				pr3 = prr;
				break;
			}
		}
		Assert.assertNotNull("Prescription 2", pr3);
		Assert.assertEquals("Medication prescriptions", 2, m3.getPrescriptionList().size());
		
		close();
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultipleSetAddNew() throws Exception {
		initPersistence();
		
		open();
		
		Patient2 p = new Patient2(null, null, "P1");
		p.setName("Chuck Norris");
		p.setTests(new PersistentSet(null, new HashSet<Test2>()));
		p.setVisits(new PersistentSet(null, new HashSet<Visit2>()));
		Visit2 v = new Visit2(null, null, "V2");
		v.setName("Visit");
		v.setPatient(p);
		v.setTests(new PersistentSet(null, new HashSet<Test2>()));
		p.getVisits().add(v);
		VitalSignTest2 vst = new VitalSignTest2(null, null, "VST1");
		vst.setName("Test");
		vst.setPatient(p);
		vst.setVisit(v);
		v.getTests().add(vst);
		
		open();
		p = save(p);
		flush();
		Long patientId = p.getId();
		v = p.getVisits().iterator().next();
		close();
		
		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		DataContext.init(null, null, PublishMode.MANUAL);
		
		Change change = new Change(Visit2.class.getName(), v.getId(), v.getVersion(), v.getUid());
		
		Patient2 p2 = new Patient2(p.getId(), p.getVersion(), p.getUid());
		p2.setName(p.getName());
		p2.setTests(new PersistentSet());
		p2.setVisits(new PersistentSet(null, new HashSet<Visit2>()));
		
		Visit2 v2 = new Visit2(v.getId(), v.getVersion(), v.getUid());
		v2.setName(v.getName());
		v2.setTests(new PersistentSet(null, new HashSet<Test2>()));
		p2.getVisits().add(v2);
		
		VitalSignTest2 vst2 = new VitalSignTest2();
		vst2.setVitalSignObservations(new PersistentSet(null, new HashSet<VitalSignObservation2>()));
		VitalSignObservation2 vso2a = new VitalSignObservation2();
		vso2a.setVitalSignTest(vst2);
		VitalSignObservation2 vso2b = new VitalSignObservation2();
		vso2b.setVitalSignTest(vst2);
		vst2.setPatient(p2);
		vst2.setVisit(v2);
		vst2.getVitalSignObservations().add(vso2a);
		vst2.getVitalSignObservations().add(vso2b);
		v2.getTests().add(vst2);
		
		CollectionChange collChange = new CollectionChange(1, 0, vst2);
		change.addCollectionChanges("tests", new CollectionChange[] { collChange });
		
		Change change2 = new Change(Visit2.class.getName(), v.getId(), v.getVersion(), v.getUid());
		change2.getChanges().put("version", v.getVersion());

		ChangeSet changeSet = new ChangeSet(new Change[] { change });

		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();
		
		Patient2 p3 = find(Patient2.class, patientId);
		
		VitalSignTest2 vst3 = null;
        for (Test2 t : p3.getVisits().iterator().next().getTests()) {
            if (!t.getUid().equals("VST1")) {
                vst3 = (VitalSignTest2)t;   // New test will not have the uid for the first one
                break;
            }
        }
		Assert.assertNotNull("Test saved", vst3.getId());
		Assert.assertEquals("Observations", 2, vst3.getVitalSignObservations().size());
		
		Object[][] updates = DataContext.get().getUpdates();
		
		Assert.assertNotNull("Updates", updates);
		
		close();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testReorderList() throws Exception {
		initPersistence();
		
		open();
		
		VitalSignTest3 test = new VitalSignTest3(null, null, "T1");
		test.setVitalSignObservations(new PersistentList(null, new ArrayList<VitalSignObservation3>()));
		VitalSignObservation3 obs1 = new VitalSignObservation3(null, null, "O1");
		obs1.setVitalSignTest(test);
		VitalSignObservation3 obs2 = new VitalSignObservation3(null, null, "O2");
		obs2.setVitalSignTest(test);
		VitalSignObservation3 obs3 = new VitalSignObservation3(null, null, "O3");
		obs3.setVitalSignTest(test);
		test.getVitalSignObservations().add(obs1);
		test.getVitalSignObservations().add(obs2);
		test.getVitalSignObservations().add(obs3);
		
		test = save(test);
		flush();
		Long testId = test.getId();
		// Long obs2Id = test.getVitalSignObservations().get(1).getId();
		Long obs3Id = test.getVitalSignObservations().get(2).getId();
		close();
		
		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		DataContext.init(null, null, PublishMode.MANUAL);
		
		Change change = new Change(VitalSignTest3.class.getName(), testId, test.getVersion(), test.getUid());
		CollectionChange collChange1 = new CollectionChange(-1, 2, new ChangeRef(VitalSignObservation3.class.getName(), "O3", new Integer(obs3Id.intValue())));
		CollectionChange collChange2 = new CollectionChange(1, 1, new ChangeRef(VitalSignObservation3.class.getName(), "O3", new Integer(obs3Id.intValue())));
		CollectionChanges collChanges = new CollectionChanges(new CollectionChange[] { collChange1, collChange2 });
		change.getChanges().put("vitalSignObservations", collChanges);
		
		ChangeSet changeSet = new ChangeSet(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();
		close();
		
		open();
		
		VitalSignTest3 test2 = find(VitalSignTest3.class, testId);
		
		Assert.assertEquals("Observations", 3, test2.getVitalSignObservations().size());
		Assert.assertEquals("Obs1", "O1", test2.getVitalSignObservations().get(0).getUid());
		Assert.assertEquals("Obs2", "O3", test2.getVitalSignObservations().get(1).getUid());
		Assert.assertEquals("Obs3", "O2", test2.getVitalSignObservations().get(2).getUid());
		
		close();
	}
}
