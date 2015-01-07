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
import org.granite.messaging.jmf.OutputContext;

/**
 * @author Franck WOLFF
 */
public class IntegerUtil {

	// ------------------------------------------------------------------------

	public static void encodeInteger(OutputContext ctx, int v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		os.write(v >>> 24);
		os.write(v >>> 16);
		os.write(v >>> 8);
		os.write(v);
	}
	
	public static int decodeInteger(InputContext ctx) throws IOException {
		return (
			ctx.safeRead() << 24 |
			ctx.safeRead() << 16 |
			ctx.safeRead() << 8  |
			ctx.safeRead()
		);
	}
	
	// ------------------------------------------------------------------------
	
	public static int significantIntegerBytesCount0(int v) {
		if (v < 0)
			return 3;
		if (v <= 0xFFFF)
			return (v <= 0xFF ? 0 : 1);
		return (v <= 0xFFFFFF ? 2 : 3);
	}
	
	public static void encodeInteger(OutputContext ctx, int v, int significantIntegerBytesCount0) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		switch (significantIntegerBytesCount0) {
		case 3:
			os.write(v >>> 24);
		case 2:
			os.write(v >>> 16);
		case 1:
			os.write(v >>> 8);
		case 0:
			os.write(v);
		}
	}
	
	public static int decodeInteger(InputContext ctx, int significantIntegerBytesCount0) throws IOException {
		int v = 0;
		
		switch (significantIntegerBytesCount0) {
		case 3:
			v |= ctx.safeRead() << 24;
		case 2:
			v |= ctx.safeRead() << 16;
		case 1:
			v |= ctx.safeRead() << 8;
		case 0:
			v |= ctx.safeRead();
		}
		
		return v;
	}

	// ------------------------------------------------------------------------
	
	public static void encodeVariableInteger(OutputContext ctx, int v) throws IOException {
		encodeVariableUnsignedInteger(ctx, (v << 1) ^ (v >> 31));
	}

	public static int decodeVariableInteger(InputContext ctx) throws IOException {
		int v = decodeVariableUnsignedInteger(ctx);
		return ((v & 0x1) == 0 ? (v >>> 1) : (-1 ^ (v >>> 1)));
	}
	
	// ------------------------------------------------------------------------
	
	public static void encodeVariableUnsignedInteger(OutputContext ctx, int v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		if (v >= 0 && v < 0x10204080) {
			if (v < 0x4080) {
				if (v < 0x80)
					os.write(v);
				else {
					v -= 0x80;
					os.write(0x80 | v);
					os.write(v >>> 7);
				}
			}
			else if (v < 0x204080) {
				v -= 0x4080;
				os.write(0x80 | v);
				os.write(0x80 | (v >>> 7));
				os.write(v >>> 14);
			}
			else {
				v -= 0x204080;
				os.write(0x80 | v);
				os.write(0x80 | (v >>> 7));
				os.write(0x80 | (v >>> 14));
				os.write(v >>> 21);
			}
		}
		else {
			os.write(0x80 | v);
			os.write(0x80 | (v >>> 7));
			os.write(0x80 | (v >>> 14));
			os.write(0x80 | (v >>> 21));
			os.write(v >>> 28);
		}
	}
	
	public static int decodeVariableUnsignedInteger(InputContext ctx) throws IOException {
		int v = ctx.safeRead();
		
		if ((v & 0x80) != 0) {
			v = (v & 0x7F) | (ctx.safeRead() << 7);
			
			if ((v & 0x4000) != 0) {
				v = (v & 0x3FFF) | (ctx.safeRead() << 14);
				
				if ((v & 0x200000) != 0) {
					v = (v & 0x1FFFFF) | (ctx.safeRead() << 21);
					
					if ((v & 0x10000000) != 0)
						v = (v & 0x0FFFFFFF) | (ctx.safeRead() << 28);
					else
						v += 0x204080;
				}
				else
					v += 0x4080;
			}
			else
				v += 0x80;
		}
		
		return v;
	}
}
