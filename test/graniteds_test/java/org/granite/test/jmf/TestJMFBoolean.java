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

public class TestJMFBoolean implements JMFConstants {
	
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
	public void testBoolean() throws IOException {

		checkBoolean(false,	bytes( JMF_BOOLEAN ));
		
		checkBoolean(true,	bytes( 0x80 | JMF_BOOLEAN ));
	}

	@Test
	public void testBooleanObject() throws ClassNotFoundException, IOException {

		checkBooleanObject(null,			bytes( JMF_NULL ));
		
		checkBooleanObject(Boolean.FALSE,	bytes( JMF_BOOLEAN_OBJECT ));
		
		checkBooleanObject(Boolean.TRUE,	bytes( 0x80 | JMF_BOOLEAN_OBJECT ));
	}
	
	private void checkBoolean(boolean v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeBoolean(v);
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
		boolean u = deserializer.readBoolean();
		deserializer.close();
		
		if (v != u) {
			StringBuilder sb = new StringBuilder(String.format("%b != %b ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
	
	private void checkBooleanObject(Boolean v, byte[] expected) throws ClassNotFoundException, IOException {
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
		
		if (!(u instanceof Boolean || u == null))
			fail("u isn't a Boolean or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%s != %s ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
