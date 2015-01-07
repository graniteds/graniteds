/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.hibernate.data;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.granite.test.tide.data.AbstractEntity;
import org.granite.test.tide.data.Address;
import org.granite.test.tide.data.Classification;
import org.granite.test.tide.data.Contact4;
import org.granite.test.tide.data.Country;
import org.granite.test.tide.data.LegalEntity;
import org.granite.test.tide.data.LineItemBag;
import org.granite.test.tide.data.LineItemBag2;
import org.granite.test.tide.data.LineItemList;
import org.granite.test.tide.data.LineItemList2;
import org.granite.test.tide.data.Order;
import org.granite.test.tide.data.Order2;
import org.granite.test.tide.data.Person4;
import org.granite.test.tide.data.Phone2;
import org.granite.test.tide.data.Phone4;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.ChangeSetApplier;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.CollectionChanges;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataContext.EntityUpdate;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.granite.tide.data.DefaultDataDispatcher;
import org.granite.tide.data.DefaultDataTopicParams;
import org.granite.tide.data.TidePersistenceAdapter;
import org.granite.tide.hibernate.HibernatePersistenceAdapter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentList;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("deprecation")
public abstract class AbstractTestHibernate3ChangeSetPublisher {
	
	private SessionFactory sessionFactory;
	private Session session;
	private Transaction tx;
	
	protected void initPersistence() {
		AnnotationConfiguration configuration = new AnnotationConfiguration()
			.addAnnotatedClass(AbstractEntity.class)
			.addAnnotatedClass(LegalEntity.class)
			.addAnnotatedClass(Address.class)
			.addAnnotatedClass(Contact1.class)
			.addAnnotatedClass(Country.class)
			.addAnnotatedClass(Person1.class)
			.addAnnotatedClass(Person4.class)
			.addAnnotatedClass(Contact4.class)
			.addAnnotatedClass(Phone.class)
			.addAnnotatedClass(Phone2.class)
			.addAnnotatedClass(Phone4.class)
			.addAnnotatedClass(OrderRepo.class)
			.addAnnotatedClass(Order.class)
			.addAnnotatedClass(LineItemList.class)
			.addAnnotatedClass(LineItemBag.class)
			.addAnnotatedClass(Order2.class)
			.addAnnotatedClass(LineItemList2.class)
			.addAnnotatedClass(LineItemBag2.class)
			.addAnnotatedClass(Classification.class)
			.setProperty("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName())
			.setProperty("hibernate.hbm2ddl.auto", "create-drop")
			.setProperty("hibernate.show_sql", "true")
			.setProperty("hibernate.connection.driver_class", org.h2.Driver.class.getName())
			.setProperty("hibernate.connection.url", "jdbc:h2:mem:test-changeset")
			.setProperty("hibernate.connection.username", "sa")
			.setProperty("hibernate.connection.password", "");
		setListeners(configuration);
		
		sessionFactory = configuration.buildSessionFactory();
	}
	
	protected abstract void setListeners(Configuration cfg);
	
	protected void open() {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
	}
	@SuppressWarnings("unchecked")
	protected <T> T find(Class<T> entityClass, Serializable id) {
		Criteria criteria = session.createCriteria(entityClass);
    	criteria.add(Restrictions.idEq(id));
        return (T)criteria.uniqueResult();
	}
	protected <T> T save(T entity) {
		session.save(entity);
		return entity;
	}
	protected <T> void delete(T entity) {
		session.delete(entity);
	}
	protected void flush() {
		session.flush();
		tx.commit();
	}
	protected void flushOnly() {
		session.flush();
	}
	protected void close() {
		session.clear();
		session.close();
	}
	protected TidePersistenceAdapter newPersistenceAdapter() {
		return new HibernatePersistenceAdapter(session);
	}

	
	@Test
	public void testSimpleChanges() throws Exception {
		initPersistence();
		
		Person1 p = new Person1(null, null, "P1");
		p.setFirstName("test");
		p.setLastName("test");
		p.setContacts(new HashSet<Contact1>());
		Address a = new Address(null, null, "A1");
		Contact1 c = new Contact1(null, null, "C1");
		c.setEmail("toto@tutu.net");
		c.setAddress(a);
		c.setPerson(p);
		p.getContacts().add(c);
		open();
		p = save(p);
		flush();
		Long personId = p.getId();
		close();

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		p = find(Person1.class, personId);
		p.setLastName("toto");
		flushOnly();
		flush();
		close();
		
		List<EntityUpdate> updates = DataContext.get().getDataUpdates();
		Assert.assertEquals("Updates size", 1, updates.size());
		EntityUpdate update = updates.get(0);
		Assert.assertEquals("Update value", "toto", ((Change)update.entity).getChanges().get("lastName"));

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		p = find(Person1.class, personId);
		a = new Address(null, null, "A2");
		c = new Contact1(null, null, "C2");
		c.setEmail("toto2@tutu.net");
		c.setAddress(a);
		c.setPerson(p);
		p.getContacts().add(c);
		flush();
		close();
		
		updates = DataContext.get().getDataUpdates();
		update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Person1.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates person", update);
		CollectionChanges collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("contacts");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		CollectionChange collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll add", 1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof Contact1);
		Assert.assertEquals("Update coll value", c.getEmail(), ((Contact1)collChange.getValue()).getEmail());
	}
	
//	@SuppressWarnings({"unchecked", "unused"})
//	@Ignore("Cannot make this kind of mapping work with Hibernate: @OneToMany(mappedBy='bla', orphanRemoval=true) + @OrderColumn")
//	@Test
//	public void testInverseListChanges() throws Exception {
//		initPersistence();
//
//		Order o = new Order(null, null, "O1");
//		o.setLineItemsList(new PersistentList(null, new ArrayList<LineItemBag>()));
//		o.setDescription("order 1");
//		LineItemList i1 = new LineItemList(null, null, "I1");
//		i1.setDescription("item 1");
//		i1.setOrder(o);
//		o.getLineItemsList().add(i1);
//		open();
//		o = save(o);
//		flush();
//		Long orderId = o.getId();
//		close();
//
//		DataContext.remove();
//		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
//
//		open();
//		o = find(Order.class, orderId);
//		LineItemList i2 = new LineItemList(null, null, "I2");
//		i2.setDescription("item 2");
//		i2.setOrder(o);
//		o.getLineItemsList().add(i2);
//		flush();
//		close();
//
//		List<EntityUpdate> updates = DataContext.get().getDataUpdates();
//		EntityUpdate update = null;
//		for (EntityUpdate u : updates) {
//			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order.class.getName())) {
//				update = u;
//				break;
//			}
//		}
//		Assert.assertNotNull("Updates order", update);
//		CollectionChanges collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsList");
//		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
//		CollectionChange collChange = collChanges.getChanges()[0];
//		Assert.assertEquals("Update coll add", 1, collChange.getType());
//		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList);
//		Assert.assertEquals("Update coll value", i2.getDescription(), ((LineItemList)collChange.getValue()).getDescription());
//
//		DataContext.remove();
//		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
//
//		open();
//		o = find(Order.class, orderId);
//		LineItemList i3 = new LineItemList(null, null, "I3");
//		i3.setDescription("item 3");
//		i3.setOrder(o);
//		o.getLineItemsList().add(1, i3);
//		flush();
//		close();
//
//		updates = DataContext.get().getDataUpdates();
//		update = null;
//		for (EntityUpdate u : updates) {
//			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order.class.getName())) {
//				update = u;
//				break;
//			}
//		}
//		Assert.assertNotNull("Updates order", update);
//		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsList");
//		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
//		collChange = collChanges.getChanges()[0];
//		Assert.assertEquals("Update coll add", 1, collChange.getType());
//		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList);
//		Assert.assertEquals("Update coll index", 1, collChange.getKey());
//		Assert.assertEquals("Update coll value", i3.getDescription(), ((LineItemList)collChange.getValue()).getDescription());
//
//		DataContext.remove();
//		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
//
//		open();
//		o = find(Order.class, orderId);
//		LineItemList li = o.getLineItemsList().remove(1);
////		delete(li);
//		flush();
//		close();
//
//		updates = DataContext.get().getDataUpdates();
//		update = null;
//		for (EntityUpdate u : updates) {
//			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order.class.getName())) {
//				update = u;
//				break;
//			}
//		}
//		Assert.assertNotNull("Updates order", update);
//		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsList");
//		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
//		collChange = collChanges.getChanges()[0];
//		Assert.assertEquals("Update coll add", -1, collChange.getType());
//		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList);
//		Assert.assertEquals("Update coll index", 1, collChange.getKey());
//		Assert.assertEquals("Update coll value", i3.getDescription(), ((LineItemList)collChange.getValue()).getDescription());
//
//		DataContext.remove();
//		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
//
//		open();
//		o = find(Order.class, orderId);
//		LineItemList i4 = new LineItemList(null, null, "I4");
//		i4.setDescription("item 4");
//		i4.setOrder(o);
//		o.getLineItemsList().add(1, i4);
//		li = o.getLineItemsList().remove(0);
////		delete(li);
//		flush();
//		close();
//
//		updates = DataContext.get().getDataUpdates();
//		update = null;
//		for (EntityUpdate u : updates) {
//			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order.class.getName())) {
//				update = u;
//				break;
//			}
//		}
//		Assert.assertNotNull("Updates order", update);
//		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsList");
//		Assert.assertEquals("Update collection", 2, collChanges.getChanges().length);
//		collChange = collChanges.getChanges()[0];
//		Assert.assertEquals("Update coll add", 1, collChange.getType());
//		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList);
//		Assert.assertEquals("Update coll index", 1, collChange.getKey());
//		Assert.assertEquals("Update coll value", i4.getDescription(), ((LineItemList)collChange.getValue()).getDescription());
//		collChange = collChanges.getChanges()[1];
//		Assert.assertEquals("Update coll remove", -1, collChange.getType());
//		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList);
//		Assert.assertEquals("Update coll index", 0, collChange.getKey());
//		Assert.assertEquals("Update coll value", i1.getDescription(), ((LineItemList)collChange.getValue()).getDescription());
//	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testInverseBagChanges() throws Exception {
		initPersistence();
		
		Order o = new Order(null, null, "O1");
		o.setLineItemsBag(new PersistentBag(null, new ArrayList<LineItemBag>()));
		o.setDescription("order 1");
		LineItemBag i1 = new LineItemBag(null, null, "I1");
		i1.setDescription("item 1");
		i1.setOrder(o);
		o.getLineItemsBag().add(0, i1);
		open();
		o = save(o);
		flush();
		Long orderId = o.getId();
		close();

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		o = find(Order.class, orderId);
		LineItemBag i2 = new LineItemBag(null, null, "I2");
		i2.setDescription("item 2");
		i2.setOrder(o);
		o.getLineItemsBag().add(i2);
		flush();
		close();
		
		List<EntityUpdate> updates = DataContext.get().getDataUpdates();
		EntityUpdate update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order.class.getName())) {				
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates order", update);
		CollectionChanges collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsBag");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		CollectionChange collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll add", 1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemBag);
		Assert.assertEquals("Update coll value", i2.getDescription(), ((LineItemBag)collChange.getValue()).getDescription());

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		o = find(Order.class, orderId);
		LineItemBag i3 = new LineItemBag(null, null, "I3");
		i3.setDescription("item 3");
		i3.setOrder(o);
		o.getLineItemsBag().add(1, i3);
		flush();
		close();
		
		updates = DataContext.get().getDataUpdates();
		update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates order", update);
		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsBag");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll add", 1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemBag);
		Assert.assertEquals("Update coll index", 1, collChange.getKey());
		Assert.assertEquals("Update coll value", i3.getDescription(), ((LineItemBag)collChange.getValue()).getDescription());

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		o = find(Order.class, orderId);
		o.getLineItemsBag().remove(1);
		flush();
		close();
		
		updates = DataContext.get().getDataUpdates();
		update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates order", update);
		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsBag");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll add", -1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemBag);
		Assert.assertEquals("Update coll index", 1, collChange.getKey());

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		o = find(Order.class, orderId);
		LineItemBag i4 = new LineItemBag(null, null, "I4");
		i4.setDescription("item 4");
		o.getLineItemsBag().add(1, i4);
		o.getLineItemsBag().remove(0);
		flush();
		close();
		
		updates = DataContext.get().getDataUpdates();
		update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates order", update);
		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsBag");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll update", 0, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemBag);
		Assert.assertEquals("Update coll index", 0, collChange.getKey());
		Assert.assertEquals("Update coll value", i4.getDescription(), ((LineItemBag)collChange.getValue()).getDescription());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testListChanges() throws Exception {
		initPersistence();
		
		Order2 o = new Order2(null, null, "O1");
		o.setLineItemsList(new PersistentList(null, new ArrayList<LineItemList2>()));
		o.setDescription("order 1");
		LineItemList2 i1 = new LineItemList2(null, null, "I1");
		i1.setDescription("item 1");
		o.getLineItemsList().add(i1);
		open();
		o = save(o);
		flush();
		Long orderId = o.getId();
		close();

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		o = find(Order2.class, orderId);
		LineItemList2 i2 = new LineItemList2(null, null, "I2");
		i2.setDescription("item 2");
		o.getLineItemsList().add(i2);
		flush();
		close();
		
		List<EntityUpdate> updates = DataContext.get().getDataUpdates();
		EntityUpdate update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order2.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates order", update);
		CollectionChanges collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsList");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		CollectionChange collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll add", 1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList2);
		Assert.assertEquals("Update coll index", 1, collChange.getKey());
		Assert.assertEquals("Update coll value", i2.getDescription(), ((LineItemList2)collChange.getValue()).getDescription());

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		o = find(Order2.class, orderId);
		LineItemList2 i3 = new LineItemList2(null, null, "I3");
		i3.setDescription("item 3");
		o.getLineItemsList().add(1, i3);
		flush();
		close();
		
		updates = DataContext.get().getDataUpdates();
		update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order2.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates order", update);
		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsList");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll add", 1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList2);
		Assert.assertEquals("Update coll index", 1, collChange.getKey());
		Assert.assertEquals("Update coll value", i3.getDescription(), ((LineItemList2)collChange.getValue()).getDescription());

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		o = find(Order2.class, orderId);
		o.getLineItemsList().remove(1);
		flush();
		close();
		
		updates = DataContext.get().getDataUpdates();
		update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order2.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates order", update);
		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsList");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll add", -1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList2);
		Assert.assertEquals("Update coll index", 1, collChange.getKey());
		Assert.assertEquals("Update coll value", i3.getDescription(), ((LineItemList2)collChange.getValue()).getDescription());

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		// Cannot make this work with Hibernate 3.6.5.Final
		// JDBC Exception: failed batch ???
//		open();
//		o = find(Order2.class, orderId);
//		LineItemList2 i4 = new LineItemList2(null, null, "I4");
//		i4.setDescription("item 4");
//		o.getLineItemsList().add(i4);
//		o.getLineItemsList().remove(0);
//		flush();
//		close();
//		
//		updates = DataContext.get().getDataUpdates();
//		update = null;
//		for (EntityUpdate u : updates) {
//			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Order2.class.getName())) {
//				update = u;
//				break;
//			}
//		}
//		Assert.assertNotNull("Updates order", update);
//		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("lineItemsList");
//		Assert.assertEquals("Update collection", 2, collChanges.getChanges().length);
//		collChange = collChanges.getChanges()[0];
//		Assert.assertEquals("Update coll remove", -1, collChange.getType());
//		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList2);
//		Assert.assertEquals("Update coll index", 0, collChange.getKey());
//		Assert.assertEquals("Update coll value", i1.getDescription(), ((LineItemList2)collChange.getValue()).getDescription());
//		collChange = collChanges.getChanges()[1];
//		Assert.assertEquals("Update coll add", 1, collChange.getType());
//		Assert.assertTrue("Update coll type", collChange.getValue() instanceof LineItemList2);
//		Assert.assertEquals("Update coll index", 1, collChange.getKey());
//		Assert.assertEquals("Update coll value", i4.getDescription(), ((LineItemList2)collChange.getValue()).getDescription());
	}
		
	@Test
	public void testMapChanges() throws Exception {
		initPersistence();
		
		OrderRepo r = new OrderRepo(null, null, "R1");
		r.setDescription("repo 1");
		Order o1 = new Order(null, null, "O1");
		o1.setDescription("order 1");
		r.getOrders().put(o1.getDescription(), o1);
		open();
		r = save(r);
		flush();
		Long repoId = r.getId();
		close();

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		r = find(OrderRepo.class, repoId);
		Order o2 = new Order(null, null, "O2");
		o2.setDescription("order 2");
		r.getOrders().put(o2.getDescription(), o2);
		flush();
		close();
		
		List<EntityUpdate> updates = DataContext.get().getDataUpdates();
		EntityUpdate update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(OrderRepo.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates repo", update);
		CollectionChanges collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("orders");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		CollectionChange collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll add", 1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof Order);
		Assert.assertEquals("Update coll index", o2.getDescription(), collChange.getKey());
		Assert.assertEquals("Update coll value", o2.getDescription(), ((Order)collChange.getValue()).getDescription());

		DataContext.remove();
		DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
		
		open();
		r = find(OrderRepo.class, repoId);
		r.getOrders().remove(o1.getDescription());
		flush();
		close();
		
		updates = DataContext.get().getDataUpdates();
		update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(OrderRepo.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates repo", update);
		collChanges = (CollectionChanges)((Change)update.entity).getChanges().get("orders");
		Assert.assertEquals("Update collection", 1, collChanges.getChanges().length);
		collChange = collChanges.getChanges()[0];
		Assert.assertEquals("Update coll remove", -1, collChange.getType());
		Assert.assertTrue("Update coll type", collChange.getValue() instanceof Order);
		Assert.assertEquals("Update coll index", o1.getDescription(), collChange.getKey());
		Assert.assertEquals("Update coll value", o1.getDescription(), ((Order)collChange.getValue()).getDescription());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testReorderList2() throws Exception {
		initPersistence();
		
		open();
		
		Classification p = new Classification(null, null, "P1");
		p.setCode("P1");
		p.setSubClassifications(new PersistentList(null, new ArrayList<Classification>()));
		Classification c1 = new Classification(null, null, "C1");
		c1.setCode("C1");
		Classification c2 = new Classification(null, null, "C2");
		c2.setCode("C2");
		Classification c3 = new Classification(null, null, "C3");
		c3.setCode("C3");
		Classification c4 = new Classification(null, null, "C4");
		c4.setCode("C4");
		p.getSubClassifications().add(c1);
		p.getSubClassifications().add(c2);
		p.getSubClassifications().add(c3);
		p.getSubClassifications().add(c4);
		
		p = save(p);
		flush();
		Long pId = p.getId();
		Long c4Id = p.getSubClassifications().get(3).getId();
		close();
		
		open();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/data/enterprise/granite-config.xml");
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		DataContext.init(null, null, PublishMode.MANUAL);
		
		Change change = new Change(Classification.class.getName(), pId, p.getVersion(), p.getUid());
		CollectionChange collChange1 = new CollectionChange(-1, 3, new ChangeRef(Classification.class.getName(), "C4", new Integer(c4Id.intValue())));
		CollectionChange collChange2 = new CollectionChange(1, 2, new ChangeRef(Classification.class.getName(), "C4", new Integer(c4Id.intValue())));
		CollectionChanges collChanges = new CollectionChanges(new CollectionChange[] { collChange1, collChange2 });
		change.getChanges().put("subClassifications", collChanges);
		
		ChangeSet changeSet = new ChangeSet(new Change[] { change });
		
		new ChangeSetApplier(newPersistenceAdapter()).applyChanges(changeSet);
		flush();
		close();
		
		List<EntityUpdate> updates = DataContext.get().getDataUpdates();
		
		Assert.assertEquals("1 update", 1, updates.size());
		Assert.assertTrue("Change update", updates.get(0).entity instanceof Change);
		Change ch = (Change)updates.get(0).entity;
		CollectionChanges cch = (CollectionChanges)ch.getChanges().get("subClassifications");
		Assert.assertEquals("Coll changes", 2, cch.getChanges().length);
		Assert.assertEquals("Coll change 1 del", -1, cch.getChanges()[0].getType());
		Assert.assertEquals("Coll change 2 add", 1, cch.getChanges()[1].getType());		
	}
		
	@Test
	public void testInheritedSetChanges() throws Exception {
		initPersistence();
		
		Person4 p = new Person4(null, null, "P1");
		p.setPhones(new ArrayList<Phone4>());
		Phone4 ph = new Phone4(null, null, "PH1");
		ph.setPhone("01 01 01 01 01");
		ph.setLegalEntity(p);
		p.getPhones().add(ph);
		open();
		p = save(p);
		flush();
		Long pId = p.getId();
		close();
		
		DataContext.init(null, null, PublishMode.MANUAL);
		
		open();
		p = find(Person4.class, pId);
		Phone4 ph2 = new Phone4(null, null, "PH2");
		ph2.setPhone("02 02 02 02 02");
		ph2.setLegalEntity(p);
		p.getPhones().add(ph2);
		flush();
		close();
		
		List<EntityUpdate> updates = DataContext.get().getDataUpdates();
		updates = DataContext.get().getDataUpdates();
		EntityUpdate update = null;
		for (EntityUpdate u : updates) {
			if (u.entity instanceof Change && ((Change)u.entity).getClassName().equals(Person4.class.getName())) {
				update = u;
				break;
			}
		}
		Assert.assertNotNull("Updates person", update);
		Assert.assertTrue("Change update", update.entity instanceof Change);
		Change ch = (Change)update.entity;
		CollectionChanges cch = (CollectionChanges)ch.getChanges().get("phones");
		Assert.assertEquals("Coll changes", 1, cch.getChanges().length);
		Assert.assertEquals("Coll change 2 add", 1, cch.getChanges()[0].getType());		
	}
}