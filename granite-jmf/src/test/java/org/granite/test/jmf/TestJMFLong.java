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

public class TestJMFLong implements JMFConstants {
	
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
	public void testSomeLong() throws IOException {

		checkLong(Long.MIN_VALUE);
		checkLong(Long.MIN_VALUE + 1);
		checkLong(Long.MIN_VALUE + 2);
		
		checkLong(-0x0100000000000000L);
		checkLong(-0x00FFFFFFFFFFFFFFL);
		
		checkLong(-0x0001000000000000L);
		checkLong(-0x0000FFFFFFFFFFFFL);
		
		checkLong(-0x0000010000000000L);
		checkLong(-0x000000FFFFFFFFFFL);
		
		checkLong(-0x0000000100000000L);
		checkLong(-0x00000000FFFFFFFFL);
		
		checkLong(-0x0000000001000000L);
		checkLong(-0x0000000000FFFFFFL);
		
		checkLong(-0x0000000000010000L);
		checkLong(-0x000000000000FFFFL);
		
		checkLong(-0x0000000000000100L);

		for (long i = -0x000000000000FFL; i < 0L; i++)
			checkLong(i);
		
		for (long i = 0L; i <= 0x000000000000FFL; i++)
			checkLong(i);

		checkLong(0x0000000000000100L);
		
		checkLong(0x000000000000FFFFL);
		checkLong(0x0000000000010000L);
		
		checkLong(0x0000000000FFFFFFL);
		checkLong(0x0000000001000000L);
		
		checkLong(0x00000000FFFFFFFFL);
		checkLong(0x0000000100000000L);
		
		checkLong(0x000000FFFFFFFFFFL);
		checkLong(0x0000010000000000L);
		
		checkLong(0x0000FFFFFFFFFFFFL);
		checkLong(0x0001000000000000L);
		
		checkLong(0x00FFFFFFFFFFFFFFL);
		checkLong(0x0100000000000000L);
		
		checkLong(Long.MAX_VALUE - 2);
		checkLong(Long.MAX_VALUE - 1);
		checkLong(Long.MAX_VALUE);
	}
	
	@Test
	public void testSomeVariableLong() throws IOException {

		checkVariableLong(Long.MIN_VALUE);
		checkVariableLong(Long.MIN_VALUE + 1);
		checkVariableLong(Long.MIN_VALUE + 2);
		
		checkVariableLong(-0x0081020408102041L);
		checkVariableLong(-0x0081020408102040L);
	
		checkVariableLong(-0x0001020408102041L);
		checkVariableLong(-0x0001020408102040L);
		
		checkVariableLong(-0x0000020408102041L);
		checkVariableLong(-0x0000020408102040L);
		
		checkVariableLong(-0x0000000408102041L);
		checkVariableLong(-0x0000000408102040L);
		
		checkVariableLong(-0x0000000008102041L);
		checkVariableLong(-0x0000000008102040L);
		
		checkVariableLong(-0x0000000000102041L);
		checkVariableLong(-0x0000000000102040L);
		
		checkVariableLong(-0x0000000000002041L);
		checkVariableLong(-0x0000000000002040L);

		checkVariableLong(-0x0000000000000041L);

		for (long i = -0x0000000000000040L; i < 0L; i++)
			checkVariableLong(i);
		
		for (long i = 0L; i < 0x0000000000000040L; i++)
			checkVariableLong(i);

		checkVariableLong(0x0000000000000040L);

		checkVariableLong(0x000000000000203FL);
		checkVariableLong(0x0000000000002040L);
		
		checkVariableLong(0x000000000010203FL);
		checkVariableLong(0x0000000000102040L);
		
		checkVariableLong(0x000000000810203FL);
		checkVariableLong(0x0000000008102040L);
		
		checkVariableLong(0x000000040810203FL);
		checkVariableLong(0x0000000408102040L);
		
		checkVariableLong(0x000002040810203FL);
		checkVariableLong(0x0000020408102040L);
		
		checkVariableLong(0x000102040810203FL);
		checkVariableLong(0x0001020408102040L);
		
		checkVariableLong(0x008102040810203FL);
		checkVariableLong(0x0081020408102040L);
		
		checkVariableLong(Long.MAX_VALUE - 2);
		checkVariableLong(Long.MAX_VALUE - 1);
		checkVariableLong(Long.MAX_VALUE);
	}
	
//	@Test
//	@Ignore
//	public void testAllLong() throws IOException {
//		for (long i = Long.MIN_VALUE; i < Long.MAX_VALUE; i++)
//			checkLong(i);
//		checkLong(Long.MAX_VALUE);
//	}
	
	@Test
	public void testSomeLongObject() throws ClassNotFoundException, IOException {

		checkLongObject(Long.MIN_VALUE);
		checkLongObject(Long.MIN_VALUE + 1);
		checkLongObject(Long.MIN_VALUE + 2);
		
		checkLongObject(-0x0100000000000000L);
		checkLongObject(-0x00FFFFFFFFFFFFFFL);
		
		checkLongObject(-0x0001000000000000L);
		checkLongObject(-0x0000FFFFFFFFFFFFL);
		
		checkLongObject(-0x0000010000000000L);
		checkLongObject(-0x000000FFFFFFFFFFL);
		
		checkLongObject(-0x0000000100000000L);
		checkLongObject(-0x00000000FFFFFFFFL);
		
		checkLongObject(-0x0000000001000000L);
		checkLongObject(-0x0000000000FFFFFFL);
		
		checkLongObject(-0x0000000000010000L);
		checkLongObject(-0x000000000000FFFFL);
		
		checkLongObject(-0x0000000000000100L);

		for (long i = -0x000000000000FFL; i < 0L; i++)
			checkLongObject(i);
		
		for (long i = 0L; i <= 0x000000000000FFL; i++)
			checkLongObject(i);

		checkLongObject(0x0000000000000100L);
		
		checkLongObject(0x000000000000FFFFL);
		checkLongObject(0x0000000000010000L);
		
		checkLongObject(0x0000000000FFFFFFL);
		checkLongObject(0x0000000001000000L);
		
		checkLongObject(0x00000000FFFFFFFFL);
		checkLongObject(0x0000000100000000L);
		
		checkLongObject(0x000000FFFFFFFFFFL);
		checkLongObject(0x0000010000000000L);
		
		checkLongObject(0x0000FFFFFFFFFFFFL);
		checkLongObject(0x0001000000000000L);
		
		checkLongObject(0x00FFFFFFFFFFFFFFL);
		checkLongObject(0x0100000000000000L);
		
		checkLongObject(Long.MAX_VALUE - 2);
		checkLongObject(Long.MAX_VALUE - 1);
		checkLongObject(Long.MAX_VALUE);
	}
	
	private void checkLong(long v) throws IOException {
		checkLong(v, false);
	}
	
	private void checkLong(long v, boolean dump) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer();
		serializer.writeLong(v);
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
		long j = deserializer.readLong();
		deserializer.close();
		
		if (v != j) {
			StringBuilder sb = new StringBuilder(String.format("0x%1$016X (%1$d) != 0x%2$016X (%2$d) ", v, j))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkVariableLong(long v) throws IOException {
		checkVariableLong(v, false);
	}
	
	private void checkVariableLong(long v, boolean dump) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeVariableLong(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (dump)
			System.out.println(bytes.length + "B. " + Util.toHexString(bytes) + " (" + v + ")");
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		long j = deserializer.readVariableLong();
		deserializer.close();
		
		if (v != j) {
			StringBuilder sb = new StringBuilder(String.format("0x%1$016X (%1$d) != 0x%2$016X (%2$d) ", v, j))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
		
	private void checkLongObject(Long v) throws ClassNotFoundException, IOException {
		checkLongObject(v, false);
	}

	private void checkLongObject(Long v, boolean dump) throws ClassNotFoundException, IOException {
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
		
		if (!(u instanceof Long || u == null))
			fail("u isn't a Long or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%d != %d ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
