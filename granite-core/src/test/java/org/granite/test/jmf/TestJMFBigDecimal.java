/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Assert;
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
		
		bigDecimal = new BigDecimal(2).pow(2048);
		Assert.assertEquals(bigDecimal, serializeDeserialize(bigDecimal));
		
		bigDecimal = new BigDecimal(7).pow(4096);
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
