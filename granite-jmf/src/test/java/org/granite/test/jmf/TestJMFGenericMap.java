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
package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFGenericMap implements JMFConstants {
	
	private CodecRegistry codecRegistry;
	
	@Before
	public void before() {
		codecRegistry = new DefaultCodecRegistry();
	}
	
	@After
	public void after() {
		codecRegistry = null;
	}
	
	static enum TestEnum {
		ONE,
		TWO,
		THREE,
		FOUR,
		FIVE,
		SIX,
		SEVEN,
		EIGHT,
		NINE,
		TEN
	};

	@Test
	public void testGenericCollection() throws ClassNotFoundException, IOException {
		
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		Map<?, ?> clone = serializeDeserialize(map);
		Assert.assertEquals(map.getClass(), clone.getClass());
		Assert.assertEquals(map, clone);
		
		map = new LinkedHashMap<Object, Object>();
		map.put("bla", Boolean.TRUE);
		map.put("bli", Boolean.FALSE);
		clone = serializeDeserialize(map);
		Assert.assertEquals(map.getClass(), clone.getClass());
		Assert.assertEquals(map, clone);
		
		map = new Hashtable<Object, Object>();
		clone = serializeDeserialize(map);
		Assert.assertEquals(map.getClass(), clone.getClass());
		Assert.assertEquals(map, clone);
		
		map = new Hashtable<Object, Object>();
		map.put("bla", Boolean.TRUE);
		map.put("bli", Boolean.FALSE);
		clone = serializeDeserialize(map);
		Assert.assertEquals(map.getClass(), clone.getClass());
		Assert.assertEquals(map, clone);
		
		map = new TreeMap<Object, Object>();
		clone = serializeDeserialize(map);
		Assert.assertEquals(TreeMap.class, clone.getClass());
		Assert.assertEquals(map.size(), clone.size());
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			Assert.assertTrue(clone.containsKey(entry.getKey()));
			Assert.assertEquals(entry.getValue(), clone.get(entry.getKey()));
		}
		
		map = new TreeMap<Object, Object>();
		map.put("bli", Boolean.FALSE);
		map.put("bla", Boolean.TRUE);
		map.put("blu", Boolean.FALSE);
		clone = serializeDeserialize(map);
		Assert.assertEquals(TreeMap.class, clone.getClass());
		Assert.assertEquals(map.size(), clone.size());
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			Assert.assertTrue(clone.containsKey(entry.getKey()));
			Assert.assertEquals(entry.getValue(), clone.get(entry.getKey()));
		}
		
		Properties properties = new Properties();
		clone = serializeDeserialize(properties);
		Assert.assertEquals(properties.getClass(), clone.getClass());
		Assert.assertEquals(properties, clone);
		
		properties = new Properties();
		properties.setProperty("bla", "blo");
		properties.setProperty("bli", "blu");
		clone = serializeDeserialize(properties);
		Assert.assertEquals(properties.getClass(), clone.getClass());
		Assert.assertEquals(properties, clone);
		
		// Use standard object codec with writeObject / readObject...
		Map<TestEnum, Boolean> emap = new EnumMap<TestEnum, Boolean>(TestEnum.class);
		clone = serializeDeserialize(emap, true);
		Assert.assertEquals(emap.getClass(), clone.getClass());
		Assert.assertEquals(emap, clone);
		
		emap = new EnumMap<TestEnum, Boolean>(TestEnum.class);
		emap.put(TestEnum.EIGHT, Boolean.TRUE);
		emap.put(TestEnum.ONE, Boolean.TRUE);
		clone = serializeDeserialize(emap, true);
		Assert.assertEquals(emap.getClass(), clone.getClass());
		Assert.assertEquals(emap, clone);
	}
	
	private Map<?, ?> serializeDeserialize(Map<?, ?> v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private Map<?, ?> serializeDeserialize(Map<?, ?> v, boolean dump) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		PrintStream ps = Util.newNullPrintStream();
		if (dump) {
			System.out.println(bytes.length + "B. " + Util.toHexString(bytes));
			ps = System.out;
		}
		
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		Map<?, ?> clone = (Map<?, ?>)deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
