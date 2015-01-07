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

import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

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

public class TestJMFSqlDate implements JMFConstants {
	
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
	public void testSomeDate() throws ClassNotFoundException, IOException {
		Date d = new Date(System.currentTimeMillis());
		Date d2 = serializeDeserialize(d);
		Assert.assertEquals(d, d2);
		
		d = new Date(0L);
		d2 = serializeDeserialize(d);
		Assert.assertEquals(d, d2);
		
		d = new Date(Long.MAX_VALUE);
		d2 = serializeDeserialize(d);
		Assert.assertEquals(d, d2);
	}
	
	@Test
	public void testSomeTime() throws ClassNotFoundException, IOException {
		Time t = new Time(System.currentTimeMillis());
		Time t2 = serializeDeserialize(t);
		Assert.assertEquals(t, t2);
		
		t = new Time(0L);
		t2 = serializeDeserialize(t);
		Assert.assertEquals(t, t2);

		t = new Time(Long.MAX_VALUE);
		t2 = serializeDeserialize(t);
		Assert.assertEquals(t, t2);
	}
	
	@Test
	public void testSomeTimestamp() throws ClassNotFoundException, IOException {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		Timestamp t2 = serializeDeserialize(t);
		Assert.assertEquals(t, t2);
		
		t = new Timestamp(0L);
		t2 = serializeDeserialize(t);
		Assert.assertEquals(t, t2);

		t = new Timestamp(Long.MAX_VALUE);
		t2 = serializeDeserialize(t);
		Assert.assertEquals(t, t2);
	}
	
	private <T> T serializeDeserialize(T v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T serializeDeserialize(T v, boolean dump) throws ClassNotFoundException, IOException {
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
		
		return (T)u;
	}
}
