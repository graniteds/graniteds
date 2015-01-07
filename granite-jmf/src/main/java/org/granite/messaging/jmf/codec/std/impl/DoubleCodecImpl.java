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
import org.granite.messaging.jmf.JMFEncodingException;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.DoubleCodec;
import org.granite.messaging.jmf.codec.std.impl.util.DoubleUtil;
import org.granite.messaging.jmf.codec.std.impl.util.LongUtil;
import org.granite.messaging.jmf.codec.std.impl.util.DoubleUtil.DoubleAsLong;

/**
 * @author Franck WOLFF
 */
public class DoubleCodecImpl extends AbstractStandardCodec<Double> implements DoubleCodec {

	protected static final int POW_10_OFFSET = 4;

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
		return double.class;
	}

	public void encode(OutputContext ctx, Double v) throws IOException {
		writeDoubleData(ctx, JMF_DOUBLE_OBJECT, v.doubleValue());
	}
	
	public Double decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		return Double.valueOf(readDoubleData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, double v) throws IOException {
		writeDoubleData(ctx, JMF_DOUBLE, v);
	}
	
	public double decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
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
		
		long bits = Double.doubleToLongBits(v);
		
		// v isn't NaN, +-Infinity or -0.0
		if ((bits & 0x7FF0000000000000L) != 0x7FF0000000000000L && bits != 0x8000000000000000L) {
			
			DoubleAsLong asLong = DoubleUtil.doubleAsLong04(v);
			if (asLong != null &&
				asLong.longValue >= LongUtil.MIN_7_BYTES_VARIABLE_LONG &&
				asLong.longValue <= LongUtil.MAX_7_BYTES_VARIABLE_LONG) {
				
				os.write(0x80 | (asLong.pow10 << POW_10_OFFSET) | jmfType);
				LongUtil.encodeVariableLong(ctx, asLong.longValue);
				return;
			}
		}
		
		os.write(jmfType);
		LongUtil.encodeLong(ctx, bits);
	}
	
	public static double readDoubleData(InputContext ctx, int parameterizedJmfType) throws IOException {
		if ((parameterizedJmfType & 0x80) != 0) {
			long asLong = LongUtil.decodeVariableLong(ctx);
			int pow10 = ((parameterizedJmfType >>> POW_10_OFFSET) & 0x07);
			
			switch (pow10) {
			case 0:
				return asLong;
			case 2:
				return asLong / 100.0;
			case 4:
				return asLong / 10000.0;
			default:
				throw new JMFEncodingException("Unsupported power of 10: " + pow10);
			}
		}
		
		return Double.longBitsToDouble(LongUtil.decodeLong(ctx));
	}
}
