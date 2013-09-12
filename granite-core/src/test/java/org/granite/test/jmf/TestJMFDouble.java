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
import org.junit.Test;

public class TestJMFDouble implements JMFConstants {
	
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
	public void testSomeDouble() throws IOException {

		checkDouble(Double.NaN, 				bytes( 0xC0 | JMF_DOUBLE ));
		checkDouble(Double.MAX_VALUE, 			bytes( JMF_DOUBLE, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xEF, 0x7F ));
		checkDouble(Double.MIN_VALUE, 			bytes( JMF_DOUBLE, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkDouble(Double.MIN_NORMAL, 			bytes( JMF_DOUBLE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00 ));
		checkDouble(Double.NEGATIVE_INFINITY, 	bytes( 0x40 | JMF_DOUBLE, 0x00, 0x00, 0x80, 0xFF ));
		checkDouble(Double.POSITIVE_INFINITY, 	bytes( 0x40 | JMF_DOUBLE, 0x00, 0x00, 0x80, 0x7F ));

		checkDouble(Long.MIN_VALUE, 			bytes( 0x80 | JMF_DOUBLE, 0x80 ));

		checkDouble(Math.PI, 					bytes( JMF_DOUBLE, 0x18, 0x2D, 0x44, 0x54, 0xFB, 0x21, 0x09, 0x40 ));
		checkDouble(-Math.PI, 					bytes( JMF_DOUBLE, 0x18, 0x2D, 0x44, 0x54, 0xFB, 0x21, 0x09, 0xC0 ));
		checkDouble(Math.pow(Math.PI, 620), 	bytes( JMF_DOUBLE, 0x45, 0x66, 0xF7, 0x54, 0x0A, 0x6F, 0xEE, 0x7F ));
		checkDouble(-Math.pow(Math.PI, 620), 	bytes( JMF_DOUBLE, 0x45, 0x66, 0xF7, 0x54, 0x0A, 0x6F, 0xEE, 0xFF ));
		checkDouble(Math.E, 					bytes( JMF_DOUBLE, 0x69, 0x57, 0x14, 0x8B, 0x0A, 0xBF, 0x05, 0x40 ));
		checkDouble(-Math.E, 					bytes( JMF_DOUBLE, 0x69, 0x57, 0x14, 0x8B, 0x0A, 0xBF, 0x05, 0xC0 ));

		checkDouble(-1.0000000000000004, 		bytes( JMF_DOUBLE, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0xBF ));
		checkDouble(-1.0000000000000003, 		bytes( JMF_DOUBLE, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0xBF ));
		checkDouble(-1.0000000000000002, 		bytes( JMF_DOUBLE, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0xBF ));
		checkDouble(-1.0000000000000001, 		bytes( 0x80 | JMF_DOUBLE, 0x81 ));
		checkDouble(-1.0, 						bytes( 0x80 | JMF_DOUBLE, 0x81 ));
		
		checkDouble(-0.0,						bytes( 0x40 | JMF_DOUBLE, 0x00, 0x00, 0x00, 0x80 ));
		checkDouble(0.0,						bytes( 0x80 | JMF_DOUBLE, 0x00 ));
		
		checkDouble(1.0, 						bytes( 0x80 | JMF_DOUBLE, 0x01 ));
		checkDouble(1.0000000000000001, 		bytes( 0x80 | JMF_DOUBLE, 0x01 ));
		checkDouble(1.0000000000000002, 		bytes( JMF_DOUBLE, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x3F ));
		checkDouble(1.0000000000000003, 		bytes( JMF_DOUBLE, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x3F ));
		checkDouble(1.0000000000000004, 		bytes( JMF_DOUBLE, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x3F ));
		
		for (double d = -0x3F; d < 0; d++)
			checkDouble(d, bytes( 0x80 | JMF_DOUBLE, (byte)-d | 0x80 ));
		
		for (double d = 0; d <= 0x3F; d++)
			checkDouble(d, bytes( 0x80 | JMF_DOUBLE, (byte)d ));
		
		for (double d = -1000.0; d < 1000.0; d++)
			checkDouble(d);
		
		for (double d = -10.0002; d < 11.0; d += 0.01)
			checkDouble(d);
	}
	
	@Test
	public void testSomeDoubleObject() throws ClassNotFoundException, IOException {
		
		checkDoubleObject(null,						bytes( JMF_NULL ));

		checkDoubleObject(Double.NaN, 				bytes( 0xC0 | JMF_DOUBLE_OBJECT ));
		checkDoubleObject(Double.MAX_VALUE, 		bytes( JMF_DOUBLE_OBJECT, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xEF, 0x7F ));
		checkDoubleObject(Double.MIN_VALUE, 		bytes( JMF_DOUBLE_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 ));
		checkDoubleObject(Double.MIN_NORMAL, 		bytes( JMF_DOUBLE_OBJECT, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00 ));
		checkDoubleObject(Double.NEGATIVE_INFINITY, bytes( 0x40 | JMF_DOUBLE_OBJECT, 0x00, 0x00, 0x80, 0xFF ));
		checkDoubleObject(Double.POSITIVE_INFINITY, bytes( 0x40 | JMF_DOUBLE_OBJECT, 0x00, 0x00, 0x80, 0x7F ));

		checkDoubleObject((double)Long.MIN_VALUE, 	bytes( 0x80 | JMF_DOUBLE_OBJECT, 0x80 ));

		checkDoubleObject(Math.PI, 					bytes( JMF_DOUBLE_OBJECT, 0x18, 0x2D, 0x44, 0x54, 0xFB, 0x21, 0x09, 0x40 ));
		checkDoubleObject(-Math.PI, 				bytes( JMF_DOUBLE_OBJECT, 0x18, 0x2D, 0x44, 0x54, 0xFB, 0x21, 0x09, 0xC0 ));
		checkDoubleObject(Math.pow(Math.PI, 620), 	bytes( JMF_DOUBLE_OBJECT, 0x45, 0x66, 0xF7, 0x54, 0x0A, 0x6F, 0xEE, 0x7F ));
		checkDoubleObject(-Math.pow(Math.PI, 620), 	bytes( JMF_DOUBLE_OBJECT, 0x45, 0x66, 0xF7, 0x54, 0x0A, 0x6F, 0xEE, 0xFF ));
		checkDoubleObject(Math.E, 					bytes( JMF_DOUBLE_OBJECT, 0x69, 0x57, 0x14, 0x8B, 0x0A, 0xBF, 0x05, 0x40 ));
		checkDoubleObject(-Math.E, 					bytes( JMF_DOUBLE_OBJECT, 0x69, 0x57, 0x14, 0x8B, 0x0A, 0xBF, 0x05, 0xC0 ));

		checkDoubleObject(-1.0000000000000004, 		bytes( JMF_DOUBLE_OBJECT, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0xBF ));
		checkDoubleObject(-1.0000000000000003, 		bytes( JMF_DOUBLE_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0xBF ));
		checkDoubleObject(-1.0000000000000002, 		bytes( JMF_DOUBLE_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0xBF ));
		checkDoubleObject(-1.0000000000000001, 		bytes( 0x80 | JMF_DOUBLE_OBJECT, 0x81 ));
		checkDoubleObject(-1.0, 					bytes( 0x80 | JMF_DOUBLE_OBJECT, 0x81 ));
		
		checkDoubleObject(-0.0,						bytes( 0x40 | JMF_DOUBLE_OBJECT, 0x00, 0x00, 0x00, 0x80 ));
		checkDoubleObject(0.0,						bytes( 0x80 | JMF_DOUBLE_OBJECT, 0x00 ));
		
		checkDoubleObject(1.0, 						bytes( 0x80 | JMF_DOUBLE_OBJECT, 0x01 ));
		checkDoubleObject(1.0000000000000001, 		bytes( 0x80 | JMF_DOUBLE_OBJECT, 0x01 ));
		checkDoubleObject(1.0000000000000002, 		bytes( JMF_DOUBLE_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x3F ));
		checkDoubleObject(1.0000000000000003, 		bytes( JMF_DOUBLE_OBJECT, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x3F ));
		checkDoubleObject(1.0000000000000004, 		bytes( JMF_DOUBLE_OBJECT, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x3F ));
		
		for (double d = -0x3F; d < 0; d++)
			checkDoubleObject(d, bytes( 0x80 | JMF_DOUBLE_OBJECT, (byte)-d | 0x80 ));
		
		for (double d = 0; d <= 0x3F; d++)
			checkDoubleObject(d, bytes( 0x80 | JMF_DOUBLE_OBJECT, (byte)d ));
		
		for (double d = -1000.0; d < 1000.0; d++)
			checkDoubleObject(d);
		
		for (double d = -10.0002; d < 11.0; d += 0.01)
			checkDoubleObject(d);
	}

	private void checkDouble(double v) throws IOException {
		checkDouble(v, null);
	}
	
	private void checkDouble(double v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeDouble(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			System.out.println(toHexString(bytes));
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(String.format(" for 0x%016X (%a)", Double.doubleToLongBits(v), v));
			
			fail(sb.toString());
		}
		
		PrintStream ps = Util.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		double u = deserializer.readDouble();
		deserializer.close();
		
		if (Double.compare(v, u) != 0) {
			StringBuilder sb = new StringBuilder(
					String.format("0x%016X (%a) != 0x%016X (%a) ",
					Double.doubleToLongBits(v), v, Double.doubleToLongBits(u), u)
				)
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}

	private void checkDoubleObject(Double v) throws ClassNotFoundException, IOException {
		checkDoubleObject(v, null);
	}
	
	private void checkDoubleObject(Double v, byte[] expected) throws ClassNotFoundException, IOException {
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
		
		if (!(u instanceof Double || u == null))
			fail("u isn't a Double or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%a != %a ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
