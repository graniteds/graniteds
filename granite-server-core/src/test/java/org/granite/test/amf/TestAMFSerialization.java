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
package org.granite.test.amf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.messaging.amf.io.AMF3Constants;
import org.granite.messaging.amf.io.AMF3Deserializer;
import org.granite.messaging.amf.io.AMF3Serializer;
import org.granite.util.XMLUtil;
import org.granite.util.XMLUtilFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import flex.messaging.io.ArrayCollection;

public class TestAMFSerialization implements AMF3Constants {
	
	private GraniteConfig graniteConfig;
	private ServicesConfig servicesConfig;
	
	@Before
	public void before() throws Exception {
		graniteConfig = new GraniteConfig(null, null, null, null);
		servicesConfig = new ServicesConfig(null, null, false);
	}
	
	@Test
	public void testAMFNull() throws IOException {
		byte[] bytes = serialize(null);
		Assert.assertEquals(1, bytes.length);
		Assert.assertEquals(AMF3_NULL, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertNull(o);
	}
	
	@Test
	public void testAMFBoolean() throws IOException {
		byte[] bytes = serialize(true);
		Assert.assertEquals(1, bytes.length);
		Assert.assertEquals(AMF3_BOOLEAN_TRUE, bytes[0]);
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Boolean);
		Assert.assertTrue(((Boolean)o).booleanValue());
		
		bytes = serialize(false);
		Assert.assertEquals(1, bytes.length);
		Assert.assertEquals(AMF3_BOOLEAN_FALSE, bytes[0]);
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Boolean);
		Assert.assertFalse(((Boolean)o).booleanValue());
	}
	
	@Test
	public void testAMFChar() throws IOException {
		for (char c = 0; c <= 0x7F; c++) {
			byte[] bytes = serialize(c);
			Assert.assertEquals(3, bytes.length);
			Assert.assertEquals(AMF3_STRING, bytes[0]);
			
			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof String);
			Assert.assertEquals(1, ((String)o).length());
			Assert.assertEquals(c, ((String)o).charAt(0));
		}
		
		for (char c = 0x80; c <= 0x7FF; c++) {
			byte[] bytes = serialize(c);
			Assert.assertEquals(4, bytes.length);
			Assert.assertEquals(AMF3_STRING, bytes[0]);

			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof String);
			Assert.assertEquals(1, ((String)o).length());
			Assert.assertEquals(c, ((String)o).charAt(0));
		}
		
		for (char c = 0x800; ; c++) {
			byte[] bytes = serialize(c);
			Assert.assertEquals(5, bytes.length);
			Assert.assertEquals(AMF3_STRING, bytes[0]);

			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof String);
			Assert.assertEquals(1, ((String)o).length());
			Assert.assertEquals(c, ((String)o).charAt(0));

			if (c == 0xFFFF)
				break;
		}
	}
	
	@Test
	public void testAMFByte() throws IOException {
		for (byte b = Byte.MIN_VALUE; b < 0; b++) {
			byte[] bytes = serialize(b);
			Assert.assertEquals(5, bytes.length);
			Assert.assertEquals(AMF3_INTEGER, bytes[0]);

			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof Integer);
			Assert.assertEquals(b, ((Integer)o).byteValue());
		}
		
		for (byte b = 0; ; b++) {
			byte[] bytes = serialize(b);
			Assert.assertEquals(2, bytes.length);
			Assert.assertEquals(AMF3_INTEGER, bytes[0]);

			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof Integer);
			Assert.assertEquals(b, ((Integer)o).byteValue());

			if (b == Byte.MAX_VALUE)
				break;
		}
	}
	
	@Test
	public void testAMFShort() throws IOException {
		for (short s = Short.MIN_VALUE; s < 0; s++) {
			byte[] bytes = serialize(s);
			Assert.assertEquals(5, bytes.length);
			Assert.assertEquals(AMF3_INTEGER, bytes[0]);
			
			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof Integer);
			Assert.assertEquals(s, ((Integer)o).shortValue());
		}
		
		for (short s = 0; s < 0x80; s++) {
			byte[] bytes = serialize(s);
			Assert.assertEquals(2, bytes.length);
			Assert.assertEquals(AMF3_INTEGER, bytes[0]);
			
			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof Integer);
			Assert.assertEquals(s, ((Integer)o).shortValue());
		}
		
		for (short s = 0x80; s < 0x4000; s++) {
			byte[] bytes = serialize(s);
			Assert.assertEquals(3, bytes.length);
			Assert.assertEquals(AMF3_INTEGER, bytes[0]);
			
			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof Integer);
			Assert.assertEquals(s, ((Integer)o).shortValue());
		}
		
		for (short s = 0x4000; ; s++) {
			byte[] bytes = serialize(s);
			Assert.assertEquals(4, bytes.length);
			Assert.assertEquals(AMF3_INTEGER, bytes[0]);
			
			Object o = deserialize(bytes);
			Assert.assertTrue(o instanceof Integer);
			Assert.assertEquals(s, ((Integer)o).shortValue());
			
			if (s == Short.MAX_VALUE)
				break;
		}
	}
	
	@Test
	public void testAMFInt() throws IOException {
		
		// Promoted to Number 
		byte[] bytes = serialize(Integer.MIN_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Integer.MIN_VALUE, ((Double)o).intValue());
		
		bytes = serialize(Integer.MIN_VALUE + 1);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Integer.MIN_VALUE + 1, ((Double)o).intValue());
		
		bytes = serialize(AMF3_INTEGER_MIN - 1);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(AMF3_INTEGER_MIN - 1, ((Double)o).intValue());
		
		// Integer
		bytes = serialize(AMF3_INTEGER_MIN);
		Assert.assertEquals(5, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(AMF3_INTEGER_MIN, ((Integer)o).intValue());
		
		bytes = serialize(AMF3_INTEGER_MIN + 1);
		Assert.assertEquals(5, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(AMF3_INTEGER_MIN + 1, ((Integer)o).intValue());
		
		bytes = serialize(AMF3_INTEGER_MIN + 1);
		Assert.assertEquals(5, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(AMF3_INTEGER_MIN + 1, ((Integer)o).intValue());
		
		bytes = serialize(-1);
		Assert.assertEquals(5, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(-1, ((Integer)o).intValue());
		
		bytes = serialize(0);
		Assert.assertEquals(2, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(0, ((Integer)o).intValue());
		
		bytes = serialize(1);
		Assert.assertEquals(2, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(1, ((Integer)o).intValue());
		
		bytes = serialize(0x7F);
		Assert.assertEquals(2, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(0x7F, ((Integer)o).intValue());
		
		bytes = serialize(0x80);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(0x80, ((Integer)o).intValue());
		
		bytes = serialize(0x3FFF);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(0x3FFF, ((Integer)o).intValue());
		
		bytes = serialize(0x4000);
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(0x4000, ((Integer)o).intValue());
		
		bytes = serialize(0x1FFFFF);
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(0x1FFFFF, ((Integer)o).intValue());
		
		bytes = serialize(0x200000);
		Assert.assertEquals(5, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(0x200000, ((Integer)o).intValue());
		
		bytes = serialize(AMF3_INTEGER_MAX);
		Assert.assertEquals(5, bytes.length);
		Assert.assertEquals(AMF3_INTEGER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Integer);
		Assert.assertEquals(AMF3_INTEGER_MAX, ((Integer)o).intValue());
		
		// Promoted to Number 
		bytes = serialize(AMF3_INTEGER_MAX + 1);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(AMF3_INTEGER_MAX + 1, ((Double)o).intValue());
		
		bytes = serialize(AMF3_INTEGER_MAX + 2);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(AMF3_INTEGER_MAX + 2, ((Double)o).intValue());
		
		bytes = serialize(Integer.MAX_VALUE - 1);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Integer.MAX_VALUE -1, ((Double)o).intValue());
		
		bytes = serialize(Integer.MAX_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Integer.MAX_VALUE, ((Double)o).intValue());
	}
	
	@Test
	public void testAMFLong() throws IOException {
		byte[] bytes = serialize(Long.MIN_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Long.MIN_VALUE, ((Double)o).longValue());
		
		// Loose of precision: Long.MIN_VALUE + 1L -> Long.MIN_VALUE...
		bytes = serialize(Long.MIN_VALUE + 1L);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Long.MIN_VALUE, ((Double)o).longValue());

		// Loose of precision: -((1L << 63) + 2L) -> -((1L << 63) + 1L)...
		bytes = serialize(-((1L << 63) + 2L));
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-((1L << 63) + 1L), ((Double)o).longValue());
		
		for (int shift = 63; shift >= 53; shift--) {
			bytes = serialize(-(1L << shift));
			Assert.assertEquals(9, bytes.length);
			Assert.assertEquals(AMF3_NUMBER, bytes[0]);
			
			o = deserialize(bytes);
			Assert.assertTrue(o instanceof Double);
			Assert.assertEquals(-(1L << shift), ((Double)o).longValue());
		}
		
		bytes = serialize(-((1L << 52) + 1L));
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-((1L << 52) + 1L), ((Double)o).longValue());
		
		bytes = serialize(-(1L << 52));
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-(1L << 52), ((Double)o).longValue());
		
		bytes = serialize(-1L);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-1L, ((Double)o).longValue());
		
		bytes = serialize(0L);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(0L, ((Double)o).longValue());
		
		bytes = serialize(1L);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(1L, ((Double)o).longValue());
		
		bytes = serialize((1L << 52) + 1L);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals((1L << 52) + 1L, ((Double)o).longValue());

		// Loose of precision: (1L << [53...63]) + 1L -> (1L << [53...63])...
		for (int shift = 53; shift <= 63; shift++) {
			bytes = serialize((1L << shift) + 1L);
			Assert.assertEquals(9, bytes.length);
			Assert.assertEquals(AMF3_NUMBER, bytes[0]);
			
			o = deserialize(bytes);
			Assert.assertTrue(o instanceof Double);
			Assert.assertEquals((1L << shift), ((Double)o).longValue());
		}
		
		// Loose of precision: Long.MAX_VALUE + 1L -> Long.MAX_VALUE...
		bytes = serialize(Long.MAX_VALUE - 1L);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Long.MAX_VALUE, ((Double)o).longValue());
		
		bytes = serialize(Long.MAX_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Long.MAX_VALUE, ((Double)o).longValue());
	}
	
	@Test
	public void testAMFFloat() throws IOException {
		byte[] bytes = serialize(Float.NEGATIVE_INFINITY);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Float.NEGATIVE_INFINITY, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(-Float.MAX_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-Float.MAX_VALUE, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(-1.5f);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-1.5f, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(-1.0f);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-1.0f, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(-0.5f);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-0.5f, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(-Float.MIN_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-Float.MIN_VALUE, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(-0.0f);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-0.0f, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(Float.NaN);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertNull(o);
		
		bytes = serialize(0.0f);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(0.0f, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(Float.MIN_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Float.MIN_VALUE, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(0.5f);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(0.5f, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(1.0f);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(1.0f, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(1.5f);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(1.5f, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(Float.MAX_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Float.MAX_VALUE, ((Double)o).floatValue(), 0.0);
		
		bytes = serialize(Float.POSITIVE_INFINITY);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Float.POSITIVE_INFINITY, ((Double)o).floatValue(), 0.0);
	}
	
	@Test
	public void testAMFDouble() throws IOException {
		byte[] bytes = serialize(Double.NEGATIVE_INFINITY);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Double.NEGATIVE_INFINITY, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(-Double.MAX_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-Double.MAX_VALUE, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(-1.5);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-1.5, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(-1.0);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-1.0, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(-0.5);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-0.5, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(-Double.MIN_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-Double.MIN_VALUE, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(-0.0);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(-0.0, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(Double.NaN);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertNull(o);
		
		bytes = serialize(0.0);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(0.0, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(Double.MIN_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Double.MIN_VALUE, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(0.5);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(0.5, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(1.0);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(1.0, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(1.5);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(1.5, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(Double.MAX_VALUE);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Double.MAX_VALUE, ((Double)o).doubleValue(), 0.0);
		
		bytes = serialize(Double.POSITIVE_INFINITY);
		Assert.assertEquals(9, bytes.length);
		Assert.assertEquals(AMF3_NUMBER, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Double);
		Assert.assertEquals(Double.POSITIVE_INFINITY, ((Double)o).doubleValue(), 0.0);
	}
	
	@Test
	public void testAMFDate() throws IOException {
		Date date = new Date(System.currentTimeMillis());
		byte[] bytes = serialize(date);
		Assert.assertEquals(10, bytes.length);
		Assert.assertEquals(AMF3_DATE, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Date);
		Assert.assertEquals(date, o);
		
		date = new Date(0L);
		bytes = serialize(date);
		Assert.assertEquals(10, bytes.length);
		Assert.assertEquals(AMF3_DATE, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Date);
		Assert.assertEquals(date, o);

		date = new java.sql.Date(System.currentTimeMillis());
		bytes = serialize(date);
		Assert.assertEquals(10, bytes.length);
		Assert.assertEquals(AMF3_DATE, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Date);
		Assert.assertEquals(date, o);
		
		date = new java.sql.Time(System.currentTimeMillis());
		
		bytes = serialize(date);
		Assert.assertEquals(10, bytes.length);
		Assert.assertEquals(AMF3_DATE, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Date);
		Assert.assertEquals(date, o);
		
		date = new java.sql.Timestamp(System.currentTimeMillis());
		
		bytes = serialize(date);
		Assert.assertEquals(10, bytes.length);
		Assert.assertEquals(AMF3_DATE, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Date);
		Assert.assertEquals(date.getTime(), ((Date)o).getTime());

		Calendar cal = Calendar.getInstance();
		bytes = serialize(cal);
		Assert.assertEquals(10, bytes.length);
		Assert.assertEquals(AMF3_DATE, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Date);
		Assert.assertEquals(cal.getTime(), o);
	}
	
	@Test
	public void testAMFDocument() throws IOException {
		String s =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
			"<root>" +
				"<child attr=\"blo\">Blabla</child>" +
			"</root>";
		XMLUtil xml = XMLUtilFactory.getXMLUtil();
		Document doc = xml.buildDocument(s);
		
		byte[] bytes = serialize(doc);
		Assert.assertEquals(102, bytes.length);
		Assert.assertEquals(AMF3_XMLSTRING, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof String);
		Assert.assertEquals(s, (String)o);
	}
	
	@Test
	public void testAMFBooleanArray() throws IOException {
		byte[] bytes = serialize(new boolean[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		bytes = serialize(new boolean[]{false});
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(1, ((Object[])o).length);
		Assert.assertTrue(((Object[])o)[0] instanceof Boolean);
		Assert.assertFalse((Boolean)((Object[])o)[0]);
		
		bytes = serialize(new boolean[]{true});
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(1, ((Object[])o).length);
		Assert.assertTrue(((Object[])o)[0] instanceof Boolean);
		Assert.assertTrue((Boolean)((Object[])o)[0]);
		
		boolean[] array = new boolean[]{true, false, false, true, false, true, true};
		
		bytes = serialize(array);
		Assert.assertEquals(3 + array.length, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		for (int i = 0; i < array.length; i++) {
			Object oi = ((Object[])o)[i];
			Assert.assertTrue(oi instanceof Boolean);
			Assert.assertEquals(array[i], (Boolean)oi);
		}
	}
	
	@Test
	public void testAMFBooleanObjectArray() throws IOException {
		byte[] bytes = serialize(new Boolean[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		bytes = serialize(new Boolean[]{false});
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(1, ((Object[])o).length);
		Assert.assertTrue(((Object[])o)[0] instanceof Boolean);
		Assert.assertFalse((Boolean)((Object[])o)[0]);
		
		bytes = serialize(new Boolean[]{true});
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(1, ((Object[])o).length);
		Assert.assertTrue(((Object[])o)[0] instanceof Boolean);
		Assert.assertTrue((Boolean)((Object[])o)[0]);
		
		Boolean[] array = new Boolean[]{null, true, false, false, true, false, true, true};
		
		bytes = serialize(array);
		Assert.assertEquals(3 + array.length, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		for (int i = 0; i < array.length; i++) {
			Object oi = ((Object[])o)[i];
			if (array[i] == null)
				Assert.assertNull(oi);
			else {
				Assert.assertTrue(oi instanceof Boolean);
				Assert.assertEquals(array[i], (Boolean)oi);
			}
		}
	}
	
	@Test
	public void testAMFCharArray() throws IOException {
		byte[] bytes = serialize(new char[0]);
		Assert.assertEquals(2, bytes.length);
		Assert.assertEquals(AMF3_STRING, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof String);
		Assert.assertEquals("", (String)o);
		
		char[] array = new char[0xFFFF + 1];
		for (char c = 0; ; c++) {
			array[c] = c;
			if (c == 0xFFFF)
				break;
		}
		bytes = serialize(array);
		Assert.assertEquals(194436, bytes.length);
		Assert.assertEquals(AMF3_STRING, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof String);
		Assert.assertEquals(new String(array), (String)o);
	}
	
	@Test
	public void testAMFCharObjectArray() throws IOException {
		byte[] bytes = serialize(new Character[0]);
		Assert.assertEquals(2, bytes.length);
		Assert.assertEquals(AMF3_STRING, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof String);
		Assert.assertEquals("", (String)o);
		
		bytes = serialize(new Character[]{null, 'a'});
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_STRING, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof String);
		Assert.assertEquals("\u0000a", (String)o);
		
		Character[] array = new Character[0xFFFF + 1];
		char[] array2 = new char[array.length];
		for (char c = 0; ; c++) {
			array[c] = Character.valueOf(c);
			array2[c] = c;
			if (c == 0xFFFF)
				break;
		}
		bytes = serialize(array);
		Assert.assertEquals(194436, bytes.length);
		Assert.assertEquals(AMF3_STRING, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof String);
		Assert.assertEquals(new String(array2), (String)o);
	}
	
	@Test
	public void testAMFByteArray() throws IOException {
		byte[] bytes = serialize(new byte[0]);
		Assert.assertEquals(2, bytes.length);
		Assert.assertEquals(AMF3_BYTEARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof byte[]);
		Assert.assertEquals(0, ((byte[])o).length);
		
		byte[] array = new byte[0xFF + 1];
		int i = 0;
		for (byte b = Byte.MIN_VALUE; ; b++) {
			array[i++] = b;
			if (b == Byte.MAX_VALUE)
				break;
		}
		bytes = serialize(array);
		Assert.assertEquals(259, bytes.length);
		Assert.assertEquals(AMF3_BYTEARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof byte[]);
		Assert.assertEquals(array.length, ((byte[])o).length);
		
		for (i = 0; i < array.length; i++)
			Assert.assertEquals(array[i], ((byte[])o)[i]);
	}
	
	@Test
	public void testAMFByteObjectArray() throws IOException {
		byte[] bytes = serialize(new Byte[0]);
		Assert.assertEquals(2, bytes.length);
		Assert.assertEquals(AMF3_BYTEARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof byte[]);
		Assert.assertEquals(0, ((byte[])o).length);
		
		Byte[] array = new Byte[0xFF + 2];
		array[0] = null;
		int i = 1;
		for (byte b = Byte.MIN_VALUE; ; b++) {
			array[i++] = Byte.valueOf(b);
			if (b == Byte.MAX_VALUE)
				break;
		}
		bytes = serialize(array);
		Assert.assertEquals(260, bytes.length);
		Assert.assertEquals(AMF3_BYTEARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof byte[]);
		Assert.assertEquals(array.length, ((byte[])o).length);
		
		byte[] bs = (byte[])o;
		for (i = 0; i < array.length; i++) {
			if (array[i] == null)
				Assert.assertEquals(0, bs[i]);
			else
				Assert.assertEquals(array[i], Byte.valueOf(bs[i]));
		}
	}
	
	@Test
	public void testAMFShortArray() throws IOException {
		byte[] bytes = serialize(new short[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		short[] array = new short[0xFFFF + 1];
		int i = 0;
		for (short b = Short.MIN_VALUE; ; b++) {
			array[i++] = b;
			if (b == Short.MAX_VALUE)
				break;
		}
		bytes = serialize(array);
		Assert.assertEquals(278405, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		for (i = 0; i < array.length; i++)
			Assert.assertEquals(array[i], ((Integer)(((Object[])o)[i])).shortValue());
	}
	
	@Test
	public void testAMFShortObjectArray() throws IOException {
		byte[] bytes = serialize(new Short[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		Short[] array = new Short[0xFFFF + 2];
		array[0] = null;
		int i = 1;
		for (short b = Short.MIN_VALUE; ; b++) {
			array[i++] = Short.valueOf(b);
			if (b == Short.MAX_VALUE)
				break;
		}
		bytes = serialize(array);
		Assert.assertEquals(278406, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (i = 0; i < array.length; i++) {
			if (os[i] == null)
				Assert.assertNull(array[i]);
			else
				Assert.assertEquals(array[i].shortValue(), ((Integer)os[i]).shortValue());
		}
	}
	
	@Test
	public void testAMFIntArray() throws IOException {
		byte[] bytes = serialize(new int[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		int[] array = new int[] {
			Integer.MIN_VALUE,
			-1,
			0,
			1,
			Integer.MAX_VALUE
		};
		
		bytes = serialize(array);
		Assert.assertEquals(30, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (int i = 0; i < array.length; i++) {
			if (i == 0 || i == array.length - 1)
				Assert.assertTrue(os[i] instanceof Double);
			else
				Assert.assertTrue(os[i] instanceof Integer);
			Assert.assertEquals(array[i], ((Number)os[i]).intValue());
		}
	}
	
	@Test
	public void testAMFIntObjectArray() throws IOException {
		byte[] bytes = serialize(new Integer[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		Integer[] array = new Integer[] {
			Integer.MIN_VALUE,
			-1,
			0,
			null,
			1,
			Integer.MAX_VALUE
		};
		
		bytes = serialize(array);
		Assert.assertEquals(31, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (int i = 0; i < array.length; i++) {
			if (os[i] == null)
				Assert.assertNull(array[i]);
			else {
				if (i == 0 || i == array.length - 1)
					Assert.assertTrue(os[i] instanceof Double);
				else
					Assert.assertTrue(os[i] instanceof Integer);
				Assert.assertEquals(array[i], Integer.valueOf(((Number)os[i]).intValue()));
			}
		}
	}
	
	@Test
	public void testAMFLongArray() throws IOException {
		byte[] bytes = serialize(new long[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		long[] array = new long[] {
			Long.MIN_VALUE,
			-1L,
			0L,
			1L,
			Long.MAX_VALUE
		};
		
		bytes = serialize(array);
		Assert.assertEquals(48, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (int i = 0; i < array.length; i++) {
			Assert.assertTrue(os[i] instanceof Double);
			Assert.assertEquals(array[i], ((Number)os[i]).longValue());
		}
	}
	
	@Test
	public void testAMFLongObjectArray() throws IOException {
		byte[] bytes = serialize(new Long[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		Long[] array = new Long[] {
			Long.MIN_VALUE,
			-1L,
			0L,
			null,
			1L,
			Long.MAX_VALUE
		};
		
		bytes = serialize(array);
		Assert.assertEquals(49, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (int i = 0; i < array.length; i++) {
			if (os[i] == null)
				Assert.assertNull(array[i]);
			else {
				Assert.assertTrue(os[i] instanceof Double);
				Assert.assertEquals(array[i], Long.valueOf(((Number)os[i]).longValue()));
			}
		}
	}
	
	@Test
	public void testAMFFloatArray() throws IOException {
		byte[] bytes = serialize(new float[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		float[] array = new float[] {
			Float.NEGATIVE_INFINITY,
			-Float.MAX_VALUE,
			-1.0f,
			-Float.MIN_VALUE,
			-0.0f,
			Float.NaN,
			0.0f,
			Float.MIN_VALUE,
			1.0f,
			Float.MAX_VALUE,
			Float.POSITIVE_INFINITY
		};
		
		bytes = serialize(array);
		Assert.assertEquals(102, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (int i = 0; i < array.length; i++) {
			if (os[i] == null)
				Assert.assertTrue(Float.isNaN(array[i]));
			else {
				Assert.assertTrue(os[i] instanceof Double);
				Assert.assertEquals(array[i], ((Number)os[i]).floatValue(), 0.0);
			}
		}
	}
	
	@Test
	public void testAMFFloatObjectArray() throws IOException {
		byte[] bytes = serialize(new Float[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		Float[] array = new Float[] {
			Float.NEGATIVE_INFINITY,
			-Float.MAX_VALUE,
			-1.0f,
			-Float.MIN_VALUE,
			-0.0f,
			Float.NaN,
			null,
			0.0f,
			Float.MIN_VALUE,
			1.0f,
			Float.MAX_VALUE,
			Float.POSITIVE_INFINITY
		};
		
		bytes = serialize(array);
		Assert.assertEquals(103, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (int i = 0; i < array.length; i++) {
			if (os[i] == null)
				Assert.assertTrue(array[i] == null || Float.isNaN(array[i]));
			else {
				Assert.assertTrue(os[i] instanceof Double);
				Assert.assertEquals(array[i], ((Number)os[i]).floatValue(), 0.0);
			}
		}
	}
	
	@Test
	public void testAMFDoubleArray() throws IOException {
		byte[] bytes = serialize(new double[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		double[] array = new double[] {
			Double.NEGATIVE_INFINITY,
			-Double.MAX_VALUE,
			-1.0,
			-Double.MIN_VALUE,
			-0.0,
			Double.NaN,
			0.0,
			Double.MIN_VALUE,
			1.0,
			Double.MAX_VALUE,
			Double.POSITIVE_INFINITY
		};
		
		bytes = serialize(array);
		Assert.assertEquals(102, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (int i = 0; i < array.length; i++) {
			if (os[i] == null)
				Assert.assertTrue(Double.isNaN(array[i]));
			else {
				Assert.assertTrue(os[i] instanceof Double);
				Assert.assertEquals(array[i], ((Number)os[i]).doubleValue(), 0.0);
			}
		}
	}
	
	@Test
	public void testAMFDoubleObjectArray() throws IOException {
		byte[] bytes = serialize(new Double[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		Double[] array = new Double[] {
			Double.NEGATIVE_INFINITY,
			-Double.MAX_VALUE,
			-1.0,
			-Double.MIN_VALUE,
			-0.0,
			Double.NaN,
			null,
			0.0,
			Double.MIN_VALUE,
			1.0,
			Double.MAX_VALUE,
			Double.POSITIVE_INFINITY
		};
		
		bytes = serialize(array);
		Assert.assertEquals(103, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		
		Object[] os = (Object[])o;
		for (int i = 0; i < array.length; i++) {
			if (os[i] == null)
				Assert.assertTrue(array[i] == null || Double.isNaN(array[i]));
			else {
				Assert.assertTrue(os[i] instanceof Double);
				Assert.assertEquals(array[i], ((Number)os[i]).doubleValue(), 0.0);
			}
		}
	}
	
	@Test
	public void testAMFObjectArray() throws IOException {
		byte[] bytes = serialize(new Object[0]);
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(0, ((Object[])o).length);
		
		Object[] array = new Object[] {
			null, "bla", Integer.valueOf(345), Double.MIN_VALUE, Boolean.TRUE
		};
		bytes = serialize(array);
		Assert.assertEquals(22, bytes.length);
		Assert.assertEquals(AMF3_ARRAY, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof Object[]);
		Assert.assertEquals(array.length, ((Object[])o).length);
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null)
				Assert.assertNull(((Object[])o)[i]);
			else
				Assert.assertEquals(array[i], ((Object[])o)[i]);
		}
	}
	
	@Test
	public void testAMFCollection() throws IOException {
		byte[] bytes = serialize(new ArrayList<Object>());
		Assert.assertEquals(39, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof ArrayCollection);
		Assert.assertEquals(0, ((ArrayCollection)o).size());
		
		bytes = serialize(new Vector<Object>());
		Assert.assertEquals(39, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof ArrayCollection);
		Assert.assertEquals(0, ((ArrayCollection)o).size());
		
		bytes = serialize(new HashSet<Object>());
		Assert.assertEquals(39, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof ArrayCollection);
		Assert.assertEquals(0, ((ArrayCollection)o).size());
		
		bytes = serialize(Arrays.asList((Object)null));
		Assert.assertEquals(40, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof ArrayCollection);
		Assert.assertEquals(1, ((ArrayCollection)o).size());
		Assert.assertNull(((ArrayCollection)o).get(0));
		
		bytes = serialize(Arrays.asList((Integer)null));
		Assert.assertEquals(40, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof ArrayCollection);
		Assert.assertEquals(1, ((ArrayCollection)o).size());
		Assert.assertNull(((ArrayCollection)o).get(0));
		
		List<Object> list = Arrays.asList(
			(Object)null, "bla", Integer.valueOf(345), Double.MIN_VALUE, Boolean.TRUE
		);
		bytes = serialize(list);
		Assert.assertEquals(58, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof ArrayCollection);
		Assert.assertEquals(list.size(), ((ArrayCollection)o).size());
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == null)
				Assert.assertNull(((ArrayCollection)o).get(i));
			else
				Assert.assertEquals(list.get(i), ((ArrayCollection)o).get(i));
		}
	}

	@Test
	public void testAMFMap() throws IOException {
		byte[] bytes = serialize(new HashMap<Object, Object>());
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		Object o = deserialize(bytes);
		Assert.assertTrue(o instanceof HashMap);
		Assert.assertEquals(0, ((HashMap<?, ?>)o).size());
		
		bytes = serialize(new LinkedHashMap<Object, Object>());
		Assert.assertEquals(4, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof HashMap);
		Assert.assertEquals(0, ((HashMap<?, ?>)o).size());
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(null, Boolean.TRUE); // will be discarded.
		map.put("null", null);
		map.put("bla", Double.MAX_VALUE);
		map.put("bli", Integer.valueOf(345));

		bytes = serialize(map);
		Assert.assertEquals(30, bytes.length);
		Assert.assertEquals(AMF3_OBJECT, bytes[0]);
		
		o = deserialize(bytes);
		Assert.assertTrue(o instanceof HashMap);
		Assert.assertEquals(map.size() - 1, ((HashMap<?, ?>)o).size());
		
		for (Map.Entry<?, ?> e : ((HashMap<?, ?>)o).entrySet()) {
			Assert.assertTrue(map.containsKey(e.getKey()));
			Assert.assertEquals(e.getValue(), map.get(e.getKey())); 
		}
	}
	
	private byte[] serialize(Object o) throws IOException {
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AMF3Serializer serializer = new AMF3Serializer(baos);
		serializer.writeObject(o);
		serializer.close();
		
		GraniteContext.release();
		
		return baos.toByteArray();
	}
	
	private Object deserialize(byte[] data) throws IOException {
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, null);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		AMF3Deserializer deserializer = new AMF3Deserializer(bais);
		Object o = deserializer.readObject();
		deserializer.close();
		
		GraniteContext.release();

		return o;
	}
}
