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
package org.granite.client.test.javafx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.granite.client.configuration.ClientGraniteConfig;
import org.granite.client.configuration.Configuration;
import org.granite.client.javafx.platform.SimpleJavaFXConfiguration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.config.AMF3Config;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestExternalizerMap {
	
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
	public void testExternalizationMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", 89L);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(map);
		out.close();
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = ((AMF3Config)graniteConfigJavaFX).newAMF3Deserializer(bais);
		Object entity = in.readObject();
		in.close();
		
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
		out.close();
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = ((AMF3Config)graniteConfigJavaFX).newAMF3Deserializer(bais);
		Object entity = in.readObject();
		in.close();
		
		Assert.assertTrue("Map type", entity instanceof Map);
		Assert.assertEquals("Map value", new Long(89), ((Map<Integer, Long>)entity).get(34));
	}
}
