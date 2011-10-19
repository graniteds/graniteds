package org.granite.test.externalizers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractJPAExternalizerTest {

	protected GraniteConfig graniteConfig;
	protected ServicesConfig servicesConfig;
	protected EntityManagerFactory entityManagerFactory;

	@Before
	public void before() throws Exception {
		Properties props = new Properties();
		props.put("javax.persistence.jdbc.driver", "org.h2.Driver");
		props.put("javax.persistence.jdbc.url", "jdbc:h2:mem:testdb");
		props.put("javax.persistence.jdbc.user", "sa");
		props.put("javax.persistence.jdbc.password", "");
		String provider = setProperties(props);
		entityManagerFactory = Persistence.createEntityManagerFactory(provider + "-pu", props);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("WEB-INF/granite/granite-config-" + provider + ".xml");
		graniteConfig = new GraniteConfig(null, is, null, null);
		servicesConfig = new ServicesConfig(null, null, false);
	}
	
	protected abstract String setProperties(Properties props);
	
	
	
	@Test
	public void testSerializationLazy() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction et = entityManager.getTransaction();
		et.begin();
		
		Entity5 e5 = new Entity5();
		e5.setName("Test");
		e5.setEntities(new HashSet<Entity6>());
		Entity6 e6 = new Entity6();
		e6.setName("Test");
		e5.getEntities().add(e6);
		e6.setEntity5(e5);
		entityManager.persist(e5);
		
		entityManager.flush();
		et.commit();
		entityManager.close();
		
		entityManager = entityManagerFactory.createEntityManager();		
		et = entityManager.getTransaction();
		et.begin();
		
		e5 = entityManager.find(Entity5.class, e5.getId());
		
		entityManager.flush();
		et.commit();
		entityManager.close();
		
		GraniteContext gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
		out.writeObject(e5);		
		GraniteContext.release();
		
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(is);
		Object obj = in.readObject();
		GraniteContext.release();
		
		Assert.assertTrue("Entity5", obj instanceof Entity5);
	}
	
	@Test
	public void testSerializationLazyEmbeddedGDS838() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction et = entityManager.getTransaction();
		et.begin();
		
		Entity2 e2 = new Entity2();
		e2.setName("Test");
		Entity3 e3 = new Entity3();
		e3.setEname("Tutu");
		e3.setEntities(new HashSet<Entity4>());
		e2.setEntity(e3);
		Entity4 e4 = new Entity4();
		e4.setName("Test");
		e3.getEntities().add(e4);
		e4.setEntity2(e2);
		entityManager.persist(e2);
		
		entityManager.flush();
		et.commit();
		entityManager.close();
		
		entityManager = entityManagerFactory.createEntityManager();		
		et = entityManager.getTransaction();
		et.begin();
		
		e2 = entityManager.find(Entity2.class, e2.getId());
		
		entityManager.flush();
		et.commit();
		entityManager.close();
		
		GraniteContext gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
		out.writeObject(e2);		
		GraniteContext.release();
		
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(is);
		Object obj = in.readObject();
		GraniteContext.release();
		
		Assert.assertTrue("Entity2", obj instanceof Entity2);
		providerSpecificAsserts((Entity2)obj);
	}
	
	protected void providerSpecificAsserts(Entity2 obj) {
	}
}
