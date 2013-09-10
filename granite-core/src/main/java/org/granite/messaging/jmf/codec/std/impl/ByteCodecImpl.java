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

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.ByteCodec;

/**
 * @author Franck WOLFF
 */
public class ByteCodecImpl extends AbstractStandardCodec<Byte> implements ByteCodec {

	public int getObjectType() {
		return JMF_BYTE_OBJECT;
	}

	public Class<?> getObjectClass() {
		return Byte.class;
	}

	public int getPrimitiveType() {
		return JMF_BYTE;
	}

	public Class<?> getPrimitiveClass() {
		return Byte.TYPE;
	}

	public void encode(OutputContext ctx, Byte v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		os.write(JMF_BYTE_OBJECT);
		os.write(v.intValue());
	}
	
	public Byte decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		if (jmfType != JMF_BYTE_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		return Byte.valueOf((byte)ctx.safeRead());
	}

	public void encodePrimitive(OutputContext ctx, int v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		os.write(JMF_BYTE);
		os.write(v);
	}
	
	public byte decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);

		if (jmfType != JMF_BYTE)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		return (byte)ctx.safeRead();
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_BYTE:
			ctx.indentPrintLn("byte: " + (byte)ctx.safeRead());
			break;
		case JMF_BYTE_OBJECT:
			ctx.indentPrintLn(Byte.class.getName() + ": " + (byte)ctx.safeRead());
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
}
