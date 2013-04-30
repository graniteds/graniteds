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

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.EnumCodec;

/**
 * @author Franck WOLFF
 */
public class EnumCodecImpl extends AbstractIntegerStringCodec<Object> implements EnumCodec {

	protected static final StringTypeHandler TYPE_HANDLER = new StringTypeHandler() {

		public int type(IntegerComponents ics, boolean reference) {
			return (reference ? (0x40 | (ics.length << 4) | JMF_ENUM) : ((ics.length << 4) | JMF_ENUM));
		}

		public int indexOrLengthBytesCount(int parameterizedJmfType) {
			return (parameterizedJmfType >> 4) & 0x03;
		}

		public boolean isReference(int parameterizedJmfType) {
			return (parameterizedJmfType & 0x40) != 0;
		}
	};

	public int getObjectType() {
		return JMF_ENUM;
	}

	public boolean accept(Object v) {
		return v.getClass().isEnum();
	}

	public void encode(OutputContext ctx, Object v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		int indexOfStoredObject = ctx.indexOfStoredObjects(v);
		if (indexOfStoredObject >= 0) {
			IntegerComponents ics = intComponents(indexOfStoredObject);
			os.write(0x80 | (ics.length << 4) | JMF_ENUM);
			writeIntData(ctx, ics);
		}
		else {
			ctx.addToStoredObjects(v);
			
			writeString(ctx, v.getClass().getName(), TYPE_HANDLER);
			
			int ordinal = ((Enum<?>)v).ordinal();
			ctx.getSharedContext().getCodecRegistry().getIntegerCodec().writeVariableInt(ctx, ordinal);
		}
	}

	public Object decode(InputContext ctx, int parameterizedJmfType) throws IOException, ClassNotFoundException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_ENUM)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 4) & 0x03, false);
		
		Object v = null;
		if ((parameterizedJmfType & 0x80) != 0)
			v = ctx.getSharedObject(indexOrLength);
		else {
			String className = readString(ctx, parameterizedJmfType, indexOrLength, TYPE_HANDLER);
			Class<?> cls = ctx.getSharedContext().getClassLoader().loadClass(className);
			
			int ordinal = codecRegistry.getIntegerCodec().readVariableInt(ctx);
			
			v = cls.getEnumConstants()[ordinal];
			ctx.addSharedObject(v);
		}
		
		return v;
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_ENUM)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 4) & 0x03, false);
		if ((parameterizedJmfType & 0x80) != 0) {
			String className = (String)ctx.getSharedObject(indexOrLength);
			ctx.indentPrintLn("<" + className + "@" + indexOrLength + ">");
		}
		else {
			String className = readString(ctx, parameterizedJmfType, indexOrLength, TYPE_HANDLER);
			int ordinal = codecRegistry.getIntegerCodec().readVariableInt(ctx);
			
			int indexOfStoredObject = ctx.addSharedObject(className);
			ctx.indentPrintLn(className + "@" + indexOfStoredObject + ": <" + ordinal + ">");
		}
	}
}
