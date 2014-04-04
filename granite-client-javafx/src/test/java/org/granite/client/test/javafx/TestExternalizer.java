/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.test.javafx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.granite.client.configuration.ClientGraniteConfig;
import org.granite.client.configuration.Configuration;
import org.granite.client.javafx.platform.SimpleJavaFXConfiguration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.config.AMF3Config;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestExternalizer {
	
	private ServicesConfig servicesConfig = null;
	private ClientGraniteConfig graniteConfigJavaFX = null;
	private GraniteConfig graniteConfigHibernate = null;
	

	@Before
	public void before() throws Exception {
		Configuration configuration = new SimpleJavaFXConfiguration();
		configuration.load();
		graniteConfigJavaFX = configuration.getGraniteConfig();
		ClientAliasRegistry aliasRegistry = (ClientAliasRegistry)graniteConfigJavaFX.getAliasRegistry();
		aliasRegistry.registerAlias(FXEntity1.class);
		aliasRegistry.registerAlias(FXEntity2.class);
		aliasRegistry.registerAlias(FXEntity1b.class);
		aliasRegistry.registerAlias(FXEntity2b.class);
		aliasRegistry.registerAlias(FXEntity1c.class);
		aliasRegistry.registerAlias(FXEntity2c.class);
		InputStream is = getClass().getClassLoader().getResourceAsStream("org/granite/client/test/javafx/granite-config-hibernate.xml");
		graniteConfigHibernate = new GraniteConfig(null, is, null, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationSetServerToClient() throws Exception {
		Entity1b entity1 = new Entity1b();
		entity1.setName("Test");
		entity1.setList(new PersistentSet(null, new HashSet<Entity2>()));
		Entity2b entity2 = new Entity2b();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = ((AMF3Config)graniteConfigJavaFX).newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof FXEntity1b);
	}

	@Test
	public void testExternalizationSetClientToServer() throws Exception {
		FXEntity1b entity1 = new FXEntity1b();
		entity1.setName("Test");
		FXEntity2b entity2 = new FXEntity2b();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = ((AMF3Config)graniteConfigJavaFX).newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof Entity1b);
	}

	@Test
	public void testExternalizationPersistentSetClientToServer() throws Exception {
		FXEntity1b entity1 = new FXEntity1b();
		entity1.setName("Test");
		FXEntity2b entity2 = new FXEntity2b();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = ((AMF3Config)graniteConfigJavaFX).newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof Entity1b);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationListServerToClient() throws Exception {
		Entity1 entity1 = new Entity1();
		entity1.setName("Test");
		entity1.setList(new PersistentList(null, new ArrayList<Entity2>()));
		Entity2 entity2 = new Entity2();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = ((AMF3Config)graniteConfigJavaFX).newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof FXEntity1);
	}

	@Test
	public void testExternalizationListClientToServer() throws Exception {
		FXEntity1 entity1 = new FXEntity1();
		entity1.setName("Test");
		FXEntity2 entity2 = new FXEntity2();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = ((AMF3Config)graniteConfigJavaFX).newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof Entity1);
	}

	@Test
	public void testExternalizationPersistentListClientToServer() throws Exception {
		FXEntity1 entity1 = new FXEntity1();
		entity1.setName("Test");
		FXEntity2 entity2 = new FXEntity2();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = ((AMF3Config)graniteConfigJavaFX).newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof Entity1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationMapServerToClient() throws Exception {
		Entity1c entity1 = new Entity1c();
		entity1.setName("Test");
		entity1.setMap(new PersistentMap(null, new HashMap<String, Entity2c>()));
		Entity2c entity2 = new Entity2c();
		entity2.setName("Test2");
		entity1.getMap().put("test", entity2);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = ((AMF3Config)graniteConfigJavaFX).newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof FXEntity1c);
		Assert.assertEquals("Entity2 value", "Test2", ((FXEntity1c)entity).getMap().get("test").getName());
	}

	@Test
	public void testExternalizationMapClientToServer() throws Exception {
		FXEntity1c entity1 = new FXEntity1c();
		entity1.setName("Test");
		FXEntity2c entity2 = new FXEntity2c();
		entity2.setName("Test2");
		entity1.getMap().put("test", entity2);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = ((AMF3Config)graniteConfigJavaFX).newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof Entity1c);
		Assert.assertEquals("Entity2 value", "Test2", ((Entity1c)entity).getMap().get("test").getName());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationPerfServerToClient() throws Exception {
		List<Entity1c> list = new ArrayList<Entity1c>(10000);
		for (int i = 0; i < 200; i++) {
			Entity1c entity1 = new Entity1c();
			entity1.setName("Test" + i);
			entity1.setValue(new BigDecimal((i+1)*67.89));
			entity1.setValue2(new BigDecimal((i+1)*23.78));
			entity1.setMap(new PersistentMap(null, new HashMap<String, Entity2c>()));
			Entity2c entity2 = new Entity2c();
			entity2.setName("Test" + i);
			entity1.getMap().put("test" + i, entity2);
			entity1.getMap().put("tost", entity2);
			list.add(entity1);
		}
		
		for (int test = 0; test < 5; test++) {
			long time = System.nanoTime();
			
			SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1000000);
			ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
			out.writeObject(list);
			
			byte[] buf = baos.toByteArray();
			
			long elapsedTimeServer = (System.nanoTime()-time)/1000000;
			System.out.println("Elapsed time server: " + elapsedTimeServer);
			time = System.nanoTime();
			System.out.println("Buf size: " + buf.length);
			
			SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			ObjectInput in = ((AMF3Config)graniteConfigJavaFX).newAMF3Deserializer(bais);
			Object read = in.readObject();
			
			long elapsedTimeClient = (System.nanoTime()-time)/1000000;
			System.out.println("Elapsed time client: " + elapsedTimeClient);
			
			Assert.assertTrue("Result type", read instanceof List<?>);
		}
	}
}
