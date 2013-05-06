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
