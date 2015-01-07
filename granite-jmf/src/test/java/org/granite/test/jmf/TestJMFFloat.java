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

public class TestJMFFloat implements JMFConstants {
	
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
	public void testSomeFloat() throws IOException {

		checkFloat(Float.NaN);
		checkFloat(Float.MAX_VALUE);
		checkFloat(Float.MIN_VALUE);
		checkFloat(Float.MIN_NORMAL);
		checkFloat(Float.NEGATIVE_INFINITY);
		checkFloat(Float.POSITIVE_INFINITY);

		checkFloat((float)Math.PI);
		checkFloat((float)-Math.PI);
		checkFloat((float)Math.E);
		checkFloat((float)-Math.E);

		checkFloat(-1.0000004F);
		checkFloat(-1.0000003F);
		checkFloat(-1.0000002F);
		checkFloat(-1.0000001F);
		checkFloat(-1.0F);
		
		checkFloat(-0.0F);
		checkFloat(0.0F);
		
		checkFloat(1.0F);
		checkFloat(1.0000001F);
		checkFloat(1.0000002F);
		checkFloat(1.0000003F);
		checkFloat(1.0000004F);
		
		for (float d = -1000.0F; d < 1000.0F; d++)
			checkFloat(d);
		
		for (float d = -10.0002F; d < 11.0F; d += 0.01)
			checkFloat(d);
	}
	
	@Test
	public void testSomeFloatObject() throws ClassNotFoundException, IOException {

		checkFloatObject(Float.NaN);
		checkFloatObject(Float.MAX_VALUE);
		checkFloatObject(Float.MIN_VALUE);
		checkFloatObject(Float.MIN_NORMAL);
		checkFloatObject(Float.NEGATIVE_INFINITY);
		checkFloatObject(Float.POSITIVE_INFINITY);

		checkFloatObject((float)Math.PI);
		checkFloatObject((float)-Math.PI);
		checkFloatObject((float)Math.E);
		checkFloatObject((float)-Math.E);

		checkFloatObject(-1.0000004F);
		checkFloatObject(-1.0000003F);
		checkFloatObject(-1.0000002F);
		checkFloatObject(-1.0000001F);
		checkFloatObject(-1.0F);
		
		checkFloatObject(-0.0F);
		checkFloatObject(0.0F);
		
		checkFloatObject(1.0F);
		checkFloatObject(1.0000001F);
		checkFloatObject(1.0000002F);
		checkFloatObject(1.0000003F);
		checkFloatObject(1.0000004F);
		
		for (float d = -1000.0F; d < 1000.0F; d++)
			checkFloatObject(d);
		
		for (float d = -10.0002F; d < 11.0F; d += 0.01)
			checkFloatObject(d);
	}
	
	private void checkFloat(float v) throws IOException {
		checkFloat(v, false);
	}
	
	private void checkFloat(float v, boolean dump) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeFloat(v);
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
		float u = deserializer.readFloat();
		deserializer.close();
		
		if (Float.floatToIntBits(v) != Float.floatToIntBits(u))
			fail(v + " != " + u);
	}
	
	private void checkFloatObject(Float v) throws ClassNotFoundException, IOException {
		checkFloatObject(v, false);
	}
	
	private void checkFloatObject(Float v, boolean dump) throws ClassNotFoundException, IOException {
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
		
		if (!(u instanceof Float))
			fail("Not a Float instance: " + u + " for: " + v);
		
		if (Float.floatToIntBits(v) != Float.floatToIntBits((Float)u))
			fail(v + " != " + u);
	}
}
