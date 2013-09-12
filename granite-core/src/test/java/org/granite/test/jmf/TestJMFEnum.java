/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Assert;
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
