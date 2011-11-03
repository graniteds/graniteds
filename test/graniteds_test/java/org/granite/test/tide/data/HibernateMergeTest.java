package org.granite.test.tide.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import junit.framework.Assert;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.hibernate.ejb.Ejb3Configuration;
import org.junit.Test;

public class HibernateMergeTest {
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private EntityTransaction tx;
	
	protected void initPersistence() {
		Ejb3Configuration configuration = new Ejb3Configuration()
			.addAnnotatedClass(Entree.class)
			.addAnnotatedClass(EntreeObs.class)
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
	public void testMergeLazyOneToOne() throws Exception {
		initPersistence();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/data/granite-config.xml");
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		GraniteContext gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		open();
		
		Entree entree = new Entree();
		entree.setName("test");
		entree = save(entree);
		
		flush();
		
		Long entreeId = entree.getId();
		close();
		
		open();
		
		entree = find(Entree.class, entreeId);
		entree.setName("test2");
		
		close();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(6000);
		ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
		out.writeObject(entree);
		
		open();
		
		ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(new ByteArrayInputStream(baos.toByteArray()));
		entree = (Entree)in.readObject();
		
		entree = save(entree);
		
		flush();
		close();
		
		open();
		
		entree = find(Entree.class, entreeId);
		
		close();
		
		Assert.assertEquals("Entree updated", "test2", entree.getName());
	}
	
	@Test
	public void testMergeLazyOneToOne2() throws Exception {
		initPersistence();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/data/granite-config.xml");
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		GraniteContext gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		open();
		
		Entree entree = new Entree();
		entree.setName("test");
		EntreeObs entreeObs = new EntreeObs();
		entree.setEntreeObs(entreeObs);
		entreeObs.setEntree(entree);
		entree = save(entree);		
		
		flush();
		
		Long entreeId = entree.getId();
		close();
		
		open();
		
		entreeObs = find(EntreeObs.class, entreeId);
		
		entree = find(Entree.class, entreeId);
		entree.setName("test2");
		
		close();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(6000);
		ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
		out.writeObject(entree);
		
		open();
		
		ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(new ByteArrayInputStream(baos.toByteArray()));
		entree = (Entree)in.readObject();
		
		entree = save(entree);
		
		flush();
		close();
		
		open();
		
		entree = find(Entree.class, entreeId);
		
		close();
		
		Assert.assertEquals("Entree updated", "test2", entree.getName());
	}
}
