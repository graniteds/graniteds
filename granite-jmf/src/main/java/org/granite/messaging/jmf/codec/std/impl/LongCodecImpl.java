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
import org.granite.messaging.jmf.codec.std.LongCodec;
import org.granite.messaging.jmf.codec.std.impl.util.LongUtil;

/**
 * @author Franck WOLFF
 */
public class LongCodecImpl extends AbstractStandardCodec<Long> implements LongCodec {

	protected static final int LENGTH_BYTE_COUNT_OFFSET = 4;
	
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
		return long.class;
	}

	public void encode(OutputContext ctx, Long v) throws IOException {
		writeLongData(ctx, JMF_LONG_OBJECT, v.longValue());
	}
	
	public Long decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		return Long.valueOf(readLongData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, long v) throws IOException {
		writeLongData(ctx, JMF_LONG, v);
	}
	
	public long decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
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
		final OutputStream os = ctx.getOutputStream();

		int opposite = 0x00;
		if (v < 0 && v != Long.MIN_VALUE) {
			opposite = 0x80;
			v = -v;
		}
		
		int count = LongUtil.significantLongBytesCount0(v);
		os.write(opposite | (count << LENGTH_BYTE_COUNT_OFFSET) | jmfType);
		LongUtil.encodeLong(ctx, v, count);
	}
	
	protected long readLongData(InputContext ctx, int parameterizedJmfType) throws IOException {
		long v = LongUtil.decodeLong(ctx, (parameterizedJmfType >>> LENGTH_BYTE_COUNT_OFFSET) & 0x07);
		if ((parameterizedJmfType & 0x80) != 0)
			v = -v;
		return v;
	}
}
