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
import java.util.HashMap;
import java.util.Map;

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

public class TestJMFHashMap implements JMFConstants {
	
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
	public void testHashMap() throws ClassNotFoundException, IOException {
		
		HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new HashMap<Object, Object>();
		hashMap.put(null, null);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new HashMap<Object, Object>();
		hashMap.put(Boolean.TRUE, Boolean.FALSE);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new HashMap<Object, Object>();
		hashMap.put("John", "Doe");
		hashMap.put(Boolean.FALSE, null);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new HashMap<Object, Object>();
		hashMap.put("John", "Doe");
		hashMap.put(Boolean.FALSE, null);
		hashMap.put(Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)0));
		hashMap.put(null, hashMap);
		int[] ints = new int[] {1,2,3,4,5,6};
		short[] shorts = new short[] {7,8,9,10};
		hashMap.put(ints, shorts);
		
//		byte[] bytes = Util.serializeJava(hashMap);
//		System.out.println("Serialization Java: " + bytes.length + "B.");
		
		HashMap<?, ?> clone = serializeDeserialize(hashMap);
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
	
	private HashMap<?, ?> serializeDeserialize(HashMap<?, ?> v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private HashMap<?, ?> serializeDeserialize(HashMap<?, ?> v, boolean dump) throws ClassNotFoundException, IOException {
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
		HashMap<?, ?> clone = (HashMap<?, ?>)deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
