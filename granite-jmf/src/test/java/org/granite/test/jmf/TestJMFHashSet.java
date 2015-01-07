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
import java.util.HashSet;

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

public class TestJMFHashSet implements JMFConstants {
	
	private CodecRegistry codecRegistry;
	
	@Before
	public void before() {
		codecRegistry = new DefaultCodecRegistry();
	}
	
	@After
	public void after() {
		codecRegistry = null;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHashSet() throws ClassNotFoundException, IOException {
		
		HashSet<?> hashSet = new HashSet<Object>();
		Assert.assertEquals(hashSet, serializeDeserialize(hashSet));
		
		hashSet = new HashSet<Object>(Arrays.asList(new Object[]{Boolean.TRUE}));
		Assert.assertEquals(hashSet, serializeDeserialize(hashSet));
		
		hashSet = new HashSet<Boolean>(Arrays.asList(new Boolean[]{Boolean.TRUE, Boolean.TRUE, Boolean.FALSE}));
		Assert.assertEquals(hashSet, serializeDeserialize(hashSet));
		
		hashSet = new HashSet<Object>(Arrays.asList(new Object[]{Boolean.TRUE, Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)0), Double.NaN}));
		Assert.assertEquals(hashSet, serializeDeserialize(hashSet));
		
		int[] ints = new int[] {1,2,3,4,5,6};
		hashSet = new HashSet<Object>(Arrays.asList(new Object[]{Boolean.TRUE, Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)0), null, Double.NaN, ints}));
		((HashSet<Object>)hashSet).add(hashSet);

//		byte[] bytes = Util.serializeJava(hashSet);
//		System.out.println("Serialization Java: " + bytes.length + "B.");

		HashSet<?> clone = serializeDeserialize(hashSet);
		Assert.assertTrue(hashSet.size() == clone.size());
		Assert.assertTrue(clone.contains(Boolean.TRUE));
		Assert.assertTrue(clone.contains(Byte.valueOf(Byte.MAX_VALUE)));
		Assert.assertTrue(clone.contains(Byte.valueOf((byte)0)));
		Assert.assertTrue(clone.contains(null));
		Assert.assertTrue(clone.contains(Double.NaN));
		
		boolean foundArray = false;
		boolean foundSet = false;
		for (Object o : clone) {
			if (o != null) {
				if (o.getClass().isArray() && o instanceof int[]) {
					foundArray = true;
					Assert.assertTrue(Arrays.equals(ints, (int[])o));
				}
				else if (o instanceof HashSet) {
					foundSet = true;
					Assert.assertTrue(o == clone);
				}
			}
		}
		Assert.assertTrue(foundArray && foundSet);
	}
	
	private HashSet<?> serializeDeserialize(HashSet<?> v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private HashSet<?> serializeDeserialize(HashSet<?> v, boolean dump) throws ClassNotFoundException, IOException {
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
		HashSet<?> clone = (HashSet<?>)deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
