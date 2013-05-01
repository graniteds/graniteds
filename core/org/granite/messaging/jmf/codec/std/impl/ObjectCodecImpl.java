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

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.JMFEncodingException;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.ObjectCodec;

/**
 * @author Franck WOLFF
 */
public class ObjectCodecImpl extends AbstractIntegerStringCodec<Object> implements ObjectCodec {

	protected static final StringTypeHandler TYPE_HANDLER = new StringTypeHandler() {

		public int type(IntegerComponents ics, boolean reference) {
			if (reference)
				return 0x40 | (ics.length << 4) | JMF_OBJECT;
			return (ics.length << 4) | JMF_OBJECT;
		}

		public int indexOrLengthBytesCount(int parameterizedJmfType) {
			return (parameterizedJmfType >> 4) & 0x03;
		}

		public boolean isReference(int parameterizedJmfType) {
			return (parameterizedJmfType & 0x40) != 0;
		}
	};

	public int getObjectType() {
		return JMF_OBJECT;
	}

	public boolean accept(Object v) {
		Class<?> cls = v.getClass();
		return !cls.isArray() && !cls.isEnum() && !(v instanceof Class);
	}

	public void encode(OutputContext ctx, Object v) throws IOException, IllegalAccessException {
		final OutputStream os = ctx.getOutputStream();
		
		int indexOfStoredObject = ctx.indexOfStoredObjects(v);
		if (indexOfStoredObject >= 0) {
			IntegerComponents ics = intComponents(indexOfStoredObject);
			os.write(0x80 | (ics.length << 4) | JMF_OBJECT);
			writeIntData(ctx, ics);
		}
		else {			
			if (!(v instanceof Serializable))
				throw new NotSerializableException(v.getClass().getName());
			
			ctx.addToStoredObjects(v);
			
			Class<?> cls = v.getClass();
			String className = cls.getName();
					
			ExtendedObjectCodec extendedCodec = ctx.getSharedContext().getCodecRegistry().findExtendedEncoder(ctx, v);
			if (extendedCodec != null)
				className = extendedCodec.getEncodedClassName(ctx, v);
			
			writeString(ctx, className, TYPE_HANDLER);
			
			if (extendedCodec != null)
				extendedCodec.encode(ctx, v);
			else if (v instanceof Externalizable && !Proxy.isProxyClass(cls))
				((Externalizable)v).writeExternal(ctx);
			else
				encodeSerializable(ctx, (Serializable)v);

			os.write(JMF_OBJECT_END);
		}
	}
	
	protected void encodeSerializable(OutputContext ctx, Serializable v) throws IOException, IllegalAccessException {
		List<Field> fields = ctx.getReflection().findSerializableFields(v.getClass());
		for (Field field : fields)
			ctx.getAndWriteField(v, field);
	}
	
	public Object decode(InputContext ctx, int parameterizedJmfType)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, SecurityException, NoSuchMethodException {
		
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		if (jmfType != JMF_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		Object v = null;

		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 4) & 0x03, false);
		if ((parameterizedJmfType & 0x80) != 0)
			v = ctx.getSharedObject(indexOrLength);
		else {
			String className = readString(ctx, parameterizedJmfType, indexOrLength, TYPE_HANDLER);
			
			Class<?> cls = ctx.getSharedContext().getReflection().loadClass(className);
			
			if (!Serializable.class.isAssignableFrom(cls))
				throw new NotSerializableException(cls.getName());
			
			ExtendedObjectCodec extendedCodec = codecRegistry.findExtendedDecoder(ctx, cls);
			if (extendedCodec != null) {
				int index = ctx.addUnresolvedSharedObject(cls);
				v = extendedCodec.newInstance(ctx, cls);
				ctx.setUnresolvedSharedObject(index, v);
				extendedCodec.decode(ctx, v);
			}
			else if (Externalizable.class.isAssignableFrom(cls)) {
				v = ctx.getReflection().newInstance(cls);
				ctx.addSharedObject(v);
				((Externalizable)v).readExternal(ctx);
			}
			else {
				v = ctx.getReflection().newInstance(cls);
				ctx.addSharedObject(v);
				decodeSerializable(ctx, (Serializable)v);
			}
			
			int mark = ctx.safeRead();
			if (mark != JMF_OBJECT_END)
				throw new JMFEncodingException("Not a Object end marker: " + mark);
		}
		
		return v;
	}
	
	protected void decodeSerializable(InputContext ctx, Serializable v)
		throws IOException, ClassNotFoundException, IllegalAccessException {

		List<Field> fields = ctx.getReflection().findSerializableFields(v.getClass());
		for (Field field : fields)
			ctx.readAndSetField(v, field);
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();

		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 4) & 0x03, false);
		
		if ((parameterizedJmfType & 0x80) != 0) {
			String className = (String)ctx.getSharedObject(indexOrLength);
			ctx.indentPrintLn("<" + className + "@" + indexOrLength + ">");
		}
		else {
			String className = readString(ctx, parameterizedJmfType, indexOrLength, TYPE_HANDLER);
			int indexOfStoredObject = ctx.addSharedObject(className);
			ctx.indentPrintLn(className + "@" + indexOfStoredObject + " {");
			ctx.incrIndent(1);
			
			while ((parameterizedJmfType = ctx.safeRead()) != JMF_OBJECT_END) {
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
}
