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
public class LongUtil {

	// ------------------------------------------------------------------------

	public static void encodeLong(OutputContext ctx, long v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		os.write((int)(v >>> 56));
		os.write((int)(v >>> 48));
		os.write((int)(v >>> 40));
		os.write((int)(v >>> 32));
		os.write((int)(v >>> 24));
		os.write((int)(v >>> 16));
		os.write((int)(v >>> 8));
		os.write((int)v);
	}
	
	public static long decodeLong(InputContext ctx) throws IOException {
		return (
			ctx.safeReadLong() << 56 |
			ctx.safeReadLong() << 48 |
			ctx.safeReadLong() << 40 |
			ctx.safeReadLong() << 32 |
			ctx.safeReadLong() << 24 |
			ctx.safeReadLong() << 16 |
			ctx.safeReadLong() << 8  |
			ctx.safeReadLong()
		);
	}

	// ------------------------------------------------------------------------
	
	public static int significantLongBytesCount0(long v) {
		if (v < 0L)
			return 7;
		if (v <= 0xFFFFFFFFL) {
			if (v <= 0xFFFFL)
				return (v <= 0xFFL ? 0 : 1);
			return (v <= 0xFFFFFFL ? 2 : 3);
		}
		if (v <= 0xFFFFFFFFFFFFL)
			return (v <= 0xFFFFFFFFFFL ? 4 : 5);
		return (v <= 0xFFFFFFFFFFFFFFL ? 6 : 7);
	}

	public static void encodeLong(OutputContext ctx, long v, int significantLongBytesCount0) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		switch (significantLongBytesCount0) {
		case 7:
			os.write((int)(v >>> 56));
		case 6:
			os.write((int)(v >>> 48));
		case 5:
			os.write((int)(v >>> 40));
		case 4:
			os.write((int)(v >>> 32));
		case 3:
			os.write((int)(v >>> 24));
		case 2:
			os.write((int)(v >>> 16));
		case 1:
			os.write((int)(v >>> 8));
		case 0:
			os.write((int)v);
		}
	}
	
	public static long decodeLong(InputContext ctx, int significantLongBytesCount0) throws IOException {
		long v = 0L;
		
		switch (significantLongBytesCount0) {
		case 7:
			v |= ctx.safeReadLong() << 56;
		case 6:
			v |= ctx.safeReadLong() << 48;
		case 5:
			v |= ctx.safeReadLong() << 40;
		case 4:
			v |= ctx.safeReadLong() << 32;
		case 3:
			v |= ctx.safeReadLong() << 24;
		case 2:
			v |= ctx.safeReadLong() << 16;
		case 1:
			v |= ctx.safeReadLong() << 8;
		case 0:
			v |= ctx.safeReadLong();
		}
		
		return v;
	}
	
	// ------------------------------------------------------------------------
	
	public static final long MIN_1_BYTES_VARIABLE_LONG = -0x40L;
	public static final long MAX_1_BYTES_VARIABLE_LONG =  0x3FL;
	
	public static final long MIN_2_BYTES_VARIABLE_LONG = -0x2040L;
	public static final long MAX_2_BYTES_VARIABLE_LONG =  0x203FL;
	
	public static final long MIN_3_BYTES_VARIABLE_LONG = -0x102040L;
	public static final long MAX_3_BYTES_VARIABLE_LONG =  0x10203FL;
	
	public static final long MIN_4_BYTES_VARIABLE_LONG = -0x8102040L;
	public static final long MAX_4_BYTES_VARIABLE_LONG =  0x810203FL;
	
	public static final long MIN_5_BYTES_VARIABLE_LONG = -0x408102040L;
	public static final long MAX_5_BYTES_VARIABLE_LONG =  0x40810203FL;
	
	public static final long MIN_6_BYTES_VARIABLE_LONG = -0x20408102040L;
	public static final long MAX_6_BYTES_VARIABLE_LONG =  0x2040810203FL;
	
	public static final long MIN_7_BYTES_VARIABLE_LONG = -0x1020408102040L;
	public static final long MAX_7_BYTES_VARIABLE_LONG =  0x102040810203FL;
	
	public static final long MIN_8_BYTES_VARIABLE_LONG = -0x81020408102040L;
	public static final long MAX_8_BYTES_VARIABLE_LONG =  0x8102040810203FL;
	
	// ------------------------------------------------------------------------

	public static void encodeVariableLong(OutputContext ctx, long v) throws IOException {
		encodeVariableUnsignedLong(ctx, (v << 1) ^ (v >> 63));
	}
	
	public static long decodeVariableLong(InputContext ctx) throws IOException {
		long v = decodeVariableUnsignedLong(ctx);
		return ((v & 0x1L) == 0 ? (v >>> 1) : (-1L ^ (v >>> 1)));
	}
	
	// ------------------------------------------------------------------------

	public static void encodeVariableUnsignedLong(OutputContext ctx, long v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		if (v >= 0 && v < 0x102040810204080L) {
			
			if (v < 0x10204080L) {
				if (v < 0x4080L) {
					if (v < 0x80L)
						os.write((int)v);
					else {
						v -= 0x80L;
						os.write(0x80 | (int)v);
						os.write((int)(v >>> 7));
					}
				}
				else if (v < 0x204080L) {
					v -= 0x4080L;
					os.write(0x80 | (int)v);
					os.write(0x80 | (int)(v >>> 7));
					os.write((int)(v >>> 14));
				}
				else {
					v -= 0x204080L;
					os.write(0x80 | (int)v);
					os.write(0x80 | (int)(v >>> 7));
					os.write(0x80 | (int)(v >>> 14));
					os.write((int)(v >>> 21));
				}
			}
			else if (v < 0x40810204080L){
				if (v < 0x810204080L) {
					v -= 0x10204080L;
					os.write(0x80 | (int)v);
					os.write(0x80 | (int)(v >>> 7));
					os.write(0x80 | (int)(v >>> 14));
					os.write(0x80 | (int)(v >>> 21));
					os.write((int)(v >>> 28));
				}
				else {
					v -= 0x810204080L;
					os.write(0x80 | (int)v);
					os.write(0x80 | (int)(v >>> 7));
					os.write(0x80 | (int)(v >>> 14));
					os.write(0x80 | (int)(v >>> 21));
					os.write(0x80 | (int)(v >>> 28));
					os.write((int)(v >>> 35));
				}
			}
			else if (v < 0x2040810204080L) {
				v -= 0x40810204080L;
				os.write(0x80 | (int)v);
				os.write(0x80 | (int)(v >>> 7));
				os.write(0x80 | (int)(v >>> 14));
				os.write(0x80 | (int)(v >>> 21));
				os.write(0x80 | (int)(v >>> 28));
				os.write(0x80 | (int)(v >>> 35));
				os.write((int)(v >>> 42));
			}
			else {
				v -= 0x2040810204080L;
				os.write(0x80 | (int)v);
				os.write(0x80 | (int)(v >>> 7));
				os.write(0x80 | (int)(v >>> 14));
				os.write(0x80 | (int)(v >>> 21));
				os.write(0x80 | (int)(v >>> 28));
				os.write(0x80 | (int)(v >>> 35));
				os.write(0x80 | (int)(v >>> 42));
				os.write((int)(v >>> 49));
			}
		}
		else {
			os.write(0x80 | (int)v);
			os.write(0x80 | (int)(v >>> 7));
			os.write(0x80 | (int)(v >>> 14));
			os.write(0x80 | (int)(v >>> 21));
			os.write(0x80 | (int)(v >>> 28));
			os.write(0x80 | (int)(v >>> 35));
			os.write(0x80 | (int)(v >>> 42));
			os.write(0x80 | (int)(v >>> 49));
			os.write((int)(v >>> 56));
		}
	}

	public static long decodeVariableUnsignedLong(InputContext ctx) throws IOException {
		long v = ctx.safeReadLong();
		
		if ((v & 0x80L) != 0) {
			v = (v & 0x7FL) | (ctx.safeReadLong() << 7);
			
			if ((v & 0x4000L) != 0) {
				v = (v & 0x3FFFL) | (ctx.safeReadLong() << 14);
				
				if ((v & 0x200000L) != 0) {
					v = (v & 0x1FFFFFL) | (ctx.safeReadLong() << 21);
					
					if ((v & 0x10000000L) != 0) {
						v = (v & 0xFFFFFFFL) | (ctx.safeReadLong() << 28);
						
						if ((v & 0x800000000L) != 0) {
							v = (v & 0x7FFFFFFFFL) | (ctx.safeReadLong() << 35);
							
							if ((v & 0x40000000000L) != 0) {
								v = (v & 0x3FFFFFFFFFFL) | (ctx.safeReadLong() << 42);

								if ((v & 0x2000000000000L) != 0) {
									v = (v & 0x1FFFFFFFFFFFFL) | (ctx.safeReadLong() << 49);

									if ((v & 0x100000000000000L) != 0)
										v = (v & 0xFFFFFFFFFFFFFFL) | (ctx.safeReadLong() << 56);
									else
										v += 0x2040810204080L;
								}
								else
									v += 0x40810204080L;
							}
							else
								v += 0x810204080L;
						}
						else
							v += 0x10204080L;
					}
					else
						v += 0x204080L;
				}
				else
					v += 0x4080L;
			}
			else
				v += 0x80L;
		}
		
		return v;
	}
}
