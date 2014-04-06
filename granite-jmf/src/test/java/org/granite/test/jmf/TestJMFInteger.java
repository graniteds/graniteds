/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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

import static org.granite.test.jmf.Util.toHexString;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJMFInteger implements JMFConstants {
	
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
	public void testSomeInt() throws IOException {

		checkInt(Integer.MIN_VALUE);
		checkInt(Integer.MIN_VALUE + 1);
		checkInt(Integer.MIN_VALUE + 2);
		
		checkInt(-0x01000000);
		checkInt(-0x00FFFFFF);
		
		checkInt(-0x00010000);
		checkInt(-0x0000FFFF);
		
		checkInt(-0x00000100);
		
		for (int i = -0x00000FF; i < 0; i++)
			checkInt(i);
		
		for (int i = 0; i <= 0x00000FF; i++)
			checkInt(i);

		checkInt(0x00000100);
		
		checkInt(0x0000FFFF);
		checkInt(0x00010000);
		
		checkInt(0x00FFFFFF);
		checkInt(0x01000000);
		
		checkInt(Integer.MAX_VALUE - 1);
		checkInt(Integer.MAX_VALUE);
	}
	
//	@Test
//	@Ignore
//	public void testAllInt() throws IOException {
//		for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
//			checkInt(i);
//		checkInt(Integer.MAX_VALUE);
//	}

	@Test
	public void testSomeVariableInt() throws IOException {

		checkVariableInt(Integer.MIN_VALUE);
		checkVariableInt(Integer.MIN_VALUE + 1);
		checkVariableInt(Integer.MIN_VALUE + 2);
		
		checkVariableInt(-0x08102041);
		checkVariableInt(-0x08102040);
		
		checkVariableInt(-0x00102041);
		checkVariableInt(-0x00102040);

		checkVariableInt(-0x00002041);
		checkVariableInt(-0x00002040);

		checkVariableInt(-0x00000041);
		checkVariableInt(-0x00000040);
		
		for (int i = -0x000003F; i < 0; i++)
			checkVariableInt(i);
		
		for (int i = 0; i < 0x000003F; i++)
			checkVariableInt(i);
		
		checkVariableInt(0x0000003F);
		checkVariableInt(0x00000040);
		
		checkVariableInt(0x0000203F);
		checkVariableInt(0x00002040);
		
		checkVariableInt(0x0010203F);
		checkVariableInt(0x00102040);
		
		checkVariableInt(0x0810203F);
		checkVariableInt(0x08102040);
		
		checkVariableInt(Integer.MAX_VALUE - 2);
		checkVariableInt(Integer.MAX_VALUE - 1);
		checkVariableInt(Integer.MAX_VALUE);
	}
	
//	@Test
//	@Ignore
//	public void testAllVariableInt() throws IOException {
//		for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
//			checkVariableInt(i);
//		checkVariableInt(Integer.MAX_VALUE);
//	}
	
	@Test
	public void testSomeIntObject() throws ClassNotFoundException, IOException {
		
		checkIntObject(Integer.MIN_VALUE);
		checkIntObject(Integer.MIN_VALUE + 1);
		checkIntObject(Integer.MIN_VALUE + 2);
		
		checkIntObject(-0x01000000);
		checkIntObject(-0x00FFFFFF);
		
		checkIntObject(-0x00010000);
		checkIntObject(-0x0000FFFF);
		
		checkIntObject(-0x00000100);
		
		for (int i = -0x00000FF; i < 0; i++)
			checkIntObject(i);
		
		for (int i = 0; i <= 0x00000FF; i++)
			checkIntObject(i);

		checkIntObject(0x00000100);
		
		checkIntObject(0x0000FFFF);
		checkIntObject(0x00010000);
		
		checkIntObject(0x00FFFFFF);
		checkIntObject(0x01000000);
		
		checkIntObject(Integer.MAX_VALUE - 1);
		checkIntObject(Integer.MAX_VALUE);
	}

	private void checkInt(int v) throws IOException {
		checkInt(v, false);
	}
	
	private void checkInt(int v, boolean dump) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer();
		serializer.writeInt(v);
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
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes);
		int j = deserializer.readInt();
		deserializer.close();
		
		if (v != j) {
			StringBuilder sb = new StringBuilder(String.format("0x%1$08X (%1$d) != 0x%2$08X (%2$d) ", v, j))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkVariableInt(int v) throws IOException {
		checkVariableInt(v, false);
	}
	
	private void checkVariableInt(int v, boolean dump) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeVariableInt(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (dump)
			System.out.println(bytes.length + "B. " + Util.toHexString(bytes) + " (" + v + ")");
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		int j = deserializer.readVariableInt();
		deserializer.close();
		
		if (v != j) {
			StringBuilder sb = new StringBuilder(String.format("0x%1$08X (%1$d) != 0x%2$08X (%2$d) ", v, j))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkIntObject(Integer v) throws ClassNotFoundException, IOException {
		checkIntObject(v, false);
	}
	
	private void checkIntObject(Integer v, boolean dump) throws ClassNotFoundException, IOException {
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
		Object u = deserializer.readObject();
		deserializer.close();
		
		if (!(u instanceof Integer || u == null))
			fail("u isn't a Integer or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%d != %d ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
