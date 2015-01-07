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
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.EnumSet;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.JMFEncodingException;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.GenericCollectionCodec;
import org.granite.messaging.jmf.codec.std.impl.util.ClassNameUtil;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;
import org.granite.messaging.reflect.ClassDescriptor;

public class GenericCollectionCodecImpl extends AbstractStandardCodec<Object> implements GenericCollectionCodec {
	
	protected static final int INDEX_OR_LENGTH_BYTE_COUNT_OFFSET = 5;

	@Override
	public int getObjectType() {
		return JMF_GENERIC_COLLECTION;
	}

	@Override
	public boolean canEncode(Object v) {
		return (v instanceof Collection) && !(v instanceof EnumSet);
	}

	@Override
	public void encode(OutputContext ctx, Object v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		int indexOfStoredObject = ctx.indexOfObject(v);
		if (indexOfStoredObject >= 0) {
			int count = IntegerUtil.significantIntegerBytesCount0(indexOfStoredObject);
			os.write(0x80 | (count << INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) | JMF_GENERIC_COLLECTION);
			IntegerUtil.encodeInteger(ctx, indexOfStoredObject, count);
		}
		else {
			if (!(v instanceof Serializable))
				throw new NotSerializableException(v.getClass().getName());

			ctx.addToObjects(v);
			
			Collection<?> coll = (Collection<?>)v;
			Object[] snapshot = coll.toArray();
			
			int count = IntegerUtil.significantIntegerBytesCount0(snapshot.length);
			os.write((count << INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) | JMF_GENERIC_COLLECTION);
			IntegerUtil.encodeInteger(ctx, snapshot.length, count);

			String className = ctx.getAlias(v.getClass().getName());
			ClassNameUtil.encodeClassName(ctx, className);

			for (Object element : snapshot)
				ctx.writeObject(element);
		}
	}

	@Override
	public Object decode(InputContext ctx, int parameterizedJmfType)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, SecurityException, NoSuchMethodException {
		
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0)
			return ctx.getObject(indexOrLength);

		String className = ctx.getAlias(ClassNameUtil.decodeClassName(ctx));
		
		ClassDescriptor desc = ctx.getClassDescriptor(className);
		Class<?> cls = desc.getCls();
		
		if (!Serializable.class.isAssignableFrom(cls))
			throw new NotSerializableException(cls.getName());

		@SuppressWarnings("unchecked")
		Collection<Object> v = (Collection<Object>)desc.newInstance();
		ctx.addToObjects(v);
		
		for (int index = 0; index < indexOrLength; index++)
			v.add(ctx.readObject());

		return v;
	}

	@Override
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_GENERIC_COLLECTION)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0) {
			String v = (String)ctx.getObject(indexOrLength);
			ctx.indentPrintLn("<" + v + "@" + indexOrLength + ">");
			return;
		}
		
		String className = ClassNameUtil.decodeClassName(ctx);
		className = ctx.getAlias(className);
		
		String v = className + "[" + indexOrLength + "]";
		int indexOfStoredObject = ctx.addToObjects(v);
		ctx.indentPrintLn(v + "@" + indexOfStoredObject + " {");
		ctx.incrIndent(1);
		
		for (int index = 0; index < indexOrLength; index++) {
			parameterizedJmfType = ctx.safeRead();
			jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
			StandardCodec<?> codec = codecRegistry.getCodec(jmfType);
			
			if (codec == null)
				throw new JMFEncodingException("No codec for JMF type: " + jmfType);
			
			codec.dump(ctx, parameterizedJmfType);

		}
			
		ctx.incrIndent(-1);
		ctx.indentPrintLn("}");
	}
}
