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

import static org.granite.test.jmf.Util.toHexString;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.UUID;

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

public class TestJMFString implements JMFConstants {
	
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
	public void testStringUTF() throws IOException {

		checkString("");
		
		for (char c = 0; c < 0x80; c++)
			checkString(String.valueOf(c));
		
		for (char c = 0x80; c < 0x800; c++)
			checkString(String.valueOf(c));
		
		for (char c = 0x800; c < 0xD800; c++)
			checkString(String.valueOf(c));
		
		// Skip 0xD800...0xDFFF (illegal)
		
		for (char c = 0xE000; c < 0xFFFF; c++)
			checkString(String.valueOf(c));
		checkString(String.valueOf((char)0xFFFF));
		
		checkString(String.valueOf(Character.toChars(0x10000)));
		for (int i = 0x10000; i <= 0x10FFFF; i++)
			checkString(String.valueOf(Character.toChars(i)));
		checkString(String.valueOf(Character.toChars(0x10FFFF)));
		
		checkString("1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\u00E9\u20AC\uF900\uFDF0\uD834\uDD1E");
		
		char[] chars = new char[0xFF];
		Arrays.fill(chars, 'a');
		checkString(new String(chars));
		
		chars = new char[0x100];
		Arrays.fill(chars, 'a');
		checkString(new String(chars));
		
		chars = new char[0xFFFF];
		Arrays.fill(chars, 'a');
		checkString(new String(chars));
		
		chars = new char[0x10000];
		Arrays.fill(chars, 'a');
		checkString(new String(chars));
	}

	@Test
	public void testStringUTFObject() throws ClassNotFoundException, IOException {

		checkStringObject("");
		
		for (char c = 0; c < 0x80; c++)
			checkStringObject(String.valueOf(c));
		
		for (char c = 0x80; c < 0x800; c++)
			checkStringObject(String.valueOf(c));
		
		for (char c = 0x800; c < 0xD800; c++)
			checkStringObject(String.valueOf(c));
		
		// Skip 0xD800...0xDFFF (illegal)
		
		for (char c = 0xE000; c < 0xFFFF; c++)
			checkStringObject(String.valueOf(c));
		checkStringObject(String.valueOf((char)0xFFFF));
		
		checkStringObject(String.valueOf(Character.toChars(0x10000)));
		for (int i = 0x10000; i <= 0x10FFFF; i++)
			checkStringObject(String.valueOf(Character.toChars(i)));
		checkStringObject(String.valueOf(Character.toChars(0x10FFFF)));
		
		checkStringObject("1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\u00E9\u20AC\uF900\uFDF0\uD834\uDD1E");
		
		char[] chars = new char[0xFF];
		Arrays.fill(chars, 'a');
		checkStringObject(new String(chars));
		
		chars = new char[0x100];
		Arrays.fill(chars, 'a');
		checkStringObject(new String(chars));
		
		chars = new char[0xFFFF];
		Arrays.fill(chars, 'a');
		checkStringObject(new String(chars));
		
		chars = new char[0x10000];
		Arrays.fill(chars, 'a');
		checkStringObject(new String(chars));
	}

	@Test
	public void testUUIDString() throws ClassNotFoundException, IOException {
		String uid = UUID.randomUUID().toString();
		Assert.assertEquals(17, checkString(uid));
		Assert.assertEquals(17, checkStringObject(uid));
		
		uid = UUID.randomUUID().toString().toUpperCase();
		Assert.assertEquals(17, checkString(uid));
		Assert.assertEquals(17, checkStringObject(uid));
		
		uid = "bb2256cf-fb4e-4a30-a500-a17d9a87d08a";
		Assert.assertEquals(17, checkString(uid));
		Assert.assertEquals(17, checkStringObject(uid));
		
		uid = "BB47A52E-2457-4616-934D-E0AD9A67FFEA";
		Assert.assertEquals(17, checkString(uid));
		Assert.assertEquals(17, checkStringObject(uid));

		// Mixed uppercase / lowercase (invalid).
		
		uid = "Bb2256cf-fb4e-4a30-a500-a17d9a87d08a";
		Assert.assertEquals(38, checkString(uid));
		Assert.assertEquals(38, checkStringObject(uid));

		uid = "bb2256Cf-fb4e-4A30-a500-a17d9a87d08a";
		Assert.assertEquals(38, checkString(uid));
		Assert.assertEquals(38, checkStringObject(uid));
		
		uid = "bb2256cf-fb4e-4a30-a500-a17d9a87d08A";
		Assert.assertEquals(38, checkString(uid));
		Assert.assertEquals(38, checkStringObject(uid));
		
		uid = "bB47A52E-2457-4616-934D-E0AD9A67FFEA";
		Assert.assertEquals(38, checkString(uid));
		Assert.assertEquals(38, checkStringObject(uid));

		uid = "BB47A52E-2457-4616-934d-e0AD9A67FFEA";
		Assert.assertEquals(38, checkString(uid));
		Assert.assertEquals(38, checkStringObject(uid));
		
		uid = "BB47A52E-2457-4616-934D-E0AD9A67FFEa";
		Assert.assertEquals(38, checkString(uid));
		Assert.assertEquals(38, checkStringObject(uid));
	}

	private int checkString(String v) throws IOException {
		return checkString(v, false);
	}
	
	private int checkString(String v, boolean dump) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeUTF(v);
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
		String u = deserializer.readUTF();
		deserializer.close();
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder('"')
				.append(v)
				.append("\" != \"")
				.append(u)
				.append('"')
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
		
		return bytes.length;
	}

	private int checkStringObject(String v) throws ClassNotFoundException, IOException {
		return checkStringObject(v, false);
	}
	
	private int checkStringObject(String v, boolean dump) throws ClassNotFoundException, IOException {
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
		
		if (!(u instanceof String || u == null))
			fail("u isn't a String or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder('"')
				.append(v)
				.append("\" != \"")
				.append(u)
				.append('"')
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
		
		return bytes.length;
	}
}
