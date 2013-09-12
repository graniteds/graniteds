/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
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

		checkInt(Integer.MIN_VALUE, 	bytes( 0x60 | JMF_INTEGER, 0x80, 0x00, 0x00, 0x00 ));
		checkInt(Integer.MIN_VALUE + 1, bytes( 0xE0 | JMF_INTEGER, 0x7F, 0xFF, 0xFF, 0xFF ));
		checkInt(Integer.MIN_VALUE + 2, bytes( 0xE0 | JMF_INTEGER, 0x7F, 0xFF, 0xFF, 0xFE ));
		
		checkInt(-0x01000000, 			bytes( 0xE0 | JMF_INTEGER, 0x01, 0x00, 0x00, 0x00 ));
		checkInt(-0x00FFFFFF, 			bytes( 0xC0 | JMF_INTEGER, 0xFF, 0xFF, 0xFF ));
		
		checkInt(-0x00010000, 			bytes( 0xC0 | JMF_INTEGER, 0x01, 0x00, 0x00 ));
		checkInt(-0x0000FFFF, 			bytes( 0xA0 | JMF_INTEGER, 0xFF, 0xFF ));
		
		checkInt(-0x00000100, 			bytes( 0xA0 | JMF_INTEGER, 0x01, 0x00 ));
		
		for (int i = -0x00000FF; i < 0; i++)
			checkInt(i, 				bytes( 0x80 | JMF_INTEGER, -i ));
		
		for (int i = 0; i <= 0x00000FF; i++)
			checkInt(i,					bytes( JMF_INTEGER, i ));

		checkInt(0x00000100,			bytes( 0x20 | JMF_INTEGER, 0x01, 0x00 ));
		
		checkInt(0x0000FFFF,			bytes( 0x20 | JMF_INTEGER, 0xFF, 0xFF ));
		checkInt(0x00010000,			bytes( 0x40 | JMF_INTEGER, 0x01, 0x00, 0x00 ));
		
		checkInt(0x00FFFFFF,			bytes( 0x40 | JMF_INTEGER, 0xFF, 0xFF, 0xFF ));
		checkInt(0x01000000,			bytes( 0x60 | JMF_INTEGER, 0x01, 0x00, 0x00, 0x00 ));
		
		checkInt(Integer.MAX_VALUE - 1, bytes( 0x60 | JMF_INTEGER, 0x7F, 0xFF, 0xFF, 0xFE ));
		checkInt(Integer.MAX_VALUE, 	bytes( 0x60 | JMF_INTEGER, 0x7F, 0xFF, 0xFF, 0xFF ));
	}
	
	@Test
	@Ignore
	public void testAllInt() throws IOException {
		for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
			checkInt(i);
		checkInt(Integer.MAX_VALUE);
	}
	
	@Test
	public void testSomeVariableInt() throws IOException {

		checkVariableInt(Integer.MIN_VALUE, 	bytes( 0x80 ));
		checkVariableInt(Integer.MIN_VALUE + 1, bytes( 0xC3, 0xDF, 0xDF, 0xDF, 0xBF ));
		checkVariableInt(Integer.MIN_VALUE + 2, bytes( 0xC3, 0xDF, 0xDF, 0xDF, 0xBE ));
		
		checkVariableInt(-0x08102040, 			bytes( 0xC0, 0x80, 0x80, 0x80, 0x00 ));
		checkVariableInt(-0x0810203F, 			bytes( 0xFF, 0xFF, 0xFF, 0x7F ));
		
		checkVariableInt(-0x00102040, 			bytes( 0xC0, 0x80, 0x80, 0x00 ));
		checkVariableInt(-0x0010203F, 			bytes( 0xFF, 0xFF, 0x7F ));
		
		checkVariableInt(-0x00002040, 			bytes( 0xC0, 0x80, 0x00 ));
		checkVariableInt(-0x0000203F, 			bytes( 0xFF, 0x7F ));
		
		checkVariableInt(-0x00000040, 			bytes( 0xC0, 0x00 ));
		
		for (int i = -0x000003F; i < 0; i++)
			checkVariableInt(i, 				bytes( 0x80 | -i ));
		
		for (int i = 0; i <= 0x000003F; i++)
			checkVariableInt(i,					bytes( i ));
		
		checkVariableInt(0x00000040, 			bytes( 0x40, 0x00 ));
		
		checkVariableInt(0x0000203F, 			bytes( 0x7F, 0x7F ));
		checkVariableInt(0x00002040, 			bytes( 0x40, 0x80, 0x00 ));
		
		checkVariableInt(0x0010203F, 			bytes( 0x7F, 0xFF, 0x7F ));
		checkVariableInt(0x00102040, 			bytes( 0x40, 0x80, 0x80, 0x00 ));
		
		checkVariableInt(0x0810203F, 			bytes( 0x7F, 0xFF, 0xFF, 0x7F ));
		checkVariableInt(0x08102040, 			bytes( 0x40, 0x80, 0x80, 0x80, 0x00 ));
		
		checkVariableInt(Integer.MAX_VALUE - 1, bytes( 0x43, 0xDF, 0xDF, 0xDF, 0xBE ));
		checkVariableInt(Integer.MAX_VALUE, 	bytes( 0x43, 0xDF, 0xDF, 0xDF, 0xBF ));
	}
	
	@Test
	@Ignore
	public void testAllVariableInt() throws IOException {
		for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++)
			checkVariableInt(i);
		checkVariableInt(Integer.MAX_VALUE);
	}
	
	@Test
	public void testSomeIntObject() throws ClassNotFoundException, IOException {
		
		checkIntObject(null,					bytes( JMF_NULL ));

		checkIntObject(Integer.MIN_VALUE, 		bytes( 0x60 | JMF_INTEGER_OBJECT, 0x80, 0x00, 0x00, 0x00 ));
		checkIntObject(Integer.MIN_VALUE + 1, 	bytes( 0xE0 | JMF_INTEGER_OBJECT, 0x7F, 0xFF, 0xFF, 0xFF ));
		checkIntObject(Integer.MIN_VALUE + 2, 	bytes( 0xE0 | JMF_INTEGER_OBJECT, 0x7F, 0xFF, 0xFF, 0xFE ));
		
		checkIntObject(-0x01000000, 			bytes( 0xE0 | JMF_INTEGER_OBJECT, 0x01, 0x00, 0x00, 0x00 ));
		checkIntObject(-0x00FFFFFF, 			bytes( 0xC0 | JMF_INTEGER_OBJECT, 0xFF, 0xFF, 0xFF ));
		
		checkIntObject(-0x00010000, 			bytes( 0xC0 | JMF_INTEGER_OBJECT, 0x01, 0x00, 0x00 ));
		checkIntObject(-0x0000FFFF, 			bytes( 0xA0 | JMF_INTEGER_OBJECT, 0xFF, 0xFF ));
		
		checkIntObject(-0x00000100, 			bytes( 0xA0 | JMF_INTEGER_OBJECT, 0x01, 0x00 ));
		
		for (int i = -0x00000FF; i < 0; i++)
			checkIntObject(i, 					bytes( 0x80 | JMF_INTEGER_OBJECT, -i ));
		
		for (int i = 0; i <= 0x00000FF; i++)
			checkIntObject(i,					bytes( JMF_INTEGER_OBJECT, i ));

		checkIntObject(0x00000100,				bytes( 0x20 | JMF_INTEGER_OBJECT, 0x01, 0x00 ));
		
		checkIntObject(0x0000FFFF,				bytes( 0x20 | JMF_INTEGER_OBJECT, 0xFF, 0xFF ));
		checkIntObject(0x00010000,				bytes( 0x40 | JMF_INTEGER_OBJECT, 0x01, 0x00, 0x00 ));
		
		checkIntObject(0x00FFFFFF,				bytes( 0x40 | JMF_INTEGER_OBJECT, 0xFF, 0xFF, 0xFF ));
		checkIntObject(0x01000000,				bytes( 0x60 | JMF_INTEGER_OBJECT, 0x01, 0x00, 0x00, 0x00 ));
		
		checkIntObject(Integer.MAX_VALUE - 1, 	bytes( 0x60 | JMF_INTEGER_OBJECT, 0x7F, 0xFF, 0xFF, 0xFE ));
		checkIntObject(Integer.MAX_VALUE, 		bytes( 0x60 | JMF_INTEGER_OBJECT, 0x7F, 0xFF, 0xFF, 0xFF ));
	}
	
	private void checkInt(int i) throws IOException {
		checkInt(i, null);
	}
	
	private void checkInt(int v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer();
		serializer.writeInt(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(String.format(" for 0x%1$08X (%1$d)", v));
			
			fail(sb.toString());
		}
		
		PrintStream ps = Util.newNullPrintStream();
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
	
	private void checkVariableInt(int i) throws IOException {
		checkVariableInt(i, null);
	}
	
	private void checkVariableInt(int v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeVariableInt(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			System.out.println(toHexString(bytes));
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(String.format(" for 0x%1$08X (%1$d)", v));
			
			fail(sb.toString());
		}
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		int j = deserializer.readVariableInt();
		deserializer.close();
		
		if (v != j) {
			StringBuilder sb = new StringBuilder(String.format("0x%1$08X (%1$d) != 0x%2$08X (%2$d) ", v, j))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkIntObject(Integer v, byte[] expected) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			System.out.println(toHexString(bytes));
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
		
		if (!(u instanceof Integer || u == null))
			fail("u isn't a Integer or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%d != %d ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
