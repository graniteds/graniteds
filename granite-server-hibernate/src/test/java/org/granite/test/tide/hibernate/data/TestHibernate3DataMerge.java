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
package org.granite.test.tide.hibernate.data;

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

import org.granite.config.AMF3Config;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.test.tide.data.Entree;
import org.granite.test.tide.data.EntreeObs;
import org.hibernate.ejb.Ejb3Configuration;
import org.junit.Assert;
import org.junit.Test;

public class TestHibernate3DataMerge {
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private EntityTransaction tx;
	
	protected void initPersistence() {
		Ejb3Configuration configuration = new Ejb3Configuration()
			.addAnnotatedClass(Entree.class)
			.addAnnotatedClass(EntreeObs.class)
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
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/hibernate/data/granite-config-hibernate.xml");
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		GraniteContext gc = SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
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
		ObjectOutput out = ((AMF3Config)gc.getGraniteConfig()).newAMF3Serializer(baos);
		out.writeObject(entree);
		
		open();
		
		ObjectInput in = ((AMF3Config)gc.getGraniteConfig()).newAMF3Deserializer(new ByteArrayInputStream(baos.toByteArray()));
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
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/hibernate/data/granite-config-hibernate.xml");
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		GraniteContext gc = SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
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
		ObjectOutput out = ((AMF3Config)gc.getGraniteConfig()).newAMF3Serializer(baos);
		out.writeObject(entree);
		
		open();
		
		ObjectInput in = ((AMF3Config)gc.getGraniteConfig()).newAMF3Deserializer(new ByteArrayInputStream(baos.toByteArray()));
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
