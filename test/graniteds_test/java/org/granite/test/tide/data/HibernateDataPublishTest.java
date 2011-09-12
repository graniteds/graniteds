package org.granite.test.tide.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import junit.framework.Assert;

import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.hibernate.ejb.Ejb3Configuration;
import org.junit.Test;

public class HibernateDataPublishTest {
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private EntityTransaction tx;
	
	protected void initPersistence() {
		Ejb3Configuration configuration = new Ejb3Configuration()
			.addAnnotatedClass(AbstractEntity0.class)
			.addAnnotatedClass(Order3.class)
			.addAnnotatedClass(LineItem.class)
			.setProperty("hibernate.dialect", org.hibernate.dialect.HSQLDialect.class.getName())
			.setProperty("hibernate.hbm2ddl.auto", "create-drop")
			.setProperty("hibernate.show_sql", "true")
			.setProperty("hibernate.connection.driver_class", org.hsqldb.jdbcDriver.class.getName())
			.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:test-publish")
			.setProperty("hibernate.connection.username", "sa")
			.setProperty("hibernate.connection.password", "");
		
		entityManagerFactory = configuration.buildEntityManagerFactory();
	}
	
	protected void open() {
		entityManager = entityManagerFactory.createEntityManager();
		tx = entityManager.getTransaction();
		tx.begin();
	}
	protected <T> T find(Class<T> entityClass, Serializable id) {
		return entityManager.find(entityClass, id);
	}
	protected <T> T save(T entity) {
		return entityManager.merge(entity);
	}
	protected <T> void remove(T entity) {
		entityManager.remove(entity);
	}
	protected void flush() {
		entityManager.flush();
		tx.commit();
	}
	protected void flushOnly() {
		entityManager.flush();
	}
	protected void close() {
		entityManager.clear();
		entityManager.close();
	}

	
	@Test
	public void testSimpleChanges() throws Exception {
		initPersistence();
		
		Order3 o = new Order3(null, null, "O1");
		o.setDescription("Order");
		o.setLineItems(new HashSet<LineItem>());
		LineItem i1 = new LineItem(null, null, "I1");
		i1.setDescription("Item 1");
		i1.setOrder(o);
		o.getLineItems().add(i1);
		LineItem i2 = new LineItem(null, null, "I2");
		i2.setDescription("Item 2");
		i2.setOrder(o);
		o.getLineItems().add(i2);
		open();
		o = save(o);
		flush();
		Long orderId = o.getId();
		for (LineItem i : o.getLineItems()) {
			if ("I2".equals(i.getUid())) {
				i2 = i;
				break;
			}
		}
		Long itemId = i2.getId();
		close();
		
		DataContext.remove();
		DataContext.init(null, PublishMode.MANUAL);
		
		open();
		o = find(Order3.class, orderId);
		i2 = find(LineItem.class, itemId);
		remove(i2);
		flush();
		close();
		
		Set<Object[]> updates = DataContext.get().getDataUpdates();
		
		Assert.assertEquals("1 update", 1, updates.size());
	}
}
