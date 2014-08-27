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
package org.granite.test.eclipselink.serialization;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.granite.eclipselink.jmf.EntityCodec;
import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.test.jmf.Util;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.granite.test.jmf.model.EntityBean;
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
