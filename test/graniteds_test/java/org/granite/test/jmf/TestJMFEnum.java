package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;

import junit.framework.Assert;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJMFEnum {
	
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
	public void testEnum() throws ClassNotFoundException, IOException {
		Assert.assertEquals(Thread.State.BLOCKED, serializeAndDeserialize(Thread.State.BLOCKED));
		Assert.assertEquals(Thread.State.NEW, serializeAndDeserialize(Thread.State.NEW));
		Assert.assertEquals(Thread.State.RUNNABLE, serializeAndDeserialize(Thread.State.RUNNABLE));
		Assert.assertEquals(Thread.State.TERMINATED, serializeAndDeserialize(Thread.State.TERMINATED));
		Assert.assertEquals(Thread.State.TIMED_WAITING, serializeAndDeserialize(Thread.State.TIMED_WAITING));
	}
	
	private Object serializeAndDeserialize(Object obj) throws ClassNotFoundException, IOException {
		return serializeAndDeserialize(obj, false);
	}
	
	private Object serializeAndDeserialize(Object obj, boolean dump) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(obj);
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
		Object clone = deserializer.readObject();
		deserializer.close();
		return clone;

	}
}
