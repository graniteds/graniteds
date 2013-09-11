/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
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

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.ShortCodec;

/**
 * @author Franck WOLFF
 */
public class ShortCodecImpl extends AbstractStandardCodec<Short> implements ShortCodec {

	public int getObjectType() {
		return JMF_SHORT_OBJECT;
	}

	public Class<?> getObjectClass() {
		return Short.class;
	}

	public int getPrimitiveType() {
		return JMF_SHORT;
	}

	public Class<?> getPrimitiveClass() {
		return Short.TYPE;
	}

	public void encode(OutputContext ctx, Short v) throws IOException {
		writeShortData(ctx, JMF_SHORT_OBJECT, v.intValue());
	}
	
	public Short decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_SHORT_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		return Short.valueOf(readShortData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, int v) throws IOException {
		writeShortData(ctx, JMF_SHORT, v);
	}
	
	public short decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_SHORT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		return readShortData(ctx, parameterizedJmfType);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);

		switch (jmfType) {
		case JMF_SHORT:
			ctx.indentPrintLn("short: " + readShortData(ctx, parameterizedJmfType));
			break;
		case JMF_SHORT_OBJECT:
			ctx.indentPrintLn(Short.class.getName() + ": " + Short.valueOf(readShortData(ctx, parameterizedJmfType)));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
	
	public void writeShortData(OutputContext ctx, int jmfType, int v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		if (v == Short.MIN_VALUE) {
			os.write(0x40 | jmfType);
			os.write(v >> 8);
			os.write(v);
		}
		else {
			int s = 0x00;
			int a = v;
			if (v < 0) {
				a = -v;
				s = 0x80;
			}
			
			if (a <= 0xFF) {
				os.write(s | jmfType);
				os.write(a);
			}
			else {
				os.write(s | 0x40 | jmfType);
				os.write(a >> 8);
				os.write(a);
			}
		}
	}
	
	public short readShortData(InputContext ctx, int parameterizedJmfType) throws IOException {
		short v = (short)ctx.safeRead();
		
		if ((parameterizedJmfType & 0x40) != 0)
			v = (short)((v << 8) | ctx.safeRead());
		
		if ((parameterizedJmfType & 0x80) != 0)
			v = (short)-v;
		
		return v;
	}
}
