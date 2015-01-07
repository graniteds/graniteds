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
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.ObjectArrayCodec;
import org.granite.messaging.jmf.codec.std.impl.util.ClassNameUtil;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;

/**
 * @author Franck WOLFF
 */
public class ObjectArrayCodecImpl extends AbstractArrayCodec implements ObjectArrayCodec {
	
	public int getObjectType() {
		return JMF_OBJECT_ARRAY;
	}

	public boolean canEncode(Object v) {
		return v.getClass().isArray() && !getComponentType(v).isPrimitive();
	}

	public void encode(OutputContext ctx, Object v) throws IOException {
		int indexOfStoredObject = ctx.indexOfObject(v);
		if (indexOfStoredObject != -1) {
			int count = IntegerUtil.significantIntegerBytesCount0(indexOfStoredObject);
			// Write the index to a stored array:
			// [parameterized type{1}, index{1,4}]
			ctx.getOutputStream().write(0x80 | (count << 4) | JMF_OBJECT_ARRAY);
			IntegerUtil.encodeInteger(ctx, indexOfStoredObject, count);
		}
		else {
			ctx.addToObjects(v);
			
			ArrayStructure structure = new ArrayStructure(v);
			String className = ctx.getAlias(structure.componentType.getName());
			int length = Array.getLength(v);
			int count = IntegerUtil.significantIntegerBytesCount0(length);

			final OutputStream os = ctx.getOutputStream();
			if (structure.dimensions == 0) {
				// Write the length and component type of the array:
				// [parameterized type{1}, length{1,4}, component type name{1+}]
				os.write((count << 4) | JMF_OBJECT_ARRAY);
				IntegerUtil.encodeInteger(ctx, length, count);
				ClassNameUtil.encodeClassName(ctx, className);
				
				writeObjectArrayContent0(ctx, v);
			}
			else {
				// Write the length, component type and dimensions of the array:
				// [parameterized type{1}, length{1,4}, component type name{2+}, dimensions{1}]
				os.write(0x40 | (count << 4) | JMF_OBJECT_ARRAY);
				IntegerUtil.encodeInteger(ctx, length, count);
				ClassNameUtil.encodeClassName(ctx, className);
				os.write(structure.dimensions);

				writeObjectArrayContent(ctx, v, structure.componentType, structure.dimensions);
				
			}
		}
	}

	protected void writeObjectArrayContent(OutputContext ctx, Object v, Class<?> componentType, int dimensions) throws IOException {
		final int length = Array.getLength(v);
		if (length == 0)
			return;
		
		dimensions--;
		final boolean writeObjectArrayContent0 = (dimensions == 0);
		final OutputStream os = ctx.getOutputStream();

		for (int index = 0; index < length; index++) {
			Object item = Array.get(v, index);
			if (item == null)
				os.write(JMF_NULL);
			else {
				int indexOfStoredObject = ctx.indexOfObject(item);
				if (indexOfStoredObject != -1) {
					int count = IntegerUtil.significantIntegerBytesCount0(indexOfStoredObject);
					// Write the index to a stored array:
					// [parameterized type{1}, index{1,4}]
					ctx.getOutputStream().write(0x80 | (count << 4) | JMF_OBJECT_ARRAY);
					IntegerUtil.encodeInteger(ctx, indexOfStoredObject, count);
				}
				else {
					ctx.addToObjects(item);
					
					int itemLength = Array.getLength(item);
					int count = IntegerUtil.significantIntegerBytesCount0(itemLength);

					// Write the length of the array:
					// [parameterized type{1}, item length{1,4}]
					os.write((count << 4) | JMF_OBJECT_ARRAY);
					IntegerUtil.encodeInteger(ctx, itemLength, count);
					
					// Write item class name:
					// [item type name{2+}]
					Class<?> itemType = getComponentType(item);
					String className = ctx.getAlias(itemType.getName());
					ClassNameUtil.encodeClassName(ctx, className);
					
					if (writeObjectArrayContent0)
						writeObjectArrayContent0(ctx, item);
					else
						writeObjectArrayContent(ctx, item, itemType, dimensions);
				}
			}
		}
	}
	
	protected void writeObjectArrayContent0(OutputContext ctx, Object v) throws IOException {
		final int length = Array.getLength(v);
		if (length == 0)
			return;
		
		for (int index = 0; index < length; index++)
			ctx.writeObject(Array.get(v, index));
	}

	public Object decode(InputContext ctx, int parameterizedJmfType) throws IOException, ClassNotFoundException {
		Object v = null;
		
		// Read the index (stored array) or length of the array:
		// [index or length{1,4}]
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >> 4) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0)
			v = ctx.getObject(indexOrLength);
		else {
			// Read the element class name and, if the 0x40 flag is set, the dimensions of the array:
			// [element class name{2+}, dimensions{0,1}]
			String componentTypeName = ctx.getAlias(ClassNameUtil.decodeClassName(ctx));
			int dimensions = ((parameterizedJmfType & 0x40) == 0 ? 0 : ctx.safeRead());
			
			Class<?> componentType = ctx.getSharedContext().getReflection().loadClass(componentTypeName);
			if (dimensions == 0)
				v = readObjectArray0(ctx, componentType, indexOrLength);
			else
				v = readObjectArray(ctx, componentType, indexOrLength, dimensions);
		}
		
		return v;
	}
	
	protected Object readObjectArray(InputContext ctx, Class<?> componentType, int length, int dimensions) throws IOException, ClassNotFoundException {
		Object v = newArray(componentType, length, dimensions);
		ctx.addToObjects(v);

		dimensions--;
		
		final boolean readObjectArray0 = (dimensions == 0);
		for (int index = 0; index < length; index++) {
			// Read the type of the element (must be JMF_NULL or JMF_OBJECT_ARRAY):
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
				else {
					// Read element class name:
					// [element class name{2+}]
					String eltClassName = ctx.getAlias(ClassNameUtil.decodeClassName(ctx));
					Class<?> eltComponentType = ctx.getReflection().loadClass(eltClassName);
					
					if (readObjectArray0)
						Array.set(v, index, readObjectArray0(ctx, eltComponentType, eltIndexOrLength));
					else
						Array.set(v, index, readObjectArray(ctx, eltComponentType, eltIndexOrLength, dimensions));
				}
			}
		}
		
		return v;
	}
	
	protected Object readObjectArray0(InputContext ctx, Class<?> componentType, int length) throws IOException, ClassNotFoundException {
		Object v = Array.newInstance(componentType, length);
		ctx.addToObjects(v);

		for (int index = 0; index < length; index++)
			Array.set(v, index, ctx.readObject());

		return v;
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();

		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_OBJECT_ARRAY)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		// Read the index (stored array) or length of the array:
		// [index or length{1,4}]
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> 4) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0)
			ctx.indentPrintLn("<" + ctx.getObject(indexOrLength) + "@" + indexOrLength + ">");
		else {
			// Read the element class name and, if the 0x40 flag is set, the dimensions of the array:
			// [element class name{2+}, dimensions{0,1}]
			String eltClassName = ctx.getAlias(ClassNameUtil.decodeClassName(ctx));
			int dimensions = ((parameterizedJmfType & 0x40) == 0 ? 0 : ctx.safeRead());
			
			if (dimensions == 0)
				dumpObjectArray0(ctx, eltClassName, indexOrLength);
			else
				dumpObjectArray(ctx, eltClassName, indexOrLength, dimensions);
		}
	}
	
	protected void dumpObjectArray(DumpContext ctx, String componentType, int length, int dimensions) throws IOException {
		String v = newDumpObjectArray(componentType, length, 0);
		int indexOfStoredObject = ctx.addToObjects(v);
		ctx.indentPrintLn(v + "@" + indexOfStoredObject + ": {");
		ctx.incrIndent(1);

		dimensions--;
		
		final boolean dumpObjectArray0 = (dimensions == 0);
		for (int index = 0; index < length; index++) {
			// Read the type of the element (must be JMF_NULL or JMF_OBJECT_ARRAY):
			// [array element type{1}]
			int eltParameterizedJmfType = ctx.safeRead();
			int eltJmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(eltParameterizedJmfType);

			if (eltJmfType == JMF_NULL)
				ctx.indentPrintLn("null");
			else if (eltJmfType == JMF_OBJECT_ARRAY) {
				// Read the length of the element (sub array):
				// [length{1, 4}]
				int eltIndexOrLength = IntegerUtil.decodeInteger(ctx, (eltParameterizedJmfType >>> 4) & 0x03);
				if ((eltParameterizedJmfType & 0x80) != 0)
					ctx.indentPrintLn("<" + ctx.getObject(eltIndexOrLength) + "@" + eltIndexOrLength + ">");
				else {
					// Read element class name:
					// [element class name{2+}]
					String eltClassName = ctx.getAlias(ClassNameUtil.decodeClassName(ctx));
					
					if (dumpObjectArray0)
						dumpObjectArray0(ctx, eltClassName, eltIndexOrLength);
					else
						dumpObjectArray(ctx, eltClassName, eltIndexOrLength, dimensions);
				}
			}
			else
				newBadTypeJMFEncodingException(eltJmfType, eltParameterizedJmfType);
		}

		ctx.incrIndent(-1);
		ctx.indentPrintLn("}");
	}
	
	protected void dumpObjectArray0(DumpContext ctx, String componentType, int length) throws IOException {
		String v = newDumpObjectArray(componentType, length, 0);
		int indexOfStoredObject = ctx.addToObjects(v);
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
}
