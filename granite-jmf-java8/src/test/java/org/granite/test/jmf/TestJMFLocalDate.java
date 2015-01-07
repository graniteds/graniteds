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
import java.time.LocalDate;

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

public class TestJMFLocalDate implements JMFConstants {
	
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
		checkDate(null);
		
		LocalDate d = LocalDate.now();
		checkDate(d);
		
		d = LocalDate.ofEpochDay(0L);
		checkDate(d);
		
		d = LocalDate.ofEpochDay(0L);
		checkDate(d);
	}
	
	private void checkDate(LocalDate v) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		PrintStream ps = Util.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		Object u = deserializer.readObject();
		deserializer.close();
		
		if (!(u instanceof LocalDate || u == null))
			fail("u isn't a LocalDate or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%s != %s ", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
