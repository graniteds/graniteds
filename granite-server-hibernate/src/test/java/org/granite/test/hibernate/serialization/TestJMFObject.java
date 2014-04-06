/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.hibernate.serialization;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;

import org.granite.hibernate.jmf.EntityCodec;
import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.test.jmf.Util;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.granite.test.jmf.model.ExternalizableBean;
import org.granite.test.jmf.model.IncludeExclude;
import org.granite.test.jmf.model.NotSerializable;
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
		Assert.assertTrue("Not null", clone == null);
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
		//obj.getOtherBeanValue().setStringValue("bla");
		obj.getOtherBeanValue().setEnumValue(Thread.State.TERMINATED);

		Object clone = serializeAndDeserialize(obj);
		
//		byte[] bytes = Util.serializeJava(obj);
//		System.out.println("Serialization Java: " + bytes.length + "B.");
		
		Assert.assertEquals(obj, clone);
	}

	@Test
	public void testNotSerializable() throws ClassNotFoundException, IOException {
		
		NotSerializable obj = new NotSerializable();

		try {
			serializeAndDeserialize(obj);
			Assert.fail("Should throw a NotSerializableException");
		}
		catch (NotSerializableException e) {
		}
	}
	@Test
	public void testIncludeExclude() throws ClassNotFoundException, IOException {
		
		IncludeExclude obj = new IncludeExclude();

		obj.setNormal(true);
		obj.setExclude("This is exclued");

		Object clone = serializeAndDeserialize(obj);
		
//		byte[] bytes = Util.serializeJava(obj);
//		System.out.println("Serialization Java: " + bytes.length + "B.");
		
		Assert.assertEquals(obj, clone);
		Assert.assertNull(((IncludeExclude)clone).getExclude());
	}

//	@Test
//	public void testEntity() throws ClassNotFoundException, IOException {
//		
//		EntityBean obj = new EntityBean();
//		obj.junitInit();
//		obj.setBooleanValue(true);
//		Object clone = serializeAndDeserialize(obj);
//		Assert.assertTrue(obj.junitEquals(clone));
//	}

	@Test
	public void testProxy() throws Exception {
//		InvocationHandler handler = new ProxyInterfaceHandler();
//		Class<?> proxyClass = Proxy.getProxyClass(getClass().getClassLoader(), new Class[] {ProxyInterface.class, Serializable.class});
//		ProxyInterface proxy = (ProxyInterface)proxyClass.getConstructor(new Class[] {InvocationHandler.class}).newInstance(new Object[] {handler});
//		
//		Object clone = serializeAndDeserialize(proxy, true);
//		
//		Assert.assertTrue(proxy.equals(clone));
	}
	
	private Object serializeAndDeserialize(Object obj) throws ClassNotFoundException, IOException {
		return serializeAndDeserialize(obj, false);
	}
	
	private Object serializeAndDeserialize(Object obj, boolean dump) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(extendedCodecRegistry);
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
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, extendedCodecRegistry);
		Object clone = deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
