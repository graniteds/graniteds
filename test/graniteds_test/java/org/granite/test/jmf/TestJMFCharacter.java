package org.granite.test.jmf;

import static org.granite.test.jmf.TestUtil.bytes;
import static org.granite.test.jmf.TestUtil.toHexString;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDeserializer;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDumper;
import org.granite.test.jmf.TestUtil.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJMFCharacter implements JMFConstants {
	
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
	public void testChar() throws IOException {
		
		for (char i = 0; i <= 0x00FF; i++)
			checkChar(i, bytes( JMF_CHARACTER, i ));
		
		for (char i = 0x100; i < 0xFFFF; i++)
			checkChar(i, bytes( 0x80 | JMF_CHARACTER, i >> 8, i ));

		checkChar(Character.MAX_VALUE, bytes( 0x80 | JMF_CHARACTER, 0xFF, 0xFF ));
	}

	@Test
	public void testCharObject() throws ClassNotFoundException, IOException {
		
		checkCharObject(null, bytes( JMF_NULL ));
		
		for (char i = 0; i <= 0x00FF; i++)
			checkCharObject(Character.valueOf(i), bytes( JMF_CHARACTER_OBJECT, i ));
		
		for (char i = 0x100; i < 0xFFFF; i++)
			checkCharObject(Character.valueOf(i), bytes( 0x80 | JMF_CHARACTER_OBJECT, i >> 8, i ));

		checkCharObject(Character.valueOf(Character.MAX_VALUE), bytes( 0x80 | JMF_CHARACTER_OBJECT, 0xFF, 0xFF ));
	}
	
	private void checkChar(char v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeChar(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (!Arrays.equals(bytes, expected)) {
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(" for ")
				.append(String.format("0x%04X (%c)", v & 0xFFFF, v));
			
			fail(sb.toString());
		}
		
		PrintStream ps = TestUtil.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		char u = deserializer.readChar();
		deserializer.close();
		
		if (v != u) {
			StringBuilder sb = new StringBuilder(String.format("0x%04X (%c) != 0x%04X (%c) ", v & 0xFFFF, v, u & 0xFFFF, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkCharObject(Character v, byte[] expected) throws ClassNotFoundException, IOException {
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
		
		PrintStream ps = TestUtil.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		Object u = deserializer.readObject();
		deserializer.close();
		
		if (!(u instanceof Character || u == null))
			fail("u isn't a Byte or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%s != %s ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
