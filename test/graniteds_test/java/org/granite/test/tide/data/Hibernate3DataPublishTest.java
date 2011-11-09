package org.granite.test.tide.data;

import java.io.Serializable;
import java.util.HashSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import junit.framework.Assert;

import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.granite.tide.data.DefaultDataDispatcher;
import org.granite.tide.data.DefaultDataTopicParams;
import org.hibernate.ejb.Ejb3Configuration;
import org.junit.Test;

public class Hibernate3DataPublishTest {
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private EntityTransaction tx;
	
	protected void initPersistence() {
		Ejb3Configuration configuration = new Ejb3Configuration()
			.addAnnotatedClass(AbstractEntity0.class)
			.addAnnotatedClass(Order3.class)
			.addAnnotatedClass(LineItem.class)
            .setProperty("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName())
            .setProperty("hibernate.hbm2ddl.auto", "create-drop")
            .setProperty("hibernate.show_sql", "true")
            .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
            .setProperty("hibernate.connection.url", "jdbc:h2:mem:test-publish")
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
		flush(true);
	}
	protected void flush(boolean commit) {
		entityManager.flush();
		if (commit)
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
        DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
        
        open();
        o = find(Order3.class, orderId);
        i2 = find(LineItem.class, itemId);
        remove(i2);
        flush();
        close();
        
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("1 update", 1, updates.length);
        Assert.assertEquals("REMOVE", updates[0][0]);
        Assert.assertEquals(i2, updates[0][1]);
    }
    
    @Test
    public void testSimpleChanges2() throws Exception {
        initPersistence();
        
        Order3 o = new Order3(null, null, "O1");
        o.setDescription("Order");
        o.setLineItems(new HashSet<LineItem>());
        LineItem i1 = new LineItem(null, null, "I1");
        i1.setDescription("Item 1");
        i1.setOrder(o);
        o.getLineItems().add(i1);
        open();
        o = save(o);
        flush();
        Long orderId = o.getId();
        close();
        
        DataContext.remove();
        DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
        
        open();
        o = find(Order3.class, orderId);
        LineItem i2 = new LineItem(null, null, "I2");
        i2.setDescription("Item 2");
        i2.setOrder(o);
        o.getLineItems().add(i2);
        flush();
        close();
        
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("2 updates", 2, updates.length);
        Assert.assertEquals("PERSIST", updates[0][0]);
        Assert.assertEquals(i2, updates[0][1]);
        Assert.assertEquals("UPDATE", updates[1][0]);
        Assert.assertEquals(o, updates[1][1]);
    }
    
    @Test
    public void testSimpleChanges3() throws Exception {
        initPersistence();
        
        Order3 o = new Order3(null, null, "O1");
        o.setDescription("Order");
        o.setLineItems(new HashSet<LineItem>());
        LineItem i1 = new LineItem(null, null, "I1");
        i1.setDescription("Item 1");
        i1.setOrder(o);
        o.getLineItems().add(i1);
        open();
        o = save(o);
        flush();
        Long orderId = o.getId();
        close();
        
        DataContext.remove();
        DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
        
        open();
        o = find(Order3.class, orderId);
        LineItem i2 = new LineItem(null, null, "I2");
        i2.setDescription("Item 2");
        i2.setOrder(o);
        o.getLineItems().add(i2);
        flush(false);
        i2.setDescription("Item 2b");
        flush();
        close();
        
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("3 updates", 3, updates.length);
        Assert.assertEquals("PERSIST", updates[0][0]);
        Assert.assertEquals(i2, updates[0][1]);
        Assert.assertEquals("UPDATE", updates[1][0]);
        Assert.assertEquals(i2, updates[1][1]);
        Assert.assertEquals("UPDATE", updates[2][0]);
        Assert.assertEquals(o, updates[2][1]);
    }
}
