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
package org.granite.messaging.jmf.codec.std.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.FloatCodec;

/**
 * @author Franck WOLFF
 */
public class FloatCodecImpl extends AbstractStandardCodec<Float> implements FloatCodec {

	public int getObjectType() {
		return JMF_FLOAT_OBJECT;
	}

	public Class<?> getObjectClass() {
		return Float.class;
	}

	public int getPrimitiveType() {
		return JMF_FLOAT;
	}

	public Class<?> getPrimitiveClass() {
		return Float.TYPE;
	}

	public void encode(OutputContext ctx, Float v) throws IOException {
		writeFloatData(ctx, JMF_FLOAT_OBJECT, v.floatValue());
	}
	
	public Float decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_FLOAT_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		return Float.valueOf(readFloatData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, float v) throws IOException {
		writeFloatData(ctx, JMF_FLOAT, v);
	}
	
	public float decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_FLOAT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		return readFloatData(ctx, parameterizedJmfType);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_FLOAT:
			ctx.indentPrintLn("float: " + readFloatData(ctx, parameterizedJmfType));
			break;
		case JMF_FLOAT_OBJECT:
			ctx.indentPrintLn(Float.class.getName() + ": " + Float.valueOf(readFloatData(ctx, parameterizedJmfType)));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
	
	public static void writeFloatData(OutputContext ctx, int jmfType, float v) throws IOException {
		int bits = Float.floatToIntBits(v);
		
		final OutputStream os = ctx.getOutputStream();
		
		os.write(jmfType);
		
		os.write(bits);
		os.write(bits >> 8);
		os.write(bits >> 16);
		os.write(bits >> 24);
	}
	
	public static float readFloatData(InputContext ctx, int type) throws IOException {
		int bits = ctx.safeRead();
		
		bits |= ctx.safeRead() << 8;
		bits |= ctx.safeRead() << 16;
		bits |= ctx.safeRead() << 24;
		
		return Float.intBitsToFloat(bits);
	}
}
