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
import java.math.BigInteger;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.BigIntegerCodec;

/**
 * @author Franck WOLFF
 */
public class BigIntegerCodecImpl extends AbstractIntegerStringCodec<BigInteger> implements BigIntegerCodec {

	public int getObjectType() {
		return JMF_BIG_INTEGER;
	}

	public Class<?> getObjectClass() {
		return BigInteger.class;
	}

	public void encode(OutputContext ctx, BigInteger v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		ctx.addToStoredObjects(v);
		
		byte[] magnitude = v.toByteArray();

		IntegerComponents ics = intComponents(magnitude.length);
		os.write((ics.length << 6) | JMF_BIG_INTEGER);
		writeIntData(ctx, ics);
		
		os.write(magnitude);
	}

	public BigInteger decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_BIG_INTEGER)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		int magnitudeLength = readIntData(ctx, (parameterizedJmfType >> 6) & 0x03, false);

		byte[] magnitude = new byte[magnitudeLength];
		ctx.safeReadFully(magnitude);
		
		BigInteger v = new BigInteger(magnitude);
		
		if (BigInteger.ZERO.equals(v))
			v = BigInteger.ZERO;
		else if (BigInteger.ONE.equals(v))
			v = BigInteger.ONE;
		else if (BigInteger.TEN.equals(v))
			v = BigInteger.TEN;
		
		return v;
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		if (jmfType != JMF_BIG_INTEGER)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		ctx.indentPrintLn(BigInteger.class.getName() + ": " + decode(ctx, parameterizedJmfType));
	}
}
