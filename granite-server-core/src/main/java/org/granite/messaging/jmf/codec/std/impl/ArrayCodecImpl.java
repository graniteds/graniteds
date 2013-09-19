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
/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
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
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.ArrayCodec;
import org.granite.messaging.jmf.codec.std.IntegerCodec;
import org.granite.messaging.jmf.codec.std.LongCodec;

/**
 * @author Franck WOLFF
 */
public class ArrayCodecImpl extends AbstractIntegerStringCodec<Object> implements ArrayCodec {
	
	public int getObjectType() {
		return JMF_ARRAY;
	}

	public boolean canEncode(Object v) {
		return v.getClass().isArray();
	}

	public void encode(OutputContext ctx, Object v) throws IOException {
		int dimensions = getArrayDimensions(v);
		Class<?> componentType = getComponentType(v);
		
		int jmfComponentType = ctx.getSharedContext().getCodecRegistry().jmfTypeOfPrimitiveClass(componentType);
		if (jmfComponentType != -1)
			writePrimitiveArray(ctx, v, jmfComponentType, dimensions, true);
		else
			writeObjectArray(ctx, v, dimensions, true);
	}
	
	public Object decode(InputContext ctx, int parameterizedJmfType) throws IOException, ClassNotFoundException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_ARRAY)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		Object v = null;
		
		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 4) & 0x03, false);
		if ((parameterizedJmfType & 0x80) != 0)
			v = ctx.getSharedObject(indexOrLength);
		else {
			int dimensions = ((parameterizedJmfType & 0x40) == 0 ? 0 : ctx.safeRead());
			int parameterizedJmfComponentType = ctx.safeRead();
			int jmfComponentType = codecRegistry.extractJmfType(parameterizedJmfComponentType);
			
			Class<?> componentType = codecRegistry.primitiveClassOfJmfType(jmfComponentType);
			
			if (componentType != null)
				v = readPrimitiveArray(ctx, componentType, jmfComponentType, indexOrLength, dimensions);
			else
				v = readObjectArray(ctx, parameterizedJmfComponentType, indexOrLength, dimensions);
		}
		
		return v;
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();

		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_ARRAY)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 4) & 0x03, false);
		if ((parameterizedJmfType & 0x80) != 0)
			ctx.indentPrintLn("<" + ctx.getSharedObject(indexOrLength) + "@" + indexOrLength + ">");
		else {
			int dimensions = ((parameterizedJmfType & 0x40) == 0 ? 0 : ctx.safeRead());
			int parameterizedJmfComponentType = ctx.safeRead();
			int jmfComponentType = codecRegistry.extractJmfType(parameterizedJmfComponentType);
			
			Class<?> componentType = codecRegistry.primitiveClassOfJmfType(jmfComponentType);
			
			if (componentType != null)
				dumpPrimitiveArray(ctx, componentType, jmfComponentType, indexOrLength, dimensions);
			else
				dumpObjectArray(ctx, parameterizedJmfComponentType, indexOrLength, dimensions);
		}
	}

	protected void writeObjectArray(OutputContext ctx, Object v, int dimensions, boolean writeDimensions) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		if (v == null)
			os.write(JMF_NULL);
		else {
			int indexOfStoredObject = ctx.indexOfStoredObjects(v);
			if (indexOfStoredObject >= 0) {
				IntegerComponents ics = intComponents(indexOfStoredObject);
				ctx.getOutputStream().write(0x80 | (ics.length << 4) | JMF_ARRAY);
				writeIntData(ctx, ics);
			}
			else {
				ctx.addToStoredObjects(v);
				
				if (dimensions == 0)
					writeObjectArray(ctx, v);
				else {
					int length = Array.getLength(v);
					
					IntegerComponents ics = intComponents(length);
					if (writeDimensions) {
						os.write(0x40 | (ics.length << 4) | JMF_ARRAY);
						writeIntData(ctx, ics);
						os.write(dimensions);
					}
					else {
						os.write((ics.length << 4) | JMF_ARRAY);
						writeIntData(ctx, ics);
					}
					
					Class<?> componentType = getComponentType(v);
					String className = ctx.getAlias(componentType.getName());
					writeString(ctx, className, JMF_STRING_TYPE_HANDLER);
					
					int subDimensions = dimensions - 1;
					for (int index = 0; index < length; index++)
						writeObjectArray(ctx, Array.get(v, index), subDimensions, false);
				}
			}
		}
	}
	
	protected void writeObjectArray(OutputContext ctx, Object v) throws IOException {
		final OutputStream os = ctx.getOutputStream();

		int length = Array.getLength(v);
		Class<?> componentType = v.getClass().getComponentType();
		String className = ctx.getAlias(componentType.getName());
		
		IntegerComponents ics = intComponents(length);
		os.write((ics.length << 4) | JMF_ARRAY);
		writeIntData(ctx, ics);

		writeString(ctx, className, JMF_STRING_TYPE_HANDLER);
		for (int index = 0; index < length; index++)
			ctx.writeObject(Array.get(v, index));
	}
	
	protected void writePrimitiveArray(OutputContext ctx, Object v, int jmfComponentType, int dimensions, boolean writeDimensionsAndType) throws IOException {
		final OutputStream os = ctx.getOutputStream();

		if (v == null)
			os.write(JMF_NULL);
		else {
			int indexOfStoredObject = ctx.indexOfStoredObjects(v);
			if (indexOfStoredObject >= 0) {
				IntegerComponents ics = intComponents(indexOfStoredObject);
				ctx.getOutputStream().write(0x80 | (ics.length << 4) | JMF_ARRAY);
				writeIntData(ctx, ics);
			}
			else {
				ctx.addToStoredObjects(v);
				if (dimensions == 0)
					writePrimitiveArray(ctx, v, jmfComponentType, writeDimensionsAndType);
				else {
					int length = Array.getLength(v);
					
					IntegerComponents ics = intComponents(length);
					if (writeDimensionsAndType) {
						os.write(0x40 | (ics.length << 4) | JMF_ARRAY);
						writeIntData(ctx, ics);
						os.write(dimensions);
						os.write(jmfComponentType);
					}
					else {
						os.write((ics.length << 4) | JMF_ARRAY);
						writeIntData(ctx, ics);
					}
					
					int subDimensions = dimensions - 1;
					for (int index = 0; index < length; index++)
						writePrimitiveArray(ctx, Array.get(v, index), jmfComponentType, subDimensions, false);
				}
			}
		}
	}
	
	protected void writePrimitiveArray(OutputContext ctx, Object v, int jmfComponentType, boolean writeType) throws IOException {
		final OutputStream os = ctx.getOutputStream();

		final int length = Array.getLength(v);
		
		IntegerComponents ics = intComponents(length);
		os.write((ics.length << 4) | JMF_ARRAY);
		writeIntData(ctx, ics);
		
		if (writeType)
			os.write(jmfComponentType);
		
		if (length == 0)
			return;
		
		switch (jmfComponentType) {
			case JMF_BOOLEAN: {
				byte[] bytes = new byte[lengthOfBooleanArray(length)];
				int i = 0, j = 0;
				for (boolean b : (boolean[])v) {
					if (b)
						bytes[i] |= 0x80 >> j;
					j++;
					if (j >= 8) {
						j = 0;
						i++;
					}
				}
				os.write(bytes);
				break;
			}
			
			case JMF_CHARACTER: {
				char[] a = (char[])v;
				for (char c : a) {
					os.write(c >> 8);
					os.write(c);
				}
				break;
			}
			
			case JMF_BYTE: {
				os.write((byte[])v);
				break;
			}
			
			case JMF_SHORT: {
				short[] a = (short[])v;
				for (short s : a) {
					os.write(s >> 8);
					os.write(s);
				}
				break;
			}
			
			case JMF_INTEGER: {
				IntegerCodec integerCodec = ctx.getSharedContext().getCodecRegistry().getIntegerCodec();
				int[] a = (int[])v;
				for (int i : a)
					integerCodec.writeVariableInt(ctx, i);
				break;
			}
			
			case JMF_LONG: {
				LongCodec longCodec = ctx.getSharedContext().getCodecRegistry().getLongCodec();
				long[] a = (long[])v;
				for (long l : a)
					longCodec.writeVariableLong(ctx, l);
				break;
			}
			
			case JMF_FLOAT: {
				float[] a = (float[])v;
				for (float f : a) {
					int bits = Float.floatToIntBits(f);
					os.write(bits);
					os.write(bits >> 8);
					os.write(bits >> 16);
					os.write(bits >> 24);
					
				}
				break;
			}
			
			case JMF_DOUBLE: {
				double[] a = (double[])v;
				for (double d : a) {
					long bits = Double.doubleToLongBits(d);
					os.write((int)bits);
					os.write((int)(bits >> 8));
					os.write((int)(bits >> 16));
					os.write((int)(bits >> 24));
					os.write((int)(bits >> 32));
					os.write((int)(bits >> 40));
					os.write((int)(bits >> 48));
					os.write((int)(bits >> 56));
				}
				break;
			}
			
			default:
				throw new JMFEncodingException("Unsupported primitive type: " + jmfComponentType);
		}
	}
	
	protected int getArrayDimensions(Object v) {
		return v.getClass().getName().lastIndexOf('[');
	}
	
	protected Class<?> getComponentType(Object v) {
		Class<?> componentType = v.getClass().getComponentType();
		while (componentType.isArray())
			componentType = componentType.getComponentType();
		return componentType;
	}
	
	protected Object readObjectArray(InputContext ctx, int parameterizedJmfComponentType, int length, int dimensions) throws IOException, ClassNotFoundException {
		Object v =  null;
		
		String componentTypeName = readString(ctx, parameterizedJmfComponentType, JMF_STRING_TYPE_HANDLER);
		componentTypeName = ctx.getAlias(componentTypeName);
		Class<?> componentType = ctx.getSharedContext().getReflection().loadClass(componentTypeName);
		
		if (dimensions == 0)
			v = readObjectArray(ctx, componentType, length);
		else {
			v = newArray(componentType, length, dimensions);
			ctx.addSharedObject(v);
			
			int subDimensions = dimensions - 1;
			for (int index = 0; index < length; index++) {
				int subParameterizedJmfType = ctx.safeRead();
				int subJmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(subParameterizedJmfType);

				if (subJmfType == JMF_NULL)
					Array.set(v, index, null);
				else if (subJmfType == JMF_ARRAY) {
					int subLengthOrIndex = readIntData(ctx, (subParameterizedJmfType >> 4) & 0x03, false);
					if ((subParameterizedJmfType & 0x80) != 0)
						Array.set(v, index, ctx.getSharedObject(subLengthOrIndex));
					else {
						int subParameterizedJmfComponentType = ctx.safeRead();
						Array.set(v, index, readObjectArray(ctx, subParameterizedJmfComponentType, subLengthOrIndex, subDimensions));
					}
				}
				else
					newBadTypeJMFEncodingException(subJmfType, subParameterizedJmfType);
			}
		}
		
		return v;
	}
	
	protected void dumpObjectArray(DumpContext ctx, int parameterizedJmfComponentType, int length, int dimensions) throws IOException {
		String componentTypeName = readString(ctx, parameterizedJmfComponentType, JMF_STRING_TYPE_HANDLER);
		
		if (dimensions == 0)
			dumpObjectArray(ctx, componentTypeName, length);
		else {
			String v = newDumpObjectArray(componentTypeName, length, dimensions);
			int indexOfStoredObject = ctx.addSharedObject(v);
			ctx.indentPrintLn(v + "@" + indexOfStoredObject + ": {");
			ctx.incrIndent(1);
			
			int subDimensions = dimensions - 1;
			for (int index = 0; index < length; index++) {
				int subParameterizedJmfType = ctx.safeRead();
				int subJmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(subParameterizedJmfType);

				if (subJmfType == JMF_NULL)
					ctx.indentPrintLn("null");
				else if (subJmfType == JMF_ARRAY) {
					int subLengthOrIndex = readIntData(ctx, (subParameterizedJmfType >> 4) & 0x03, false);
					if ((subParameterizedJmfType & 0x80) != 0)
						ctx.indentPrintLn("<" + ctx.getSharedObject(subLengthOrIndex) + "@" + subLengthOrIndex + ">");
					else {
						int subParameterizedJmfComponentType = ctx.safeRead();
						dumpObjectArray(ctx, subParameterizedJmfComponentType, subLengthOrIndex, subDimensions);
					}
				}
				else
					newBadTypeJMFEncodingException(subJmfType, subParameterizedJmfType);
			}

			ctx.incrIndent(-1);
			ctx.indentPrintLn("}");
		}
	}
	
	protected Object readObjectArray(InputContext ctx, Class<?> componentType, int length) throws IOException, ClassNotFoundException {
		Object v = Array.newInstance(componentType, length);
		ctx.addSharedObject(v);
		
		for (int index = 0; index < length; index++)
			Array.set(v, index, ctx.readObject());
		
		return v;
	}
	
	protected void dumpObjectArray(DumpContext ctx, String componentTypeName, int length) throws IOException {
		String v = newDumpObjectArray(componentTypeName, length, 0);
		int indexOfStoredObject = ctx.addSharedObject(v);
		ctx.indentPrintLn(v + "@" + indexOfStoredObject + ": {");
		ctx.incrIndent(1);
		
		for (int index = 0; index < length; index++) {
			int parameterizedJmfType = ctx.safeRead();
			int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
			StandardCodec<?> codec = ctx.getSharedContext().getCodecRegistry().getCodec(jmfType);
			
			if (codec == null)
				throw new JMFEncodingException("No codec for JMF type: " + jmfType);
			
			codec.dump(ctx, parameterizedJmfType);
		}

		ctx.incrIndent(-1);
		ctx.indentPrintLn("}");
	}
	
	protected String newDumpObjectArray(String componentTypeName, int length, int dimensions) {
		StringBuilder sb = new StringBuilder(componentTypeName);
		
		sb.append('[').append(length).append(']');
		
		for (int i = 0; i < dimensions; i++)
			sb.append("[]");
		
		return sb.toString();
		
	}
	
	protected Object readPrimitiveArray(InputContext ctx, Class<?> componentType, int jmfComponentType, int length, int dimensions) throws IOException {
		Object v =  null;
		
		if (dimensions == 0)
			v = readPrimitiveArray(ctx, componentType, jmfComponentType, length);
		else {
			v = newArray(componentType, length, dimensions);
			ctx.addSharedObject(v);
			
			int subDimensions = dimensions - 1;
			for (int index = 0; index < length; index++) {
				int subArrayJmfType = ctx.safeRead();
				if (subArrayJmfType == JMF_NULL)
					Array.set(v, index, null);
				else {
					int subLengthOrIndex = readIntData(ctx, (subArrayJmfType >> 4) & 0x03, false);
					if ((subArrayJmfType & 0x80) != 0)
						Array.set(v, index, ctx.getSharedObject(subLengthOrIndex));
					else
						Array.set(v, index, readPrimitiveArray(ctx, componentType, jmfComponentType, subLengthOrIndex, subDimensions));
				}
			}
		}
		
		return v;
	}
	
	protected void dumpPrimitiveArray(DumpContext ctx, Class<?> componentType, int jmfComponentType, int length, int dimensions) throws IOException {
		if (dimensions == 0)
			dumpPrimitiveArray(ctx, componentType, jmfComponentType, length);
		else {
			String v = newDumpPrimitiveArray(jmfComponentType, length, dimensions);
			int indexOfStoredObject = ctx.addSharedObject(v);
			ctx.indentPrintLn(v + "@" + indexOfStoredObject + ": {");
			ctx.incrIndent(1);
			
			int subDimensions = dimensions - 1;
			for (int index = 0; index < length; index++) {
				int subArrayJmfType = ctx.safeRead();
				if (subArrayJmfType == JMF_NULL)
					ctx.indentPrintLn("null");
				else {
					int subLengthOrIndex = readIntData(ctx, (subArrayJmfType >> 4) & 0x03, false);
					if ((subArrayJmfType & 0x80) != 0)
						ctx.indentPrintLn("<" + ctx.getSharedObject(subLengthOrIndex) + "@" + subLengthOrIndex + ">");
					else
						dumpPrimitiveArray(ctx, componentType, jmfComponentType, subLengthOrIndex, subDimensions);
				}
			}
			
			ctx.incrIndent(-1);
			ctx.indentPrintLn("}");
		}
	}
	
	protected Object readPrimitiveArray(InputContext ctx, Class<?> componentType, int jmfComponentType, int length) throws IOException {
		Object v = null;
		
		if (length == 0)
			v = Array.newInstance(componentType, length);
		else {
			switch (jmfComponentType) {
				case JMF_BOOLEAN: {
					boolean[] a = new boolean[length];
					int nb = lengthOfBooleanArray(length);
					for (int i = 0; i < nb; i++) {
						int b = ctx.safeRead();
						for (int j = 0; j < 8; j++) {
							int index = (i * 8) + j;
							if (index >= length)
								break;
							a[index] = ((b & (0x80 >> j)) != 0);
						}
					}
					v = a;
					break;
				}
				
				case JMF_CHARACTER: {
					char[] a = new char[length];
					for (int i = 0; i < length; i++)
						a[i] = (char)((ctx.safeRead() << 8) | ctx.safeRead());
					v = a;
					break;
				}
				
				case JMF_BYTE: {
					byte[] a = new byte[length];
					ctx.safeReadFully(a);
					v = a;
					break;
				}
				
				case JMF_SHORT: {
					short[] a = new short[length];
					for (int i = 0; i < length; i++)
						a[i] = (short)((ctx.safeRead() << 8) | ctx.safeRead());
					v = a;
					break;
				}
				
				case JMF_INTEGER: {
					IntegerCodec integerCodec = ctx.getSharedContext().getCodecRegistry().getIntegerCodec();
					int[] a = new int[length];
					for (int i = 0; i < length; i++)
						a[i] = integerCodec.readVariableInt(ctx);
					v = a;
					break;
				}
				
				case JMF_LONG: {
					LongCodec longCodec = ctx.getSharedContext().getCodecRegistry().getLongCodec();
					long[] a = new long[length];
					for (int i = 0; i < length; i++)
						a[i] = longCodec.readVariableLong(ctx);
					v = a;
					break;
				}
				
				case JMF_FLOAT: {
					float[] a = new float[length];
					for (int i = 0; i < length; i++)
						a[i] = FloatCodecImpl.readFloatData(ctx, jmfComponentType);
					v = a;
					break;
				}
				
				case JMF_DOUBLE: {
					double[] a = new double[length];
					for (int i = 0; i < length; i++)
						a[i] = DoubleCodecImpl.readDoubleData(ctx, jmfComponentType);
					v = a;
					break;
				}
				
				default:
					throw new JMFEncodingException("Unsupported primitive type: " + jmfComponentType);
			}
		}
		
		ctx.addSharedObject(v);
		
		return v;
	}
	
	protected void dumpPrimitiveArray(DumpContext ctx, Class<?> componentType, int jmfComponentType, int length) throws IOException {

		String v = newDumpPrimitiveArray(jmfComponentType, length, 0);
		int indexOfStoredObject = ctx.addSharedObject(v);
		ctx.indentPrint(v + "@" + indexOfStoredObject + ": {");
		
		switch (jmfComponentType) {
			case JMF_BOOLEAN: {
				int nb = lengthOfBooleanArray(length);
				for (int i = 0; i < nb; i++) {
					int b = ctx.safeRead();
					for (int j = 0; j < 8; j++) {
						int index = (i * 8) + j;
						if (index >= length)
							break;
						if (index > 0)
							ctx.print(", ");
						ctx.print(String.valueOf(((b & (0x80 >> j)) != 0)));
					}
				}
				break;
			}
			
			case JMF_CHARACTER: {
				for (int i = 0; i < length; i++) {
					if (i > 0)
						ctx.print(", ");
					ctx.print(String.valueOf((char)((ctx.safeRead() << 8) | ctx.safeRead())));
				}
				break;
			}
			
			case JMF_BYTE: {
				for (int i = 0; i < length; i++) {
					if (i > 0)
						ctx.print(", ");
					ctx.print(String.valueOf((byte)ctx.safeRead()));
				}
				break;
			}
			
			case JMF_SHORT: {
				for (int i = 0; i < length; i++) {
					if (i > 0)
						ctx.print(", ");
					ctx.print(String.valueOf((short)(ctx.safeRead() << 8) | ctx.safeRead()));
				}
				break;
			}
			
			case JMF_INTEGER: {
				IntegerCodec integerCodec = ctx.getSharedContext().getCodecRegistry().getIntegerCodec();
				for (int i = 0; i < length; i++) {
					if (i > 0)
						ctx.print(", ");
					ctx.print(String.valueOf(integerCodec.readVariableInt(ctx)));
				}
				break;
			}
			
			case JMF_LONG: {
				LongCodec longCodec = ctx.getSharedContext().getCodecRegistry().getLongCodec();
				for (int i = 0; i < length; i++) {
					if (i > 0)
						ctx.print(", ");
					ctx.print(String.valueOf(longCodec.readVariableLong(ctx)));
				}
				break;
			}
			
			case JMF_FLOAT: {
				for (int i = 0; i < length; i++) {
					if (i > 0)
						ctx.print(", ");
					ctx.print(String.valueOf(FloatCodecImpl.readFloatData(ctx, jmfComponentType)));
				}
				break;
			}
			
			case JMF_DOUBLE: {
				for (int i = 0; i < length; i++) {
					if (i > 0)
						ctx.print(", ");
					ctx.print(String.valueOf(DoubleCodecImpl.readDoubleData(ctx, jmfComponentType)));
				}
				break;
			}
			
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
	
	protected Object newArray(Class<?> type, int length, int dimensions) {
		int[] ld = new int[dimensions + 1];
		ld[0] = length;
		return Array.newInstance(type, ld);
	}
	
	protected int lengthOfBooleanArray(int nb) {
		return (nb / 8) + (nb % 8 != 0 ? 1 : 0);
	}
}
