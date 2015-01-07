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
import org.granite.messaging.jmf.codec.std.IntegerCodec;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;

/**
 * @author Franck WOLFF
 */
public class IntegerCodecImpl extends AbstractStandardCodec<Integer> implements IntegerCodec {

	protected static final int LENGTH_BYTE_COUNT_OFFSET = 5;

	public int getObjectType() {
		return JMF_INTEGER_OBJECT;
	}

	public Class<?> getObjectClass() {
		return Integer.class;
	}

	public int getPrimitiveType() {
		return JMF_INTEGER;
	}

	public Class<?> getPrimitiveClass() {
		return int.class;
	}

	public void encode(OutputContext ctx, Integer v) throws IOException {
		writeIntData(ctx, JMF_INTEGER_OBJECT, v.intValue());
	}
	
	public Integer decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		return Integer.valueOf(readIntData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, int v) throws IOException {
		writeIntData(ctx, JMF_INTEGER, v);
	}
	
	public int decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		return readIntData(ctx, parameterizedJmfType);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_INTEGER:
			ctx.indentPrintLn("int: " + readIntData(ctx, parameterizedJmfType));
			break;
		case JMF_INTEGER_OBJECT:
			ctx.indentPrintLn(Integer.class.getName() + ": " + Integer.valueOf(readIntData(ctx, parameterizedJmfType)));
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
	
	protected void writeIntData(OutputContext ctx, int jmfType, int v) throws IOException {
		final OutputStream os = ctx.getOutputStream();

		int opposite = 0x00;
		if (v < 0 && v != Integer.MIN_VALUE) {
			opposite = 0x80;
			v = -v;
		}
		
		int count = IntegerUtil.significantIntegerBytesCount0(v);
		os.write(opposite | (count << LENGTH_BYTE_COUNT_OFFSET) | jmfType);
		IntegerUtil.encodeInteger(ctx, v, count);
	}
	
	protected int readIntData(InputContext ctx, int parameterizedJmfType) throws IOException {
		int v = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> LENGTH_BYTE_COUNT_OFFSET) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0)
			v = -v;
		return v;
	}
}
