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
package org.granite.messaging.jmf.codec.std.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.JMFEncodingException;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.PrimitiveArrayCodec;
import org.granite.messaging.jmf.codec.std.impl.util.FloatUtil;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;
import org.granite.messaging.jmf.codec.std.impl.util.LongUtil;
import org.granite.messaging.jmf.codec.std.impl.util.DoubleUtil;

/**
 * @author Franck WOLFF
 */
public class PrimitiveArrayCodecImpl extends AbstractArrayCodec implements PrimitiveArrayCodec {
	
	protected static final boolean[] BOOLEAN_0 = new boolean[0];
	protected static final char[] CHARACTER_0 = new char[0];
	protected static final byte[] BYTE_0 = new byte[0];
	protected static final short[] SHORT_0 = new short[0];
	protected static final int[] INTEGER_0 = new int[0];
	protected static final long[] LONG_0 = new long[0];
	protected static final float[] FLOAT_0 = new float[0];
	protected static final double[] DOUBLE_0 = new double[0];
	
	public int getObjectType() {
		return JMF_PRIMITIVE_ARRAY;
	}

	public boolean canEncode(Object v) {
		return v.getClass().isArray() && getComponentType(v).isPrimitive();
	}

	public void encode(OutputContext ctx, Object v) throws IOException {
		int indexOfStoredObject = ctx.indexOfObject(v);
		if (indexOfStoredObject != -1) {
			int count = IntegerUtil.significantIntegerBytesCount0(indexOfStoredObject);

			// Write the index to a stored array:
			// [parameterized type{1}, index{1,4}]
			ctx.getOutputStream().write(0x80 | (count << 4) | JMF_PRIMITIVE_ARRAY);
			IntegerUtil.encodeInteger(ctx, indexOfStoredObject, count);
		}
		else {
			ctx.addToObjects(v);
			
			ArrayStructure structure = new ArrayStructure(v);
			int jmfComponentType = primitiveClassToJmfType(structure.componentType);
			int length = Array.getLength(v);
			int count = IntegerUtil.significantIntegerBytesCount0(length);

			final OutputStream os = ctx.getOutputStream();
			
			if (structure.dimensions == 0) {
				// Write the length and component type of the array:
				// [parameterized type{1}, length{1,4}, jmf component type{1}]
				os.write((count << 4) | JMF_PRIMITIVE_ARRAY);
				IntegerUtil.encodeInteger(ctx, length, count);
				os.write(jmfComponentType);
				
				writePrimitiveArrayContent0(ctx, v, jmfComponentType);
			}
			else {
				// Write the length, component type and dimensions of the array:
				// [parameterized type{1}, length{1,4}, jmf component type{1}, dimensions{1}]
				os.write(0x40 | (count << 4) | JMF_PRIMITIVE_ARRAY);
				IntegerUtil.encodeInteger(ctx, length, count);
				os.write(jmfComponentType);
				os.write(structure.dimensions);
				
				writePrimitiveArrayContent(ctx, v, jmfComponentType, structure.dimensions);
			}
		}
	}
	
	protected void writePrimitiveArrayContent(OutputContext ctx, Object v, int jmfComponentType, int dimensions) throws IOException {
		final int length = Array.getLength(v);
		if (length == 0)
			return;
		
		dimensions--;
		final boolean writePrimitiveArray0 = (dimensions == 0);
		final OutputStream os = ctx.getOutputStream();

		for (int index = 0; index < length; index++) {
			Object component = Array.get(v, index);
			if (component == null)
				os.write(JMF_NULL);
			else {
				int indexOfStoredObject = ctx.indexOfObject(component);
				if (indexOfStoredObject != -1) {
					int count = IntegerUtil.significantIntegerBytesCount0(indexOfStoredObject);
					// Write the index to a stored array:
					// [parameterized type{1}, index{1,4}]
					ctx.getOutputStream().write(0x80 | (count << 4) | JMF_PRIMITIVE_ARRAY);
					IntegerUtil.encodeInteger(ctx, indexOfStoredObject, count);
				}
				else {
					ctx.addToObjects(component);
					
					int componentLength = Array.getLength(component);
					int count = IntegerUtil.significantIntegerBytesCount0(componentLength);

					// Write the length of the array:
					// [parameterized type{1}, length{1,4}]
					os.write((count << 4) | JMF_PRIMITIVE_ARRAY);
					IntegerUtil.encodeInteger(ctx, componentLength, count);
					
					if (writePrimitiveArray0)
						writePrimitiveArrayContent0(ctx, component, jmfComponentType);
					else
						writePrimitiveArrayContent(ctx, component, jmfComponentType, dimensions);
				}
			}
		}
	}
	
	protected void writePrimitiveArrayContent0(OutputContext ctx, Object v, int jmfComponentType) throws IOException {
		switch (jmfComponentType) {
			case JMF_BOOLEAN:
				writeBooleanArrayContent0(ctx, (boolean[])v);
				break;
			case JMF_CHARACTER:
				writeCharacterArrayContent0(ctx, (char[])v);
				break;
			case JMF_BYTE:
				writeByteArrayContent0(ctx, (byte[])v);
				break;
			case JMF_SHORT:
				writeShortArrayContent0(ctx, (short[])v);
				break;
			case JMF_INTEGER:
				writeIntegerArrayContent0(ctx, (int[])v);
				break;
			case JMF_LONG:
				writeLongArrayContent0(ctx, (long[])v);
				break;
			case JMF_FLOAT:
				writeFloatArrayContent0(ctx, (float[])v);
				break;
			case JMF_DOUBLE:
				writeDoubleArrayContent0(ctx, (double[])v);
				break;
			default:
				throw new JMFEncodingException("Unsupported primitive type: " + jmfComponentType);
		}
	}
	
	protected void writeBooleanArrayContent0(OutputContext ctx, boolean[] v) throws IOException {
		if (v.length == 0)
			return;

		final OutputStream os = ctx.getOutputStream();

		byte[] bytes = new byte[lengthOfBooleanArray(v.length)];
		int i = 0, j = 0;
		for (boolean b : v) {
			if (b)
				bytes[i] |= 0x80 >>> j;
			j++;
			if (j >= 8) {
				j = 0;
				i++;
			}
		}
		os.write(bytes);
	}
	
	protected void writeCharacterArrayContent0(OutputContext ctx, char[] v) throws IOException {
		if (v.length == 0)
			return;

		final OutputStream os = ctx.getOutputStream();

		for (char c : v) {
			os.write(c >>> 8);
			os.write(c);
		}
	}
	
	protected void writeByteArrayContent0(OutputContext ctx, byte[] v) throws IOException {
		if (v.length == 0)
			return;

		ctx.getOutputStream().write(v);
	}
	
	protected void writeShortArrayContent0(OutputContext ctx, short[] v) throws IOException {
		if (v.length == 0)
			return;

		final OutputStream os = ctx.getOutputStream();
		
		for (short s : v) {
			os.write(s >>> 8);
			os.write(s);
		}
	}
	
	protected void writeIntegerArrayContent0(OutputContext ctx, int[] v) throws IOException {
		if (v.length == 0)
			return;

		for (int i : v)
			IntegerUtil.encodeVariableInteger(ctx, i);
	}
	
	protected void writeLongArrayContent0(OutputContext ctx, long[] v) throws IOException {
		if (v.length == 0)
			return;

		for (long l : v)
			LongUtil.encodeVariableLong(ctx, l);
	}
	
	protected void writeFloatArrayContent0(OutputContext ctx, float[] v) throws IOException {
		if (v.length == 0)
			return;

		for (float f : v)
			FloatUtil.encodeFloat(ctx, f);
	}
	
	protected void writeDoubleArrayContent0(OutputContext ctx, double[] v) throws IOException {
		if (v.length == 0)
			return;

		for (double d : v)
			DoubleUtil.encodeVariableDouble(ctx, d);
	}
	
	public Object decode(InputContext ctx, int parameterizedJmfType) throws IOException, ClassNotFoundException {
		Object v = null;
		
		// Read the index (stored array) or length of the array:
		// [index or length{1,4}]
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> 4) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0)
			v = ctx.getObject(indexOrLength);
		else {
			// Read the component type and, if the 0x40 flag is set, the dimensions of the array:
			// [component type{1}, dimensions{0,1}]
			int jmfComponentType = ctx.safeRead();
			int dimensions = ((parameterizedJmfType & 0x40) == 0 ? 0 : ctx.safeRead());
			
			Class<?> componentType = jmfTypeToPrimitiveClass(jmfComponentType);
			if (dimensions == 0)
				v = readPrimitiveArray0(ctx, componentType, jmfComponentType, indexOrLength);
			else
				v = readPrimitiveArray(ctx, componentType, jmfComponentType, indexOrLength, dimensions);
		}
		
		return v;
	}
	
	protected Object readPrimitiveArray(InputContext ctx, Class<?> componentType, int jmfComponentType, int length, int dimensions) throws IOException {
		Object v = newArray(componentType, length, dimensions);
		ctx.addToObjects(v);
		
		dimensions--;

		final boolean readPrimitiveArray0 = (dimensions == 0);
		for (int index = 0; index < length; index++) {
			// Read the type of the element (must be JMF_NULL or JMF_PRIMITIVE_ARRAY):
			// [array element type{1}]
			int eltParameterizedJmfType = ctx.safeRead();
			
			if (eltParameterizedJmfType == JMF_NULL)
				Array.set(v, index, null);
			else {
				// Read the length of the element (sub array):
				// [length{1, 4}]
				int eltIndexOrLength = IntegerUtil.decodeInteger(ctx, (eltParameterizedJmfType >>> 4) & 0x03);
				if ((eltParameterizedJmfType & 0x80) != 0)
					Array.set(v, index, ctx.getObject(eltIndexOrLength));
				else if (readPrimitiveArray0)
					Array.set(v, index, readPrimitiveArray0(ctx, componentType, jmfComponentType, eltIndexOrLength));
				else
					Array.set(v, index, readPrimitiveArray(ctx, componentType, jmfComponentType, eltIndexOrLength, dimensions));
			}
		}
		
		return v;
	}
	
	protected Object readPrimitiveArray0(InputContext ctx, Class<?> componentType, int jmfComponentType, int length) throws IOException {
		Object v = null;
		
		switch (jmfComponentType) {
			case JMF_BOOLEAN:
				v = readBooleanArray0(ctx, length);
				break;
			case JMF_CHARACTER:
				v = readCharacterArray0(ctx, length);
				break;
			case JMF_BYTE:
				v = readByteArray0(ctx, length);
				break;
			case JMF_SHORT:
				v = readShortArray0(ctx, length);
				break;
			case JMF_INTEGER:
				v = readIntegerArray0(ctx, length);
				break;
			case JMF_LONG:
				v = readLongArray0(ctx, length);
				break;
			case JMF_FLOAT:
				v = readFloatArray0(ctx, length);
				break;
			case JMF_DOUBLE:
				v = readDoubleArray0(ctx, length);
				break;
			default:
				throw new JMFEncodingException("Unsupported primitive type: " + jmfComponentType);
		}
		
		ctx.addToObjects(v);
		
		return v;
	}
	
	protected boolean[] readBooleanArray0(InputContext ctx, int length) throws IOException {
		if (length == 0)
			return BOOLEAN_0;
		
		boolean[] a = new boolean[length];
		int nb = lengthOfBooleanArray(length);
		for (int i = 0; i < nb; i++) {
			int b = ctx.safeRead();
			for (int j = 0; j < 8; j++) {
				int index = (i * 8) + j;
				if (index >= length)
					break;
				a[index] = ((b & (0x80 >>> j)) != 0);
			}
		}
		return a;
	}
	
	protected char[] readCharacterArray0(InputContext ctx, int length) throws IOException {
		if (length == 0)
			return CHARACTER_0;
		
		char[] a = new char[length];
		for (int i = 0; i < length; i++)
			a[i] = (char)((ctx.safeRead() << 8) | ctx.safeRead());
		return a;
	}
	
	protected byte[] readByteArray0(InputContext ctx, int length) throws IOException {
		if (length == 0)
			return BYTE_0;
		
		byte[] a = new byte[length];
		ctx.safeReadFully(a);
		return a;
	}
	
	protected short[] readShortArray0(InputContext ctx, int length) throws IOException {
		if (length == 0)
			return SHORT_0;
		
		short[] a = new short[length];
		for (int i = 0; i < length; i++)
			a[i] = (short)((ctx.safeRead() << 8) | ctx.safeRead());
		return a;
	}
	
	protected int[] readIntegerArray0(InputContext ctx, int length) throws IOException {
		if (length == 0)
			return INTEGER_0;

		int[] a = new int[length];
		for (int i = 0; i < length; i++)
			a[i] = IntegerUtil.decodeVariableInteger(ctx);
		return a;
	}
	
	protected long[] readLongArray0(InputContext ctx, int length) throws IOException {
		if (length == 0)
			return LONG_0;

		long[] a = new long[length];
		for (int i = 0; i < length; i++)
			a[i] = LongUtil.decodeVariableLong(ctx);
		return a;
	}
	
	protected float[] readFloatArray0(InputContext ctx, int length) throws IOException {
		if (length == 0)
			return FLOAT_0;

		float[] a = new float[length];
		for (int i = 0; i < length; i++)
			a[i] = FloatUtil.decodeFloat(ctx);
		return a;
	}
	
	protected double[] readDoubleArray0(InputContext ctx, int length) throws IOException {
		if (length == 0)
			return DOUBLE_0;
		
		double[] a = new double[length];
		for (int i = 0; i < length; i++)
			a[i] = DoubleUtil.decodeVariableDouble(ctx);
		return a;
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();

		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_PRIMITIVE_ARRAY)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		// Read the index (stored array) or length of the array:
		// [index or length{1,4}]
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> 4) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0)
			ctx.indentPrintLn("<" + ctx.getObject(indexOrLength) + "@" + indexOrLength + ">");
		else {
			// Read the component type and, if the 0x40 flag is set, the dimensions of the array:
			// [component type{1}, dimensions{0,1}]
			int jmfComponentType = ctx.safeRead();
			int dimensions = ((parameterizedJmfType & 0x40) == 0 ? 0 : ctx.safeRead());
			
			Class<?> componentType = jmfTypeToPrimitiveClass(jmfComponentType);
			if (dimensions == 0)
				dumpPrimitiveArray0(ctx, componentType, jmfComponentType, indexOrLength);
			else
				dumpPrimitiveArray(ctx, componentType, jmfComponentType, indexOrLength, dimensions);
		}
	}
	
	protected void dumpPrimitiveArray(DumpContext ctx, Class<?> componentType, int jmfComponentType, int length, int dimensions) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();

		String v = newDumpPrimitiveArray(jmfComponentType, length, dimensions);
		int indexOfStoredObject = ctx.addToObjects(v);
		ctx.indentPrintLn(v + "@" + indexOfStoredObject + ": {");
		ctx.incrIndent(1);
		
		dimensions--;

		final boolean dumpPrimitiveArray0 = (dimensions == 0);
		for (int index = 0; index < length; index++) {
			// Read the type of the element (must be JMF_NULL or JMF_PRIMITIVE_ARRAY):
			// [array element type{1}]
			int parameterizedJmfType = ctx.safeRead();
			int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
			
			if (jmfType == JMF_NULL)
				ctx.indentPrintLn("null");
			else if (jmfType == JMF_PRIMITIVE_ARRAY) {
				// Read the length of the sub array:
				// [length{1, 4}]
				int subLengthOrIndex = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> 4) & 0x03);
				if ((parameterizedJmfType & 0x80) != 0)
					ctx.indentPrintLn("<" + ctx.getObject(subLengthOrIndex) + "@" + subLengthOrIndex + ">");
				else if (dumpPrimitiveArray0)
					dumpPrimitiveArray0(ctx, componentType, jmfComponentType, subLengthOrIndex);
				else
					dumpPrimitiveArray(ctx, componentType, jmfComponentType, subLengthOrIndex, dimensions);
			}
			else
				newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
		
		ctx.incrIndent(-1);
		ctx.indentPrintLn("}");
	}
	
	protected void dumpPrimitiveArray0(DumpContext ctx, Class<?> componentType, int jmfComponentType, int length) throws IOException {
		String v = newDumpPrimitiveArray(jmfComponentType, length, 0);
		int indexOfStoredObject = ctx.addToObjects(v);
		ctx.indentPrint(v + "@" + indexOfStoredObject + ": {");
		
		switch (jmfComponentType) {
			case JMF_BOOLEAN:
				dumpBooleanArray0(ctx, length);
				break;
			case JMF_CHARACTER:
				dumpCharacterArray0(ctx, length);
				break;
			case JMF_BYTE:
				dumpByteArray0(ctx, length);
				break;
			case JMF_SHORT:
				dumpShortArray0(ctx, length);
				break;
			case JMF_INTEGER:
				dumpIntegerArray0(ctx, length);
				break;
			case JMF_LONG:
				dumpLongArray0(ctx, length);
				break;
			case JMF_FLOAT:
				dumpFloatArray0(ctx, length);
				break;
			case JMF_DOUBLE:
				dumpDoubleArray0(ctx, length);
				break;
			default:
				throw new JMFEncodingException("Unsupported primitive type: " + jmfComponentType);
		}

		ctx.noIndentPrintLn("}");
	}
	
	protected String newDumpPrimitiveArray(int jmfComponentType, int length, int dimensions) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		switch (jmfComponentType) {
			case JMF_BOOLEAN: sb.append("boolean"); break;
			case JMF_CHARACTER: sb.append("char"); break;
			case JMF_BYTE: sb.append("byte"); break;
			case JMF_SHORT: sb.append("short"); break;
			case JMF_INTEGER: sb.append("int"); break;
			case JMF_LONG: sb.append("long"); break;
			case JMF_FLOAT: sb.append("float"); break;
			case JMF_DOUBLE: sb.append("double"); break;
			default: throw new JMFEncodingException("Unsupported primitive type: " + jmfComponentType);
		}
		
		sb.append('[').append(length).append(']');
		
		for (int i = 0; i < dimensions; i++)
			sb.append("[]");
		
		return sb.toString();
	}
	
	protected void dumpBooleanArray0(DumpContext ctx, int length) throws IOException {
		if (length == 0)
			return;

		int nb = lengthOfBooleanArray(length);
		for (int i = 0; i < nb; i++) {
			int b = ctx.safeRead();
			for (int j = 0; j < 8; j++) {
				int index = (i * 8) + j;
				if (index >= length)
					break;
				if (index > 0)
					ctx.print(", ");
				ctx.print(String.valueOf(((b & (0x80 >>> j)) != 0)));
			}
		}
	}
	
	protected void dumpCharacterArray0(DumpContext ctx, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			if (i > 0)
				ctx.print(", ");
			ctx.print(String.valueOf((char)((ctx.safeRead() << 8) | ctx.safeRead())));
		}
	}
	
	protected void dumpByteArray0(DumpContext ctx, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			if (i > 0)
				ctx.print(", ");
			ctx.print(String.valueOf((byte)ctx.safeRead()));
		}
	}
	
	protected void dumpShortArray0(DumpContext ctx, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			if (i > 0)
				ctx.print(", ");
			ctx.print(String.valueOf((short)(ctx.safeRead() << 8) | ctx.safeRead()));
		}
	}
	
	protected void dumpIntegerArray0(DumpContext ctx, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			if (i > 0)
				ctx.print(", ");
			ctx.print(String.valueOf(IntegerUtil.decodeVariableInteger(ctx)));
		}
	}
	
	protected void dumpLongArray0(DumpContext ctx, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			if (i > 0)
				ctx.print(", ");
			ctx.print(String.valueOf(LongUtil.decodeVariableLong(ctx)));
		}
	}
	
	protected void dumpFloatArray0(DumpContext ctx, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			if (i > 0)
				ctx.print(", ");
			ctx.print(String.valueOf(FloatUtil.decodeFloat(ctx)));
		}
	}
	
	protected void dumpDoubleArray0(DumpContext ctx, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			if (i > 0)
				ctx.print(", ");
			ctx.print(String.valueOf(DoubleUtil.decodeVariableDouble(ctx)));
		}
	}
	
	protected int lengthOfBooleanArray(int nb) {
		return (nb / 8) + (nb % 8 != 0 ? 1 : 0);
	}
	
	protected int primitiveClassToJmfType(Class<?> primitiveClass) throws JMFEncodingException {
		if (primitiveClass == byte.class)
			return JMF_BYTE;
		if (primitiveClass == int.class)
			return JMF_INTEGER;
		if (primitiveClass == char.class)
			return JMF_CHARACTER;
		if (primitiveClass == double.class)
			return JMF_DOUBLE;
		if (primitiveClass == long.class)
			return JMF_LONG;
		if (primitiveClass == boolean.class)
			return JMF_BOOLEAN;
		if (primitiveClass == float.class)
			return JMF_FLOAT;
		if (primitiveClass == short.class)
			return JMF_SHORT;
		throw new JMFEncodingException("Not a primitive class: " + primitiveClass);
	}
	
	protected Class<?> jmfTypeToPrimitiveClass(int jmfType) throws JMFEncodingException {
		switch (jmfType) {
		case JMF_BOOLEAN:
			return boolean.class;
		case JMF_BYTE:
			return byte.class;
		case JMF_CHARACTER:
			return char.class;
		case JMF_SHORT:
			return short.class;
		case JMF_INTEGER:
			return int.class;
		case JMF_LONG:
			return long.class;
		case JMF_FLOAT:
			return float.class;
		case JMF_DOUBLE:
			return double.class;
		}
		throw new JMFEncodingException("Not a primitive JMF type: " + jmfType);
	}
}
