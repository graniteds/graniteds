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

public class TestJMFByte implements JMFConstants {
	
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
	public void testByte() throws IOException {
		for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++)
			checkByte(i, bytes( JMF_BYTE, i ));
		checkByte(Byte.MAX_VALUE, bytes( JMF_BYTE, 0x7F ));
	}

	@Test
	public void testByteObject() throws ClassNotFoundException, IOException {
		checkByteObject(null, bytes( JMF_NULL ));
		
		for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++)
			checkByteObject(Byte.valueOf((byte)i), bytes( JMF_BYTE_OBJECT, i ));
	}
	
	private void checkByte(byte v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeByte(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (!Arrays.equals(bytes, expected)) {
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(" for ")
				.append(String.format("0x%02X", v & 0xFF));
			
			fail(sb.toString());
		}
		
		PrintStream ps = Util.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		byte u = deserializer.readByte();
		deserializer.close();
		
		if (v != u) {
			StringBuilder sb = new StringBuilder(String.format("0x%02X != 0x%02X ", v & 0xFF, u & 0xFF))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkByteObject(Byte v, byte[] expected) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (!Arrays.equals(bytes, expected)) {
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
		
		if (!(u instanceof Byte || u == null))
			fail("u isn't a Byte or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%d != %d ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
