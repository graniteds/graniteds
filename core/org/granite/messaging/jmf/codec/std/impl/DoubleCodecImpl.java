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
import org.granite.messaging.jmf.codec.std.DoubleCodec;

/**
 * @author Franck WOLFF
 */
public class DoubleCodecImpl extends AbstractStandardCodec<Double> implements DoubleCodec {

	public int getObjectType() {
		return JMF_DOUBLE_OBJECT;
	}

	public Class<?> getObjectClass() {
		return Double.class;
	}

	public int getPrimitiveType() {
		return JMF_DOUBLE;
	}

	public Class<?> getPrimitiveClass() {
		return Double.TYPE;
	}

	public void encode(OutputContext ctx, Double v) throws IOException {
		writeDoubleData(ctx, JMF_DOUBLE_OBJECT, v.doubleValue());
	}
	
	public Double decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);

		if (jmfType != JMF_DOUBLE_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		return Double.valueOf(readDoubleData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, double v) throws IOException {
		writeDoubleData(ctx, JMF_DOUBLE, v);
	}
	
	public double decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);

		if (jmfType != JMF_DOUBLE)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		return readDoubleData(ctx, parameterizedJmfType);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_DOUBLE:
			ctx.indentPrintLn("double: " + readDoubleData(ctx, parameterizedJmfType));
			break;
		case JMF_DOUBLE_OBJECT:
			ctx.indentPrintLn(Double.class.getName() + ": " + Double.valueOf(readDoubleData(ctx, parameterizedJmfType)));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
	
	public static void writeDoubleData(OutputContext ctx, int jmfType, double v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		if (v == (float)v || Double.isNaN(v)) {
			os.write(0x80 | jmfType);
			
			int bits = Float.floatToIntBits((float)v);
			os.write(bits);
			os.write(bits >> 8);
			os.write(bits >> 16);
			os.write(bits >> 24);
		}
		else {
			os.write(jmfType);

			long bits = Double.doubleToLongBits(v);
			os.write((int)bits);
			os.write((int)(bits >> 8));
			os.write((int)(bits >> 16));
			os.write((int)(bits >> 24));
			os.write((int)(bits >> 32));
			os.write((int)(bits >> 40));
			os.write((int)(bits >> 48));
			os.write((int)(bits >> 56));
		}
	}
	
	public static double readDoubleData(InputContext ctx, int type) throws IOException {
		double v = 0.0;
		
		if ((type & 0x80) != 0) {
			int i = ctx.safeRead();
			i |= ctx.safeRead() << 8;
			i |= ctx.safeRead() << 16;
			i |= ctx.safeRead() << 24;
			v = Float.intBitsToFloat(i);
		}
		else {
			long l = ctx.safeRead();
			l |= ((long)ctx.safeRead()) << 8;
			l |= ((long)ctx.safeRead()) << 16;
			l |= ((long)ctx.safeRead()) << 24;
			l |= ((long)ctx.safeRead()) << 32;
			l |= ((long)ctx.safeRead()) << 40;
			l |= ((long)ctx.safeRead()) << 48;
			l |= ((long)ctx.safeRead()) << 56;
			v = Double.longBitsToDouble(l);
		}
		
		return v;
	}
}
