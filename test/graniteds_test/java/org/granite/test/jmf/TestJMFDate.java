package org.granite.test.jmf;

import static org.granite.test.jmf.TestUtil.toHexString;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

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

public class TestJMFDate implements JMFConstants {
	
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
	public void testSomeDate() throws ClassNotFoundException, IOException {
		checkDate(null);
		
		Date d = new Date();
		checkDate(d);
		
		d = new Date(0L);
		checkDate(d);
		
		d = new Date(Long.MAX_VALUE);
		checkDate(d);
	}
	
	private void checkDate(Date v) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		PrintStream ps = TestUtil.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		Object u = deserializer.readObject();
		deserializer.close();
		
		if (!(u instanceof Date || u == null))
			fail("u isn't a Date or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%s != %s ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
