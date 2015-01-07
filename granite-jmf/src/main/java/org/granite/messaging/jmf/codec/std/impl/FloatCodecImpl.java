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

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.FloatCodec;
import org.granite.messaging.jmf.codec.std.impl.util.FloatUtil;

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
		return float.class;
	}

	
	public void encode(OutputContext ctx, Float v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		os.write(JMF_FLOAT_OBJECT);
		FloatUtil.encodeFloat(ctx, v.floatValue());
	}
	
	public Float decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		return Float.valueOf(FloatUtil.decodeFloat(ctx));
	}

	
	public void encodePrimitive(OutputContext ctx, float v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		os.write(JMF_FLOAT);
		FloatUtil.encodeFloat(ctx, v);
	}
	
	public float decodePrimitive(InputContext ctx) throws IOException {
		ctx.safeRead();
		return FloatUtil.decodeFloat(ctx);
	}

	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_FLOAT:
			ctx.indentPrintLn("float: " + FloatUtil.decodeFloat(ctx));
			break;
		case JMF_FLOAT_OBJECT:
			ctx.indentPrintLn(Float.class.getName() + ": " + Float.valueOf(FloatUtil.decodeFloat(ctx)));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
}
