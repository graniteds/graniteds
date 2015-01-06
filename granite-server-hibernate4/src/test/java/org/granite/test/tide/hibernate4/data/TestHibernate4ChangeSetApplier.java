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
package org.granite.test.tide.hibernate4.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.granite.hibernate4.ProxyFactory;
import org.granite.test.tide.data.AbstractEntity;
import org.granite.test.tide.data.Address;
import org.granite.test.tide.data.Classification;
import org.granite.test.tide.data.Contact2;
import org.granite.test.tide.data.Country;
import org.granite.test.tide.data.LineItemBag;
import org.granite.test.tide.data.LineItemBag2;
import org.granite.test.tide.data.LineItemList;
import org.granite.test.tide.data.LineItemList2;
import org.granite.test.tide.data.Medication;
import org.granite.test.tide.data.Order;
import org.granite.test.tide.data.Order2;
import org.granite.test.tide.data.Patient;
import org.granite.test.tide.data.Person2;
import org.granite.test.tide.data.Phone2;
import org.granite.test.tide.data.Prescription;
import org.granite.tide.data.TidePersistenceAdapter;
import org.granite.tide.hibernate4.HibernateDataChangePublishListener;
import org.granite.tide.hibernate4.HibernatePersistenceAdapter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class TestHibernate4ChangeSetApplier extends AbstractTestChangeSetApplier {
	
	private SessionFactory sessionFactory;
	private Session session;
	private Transaction tx;
	
	@Override
	protected void initPersistence() {
		AnnotationConfiguration configuration = new AnnotationConfiguration()
			.addAnnotatedClass(AbstractEntity.class)
			.addAnnotatedClass(Address.class)
			.addAnnotatedClass(Contact1.class)
			.addAnnotatedClass(Country.class)
			.addAnnotatedClass(Person1.class)
			.addAnnotatedClass(Phone.class)
			.addAnnotatedClass(Contact2.class)
			.addAnnotatedClass(Classification.class)
			.addAnnotatedClass(Person2.class)
			.addAnnotatedClass(Phone2.class)
			.addAnnotatedClass(OrderRepo.class)
			.addAnnotatedClass(Order.class)
			.addAnnotatedClass(LineItemBag.class)
			.addAnnotatedClass(LineItemList.class)
			.addAnnotatedClass(Order2.class)
			.addAnnotatedClass(LineItemBag2.class)
			.addAnnotatedClass(LineItemList2.class)
			.addAnnotatedClass(AbstractEntitySoftDelete.class)
			.addAnnotatedClass(AddressSoftDelete.class)
			.addAnnotatedClass(ContactSoftDelete.class)
			.addAnnotatedClass(CountrySoftDelete.class)
			.addAnnotatedClass(PersonSoftDelete.class)
			.addAnnotatedClass(PhoneSoftDelete.class)
			.addAnnotatedClass(Patient.class)
			.addAnnotatedClass(Patient3.class)
			.addAnnotatedClass(Medication.class)
			.addAnnotatedClass(Medication3.class)
			.addAnnotatedClass(Prescription.class)
			.addAnnotatedClass(Person3.class)
			.addAnnotatedClass(Contact3.class)
			.addAnnotatedClass(Patient2.class)
			.addAnnotatedClass(Visit2.class)
			.addAnnotatedClass(Test2.class)
			.addAnnotatedClass(Alert.class)
			.addAnnotatedClass(VitalSignTest2.class)
			.addAnnotatedClass(VitalSignObservation2.class)
			.addAnnotatedClass(VitalSignTest3.class)
			.addAnnotatedClass(VitalSignObservation3.class)
			.setProperty("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName())
			.setProperty("hibernate.hbm2ddl.auto", "create-drop")
			.setProperty("hibernate.show_sql", "true")
			.setProperty("hibernate.connection.driver_class", org.h2.Driver.class.getName())
			.setProperty("hibernate.connection.url", "jdbc:h2:mem:test-changeset")
			.setProperty("hibernate.connection.username", "sa")
			.setProperty("hibernate.connection.password", "");
		
		sessionFactory = configuration.buildSessionFactory();
		
		EventListenerRegistry registry = ((SessionFactoryImpl)sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
		registry.appendListeners(EventType.POST_INSERT, new HibernateDataChangePublishListener());
		registry.appendListeners(EventType.POST_UPDATE, new HibernateDataChangePublishListener());
		registry.appendListeners(EventType.POST_DELETE, new HibernateDataChangePublishListener());
	}

	@Override
	protected void open() {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
	}
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T find(Class<T> entityClass, Serializable id) {
		return (T)session.load(entityClass, id);
	}
	@Override
	protected <T> T save(T entity) {
		session.save(entity);
		return entity;
	}
	@Override
	protected void flush() {
		session.flush();
		tx.commit();
	}
	@Override
	protected void close() {
		session.clear();
		session.close();
	}
	@Override
	protected TidePersistenceAdapter newPersistenceAdapter() {
		return new HibernatePersistenceAdapter(session);
	}
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T newProxy(Class<T> entityClass, Serializable id) {
		ProxyFactory proxyFactory = new ProxyFactory(JavassistLazyInitializer.class.getName());
		return (T)proxyFactory.getProxyInstance(entityClass.getName(), entityClass.getName(), id);
	}


	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleHibernateMerge() {
		initPersistence();
		
		open();
		
		Person3 p = new Person3();
		Contact3 c1 = new Contact3();
		c1.setPerson(p);
		Contact3 c2 = new Contact3();
		c2.setPerson(p);
		Contact3 c3 = new Contact3();
		c3.setPerson(p);
		Contact3 c4 = new Contact3();
		c4.setPerson(p);
		Contact3 c5 = new Contact3();
		c5.setPerson(p);
		p.setContacts(new HashSet<Contact3>());
		p.getContacts().add(c1);
		p.getContacts().add(c2);
		p.getContacts().add(c3);
		p.getContacts().add(c4);
		p.getContacts().add(c5);
		
		session.save(p);
		for (Contact3 c : p.getContacts()) {
			c.setPerson(p);
			session.save(c);
		}
		flush();
		
		close();
		
		open();

		Person3 q = new Person3(p.getId());
		Contact3 d1 = new Contact3(c1.getId());
		d1.setPerson(q);
		Contact3 d2 = new Contact3(c2.getId());
		d2.setPerson(q);
		Contact3 d3 = new Contact3(c3.getId());
		d3.setPerson(q);
		Contact3 d4 = new Contact3(c4.getId());
		d4.setPerson(q);
		Contact3 d5 = new Contact3(c5.getId());
		d5.setPerson(q);
		Contact3 d6 = new Contact3();
		d6.setPerson(q);
		q.setContacts(new PersistentSet(null, new HashSet<Contact3>()));
		q.getContacts().add(d1);
		q.getContacts().add(d2);
		q.getContacts().add(d3);
		q.getContacts().add(d4);
		q.getContacts().add(d5);
		q.getContacts().add(d6);
		
		List<Contact3> lc = session.createQuery( "from Contact3 where person=:p").setParameter("p", p).list();
        for (Contact3 c : lc) {
        	boolean contains = false;
        	for (Contact3 d : q.getContacts()) {
	            if ( d.getId() != null && d.getId().compareTo(c.getId()) == 0 ) {
	            	contains = true;
	            	break;
	            }
        	}
        	if (!contains)
        		session.delete(c);
        }
        
        Set<Contact3> merged = new HashSet<Contact3>();
        for (Contact3 d : q.getContacts())
             merged.add((Contact3)session.merge(d));
        
        q.getContacts().clear();
        q.getContacts().addAll(merged);
        
        q = (Person3)session.merge(q);
        
        flush();
        
        for (Contact3 d : q.getContacts())
        	Assert.assertSame("Person", q, d.getPerson());
        
        close();
	}	
}
