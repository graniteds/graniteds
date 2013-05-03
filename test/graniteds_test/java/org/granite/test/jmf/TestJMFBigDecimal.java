package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;

import junit.framework.Assert;

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

public class TestJMFBigDecimal implements JMFConstants {
	
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
	public void testBigDecimal() throws ClassNotFoundException, IOException {
		BigDecimal bigDecimal = BigDecimal.ZERO;
		Assert.assertTrue(bigDecimal == serializeDeserialize(bigDecimal));
		
		bigDecimal = BigDecimal.ONE;
		Assert.assertTrue(bigDecimal == serializeDeserialize(bigDecimal));
		
		bigDecimal = BigDecimal.TEN;
		Assert.assertTrue(bigDecimal == serializeDeserialize(bigDecimal));
		
		bigDecimal = new BigDecimal("-1");
		Assert.assertEquals(bigDecimal, serializeDeserialize(bigDecimal));
		
		bigDecimal = new BigDecimal("-10");
		Assert.assertEquals(bigDecimal, serializeDeserialize(bigDecimal));
		
		bigDecimal = new BigDecimal("-123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		Assert.assertEquals(bigDecimal, serializeDeserialize(bigDecimal));
		
		bigDecimal = new BigDecimal("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		Assert.assertEquals(bigDecimal, serializeDeserialize(bigDecimal));
		
		bigDecimal = new BigDecimal("-1234567890123456789012345678901234.56789012345678901234567890123456789012345678901234567890");
		Assert.assertEquals(bigDecimal, serializeDeserialize(bigDecimal));
		
		bigDecimal = new BigDecimal("1234567890123456789012345678901234567890123456789.01234567890123456789012345678901234567890");
		Assert.assertEquals(bigDecimal, serializeDeserialize(bigDecimal));
		
		bigDecimal = new BigDecimal("10000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000001");
		Assert.assertEquals(bigDecimal, serializeDeserialize(bigDecimal));
		
//		byte[] bytes = Util.serializeJava(bigDecimal);
//		System.out.println("Serialization Java: " + bytes.length + "B.");
	}
	
	private BigDecimal serializeDeserialize(BigDecimal v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private BigDecimal serializeDeserialize(BigDecimal v, boolean dump) throws ClassNotFoundException, IOException {
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
		BigDecimal clone = (BigDecimal)deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
