/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import static org.granite.test.jmf.Util.bytes;
import static org.granite.test.jmf.Util.toHexString;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

		checkLong(Long.MIN_VALUE, 		bytes( 0x70 | JMF_LONG, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkLong(Long.MIN_VALUE + 1,	bytes( 0xF0 | JMF_LONG, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLong(Long.MIN_VALUE + 2,	bytes( 0xF0 | JMF_LONG, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE ));
		
		checkLong(-0x0100000000000000L,	bytes( 0xF0 | JMF_LONG, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkLong(-0x00FFFFFFFFFFFFFFL,	bytes( 0xE0 | JMF_LONG, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		
		checkLong(-0x0001000000000000L,	bytes( 0xE0 | JMF_LONG, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkLong(-0x0000FFFFFFFFFFFFL,	bytes( 0xD0 | JMF_LONG, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		
		checkLong(-0x0000010000000000L,	bytes( 0xD0 | JMF_LONG, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkLong(-0x000000FFFFFFFFFFL,	bytes( 0xC0 | JMF_LONG, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		
		checkLong(-0x0000000100000000L,	bytes( 0xC0 | JMF_LONG, 0x01, 0x00, 0x00, 0x00, 0x00 ));
		checkLong(-0x00000000FFFFFFFFL,	bytes( 0xB0 | JMF_LONG, 0xFF, 0xFF, 0xFF, 0xFF ));
		
		checkLong(-0x0000000001000000L,	bytes( 0xB0 | JMF_LONG, 0x01, 0x00, 0x00, 0x00 ));
		checkLong(-0x0000000000FFFFFFL,	bytes( 0xA0 | JMF_LONG, 0xFF, 0xFF, 0xFF ));
		
		checkLong(-0x0000000000010000L,	bytes( 0xA0 | JMF_LONG, 0x01, 0x00, 0x00 ));
		checkLong(-0x000000000000FFFFL,	bytes( 0x90 | JMF_LONG, 0xFF, 0xFF ));
		
		checkLong(-0x0000000000000100L,	bytes( 0x90 | JMF_LONG, 0x01, 0x00 ));

		for (long i = -0x000000000000FFL; i < 0L; i++)
			checkLong(i, 				bytes( 0x80 | JMF_LONG, (int)-i ));
		
		for (long i = 0L; i <= 0x000000000000FFL; i++)
			checkLong(i,				bytes( JMF_LONG, (int)i ));

		checkLong(0x0000000000000100L,	bytes( 0x10 | JMF_LONG, 0x01, 0x00 ));
		
		checkLong(0x000000000000FFFFL,	bytes( 0x10 | JMF_LONG, 0xFF, 0xFF ));
		checkLong(0x0000000000010000L,	bytes( 0x20 | JMF_LONG, 0x01, 0x00, 0x00 ));
		
		checkLong(0x0000000000FFFFFFL,	bytes( 0x20 | JMF_LONG, 0xFF, 0xFF, 0xFF ));
		checkLong(0x0000000001000000L,	bytes( 0x30 | JMF_LONG, 0x01, 0x00, 0x00, 0x00 ));
		
		checkLong(0x00000000FFFFFFFFL,	bytes( 0x30 | JMF_LONG, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLong(0x0000000100000000L,	bytes( 0x40 | JMF_LONG, 0x01, 0x00, 0x00, 0x00, 0x00 ));
		
		checkLong(0x000000FFFFFFFFFFL,	bytes( 0x40 | JMF_LONG, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLong(0x0000010000000000L,	bytes( 0x50 | JMF_LONG, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		
		checkLong(0x0000FFFFFFFFFFFFL,	bytes( 0x50 | JMF_LONG, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLong(0x0001000000000000L,	bytes( 0x60 | JMF_LONG, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		
		checkLong(0x00FFFFFFFFFFFFFFL,	bytes( 0x60 | JMF_LONG, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLong(0x0100000000000000L,	bytes( 0x70 | JMF_LONG, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		
		checkLong(Long.MAX_VALUE - 1,	bytes( 0x70 | JMF_LONG, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE ));
		checkLong(Long.MAX_VALUE,		bytes( 0x70 | JMF_LONG, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
	}
	
	@Test
	public void testSomeVariableLong() throws IOException {

		checkVariableLong(Long.MIN_VALUE, 		bytes( 0x80 ));
		checkVariableLong(Long.MIN_VALUE + 1,	bytes( 0xFF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xBF ));
		checkVariableLong(Long.MIN_VALUE + 2,	bytes( 0xFF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xBE ));
		
		checkVariableLong(-0x0081020408102040L,	bytes( 0xC0, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00 ));
		checkVariableLong(-0x008102040810203FL,	bytes( 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F ));
		
		checkVariableLong(-0x0001020408102040L,	bytes( 0xC0, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00 ));
		checkVariableLong(-0x000102040810203FL,	bytes( 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F ));
		
		checkVariableLong(-0x0000020408102040L,	bytes( 0xC0, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00 ));
		checkVariableLong(-0x000002040810203FL,	bytes( 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F ));
		
		checkVariableLong(-0x0000000408102040L,	bytes( 0xC0, 0x80, 0x80, 0x80, 0x80, 0x00 ));
		checkVariableLong(-0x000000040810203FL,	bytes( 0xFF, 0xFF, 0xFF, 0xFF, 0x7F ));
		
		checkVariableLong(-0x0000000008102040L,	bytes( 0xC0, 0x80, 0x80, 0x80, 0x00 ));
		checkVariableLong(-0x000000000810203FL,	bytes( 0xFF, 0xFF, 0xFF, 0x7F ));
		
		checkVariableLong(-0x0000000000102040L,	bytes( 0xC0, 0x80, 0x80, 0x00 ));
		checkVariableLong(-0x000000000010203FL,	bytes( 0xFF, 0xFF, 0x7F ));
		
		checkVariableLong(-0x0000000000002040L,	bytes( 0xC0, 0x80, 0x00 ));
		checkVariableLong(-0x000000000000203FL,	bytes( 0xFF, 0x7F ));

		checkVariableLong(-0x0000000000000040L,	bytes( 0xC0, 0x00 ));

		for (long i = -0x0000000000003FL; i < 0L; i++)
			checkVariableLong(i, 				bytes( 0x80 | (int)-i ));
		
		for (long i = 0L; i <= 0x0000000000003FL; i++)
			checkVariableLong(i,				bytes( (int)i ));

		checkVariableLong(0x0000000000000040L,	bytes( 0x40, 0x00 ));

		checkVariableLong(0x000000000000203FL,	bytes( 0x7F, 0x7F ));
		checkVariableLong(0x0000000000002040L,	bytes( 0x40, 0x80, 0x00 ));
		
		checkVariableLong(0x000000000010203FL,	bytes( 0x7F, 0xFF, 0x7F ));
		checkVariableLong(0x0000000000102040L,	bytes( 0x40, 0x80, 0x80, 0x00 ));
		
		checkVariableLong(0x000000000810203FL,	bytes( 0x7F, 0xFF, 0xFF, 0x7F ));
		checkVariableLong(0x0000000008102040L,	bytes( 0x40, 0x80, 0x80, 0x80, 0x00 ));
		
		checkVariableLong(0x000000040810203FL,	bytes( 0x7F, 0xFF, 0xFF, 0xFF, 0x7F ));
		checkVariableLong(0x0000000408102040L,	bytes( 0x40, 0x80, 0x80, 0x80, 0x80, 0x00 ));
		
		checkVariableLong(0x000002040810203FL,	bytes( 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F ));
		checkVariableLong(0x0000020408102040L,	bytes( 0x40, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00 ));
		
		checkVariableLong(0x000102040810203FL,	bytes( 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F ));
		checkVariableLong(0x0001020408102040L,	bytes( 0x40, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00 ));
		
		checkVariableLong(0x008102040810203FL,	bytes( 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x7F ));
		checkVariableLong(0x0081020408102040L,	bytes( 0x40, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x00 ));
		
		checkVariableLong(Long.MAX_VALUE - 1,	bytes( 0x7F, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xBE ));
		checkVariableLong(Long.MAX_VALUE,		bytes( 0x7F, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xDF, 0xBF ));
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

		checkLongObject(null, 					bytes( JMF_NULL ));

		checkLongObject(Long.MIN_VALUE, 		bytes( 0x70 | JMF_LONG_OBJECT, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkLongObject(Long.MIN_VALUE + 1,		bytes( 0xF0 | JMF_LONG_OBJECT, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLongObject(Long.MIN_VALUE + 2,		bytes( 0xF0 | JMF_LONG_OBJECT, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE ));
		
		checkLongObject(-0x0100000000000000L,	bytes( 0xF0 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkLongObject(-0x00FFFFFFFFFFFFFFL,	bytes( 0xE0 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		
		checkLongObject(-0x0001000000000000L,	bytes( 0xE0 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkLongObject(-0x0000FFFFFFFFFFFFL,	bytes( 0xD0 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		
		checkLongObject(-0x0000010000000000L,	bytes( 0xD0 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkLongObject(-0x000000FFFFFFFFFFL,	bytes( 0xC0 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		
		checkLongObject(-0x0000000100000000L,	bytes( 0xC0 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00 ));
		checkLongObject(-0x00000000FFFFFFFFL,	bytes( 0xB0 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF ));
		
		checkLongObject(-0x0000000001000000L,	bytes( 0xB0 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00 ));
		checkLongObject(-0x0000000000FFFFFFL,	bytes( 0xA0 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF ));
		
		checkLongObject(-0x0000000000010000L,	bytes( 0xA0 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00 ));
		checkLongObject(-0x000000000000FFFFL,	bytes( 0x90 | JMF_LONG_OBJECT, 0xFF, 0xFF ));
		
		checkLongObject(-0x0000000000000100L,	bytes( 0x90 | JMF_LONG_OBJECT, 0x01, 0x00 ));

		for (long i = -0x000000000000FFL; i < 0L; i++)
			checkLongObject(i, 					bytes( 0x80 | JMF_LONG_OBJECT, (int)-i ));
		
		for (long i = 0L; i <= 0x000000000000FFL; i++)
			checkLongObject(i,					bytes( JMF_LONG_OBJECT, (int)i ));

		checkLongObject(0x0000000000000100L,	bytes( 0x10 | JMF_LONG_OBJECT, 0x01, 0x00 ));
		
		checkLongObject(0x000000000000FFFFL,	bytes( 0x10 | JMF_LONG_OBJECT, 0xFF, 0xFF ));
		checkLongObject(0x0000000000010000L,	bytes( 0x20 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00 ));
		
		checkLongObject(0x0000000000FFFFFFL,	bytes( 0x20 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF ));
		checkLongObject(0x0000000001000000L,	bytes( 0x30 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00 ));
		
		checkLongObject(0x00000000FFFFFFFFL,	bytes( 0x30 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLongObject(0x0000000100000000L,	bytes( 0x40 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00 ));
		
		checkLongObject(0x000000FFFFFFFFFFL,	bytes( 0x40 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLongObject(0x0000010000000000L,	bytes( 0x50 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		
		checkLongObject(0x0000FFFFFFFFFFFFL,	bytes( 0x50 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLongObject(0x0001000000000000L,	bytes( 0x60 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		
		checkLongObject(0x00FFFFFFFFFFFFFFL,	bytes( 0x60 | JMF_LONG_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
		checkLongObject(0x0100000000000000L,	bytes( 0x70 | JMF_LONG_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		
		checkLongObject(Long.MAX_VALUE - 1,		bytes( 0x70 | JMF_LONG_OBJECT, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE ));
		checkLongObject(Long.MAX_VALUE,			bytes( 0x70 | JMF_LONG_OBJECT, 0x7F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF ));
	}
	
	private void checkLong(long i) throws IOException {
		checkLong(i, null);
	}
	
	private void checkLong(long v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer();
		serializer.writeLong(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(String.format(" for 0x%1$016X (%1$d)", v));
			
			fail(sb.toString());
		}
		
		PrintStream ps = Util.newNullPrintStream();
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
	
	private void checkVariableLong(long v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeVariableLong(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			System.out.println(toHexString(bytes));
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(String.format(" for 0x%1$016X (%1$d)", v));
			
			fail(sb.toString());
		}
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		long j = deserializer.readVariableLong();
		deserializer.close();
		
		if (v != j) {
			StringBuilder sb = new StringBuilder(String.format("0x%1$016X (%1$d) != 0x%2$016X (%2$d) ", v, j))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkLongObject(Long v, byte[] expected) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(" for ")
				.append(v);
			
			fail(sb.toString());
		}
		
		PrintStream ps = Util.newNullPrintStream();
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
