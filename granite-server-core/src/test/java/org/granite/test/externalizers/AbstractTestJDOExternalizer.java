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
package org.granite.test.externalizers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.granite.config.AMF3Config;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractTestJDOExternalizer {

	protected GraniteConfig graniteConfig;
	protected ServicesConfig servicesConfig;
	protected PersistenceManagerFactory persistenceManagerFactory;

	@Before
	public void before() throws Exception {
		Properties props = new Properties();
		String provider = setProperties(props);
		persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(props);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("WEB-INF/granite/granite-config-" + provider + ".xml");
		graniteConfig = new GraniteConfig(null, is, null, null);
		servicesConfig = new ServicesConfig(null, null, false);
	}
	
	protected abstract String setProperties(Properties props);
	
	
	
	@Test
	public void testSerializationLazy() throws Exception {
		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
		persistenceManager.currentTransaction().begin();
		
		EntityJDO5 e5 = new EntityJDO5();
		e5.setName("Test");
		e5.setEntities(new HashSet<EntityJDO6>());
		EntityJDO6 e6 = new EntityJDO6();
		e6.setName("Test");
		e5.getEntities().add(e6);
		e6.setEntity5(e5);
		persistenceManager.makePersistent(e5);
		
		persistenceManager.flush();
		persistenceManager.currentTransaction().commit();
		persistenceManager.close();
		
		persistenceManager = persistenceManagerFactory.getPersistenceManager();
		persistenceManager.currentTransaction().begin();
		
		e5 = persistenceManager.getObjectById(EntityJDO5.class, e5.getId());
		
		persistenceManager.flush();
		persistenceManager.currentTransaction().commit();
		persistenceManager.close();
		
		GraniteContext gc = SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = ((AMF3Config)gc.getGraniteConfig()).newAMF3Serializer(baos);
		out.writeObject(e5);	
		out.close();
		GraniteContext.release();
		
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		gc = SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		ObjectInput in = ((AMF3Config)gc.getGraniteConfig()).newAMF3Deserializer(is);
		Object obj = in.readObject();
		in.close();
		GraniteContext.release();
		
		Assert.assertTrue("Entity5", obj instanceof EntityJDO5);
	}
	
//	@Test
//	public void testSerializationLazyEmbeddedGDS838() throws Exception {
//		EntityManager entityManager = entityManagerFactory.createEntityManager();
//		EntityTransaction et = entityManager.getTransaction();
//		et.begin();
//		
//		Entity2 e2 = new Entity2();
//		e2.setName("Test");
//		Entity3 e3 = new Entity3();
//		e3.setEname("Tutu");
//		e3.setEntities(new HashSet<Entity4>());
//		e2.setEntity(e3);
//		Entity4 e4 = new Entity4();
//		e4.setName("Test");
//		e3.getEntities().add(e4);
//		e4.setEntity2(e2);
//		entityManager.persist(e2);
//		
//		entityManager.flush();
//		et.commit();
//		entityManager.close();
//		
//		entityManager = entityManagerFactory.createEntityManager();		
//		et = entityManager.getTransaction();
//		et.begin();
//		
//		e2 = entityManager.find(Entity2.class, e2.getId());
//		
//		entityManager.flush();
//		et.commit();
//		entityManager.close();
//		
//		GraniteContext gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//		ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
//		out.writeObject(e2);		
//		GraniteContext.release();
//		
//		InputStream is = new ByteArrayInputStream(baos.toByteArray());
//		gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//		ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(is);
//		Object obj = in.readObject();
//		GraniteContext.release();
//		
//		Assert.assertTrue("Entity2", obj instanceof Entity2);
//		if (gc.getGraniteConfig().getClassGetter().getClass().getName().indexOf("hibernate") >= 0)
//		    Assert.assertFalse("Entity 2 entities not loaded", gc.getGraniteConfig().getClassGetter().isInitialized(((Entity2)obj).getEntity(), "entities", ((Entity2)obj).getEntity().getEntities()));
//	}
//    
//    @Test
//    public void testSerializationUnmanagedEntity() throws Exception {
//        Entity6 e6 = new Entity6();
//        e6.setName("Test");
//        
//        GraniteContext gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//        ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
//        out.writeObject(e6);        
//        GraniteContext.release();
//        
//        InputStream is = new ByteArrayInputStream(baos.toByteArray());
//        gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//        ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(is);
//        Object obj = in.readObject();
//        GraniteContext.release();
//        
//        Assert.assertTrue("Entity6", obj instanceof Entity6);
//    }
//    
//    @Test
//    public void testSerializationProxy() throws Exception {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        EntityTransaction et = entityManager.getTransaction();
//        et.begin();
//        
//        Entity8 e8 = new Entity8();
//        e8.setName("Test");
//        Entity7 e7 = new Entity7();
//        e7.setName("Test");
//        e7.setEntity8(e8);
//        entityManager.persist(e7);
//        
//        entityManager.flush();
//        et.commit();
//        entityManager.close();
//        
//        entityManager = entityManagerFactory.createEntityManager();     
//        et = entityManager.getTransaction();
//        et.begin();
//        
//        e7 = entityManager.find(Entity7.class, e7.getId());
//        
//        entityManager.flush();
//        et.commit();
//        entityManager.close();
//        
//        GraniteContext gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//        
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//        ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
//        
//        out.writeObject(e7);        
//        GraniteContext.release();
//        
//        InputStream is = new ByteArrayInputStream(baos.toByteArray());
//        gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//        ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(is);
//        Object obj = in.readObject();
//        GraniteContext.release();
//        
//        Assert.assertTrue("Entity7", obj instanceof Entity7);
//        Assert.assertFalse("Entity8 not loaded", gc.getGraniteConfig().getClassGetter().isInitialized(obj, "entity8", ((Entity7)obj).getEntity8()));
//    }
//    
//    @Test
//    public void testSerializationProxy2() throws Exception {
//        GraniteContext gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        EntityTransaction et = entityManager.getTransaction();
//        et.begin();
//        
//        Entity8 e8 = new Entity8();
//        e8.setName("Test");
//        Entity7 e7 = new Entity7();
//        e7.setName("Test");
//        e7.setEntity8(e8);
//        entityManager.persist(e7);
//        
//        entityManager.flush();
//        et.commit();
//        entityManager.close();
//        
//        entityManager = entityManagerFactory.createEntityManager();     
//        et = entityManager.getTransaction();
//        et.begin();
//        
//        e7 = entityManager.find(Entity7.class, e7.getId());
//        
//        gc.getGraniteConfig().getClassGetter().initialize(e7, "entity8", e7.getEntity8());
//        
//        entityManager.flush();
//        et.commit();
//        entityManager.close();
//        
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//        ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
//        out.writeObject(e7);        
//        GraniteContext.release();
//        
//        InputStream is = new ByteArrayInputStream(baos.toByteArray());
//        gc = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
//        ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(is);
//        Object obj = in.readObject();
//        GraniteContext.release();
//        
//        Assert.assertTrue("Entity7", obj instanceof Entity7);
//        Assert.assertTrue("Entity8 loaded", gc.getGraniteConfig().getClassGetter().isInitialized(obj, "entity8", ((Entity7)obj).getEntity8()));
//    }
}
