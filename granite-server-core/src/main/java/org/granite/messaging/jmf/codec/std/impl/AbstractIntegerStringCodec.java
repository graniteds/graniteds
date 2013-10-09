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

import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractIntegerStringCodec<T> extends AbstractStandardCodec<T> {
	
	protected void writeIntData(OutputContext ctx, IntegerComponents ics) throws IOException {
		int v = ics.value;
		
		final OutputStream os = ctx.getOutputStream();
		
		switch (ics.length) {
		case 3:
			os.write(v >> 24);
		case 2:
			os.write(v >> 16);
		case 1:
			os.write(v >> 8);
		case 0:
			os.write(v);
			break;
		}
	}
	
	protected int readIntData(InputContext ctx, int length, boolean opposite) throws IOException {
		int v = 0;
		
		switch (length) {
		case 3:
			v |= ctx.safeRead() << 24;
		case 2:
			v |= ctx.safeRead() << 16;
		case 1:
			v |= ctx.safeRead() << 8;
		case 0:
			v |= ctx.safeRead();
		}
		
		if (opposite)
			v = -v;
		
		return v;
	}

	protected IntegerComponents intComponents(int v) {
		int s = 0;
		int l = 3; // --> Integer.MIN_VALUE
		if (v != Integer.MIN_VALUE) {
			if (v < 0) {
				s = 1;
				v = -v;
			}
			if (v <= 0xFFFF)
				l = (v <= 0xFF ? 0 : 1);
			else
				l = (v <= 0xFFFFFF ? 2 : 3);
		}
		return new IntegerComponents(s, l, v);
	}
	
	protected void writeString(OutputContext ctx, String v, StringTypeHandler handler) throws IOException {
		if (v == null)
			throw new NullPointerException("String value cannot be null");
		
		final OutputStream os = ctx.getOutputStream();
		
		int indexOfStoredString = ctx.indexOfStoredStrings(v);
		
		if (indexOfStoredString >= 0) {
			IntegerComponents ics = intComponents(indexOfStoredString);
			os.write(handler.type(ics, true));
			writeIntData(ctx, ics);
		}
		else {
			ctx.addToStoredStrings(v);
			
			if (v.length() == 0) {
				os.write(handler.type(IntegerComponents.ZERO, false));
				os.write(0);
			}
			else {
				final byte[] bytes = v.getBytes(UTF8);
				final int length = bytes.length;
	
				IntegerComponents ics = intComponents(length);
				os.write(handler.type(ics, false));
				writeIntData(ctx, ics);
	
				os.write(bytes);
			}
		}
	}
	
	protected String readString(InputContext ctx, int parameterizedJmfType, StringTypeHandler handler) throws IOException {
		int indexOrLength = readIntData(ctx, handler.indexOrLengthBytesCount(parameterizedJmfType), false);
		return readString(ctx, parameterizedJmfType, indexOrLength, handler);
	}
	
	protected String readString(InputContext ctx, int parameterizedJmfType, int indexOrLength, StringTypeHandler handler) throws IOException {
		if (handler.isReference(parameterizedJmfType))
			return ctx.getSharedString(indexOrLength);

		byte[] bytes = new byte[indexOrLength];
		ctx.safeReadFully(bytes);
		String s = new String(bytes, UTF8);
		
		ctx.addSharedString(s);
		
		return s;
	}
	
	protected static class IntegerComponents {
		
		public static final IntegerComponents ZERO = new IntegerComponents(0, 0, 0);
		
		public final int sign;
		public final int length;
		public final int value;

		public IntegerComponents(int sign, int length, int value) {
			this.sign = sign;
			this.length = length;
			this.value = value;
		}
	}
	
	protected static interface StringTypeHandler {
		
		int type(IntegerComponents ics, boolean reference);
		int indexOrLengthBytesCount(int parameterizedJmfType);
		boolean isReference(int parameterizedJmfType);
	}

	protected static final StringTypeHandler JMF_STRING_TYPE_HANDLER = new StringTypeHandler() {

		public int type(IntegerComponents ics, boolean reference) {
			if (reference)
				return 0x80 | (ics.length << 5) | JMF_STRING;
			return (ics.length << 5) | JMF_STRING;
		}

		public int indexOrLengthBytesCount(int parameterizedJmfType) {
			return (parameterizedJmfType >> 5) & 0x03;
		}

		public boolean isReference(int parameterizedJmfType) {
			return (parameterizedJmfType & 0x80) != 0;
		}
	};
}
