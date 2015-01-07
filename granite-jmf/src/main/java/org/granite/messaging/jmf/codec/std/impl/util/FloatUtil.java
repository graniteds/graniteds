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
public class FloatUtil {

	// ------------------------------------------------------------------------
	
	public static void encodeFloat(OutputContext ctx, float v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		int bits = Float.floatToIntBits(v);
		os.write(bits >>> 24);
		os.write(bits >>> 16);
		os.write(bits >>> 8);
		os.write(bits);
	}
	
	public static float decodeFloat(InputContext ctx) throws IOException {
		return Float.intBitsToFloat(
			ctx.safeRead() << 24 |
			ctx.safeRead() << 16 |
			ctx.safeRead() << 8  |
			ctx.safeRead()
		);
	}
}
