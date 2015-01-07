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
import java.math.BigDecimal;
import java.math.BigInteger;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.BigDecimalCodec;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;

/**
 * @author Franck WOLFF
 */
public class BigDecimalCodecImpl extends AbstractStandardCodec<BigDecimal> implements BigDecimalCodec {

	protected static final int LENGTH_BYTE_COUNT_OFFSET = 6;

	public int getObjectType() {
		return JMF_BIG_DECIMAL;
	}

	public Class<?> getObjectClass() {
		return BigDecimal.class;
	}

	public void encode(OutputContext ctx, BigDecimal v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		int scale = v.scale();
		byte[] magnitude = v.unscaledValue().toByteArray();

		int count = IntegerUtil.significantIntegerBytesCount0(magnitude.length);
		os.write((count << LENGTH_BYTE_COUNT_OFFSET) | JMF_BIG_DECIMAL);
		IntegerUtil.encodeInteger(ctx, magnitude.length, count);
		
		IntegerUtil.encodeInteger(ctx, scale);	// scale can be negative
		
		os.write(magnitude);
	}

	public BigDecimal decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int magnitudeLength = IntegerUtil.decodeInteger(ctx, parameterizedJmfType >>> LENGTH_BYTE_COUNT_OFFSET);
		int scale = IntegerUtil.decodeInteger(ctx);

		byte[] magnitude = new byte[magnitudeLength];
		ctx.safeReadFully(magnitude);
		
		BigDecimal v = new BigDecimal(new BigInteger(magnitude), scale);
		
		if (BigDecimal.ZERO.equals(v))
			v = BigDecimal.ZERO;
		else if (BigDecimal.ONE.equals(v))
			v = BigDecimal.ONE;
		else if (BigDecimal.TEN.equals(v))
			v = BigDecimal.TEN;
		
		return v;
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		if (jmfType != JMF_BIG_DECIMAL)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		ctx.indentPrintLn(BigDecimal.class.getName() + ": " + decode(ctx, parameterizedJmfType));
	}
}
