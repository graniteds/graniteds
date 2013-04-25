package org.granite.test.jmf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.ext.EntityCodec;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDeserializer;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDumper;
import org.granite.test.jmf.TestUtil.ByteArrayJMFSerializer;
import org.granite.test.jmf.model.EntityBean;
import org.granite.test.jmf.model.ExternalizableBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFObject {
	
	private CodecRegistry codecRegistry;
	private CodecRegistry extendedCodecRegistry;
	
	@Before
	public void before() {
		codecRegistry = new DefaultCodecRegistry();
		extendedCodecRegistry = new DefaultCodecRegistry(Arrays.asList((ExtendedObjectCodec)new EntityCodec()));
	}
	
	@After
	public void after() {
		codecRegistry = null;
		extendedCodecRegistry = null;
	}

	@Test
	public void testNull() throws ClassNotFoundException, IOException {
		Object clone = serializeAndDeserialize(null);
		assertTrue("Not null", clone == null);
	}

	@Test
	public void testExternalizable() throws ClassNotFoundException, IOException {
		
		ExternalizableBean obj = new ExternalizableBean();

		obj.setBooleanValue(true);
		obj.setCharValue('h');
		
		obj.setByteValue((byte)-34);
		obj.setShortValue((short)3574);
		obj.setIntValue(723574);
		obj.setLongValue(3475834534856L);
		
		obj.setFloatValue(123.01F);
		obj.setDoubleValue(1000.0001);

		obj.setBooleanObjectValue(Boolean.FALSE);
		obj.setCharObjectValue(Character.valueOf('j'));
		
		obj.setByteObjectValue(Byte.valueOf((byte)23));
		obj.setShortObjectValue(Short.valueOf((short)-74));
		obj.setIntObjectValue(Integer.valueOf(-45));
		obj.setLongObjectValue(Long.valueOf(-5476L));
		
		obj.setFloatObjectValue(Float.valueOf(-1.02F));
		obj.setDoubleObjectValue(Double.valueOf(-234.034));
		
		obj.setStringValue(ExternalizableBean.class.getName());
		obj.setDateValue(new Date());
		
		obj.setEnumValue(Thread.State.TERMINATED);
		obj.setByteArrayValue(new byte[]{0, 1, 2, 3, 4});
		
		obj.setOtherBeanValue(new ExternalizableBean());
		obj.getOtherBeanValue().setStringValue("bla");
		obj.getOtherBeanValue().setEnumValue(Thread.State.TERMINATED);

		Object clone = serializeAndDeserialize(obj);
		
//		byte[] bytes = Util.serializeJava(obj);
//		System.out.println("Serialization Java: " + bytes.length + "B.");
		
		assertEquals(obj, clone);
	}
	@Test
	public void testEntity() throws ClassNotFoundException, IOException {
		
		EntityBean obj = new EntityBean();
		obj.junitInit();
		obj.setBooleanValue(true);
		Object clone = serializeAndDeserialize(obj);
		Assert.assertTrue(obj.junitEquals(clone));
	}
	
	private Object serializeAndDeserialize(Object obj) throws ClassNotFoundException, IOException {
		return serializeAndDeserialize(obj, false);
	}
	
	private Object serializeAndDeserialize(Object obj, boolean dump) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(extendedCodecRegistry);
		serializer.writeObject(obj);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		
		PrintStream ps = TestUtil.newNullPrintStream();
		if (dump) {
			System.out.println(bytes.length + "B. " + TestUtil.toHexString(bytes));
			ps = System.out;
		}
		
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, extendedCodecRegistry);
		Object clone = deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
