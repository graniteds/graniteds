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
import org.granite.messaging.jmf.codec.std.IntegerCodec;

/**
 * @author Franck WOLFF
 */
public class IntegerCodecImpl extends AbstractIntegerStringCodec<Integer> implements IntegerCodec {

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
		return Integer.TYPE;
	}

	public void encode(OutputContext ctx, Integer v) throws IOException {
		writeIntData(ctx, JMF_INTEGER_OBJECT, v.intValue());
	}
	
	public Integer decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_INTEGER_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		return Integer.valueOf(readIntData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, int v) throws IOException {
		writeIntData(ctx, JMF_INTEGER, v);
	}
	
	public int decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_INTEGER)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
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
		IntegerComponents ics = intComponents(v);
		ctx.getOutputStream().write((ics.sign << 7) | (ics.length << 5) | jmfType);
		writeIntData(ctx, ics);
	}
	
	protected int readIntData(InputContext ctx, int parameterizedJmfType) throws IOException {
		return readIntData(ctx, (parameterizedJmfType >> 5) & 0x03, (parameterizedJmfType & 0x80) != 0);
	}
	
	public void writeVariableInt(OutputContext ctx, int v) throws IOException {

		final OutputStream os = ctx.getOutputStream();
		
		if (v == Integer.MIN_VALUE)
			os.write(0x80); // negative 0.
		else {
			int sign = 0x00;
			if (v < 0) {
				sign = 0x80;
				v = -v;
			}

			int bytesCount = lengthOfVariableAbsoluteInt(v);
			v -= deltaForVariableAbsoluteIntLength(bytesCount);
			
			switch (bytesCount) {
			case 0:
				os.write(sign | v);
				break;
			case 1: case 2: case 3:
				os.write(sign | 0x40 | (v >> (bytesCount * 7)));
				for (int i = bytesCount - 1; i > 0; i--)
					os.write(0x80 | (v >> (i * 7)));
				os.write(0x7F & v);
				break;
			case 4:
				os.write(sign | 0x40 | (v >> 29));
				os.write(0x80 | (v >> 22));
				os.write(0x80 | (v >> 15));
				os.write(0x80 | (v >> 8));
				os.write(v);
				break;
			}
		}
	}
	
	public int readVariableInt(InputContext ctx) throws IOException {
		int v = ctx.safeRead();
		
		if (v == 0x80)
			v = Integer.MIN_VALUE;
		else {
			boolean opposite = ((v & 0x80) != 0);
			boolean readNext = (v & 0x40) != 0;
			
			v &= 0x3F;
			
			if (readNext) {
				int l = 1;
				for (int i = 1; i <= 3; i++) {
					int u = ctx.safeRead();
					v = (v << 7) | (u & 0x7F);
					if ((u & 0x80) == 0) {
						readNext = false;
						break;
					}
					l++;
				}
				if (readNext)
					v = (v << 8) | ctx.safeRead();
				v += deltaForVariableAbsoluteIntLength(l);
			}
			
			if (opposite)
				v = -v;
		}
		
		return v;
	}
	
	protected static final int[] VARIABLE_INT_DELTAS = new int[5];
	static {
		VARIABLE_INT_DELTAS[0] = 0;
		for (int i = 1; i < VARIABLE_INT_DELTAS.length; i++)
			VARIABLE_INT_DELTAS[i] = (VARIABLE_INT_DELTAS[i-1] << 7) | 0x40;
	}
	
	protected static int lengthOfVariableAbsoluteInt(int abs) {
		for (int i = 1; i < VARIABLE_INT_DELTAS.length; i++) {
			if (abs < VARIABLE_INT_DELTAS[i])
				return (i - 1);
		}
		return 4;
	}
	
	protected static int deltaForVariableAbsoluteIntLength(int len) {
		return VARIABLE_INT_DELTAS[len];
	}
}
