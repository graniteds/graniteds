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
import org.granite.messaging.jmf.codec.std.LongCodec;

/**
 * @author Franck WOLFF
 */
public class LongCodecImpl extends AbstractStandardCodec<Long> implements LongCodec {

	public int getObjectType() {
		return JMF_LONG_OBJECT;
	}

	public Class<?> getObjectClass() {
		return Long.class;
	}

	public int getPrimitiveType() {
		return JMF_LONG;
	}

	public Class<?> getPrimitiveClass() {
		return Long.TYPE;
	}

	public void encode(OutputContext ctx, Long v) throws IOException {
		writeLongData(ctx, JMF_LONG_OBJECT, v.longValue());
	}
	
	public Long decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);

		if (jmfType != JMF_LONG_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		return Long.valueOf(readLongData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, long v) throws IOException {
		writeLongData(ctx, JMF_LONG, v);
	}
	
	public long decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);

		if (jmfType != JMF_LONG)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		return readLongData(ctx, parameterizedJmfType);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_LONG:
			ctx.indentPrintLn("long: " + readLongData(ctx, parameterizedJmfType));
			break;
		case JMF_LONG_OBJECT:
			ctx.indentPrintLn(Long.class.getName() + ": " + Long.valueOf(readLongData(ctx, parameterizedJmfType)));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}

	protected void writeLongData(OutputContext ctx, int jmfType, long v) throws IOException {
		int l = 7; // --> Long.MIN_VALUE
		int s = 0x00;
		if (v != Long.MIN_VALUE) {
			if (v < 0) {
				s = 0x80;
				v = -v;
			}
			l = lenghtOfAbsoluteLong(v);
		}
		
		final OutputStream os = ctx.getOutputStream();
		
		os.write(s | (l << 4) | jmfType);
		switch (l) {
		case 7:
			os.write((int)(v >> 56));
		case 6:
			os.write((int)(v >> 48));
		case 5:
			os.write((int)(v >> 40));
		case 4:
			os.write((int)(v >> 32));
		case 3:
			os.write((int)(v >> 24));
		case 2:
			os.write((int)(v >> 16));
		case 1:
			os.write((int)(v >> 8));
		case 0:
			os.write((int)v);
			break;
		}
	}
	
	protected int lenghtOfAbsoluteLong(long v) {
		if (v <= 0x00000000FFFFFFFFL) {
			if (v <= 0x000000000000FFFFL)
				return (v <= 0x00000000000000FFL ? 0 : 1);
			return (v <= 0x0000000000FFFFFFL ? 2 : 3);
		}
		
		if (v <= 0x0000FFFFFFFFFFFFL)
			return (v <= 0x000000FFFFFFFFFFL ? 4 : 5);
		return (v <= 0x00FFFFFFFFFFFFFFL ? 6 : 7);
	}
	
	protected long readLongData(InputContext ctx, int parameterizedJmfType) throws IOException {
		long v = 0;
		
		switch ((parameterizedJmfType >> 4) & 0x07) {
		case 7:
			v |= ((long)ctx.safeRead()) << 56;
		case 6:
			v |= ((long)ctx.safeRead()) << 48;
		case 5:
			v |= ((long)ctx.safeRead()) << 40;
		case 4:
			v |= ((long)ctx.safeRead()) << 32;
		case 3:
			v |= ((long)ctx.safeRead()) << 24;
		case 2:
			v |= ((long)ctx.safeRead()) << 16;
		case 1:
			v |= ((long)ctx.safeRead()) << 8;
		case 0:
			v |= ctx.safeRead();
		}

		if ((parameterizedJmfType & 0x80) != 0)
			v = -v;
		
		return v;
	}
	
	public void writeVariableLong(OutputContext ctx, long v) throws IOException {

		final OutputStream os = ctx.getOutputStream();
		
		if (v == Long.MIN_VALUE)
			os.write(0x80);
		else {
			int sign = 0x00;
			if (v < 0) {
				sign = 0x80;
				v = -v;
			}
			
			int bytesCount = lengthOfVariableAbsoluteLong(v);
			v -= deltaForVariableAbsoluteLongLength(bytesCount);
			
			switch (bytesCount) {
			case 0:
				os.write(sign | (int)v);
				break;
			case 1: case 2: case 3: case 4: case 5: case 6: case 7:
				os.write(sign | 0x40 | (int)(v >> (bytesCount * 7)));
				for (int i = bytesCount - 1; i > 0; i--)
					os.write(0x80 | (int)(v >> (i * 7)));
				os.write(0x7F & (int)v);
				break;
			case 8:
				os.write(sign | 0x40 | (int)(v >> 57));
				os.write(0x80 | (int)(v >> 50));
				os.write(0x80 | (int)(v >> 43));
				os.write(0x80 | (int)(v >> 36));
				os.write(0x80 | (int)(v >> 29));
				os.write(0x80 | (int)(v >> 22));
				os.write(0x80 | (int)(v >> 15));
				os.write(0x80 | (int)(v >> 8));
				os.write((int)v);
				break;
			}
		}
	}
	
	public long readVariableLong(InputContext ctx) throws IOException {
		long v = ctx.safeRead();
		
		if (v == 0x80L)
			v = Long.MIN_VALUE;
		else {
			boolean opposite = ((v & 0x80L) != 0);
			boolean readNext = (v & 0x40L) != 0;
			
			v &= 0x3FL;
			
			if (readNext) {
				int l = 1;
				for (int i = 1; i <= 7; i++) {
					long u = ctx.safeRead();
					v = (v << 7) | (u & 0x7FL);
					if ((u & 0x80L) == 0) {
						readNext = false;
						break;
					}
					l++;
				}
				if (readNext)
					v = (v << 8) | ctx.safeRead();
				v += deltaForVariableAbsoluteLongLength(l);
			}
			
			if (opposite)
				v = -v;
		}
		
		return v;
	}

	protected static final long[] VARIABLE_LONG_DELTAS = new long[9];
	static {
		VARIABLE_LONG_DELTAS[0] = 0L;
		for (int i = 1; i < VARIABLE_LONG_DELTAS.length; i++)
			VARIABLE_LONG_DELTAS[i] = (VARIABLE_LONG_DELTAS[i-1] << 7) | 0x40L;
	}
	
	protected static int lengthOfVariableAbsoluteLong(long abs) {
		for (int i = 1; i < VARIABLE_LONG_DELTAS.length; i++) {
			if (abs < VARIABLE_LONG_DELTAS[i])
				return (i - 1);
		}
		return 8;
	}
	
	protected static long deltaForVariableAbsoluteLongLength(int len) {
		return VARIABLE_LONG_DELTAS[len];
	}
}
