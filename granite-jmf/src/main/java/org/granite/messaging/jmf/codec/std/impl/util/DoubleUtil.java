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
package org.granite.messaging.jmf.codec.std.impl.util;

import java.io.IOException;
import java.io.OutputStream;

import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.JMFEncodingException;
import org.granite.messaging.jmf.OutputContext;

/**
 * @author Franck WOLFF
 */
public class DoubleUtil {
	
	// ------------------------------------------------------------------------

	public static class DoubleAsLong {
		
		public final long longValue;
		public final int pow10;
		
		public DoubleAsLong(long longValue, int pow10) {
			this.longValue = longValue;
			this.pow10 = pow10;
		}
	}
	
	@SuppressWarnings("cast")
	public static DoubleAsLong doubleAsLong04(double v) {
		long asLong = (long)v;
		if (v == (double)asLong)
			return new DoubleAsLong(asLong, 0);
		
		asLong = (long)(v * 100.0);
		if (v == (asLong / 100.0))
			return new DoubleAsLong(asLong, 2);
		asLong += (asLong < 0 ? -1 : 1);
		if (v == (asLong / 100.0))
			return new DoubleAsLong(asLong, 2);
		
		asLong = (long)(v * 10000.0);
		if (v == (asLong / 10000.0))
			return new DoubleAsLong(asLong, 4);
		asLong += (asLong < 0 ? -1 : 1);
		if (v == (asLong / 10000.0))
			return new DoubleAsLong(asLong, 4);
		
		return null;
	}

	// ------------------------------------------------------------------------
	
	public static void encodeDouble(OutputContext ctx, double v) throws IOException {
		LongUtil.encodeLong(ctx, Double.doubleToLongBits(v));
	}
	
	public static double decodeDouble(InputContext ctx) throws IOException {
		return Double.longBitsToDouble(LongUtil.decodeLong(ctx));
	}

	// ------------------------------------------------------------------------

	/*
	 * Double special values:
	 * 
	 * 0x7FF0000000000000L							-> +Infinity
	 * 0x7FF0000000000001L ... 0x7FF7FFFFFFFFFFFFL	-> 1st range of Quiet NaNs
	 * 0x7FF8000000000000L							-> Canonical NaN
	 * 0x7FF8000000000001L ... 0x7FFFFFFFFFFFFFFFL	-> 1st range of Signaling NaNs
	 * 0xFFF0000000000000L							-> -Infinity
	 * 0xFFF0000000000001L ... 0xFFF7FFFFFFFFFFFFL	-> 2nd range of Quiet NaNs
	 * 0xFFF8000000000000L ... 0xFFFFFFFFFFFFFFFFL	-> 2nd range of Signaling NaNs
	 * 
	 * Which gives the following 2-bytes prefixes:
	 * 
	 * 0x7FF0 -> +Infinity
	 * 0x7FF8 -> NaN
	 * 0xFFF0 -> -Infinity
	 * 
	 * 0x7FF1 ... 0x7FF7 \
	 * 0x7FF9 ... 0x7FFF  > Non canonical NaN prefixes.
	 * 0xFFF1 ... 0xFFFF /
	 */
	
	public static void encodeVariableDouble(OutputContext ctx, double v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		long bits = Double.doubleToRawLongBits(v);
		if ((bits & 0x7FF0000000000000L) == 0x7FF0000000000000L) {
			os.write(0x7F);
			if (bits == 0x7FF0000000000000L) // +Infinity
				os.write(0xF0);
			else if (bits == 0xFFF0000000000000L) // -Infinity
				os.write(0xF1);
			else // NaN (canonical or not)
				os.write(0xF2);
		}
		else if (bits == 0x8000000000000000L) { // -0.0
			os.write(0x7F);
			os.write(0xF3);
		}
		else {
			DoubleAsLong asLong = doubleAsLong04(v);
			if (asLong != null &&
				asLong.longValue >= LongUtil.MIN_5_BYTES_VARIABLE_LONG &&
				asLong.longValue <= LongUtil.MAX_5_BYTES_VARIABLE_LONG) {
				
				os.write(0xFF);
				os.write(0xF0 | asLong.pow10); // 0xF0, 0xF2 or 0xF4.
				LongUtil.encodeVariableLong(ctx, asLong.longValue);
			}
			else
				LongUtil.encodeLong(ctx, bits);
		}
	}
	
	public static double decodeVariableDouble(InputContext ctx) throws IOException {
		
		int prefix = ctx.safeRead() << 8 | ctx.safeRead();
		switch (prefix) {
		case 0x7FF0:
			return Double.POSITIVE_INFINITY;
		case 0x7FF1:
			return Double.NEGATIVE_INFINITY;
		case 0x7FF2:
			return Double.NaN;
		case 0x7FF3:
			return -0.0;
		}
		
		if ((prefix & 0xFFF0) == 0xFFF0) {
			long asLong = LongUtil.decodeVariableLong(ctx);
			int pow10 = (prefix & 0x0F);
			
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
		
		return Double.longBitsToDouble(((long)prefix) << 48 | LongUtil.decodeLong(ctx, 5));
	}
}
