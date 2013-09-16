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
import org.junit.Test;

public class TestJMFShort implements JMFConstants {
	
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
	public void testShort() throws IOException {
		
		checkShort(Short.MIN_VALUE, bytes( 0x40 | JMF_SHORT, 0x80, 0x00 ));
		
		for (short s = Short.MIN_VALUE + 1; s < -0x00FF; s++)
			checkShort(s, bytes( 0xC0 | JMF_SHORT, (-s) >> 8, -s ));
		
		for (short s = -0x00FF; s < 0; s++)
			checkShort(s, bytes( 0x80 | JMF_SHORT, -s ));
		
		for (short s = 0; s <= 0x00FF; s++)
			checkShort(s, bytes( JMF_SHORT, s ));
		
		for (short s = 0x0100; s < Short.MAX_VALUE; s++)
			checkShort(s, bytes( 0x40 | JMF_SHORT, s >> 8, s ));
		
		checkShort(Short.MAX_VALUE, bytes( 0x40 | JMF_SHORT, 0x7F, 0xFF ));
	}

	@Test
	public void testShortObject() throws ClassNotFoundException, IOException {
		
		checkShortObject(null, bytes( JMF_NULL ));
		
		checkShortObject(Short.valueOf(Short.MIN_VALUE), bytes( 0x40 | JMF_SHORT_OBJECT, 0x80, 0x00 ));
		
		for (short s = Short.MIN_VALUE + 1; s < -0x00FF; s++)
			checkShortObject(Short.valueOf(s), bytes( 0xC0 | JMF_SHORT_OBJECT, (-s) >> 8, -s ));
		
		for (short s = -0x00FF; s < 0; s++)
			checkShortObject(Short.valueOf(s), bytes( 0x80 | JMF_SHORT_OBJECT, -s ));
		
		for (short s = 0; s <= 0x00FF; s++)
			checkShortObject(Short.valueOf(s), bytes( JMF_SHORT_OBJECT, s ));
		
		for (short s = 0x0100; s < Short.MAX_VALUE; s++)
			checkShortObject(Short.valueOf(s), bytes( 0x40 | JMF_SHORT_OBJECT, s >> 8, s ));
		
		checkShortObject(Short.valueOf(Short.MAX_VALUE), bytes( 0x40 | JMF_SHORT_OBJECT, 0x7F, 0xFF ));
	}
	
	private void checkShort(short v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeShort(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(" for ")
				.append(String.format("0x%04X (%d)", v & 0xFFFF, v));
			
			fail(sb.toString());
		}
		
		PrintStream ps = Util.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		short u = deserializer.readShort();
		deserializer.close();
		
		if (v != u) {
			StringBuilder sb = new StringBuilder(String.format("0x%04X (%d) != 0x%04X (%d) ", v & 0xFFFF, v, u & 0xFFFF, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkShortObject(Short v, byte[] expected) throws ClassNotFoundException, IOException {
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
		
		if (!(u instanceof Short || u == null))
			fail("u isn't a Short or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%d != %d ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
