/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import static org.granite.test.jmf.Util.toHexString;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;

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

public class TestJMFDouble implements JMFConstants {
	
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
	public void testSomeDouble() throws IOException {

		checkDouble(Double.NaN);
		checkDouble(Double.MAX_VALUE);
		checkDouble(Double.MIN_VALUE);
		checkDouble(Double.MIN_NORMAL);
		checkDouble(Double.NEGATIVE_INFINITY);
		checkDouble(Double.POSITIVE_INFINITY);

		checkDouble(Long.MIN_VALUE);

		checkDouble(Math.PI);
		checkDouble(-Math.PI);
		checkDouble(Math.pow(Math.PI, 620));
		checkDouble(-Math.pow(Math.PI, 620));
		checkDouble(Math.E);
		checkDouble(-Math.E);

		checkDouble(-1.0000000000000004);
		checkDouble(-1.0000000000000003);
		checkDouble(-1.0000000000000002);
		checkDouble(-1.0000000000000001);
		checkDouble(-1.0);
		
		checkDouble(-0.0);
		checkDouble(0.0);
		checkDouble(0.0001);
		checkDouble(0.001);
		checkDouble(0.01);
		checkDouble(0.1);
		
		checkDouble(1.0);
		checkDouble(1.0000000000000001);
		checkDouble(1.0000000000000002);
		checkDouble(1.0000000000000003);
		checkDouble(1.0000000000000004);
		
		checkDouble(28147497671.0654);
		checkDouble(28147497671.0655);
		checkDouble(28147497671.0656);
		
		for (double d = -0x3F; d < 0; d++)
			checkDouble(d);
		
		for (double d = 0; d <= 0x3F; d++)
			checkDouble(d);
		
		for (double d = -1000.0; d < 1000.0; d++)
			checkDouble(d);
		
		for (double d = -10.0002; d < 11.0; d += 0.01)
			checkDouble(d);
	}
	
	@Test
	public void testSomeDoubleObject() throws ClassNotFoundException, IOException {
		
		checkDoubleObject(Double.NaN);
		checkDoubleObject(Double.MAX_VALUE);
		checkDoubleObject(Double.MIN_VALUE);
		checkDoubleObject(Double.MIN_NORMAL);
		checkDoubleObject(Double.NEGATIVE_INFINITY);
		checkDoubleObject(Double.POSITIVE_INFINITY);

		checkDoubleObject((double)Long.MIN_VALUE);

		checkDoubleObject(Math.PI);
		checkDoubleObject(-Math.PI);
		checkDoubleObject(Math.pow(Math.PI, 620));
		checkDoubleObject(-Math.pow(Math.PI, 620));
		checkDoubleObject(Math.E);
		checkDoubleObject(-Math.E);

		checkDoubleObject(-1.0000000000000004);
		checkDoubleObject(-1.0000000000000003);
		checkDoubleObject(-1.0000000000000002);
		checkDoubleObject(-1.0000000000000001);
		checkDoubleObject(-1.0);
		
		checkDoubleObject(-0.0);
		checkDoubleObject(0.0);
		
		checkDoubleObject(1.0);
		checkDoubleObject(1.0000000000000001);
		checkDoubleObject(1.0000000000000002);
		checkDoubleObject(1.0000000000000003);
		checkDoubleObject(1.0000000000000004);
		
		for (double d = -0x3F; d < 0; d++)
			checkDoubleObject(d);
		
		for (double d = 0; d <= 0x3F; d++)
			checkDoubleObject(d);
		
		for (double d = -1000.0; d < 1000.0; d++)
			checkDoubleObject(d);
		
		for (double d = -10.0002; d < 11.0; d += 0.01)
			checkDoubleObject(d);
	}

	private void checkDouble(double v) throws IOException {
		checkDouble(v, false);
	}
	
	private void checkDouble(double v, boolean dump) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeDouble(v);
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
		double u = deserializer.readDouble();
		deserializer.close();
		
		if (Double.compare(v, u) != 0) {
			StringBuilder sb = new StringBuilder(
					String.format("0x%016X (%a) != 0x%016X (%a) ",
					Double.doubleToLongBits(v), v, Double.doubleToLongBits(u), u)
				)
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}

	private void checkDoubleObject(Double v) throws ClassNotFoundException, IOException {
		checkDoubleObject(v, false);
	}
	
	private void checkDoubleObject(Double v, boolean dump) throws ClassNotFoundException, IOException {
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
		Object u = deserializer.readObject();
		deserializer.close();
		
		if (!(u instanceof Double || u == null))
			fail("u isn't a Double or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%a != %a ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
