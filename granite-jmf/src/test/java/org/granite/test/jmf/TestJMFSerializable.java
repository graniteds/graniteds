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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.granite.test.jmf.model.BeanWriteReplace;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFSerializable implements JMFConstants {
	
	private CodecRegistry codecRegistry;
	
	@Before
	public void before() {
		codecRegistry = new DefaultCodecRegistry();
	}
	
	@After
	public void after() {
		codecRegistry = null;
	}

	@Test
	public void testWriteObjectReadObject() throws ClassNotFoundException, IOException {
		
		LinkedHashMap<Object, Object> hashMap = new LinkedHashMap<Object, Object>();
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new LinkedHashMap<Object, Object>();
		hashMap.put(null, null);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new LinkedHashMap<Object, Object>();
		hashMap.put(Boolean.TRUE, Boolean.FALSE);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new LinkedHashMap<Object, Object>();
		hashMap.put("John", "Doe");
		hashMap.put(Boolean.FALSE, null);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new LinkedHashMap<Object, Object>();
		hashMap.put("John", "Doe");
		hashMap.put(Boolean.FALSE, null);
		hashMap.put(Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)0));
		hashMap.put(null, hashMap);
		int[] ints = new int[] {1,2,3,4,5,6};
		short[] shorts = new short[] {7,8,9,10};
		hashMap.put(ints, shorts);
		
//		byte[] bytes = Util.serializeJava(hashMap);
//		System.out.println("Serialization Java: " + bytes.length + "B.");
		
		LinkedHashMap<?, ?> clone = (LinkedHashMap<?, ?>)serializeDeserialize(hashMap);
		Assert.assertEquals(hashMap.size(), clone.size());
		Assert.assertTrue(clone.containsKey("John"));
		Assert.assertTrue("Doe".equals(clone.get("John")));
		Assert.assertTrue(clone.containsKey(Byte.valueOf(Byte.MAX_VALUE)));
		Assert.assertTrue(Byte.valueOf((byte)0).equals(clone.get(Byte.valueOf(Byte.MAX_VALUE))));
		
		boolean foundArray = false;
		boolean foundMap = false;
		for (Map.Entry<?, ?> o : clone.entrySet()) {
			if (o.getKey() == null) {
				foundMap = true;
				Assert.assertTrue(o.getValue() == clone);
			}
			else if (o.getKey().getClass().isArray() && o.getKey() instanceof int[]) {
				foundArray = true;
				Assert.assertTrue(Arrays.equals(ints, (int[])o.getKey()));
				Assert.assertTrue(Arrays.equals(shorts, (short[])o.getValue()));
			}
		}
		Assert.assertTrue(foundArray && foundMap);
	}

	@Test
	public void testWriteReplaceReadResolve() throws ClassNotFoundException, IOException {
		BeanWriteReplace bean = new BeanWriteReplace(75);
		Object clone = serializeDeserialize(bean);
		Assert.assertTrue(clone instanceof BeanWriteReplace);
		Assert.assertEquals(bean.getValue(), ((BeanWriteReplace)clone).getValue());
		
		BeanWriteReplace[] beans = new BeanWriteReplace[]{bean, bean};
		clone = serializeDeserialize(beans);
		Assert.assertTrue(clone != null && clone.getClass().isArray() && clone.getClass().getComponentType() == BeanWriteReplace.class);
		Assert.assertEquals(beans.length, ((BeanWriteReplace[])clone).length);
		Assert.assertEquals(beans[0].getValue(), ((BeanWriteReplace[])clone)[0].getValue());
		Assert.assertTrue(((BeanWriteReplace[])clone)[0] == ((BeanWriteReplace[])clone)[1]);
	}
	
	private Object serializeDeserialize(Object v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private Object serializeDeserialize(Object v, boolean dump) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		PrintStream ps = Util.newNullPrintStream();
		if (dump) {
			System.out.println(serializer.toDumpString());
			System.out.println(bytes.length + "B. " + Util.toHexString(bytes));
			ps = System.out;
		}
		
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		Object clone = deserializer.readObject();
		deserializer.close();
		if (dump)
			System.out.println(deserializer.toDumpString());
		return clone;
	}
}
