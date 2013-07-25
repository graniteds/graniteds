package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Random;

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

public class TestJMFBigInteger implements JMFConstants {
	
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
	public void testBigInteger() throws ClassNotFoundException, IOException {
		BigInteger bigInteger = BigInteger.ZERO;
		Assert.assertTrue(bigInteger == serializeDeserialize(bigInteger));
		
		bigInteger = BigInteger.ONE;
		Assert.assertTrue(bigInteger == serializeDeserialize(bigInteger));
		
		bigInteger = BigInteger.TEN;
		Assert.assertTrue(bigInteger == serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger("-1");
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger("-10");
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger(64, new Random());
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger(128, new Random());
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger(256, new Random());
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger(512, new Random());
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger(1024, new Random());
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger("-123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger("2").pow(2048);
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
		bigInteger = new BigInteger("7").pow(4096);
		Assert.assertEquals(bigInteger, serializeDeserialize(bigInteger));
		
//		byte[] bytes = Util.serializeJava(bigInteger);
//		System.out.println("Serialization Java: " + bytes.length + "B.");
	}
	
	private BigInteger serializeDeserialize(BigInteger v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private BigInteger serializeDeserialize(BigInteger v, boolean dump) throws ClassNotFoundException, IOException {
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
		BigInteger clone = (BigInteger)deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
