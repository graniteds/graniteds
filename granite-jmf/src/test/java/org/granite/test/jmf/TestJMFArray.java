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
import java.util.Arrays;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.granite.test.jmf.model.ExternalizableBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFArray implements JMFConstants {
	
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
	public void testNullArray() throws ClassNotFoundException, IOException {
		Assert.assertEquals(null, serializeDeserializeArray(null));
	}
	
	@Test
	public void testBooleanArray() throws ClassNotFoundException, IOException {
		boolean[] booleans = {};
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		booleans = new boolean[]{true};
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		booleans = new boolean[]{false};
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		booleans = new boolean[]{true, false};
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		booleans = new boolean[]{true, false, true, false, true, false, true, true};
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		booleans = new boolean[]{true, false, true, false, true, false, true, true, false};
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		booleans = new boolean[]{true, false, true, false, true, false, true, true, false, true, true, false};
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		booleans = new boolean[1024];
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		booleans = new boolean[1025];
		booleans[1024] = true;
		Assert.assertTrue(Arrays.equals(booleans, (boolean[])serializeDeserializeArray(booleans)));
		
		boolean[][] booleans2d = {};
		Assert.assertTrue(Arrays.deepEquals(booleans2d, (boolean[][])serializeDeserializeArray(booleans2d)));
		
		booleans = new boolean[]{true, false, true, false, true, false, true, true, false};
		booleans2d = new boolean[][]{booleans, booleans, null, {false, false, true}, booleans};
		Assert.assertTrue(Arrays.deepEquals(booleans2d, (boolean[][])serializeDeserializeArray(booleans2d)));
		
		booleans2d = new boolean[][]{{true, false}, {false}, null};
		Assert.assertTrue(Arrays.deepEquals(booleans2d, (boolean[][])serializeDeserializeArray(booleans2d)));
		
		booleans2d = new boolean[][]{{true, false, false, true, false, false, false, true}, {false}, {false}, {false}};
		Assert.assertTrue(Arrays.deepEquals(booleans2d, (boolean[][])serializeDeserializeArray(booleans2d)));
		
		booleans2d = new boolean[][]{
			{true, false, false, true, false, false, false, true},
			{true, false, false, true, false, false, false, true, true},
			{false},
			{false, true, false}
		};
		Assert.assertTrue(Arrays.deepEquals(booleans2d, (boolean[][])serializeDeserializeArray(booleans2d)));
		
		boolean[][][] booleans3d = {
			{
				{true, false, true, true, false, false, false, true},
				booleans2d[1]
			},
			null,
			booleans2d,
			{
				{false},
				{false, true, true},
				null
			},
			booleans2d
		};
		Assert.assertTrue(Arrays.deepEquals(booleans3d, (boolean[][][])serializeDeserializeArray(booleans3d)));
		
		boolean[][][][][] booleans5d = {};
		Assert.assertTrue(Arrays.deepEquals(booleans5d, (boolean[][][][][])serializeDeserializeArray(booleans5d)));
		
		booleans5d = new boolean[5][4][3][2][1];
		Assert.assertTrue(Arrays.deepEquals(booleans5d, (boolean[][][][][])serializeDeserializeArray(booleans5d)));
	}
	
	@Test
	public void testByteArray() throws ClassNotFoundException, IOException {
		byte[] bytes = {};
		Assert.assertTrue(Arrays.equals(bytes, (byte[])serializeDeserializeArray(bytes)));
		
		bytes = new byte[]{Byte.MIN_VALUE};
		Assert.assertTrue(Arrays.equals(bytes, (byte[])serializeDeserializeArray(bytes)));
		
		bytes = new byte[]{-1};
		Assert.assertTrue(Arrays.equals(bytes, (byte[])serializeDeserializeArray(bytes)));
		
		bytes = new byte[]{0};
		Assert.assertTrue(Arrays.equals(bytes, (byte[])serializeDeserializeArray(bytes)));
		
		bytes = new byte[]{1};
		Assert.assertTrue(Arrays.equals(bytes, (byte[])serializeDeserializeArray(bytes)));
		
		bytes = new byte[]{Byte.MAX_VALUE};
		Assert.assertTrue(Arrays.equals(bytes, (byte[])serializeDeserializeArray(bytes)));
		
		bytes = new byte[]{1, 2, 3, 4};
		Assert.assertTrue(Arrays.equals(bytes, (byte[])serializeDeserializeArray(bytes)));
		
		bytes = new byte[-((int)Byte.MIN_VALUE) * 2];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte)i;
		Assert.assertTrue(Arrays.equals(bytes, (byte[])serializeDeserializeArray(bytes)));
		
		byte[][] bytes2d = {};
		Assert.assertTrue(Arrays.deepEquals(bytes2d, (byte[][])serializeDeserializeArray(bytes2d)));
		
		bytes2d = new byte[][]{{}, {}};
		Assert.assertTrue(Arrays.deepEquals(bytes2d, (byte[][])serializeDeserializeArray(bytes2d)));
		
		bytes2d = new byte[][]{{(byte)0x01, (byte)0x02}, {(byte)0x03}, null};
		Assert.assertTrue(Arrays.deepEquals(bytes2d, (byte[][])serializeDeserializeArray(bytes2d)));
		
		byte[][][][][] bytes5d = {};
		Assert.assertTrue(Arrays.deepEquals(bytes5d, (byte[][][][][])serializeDeserializeArray(bytes5d)));
		
		bytes5d = new byte[5][4][3][2][1];
		Assert.assertTrue(Arrays.deepEquals(bytes5d, (byte[][][][][])serializeDeserializeArray(bytes5d)));
	}
	
	@Test
	public void testShortArray() throws ClassNotFoundException, IOException {
		short[] shorts = {};
		Assert.assertTrue(Arrays.equals(shorts, (short[])serializeDeserializeArray(shorts)));
		
		shorts = new short[]{Short.MIN_VALUE};
		Assert.assertTrue(Arrays.equals(shorts, (short[])serializeDeserializeArray(shorts)));
		
		shorts = new short[]{-1};
		Assert.assertTrue(Arrays.equals(shorts, (short[])serializeDeserializeArray(shorts)));
		
		shorts = new short[]{0};
		Assert.assertTrue(Arrays.equals(shorts, (short[])serializeDeserializeArray(shorts)));
		
		shorts = new short[]{1};
		Assert.assertTrue(Arrays.equals(shorts, (short[])serializeDeserializeArray(shorts)));
		
		shorts = new short[]{Short.MAX_VALUE};
		Assert.assertTrue(Arrays.equals(shorts, (short[])serializeDeserializeArray(shorts)));
		
		shorts = new short[]{(short)0x01, (short)0x02, (short)0x03, (short)0x04};
		Assert.assertTrue(Arrays.equals(shorts, (short[])serializeDeserializeArray(shorts)));
		
		shorts = new short[-((int)Short.MIN_VALUE) * 2];
		for (int i = 0; i < shorts.length; i++)
			shorts[i] = (short)i;
		Assert.assertTrue(Arrays.equals(shorts, (short[])serializeDeserializeArray(shorts)));
		
		short[][] shorts2d = {};
		Assert.assertTrue(Arrays.deepEquals(shorts2d, (short[][])serializeDeserializeArray(shorts2d)));
		
		shorts2d = new short[][]{{}, {}};
		Assert.assertTrue(Arrays.deepEquals(shorts2d, (short[][])serializeDeserializeArray(shorts2d)));
		
		shorts2d = new short[][]{{(short)0x01, (short)0x02}, {(short)0x03}, null};
		Assert.assertTrue(Arrays.deepEquals(shorts2d, (short[][])serializeDeserializeArray(shorts2d)));
		
		short[][][][][] shorts5d = {};
		Assert.assertTrue(Arrays.deepEquals(shorts5d, (short[][][][][])serializeDeserializeArray(shorts5d)));
		
		shorts5d = new short[5][4][3][2][1];
		Assert.assertTrue(Arrays.deepEquals(shorts5d, (short[][][][][])serializeDeserializeArray(shorts5d)));
	}
	
	@Test
	public void testIntArray() throws ClassNotFoundException, IOException {
		int[] ints = {};
		Assert.assertTrue(Arrays.equals(ints, (int[])serializeDeserializeArray(ints)));
		
		ints = new int[]{Integer.MIN_VALUE};
		Assert.assertTrue(Arrays.equals(ints, (int[])serializeDeserializeArray(ints)));
		
		ints = new int[]{-1};
		Assert.assertTrue(Arrays.equals(ints, (int[])serializeDeserializeArray(ints)));
		
		ints = new int[]{0};
		Assert.assertTrue(Arrays.equals(ints, (int[])serializeDeserializeArray(ints)));
		
		ints = new int[]{1};
		Assert.assertTrue(Arrays.equals(ints, (int[])serializeDeserializeArray(ints)));
		
		ints = new int[]{Integer.MAX_VALUE};
		Assert.assertTrue(Arrays.equals(ints, (int[])serializeDeserializeArray(ints)));
		
		ints = new int[]{0x01, 0x02, 0x03, 0x04};
		Assert.assertTrue(Arrays.equals(ints, (int[])serializeDeserializeArray(ints)));
		
		ints = new int[-Short.MIN_VALUE * 2];
		for (int i = 0; i < ints.length; i++)
			ints[i] = i;
		Assert.assertTrue(Arrays.equals(ints, (int[])serializeDeserializeArray(ints)));
		
		int[][] ints2d = {};
		Assert.assertTrue(Arrays.deepEquals(ints2d, (int[][])serializeDeserializeArray(ints2d)));
		
		ints2d = new int[][]{{}, {}};
		Assert.assertTrue(Arrays.deepEquals(ints2d, (int[][])serializeDeserializeArray(ints2d)));
		
		ints2d = new int[][]{{0x01, 0x02}, {0x03}, null};
		Assert.assertTrue(Arrays.deepEquals(ints2d, (int[][])serializeDeserializeArray(ints2d)));
		
		int[][][][][] ints5d = {};
		Assert.assertTrue(Arrays.deepEquals(ints5d, (int[][][][][])serializeDeserializeArray(ints5d)));
		
		ints5d = new int[5][4][3][2][1];
		Assert.assertTrue(Arrays.deepEquals(ints5d, (int[][][][][])serializeDeserializeArray(ints5d)));
	}
	
	@Test
	public void testLongArray() throws ClassNotFoundException, IOException {
		long[] longs = {};
		Assert.assertTrue(Arrays.equals(longs, (long[])serializeDeserializeArray(longs)));
		
		longs = new long[]{Long.MIN_VALUE};
		Assert.assertTrue(Arrays.equals(longs, (long[])serializeDeserializeArray(longs)));
		
		longs = new long[]{-1};
		Assert.assertTrue(Arrays.equals(longs, (long[])serializeDeserializeArray(longs)));
		
		longs = new long[]{0};
		Assert.assertTrue(Arrays.equals(longs, (long[])serializeDeserializeArray(longs)));
		
		longs = new long[]{1};
		Assert.assertTrue(Arrays.equals(longs, (long[])serializeDeserializeArray(longs)));
		
		longs = new long[]{Long.MAX_VALUE};
		Assert.assertTrue(Arrays.equals(longs, (long[])serializeDeserializeArray(longs)));
		
		longs = new long[]{0x01, 0x02, 0x03, 0x04};
		Assert.assertTrue(Arrays.equals(longs, (long[])serializeDeserializeArray(longs)));
		
		longs = new long[-Short.MIN_VALUE * 2];
		for (int i = 0; i < longs.length; i++)
			longs[i] = i;
		Assert.assertTrue(Arrays.equals(longs, (long[])serializeDeserializeArray(longs)));
		
		long[][] longs2d = {};
		Assert.assertTrue(Arrays.deepEquals(longs2d, (long[][])serializeDeserializeArray(longs2d)));
		
		longs2d = new long[][]{{}, {}};
		Assert.assertTrue(Arrays.deepEquals(longs2d, (long[][])serializeDeserializeArray(longs2d)));
		
		longs2d = new long[][]{{0x01, 0x02}, {0x03}, null};
		Assert.assertTrue(Arrays.deepEquals(longs2d, (long[][])serializeDeserializeArray(longs2d)));
		
		long[][][][][] longs5d = {};
		Assert.assertTrue(Arrays.deepEquals(longs5d, (long[][][][][])serializeDeserializeArray(longs5d)));
		
		longs5d = new long[5][4][3][2][1];
		Assert.assertTrue(Arrays.deepEquals(longs5d, (long[][][][][])serializeDeserializeArray(longs5d)));
	}
	
	@Test
	public void testFloatArray() throws ClassNotFoundException, IOException {
		float[] floats = {};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{Float.NaN};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{Float.NEGATIVE_INFINITY};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{-1.0F};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{0.0F};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{Float.MIN_VALUE};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{Float.MIN_NORMAL};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{Float.MAX_VALUE};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{Float.POSITIVE_INFINITY};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		floats = new float[]{0.0F, 1.0F, 2.0F, 3.0F};
		Assert.assertTrue(Arrays.equals(floats, (float[])serializeDeserializeArray(floats)));
		
		float[][] floats2d = {};
		Assert.assertTrue(Arrays.deepEquals(floats2d, (float[][])serializeDeserializeArray(floats2d)));
		
		floats2d = new float[][]{{}, {}};
		Assert.assertTrue(Arrays.deepEquals(floats2d, (float[][])serializeDeserializeArray(floats2d)));
		
		floats2d = new float[][]{{0.0F, 1.0F}, {2.0F}, null};
		Assert.assertTrue(Arrays.deepEquals(floats2d, (float[][])serializeDeserializeArray(floats2d)));
		
		float[][][][][] floats5d = {};
		Assert.assertTrue(Arrays.deepEquals(floats5d, (float[][][][][])serializeDeserializeArray(floats5d)));
		
		floats5d = new float[5][4][3][2][1];
		Assert.assertTrue(Arrays.deepEquals(floats5d, (float[][][][][])serializeDeserializeArray(floats5d)));
	}
	
	@Test
	public void testDoubleArray() throws ClassNotFoundException, IOException {
		double[] doubles = {};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{Double.NaN};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{Double.NEGATIVE_INFINITY};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{-1.0};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{0.0};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{Double.MIN_VALUE};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{Double.MIN_NORMAL};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{Double.MAX_VALUE};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{Double.POSITIVE_INFINITY};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		doubles = new double[]{0.0, 1.0, 2.0, 3.0};
		Assert.assertTrue(Arrays.equals(doubles, (double[])serializeDeserializeArray(doubles)));
		
		double[][] doubles2d = {};
		Assert.assertTrue(Arrays.deepEquals(doubles2d, (double[][])serializeDeserializeArray(doubles2d)));
		
		doubles2d = new double[][]{{}, {}};
		Assert.assertTrue(Arrays.deepEquals(doubles2d, (double[][])serializeDeserializeArray(doubles2d)));
		
		doubles2d = new double[][]{{0.0, 1.0}, {2.0}, null};
		Assert.assertTrue(Arrays.deepEquals(doubles2d, (double[][])serializeDeserializeArray(doubles2d)));
		
		double[][][][][] doubles5d = {};
		Assert.assertTrue(Arrays.deepEquals(doubles5d, (double[][][][][])serializeDeserializeArray(doubles5d)));
		
		doubles5d = new double[5][4][3][2][1];
		Assert.assertTrue(Arrays.deepEquals(doubles5d, (double[][][][][])serializeDeserializeArray(doubles5d)));
	}
	
	@Test
	public void testCharArray() throws ClassNotFoundException, IOException {
		char[] chars = {};
		Assert.assertTrue(Arrays.equals(chars, (char[])serializeDeserializeArray(chars)));
		
		chars = new char[]{Character.MIN_VALUE};
		Assert.assertTrue(Arrays.equals(chars, (char[])serializeDeserializeArray(chars)));
		
		chars = new char[]{1};
		Assert.assertTrue(Arrays.equals(chars, (char[])serializeDeserializeArray(chars)));
		
		chars = new char[]{Character.MAX_VALUE};
		Assert.assertTrue(Arrays.equals(chars, (char[])serializeDeserializeArray(chars)));
		
		chars = new char[]{(char)0x01, (char)0x02, (char)0x03, (char)0x04};
		Assert.assertTrue(Arrays.equals(chars, (char[])serializeDeserializeArray(chars)));
		
		chars = new char[Character.MAX_VALUE + 1];
		for (int i = 0; i < chars.length; i++)
			chars[i] = (char)i;
		Assert.assertTrue(Arrays.equals(chars, (char[])serializeDeserializeArray(chars)));
		
		char[][] chars2d = {};
		Assert.assertTrue(Arrays.deepEquals(chars2d, (char[][])serializeDeserializeArray(chars2d)));
		
		chars2d = new char[][]{{}, {}};
		Assert.assertTrue(Arrays.deepEquals(chars2d, (char[][])serializeDeserializeArray(chars2d)));
		
		chars2d = new char[][]{{(char)0x01, (char)0x02}, {(char)0x03}, null};
		Assert.assertTrue(Arrays.deepEquals(chars2d, (char[][])serializeDeserializeArray(chars2d)));
		
		char[][][][][] chars5d = {};
		Assert.assertTrue(Arrays.deepEquals(chars5d, (char[][][][][])serializeDeserializeArray(chars5d)));
		
		chars5d = new char[5][4][3][2][1];
		Assert.assertTrue(Arrays.deepEquals(chars5d, (char[][][][][])serializeDeserializeArray(chars5d)));
	}
	
	@Test
	public void testObjectArray() throws ClassNotFoundException, IOException {
		Object[] objects = {};
		Assert.assertTrue(Arrays.equals(objects, (Object[])serializeDeserializeArray(objects)));

		objects = new Object[]{null};
		Assert.assertTrue(Arrays.equals(objects, (Object[])serializeDeserializeArray(objects)));

		objects = new Object[]{"bla"};
		Assert.assertTrue(Arrays.equals(objects, (Object[])serializeDeserializeArray(objects)));

		objects = new Object[]{"bla", 234, 1.0};
		Assert.assertTrue(Arrays.equals(objects, (Object[])serializeDeserializeArray(objects)));

		objects = new Object[]{"bla", 234, 1.0, null, 0L, 3.0F, 'c'};
		Assert.assertTrue(Arrays.equals(objects, (Object[])serializeDeserializeArray(objects)));

		Integer[] integers = {1, 2, 3, 4, 5};
		Assert.assertTrue(Arrays.equals(integers, (Integer[])serializeDeserializeArray(integers)));
		
		Object[][] objects2d = {};
		Assert.assertTrue(Arrays.deepEquals(objects2d, (Object[][])serializeDeserializeArray(objects2d)));
		
		objects2d = new Object[][]{null};
		Assert.assertTrue(Arrays.deepEquals(objects2d, (Object[][])serializeDeserializeArray(objects2d)));
		
		objects2d = new Object[][]{{}, {}};
		Assert.assertTrue(Arrays.deepEquals(objects2d, (Object[][])serializeDeserializeArray(objects2d)));
		
		objects2d = new Object[][]{objects, objects};
		Assert.assertTrue(Arrays.deepEquals(objects2d, (Object[][])serializeDeserializeArray(objects2d)));
		
		objects2d = new Object[][]{{"bla", 345}, {null}};
		Assert.assertTrue(Arrays.deepEquals(objects2d, (Object[][])serializeDeserializeArray(objects2d)));
		
		Object[][][] objects3d = {
			{
				{true, "bla", false, 1.0, false, false, false, true},
				{true, false, 45, true, "bla", false, false, true, true},
				objects
			},
			null,
			{
				new Boolean[] {false, true},
				{false, -1, "bla"},
				new Integer[] {0, 1, 2, 3, 4, 5},
				null,
				objects
			},
			{
				new ExternalizableBean[] {},
				objects
			}
		};
		Object[][][] objects3dCopy = (Object[][][])serializeDeserializeArray(objects3d);
		Assert.assertTrue(Arrays.deepEquals(objects3d, objects3dCopy));
		Assert.assertEquals(Object.class, objects3dCopy.getClass().getComponentType().getComponentType().getComponentType());
		Assert.assertEquals(Boolean.class, objects3dCopy[2][0].getClass().getComponentType());
		Assert.assertEquals(Object.class, objects3dCopy[2][1].getClass().getComponentType());
		Assert.assertEquals(Integer.class, objects3dCopy[2][2].getClass().getComponentType());
		
		Object[][][][][] objects5d = {};
		Assert.assertTrue(Arrays.deepEquals(objects5d, (Object[][][][][])serializeDeserializeArray(objects5d)));
		
		objects5d = new Object[5][4][3][2][1];
		objects5d[2][2][0][1][0] = "bla";
		Assert.assertTrue(Arrays.deepEquals(objects5d, (Object[][][][][])serializeDeserializeArray(objects5d)));
	}
	
	private Object serializeDeserializeArray(Object v) throws ClassNotFoundException, IOException {
		return serializeDeserializeArray(v, false);
	}
	
	private Object serializeDeserializeArray(Object v, boolean dump) throws ClassNotFoundException, IOException {
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
		Object clone = deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
