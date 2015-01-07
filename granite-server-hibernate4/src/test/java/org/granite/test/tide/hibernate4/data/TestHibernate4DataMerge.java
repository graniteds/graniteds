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
package org.granite.test.tide.hibernate4.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.DefaultClientSharedContext;
import org.granite.client.messaging.jmf.ext.ClientEntityCodec;
import org.granite.config.AMF3Config;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.hibernate4.jmf.EntityCodec;
import org.granite.hibernate4.jmf.PersistentBagCodec;
import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.jmf.SharedContext;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.test.tide.data.Entree;
import org.granite.test.tide.data.EntreeObs;
import org.granite.test.tide.hibernate4.data.client.AbstractClientEntity;
import org.granite.test.tide.hibernate4.data.client.ClientKeyValue;
import org.granite.test.tide.hibernate4.data.client.ClientPerson10;
import org.junit.Assert;
import org.junit.Test;

public class TestHibernate4DataMerge {
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private EntityTransaction tx;
	
	protected void initPersistence() {
		Map<String, String> props = new HashMap<String, String>();
		props.put("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName());
		props.put("hibernate.hbm2ddl.auto", "create-drop");
		props.put("hibernate.show_sql", "true");
		props.put("hibernate.connection.driver_class", "org.h2.Driver");
		props.put("hibernate.connection.url", "jdbc:h2:mem:test-publish");
		props.put("hibernate.connection.username", "sa");
		props.put("hibernate.connection.password", "");
		
		entityManagerFactory = Persistence.createEntityManagerFactory("hibernate4-merge-pu", props);
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
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/hibernate4/data/granite-config-hibernate4.xml");
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
		out.close();
		
		open();
		
		ObjectInput in = ((AMF3Config)gc.getGraniteConfig()).newAMF3Deserializer(new ByteArrayInputStream(baos.toByteArray()));
		entree = (Entree)in.readObject();
		in.close();
		
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
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/test/tide/hibernate4/data/granite-config-hibernate4.xml");
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
		out.close();
		
		open();
		
		ObjectInput in = ((AMF3Config)gc.getGraniteConfig()).newAMF3Deserializer(new ByteArrayInputStream(baos.toByteArray()));
		entree = (Entree)in.readObject();
		in.close();
		
		entree = save(entree);
		
		flush();
		close();
		
		open();
		
		entree = find(Entree.class, entreeId);
		
		close();
		
		Assert.assertEquals("Entree updated", "test2", entree.getName());
	}
	
	@Test
	public void testMergeJMFList() throws Exception {
		initPersistence();
		
		open();
		
		Person10 person = new Person10(null, null, "P1");
		person.setLastName("Blo");
		person = save(person);		
		
		flush();
		
		Long personId = person.getId();
		close();
		
		open();
		
		person = find(Person10.class, personId);
		person.getParameters().toString();
		
		close();

		CodecRegistry serverCodecRegistry = new DefaultCodecRegistry(Arrays.asList(new EntityCodec(), new PersistentBagCodec()));
		SharedContext serverSharedContext = new DefaultSharedContext(serverCodecRegistry);
		CodecRegistry clientCodecRegistry = new DefaultCodecRegistry(Arrays.asList((ExtendedObjectCodec)new ClientEntityCodec()));		
		ClientSharedContext clientSharedContext = new DefaultClientSharedContext(clientCodecRegistry, new ArrayList<String>(), null, new ClientAliasRegistry());
		clientSharedContext.getAliasRegistry().registerAlias(AbstractClientEntity.class);
		clientSharedContext.getAliasRegistry().registerAlias(ClientPerson10.class);
		clientSharedContext.getAliasRegistry().registerAlias(ClientKeyValue.class);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(6000);
		JMFSerializer ser = new JMFSerializer(baos, serverSharedContext);
		ser.writeObject(person);
		ser.close();
		
		JMFDeserializer des = new JMFDeserializer(new ByteArrayInputStream(baos.toByteArray()), clientSharedContext);
		ClientPerson10 cperson = (ClientPerson10)des.readObject();
		des.close();

		cperson.getParameters().add(new ClientKeyValue(null, null, "KV1", "test", "test"));		
		
		baos = new ByteArrayOutputStream(6000);
		ser = new JMFSerializer(baos, clientSharedContext);
		ser.writeObject(cperson);
		ser.close();
		
		des = new JMFDeserializer(new ByteArrayInputStream(baos.toByteArray()), serverSharedContext);
		person = (Person10)des.readObject();
		des.close();
		
		open();
		
		person = save(person);
		
		flush();
		close();
		
		open();
		
		person = find(Person10.class, personId);
		person.getParameters().toString();
		
		TypedQuery<KeyValue> qkv = entityManager.createQuery("select kv from KeyValue kv", KeyValue.class);
		List<KeyValue> list = qkv.getResultList();		
		
		close();
		
		Assert.assertEquals("Person updated", 1, person.getParameters().size());
		Assert.assertNotNull("KeyValue saved", person.getParameters().get(0).getId());
		Assert.assertEquals("KeyValue saved", Long.valueOf(0L), person.getParameters().get(0).getVersion());
		
		Assert.assertEquals("KeyValue count", 1, list.size());
	}
}
