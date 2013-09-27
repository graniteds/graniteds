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
import java.util.HashMap;
import java.util.Map;

import org.granite.client.configuration.Configuration;
import org.granite.client.javafx.platform.SimpleJavaFXConfiguration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestExternalizerMap {
	
	private ServicesConfig servicesConfig = null;
	private GraniteConfig graniteConfigJavaFX = null;
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
	public void testExternalizationMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", 89L);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(map);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Map type", entity instanceof Map);
		Assert.assertEquals("Map value", new Long(89), ((Map<String, Long>)entity).get("test"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationMap2() throws Exception {
		Map<Integer, Long> map = new HashMap<Integer, Long>();
		map.put(34, 89L);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(map);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Map type", entity instanceof Map);
		Assert.assertEquals("Map value", new Long(89), ((Map<Integer, Long>)entity).get(34));
	}
}
