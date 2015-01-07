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
import org.granite.messaging.jmf.codec.std.CharacterCodec;

/**
 * @author Franck WOLFF
 */
public class CharacterCodecImpl extends AbstractStandardCodec<Character> implements CharacterCodec {

	public int getObjectType() {
		return JMF_CHARACTER_OBJECT;
	}

	public Class<?> getObjectClass() {
		return Character.class;
	}

	public int getPrimitiveType() {
		return JMF_CHARACTER;
	}

	public Class<?> getPrimitiveClass() {
		return char.class;
	}

	public void encode(OutputContext ctx, Character v) throws IOException {
		writeCharData(ctx, JMF_CHARACTER_OBJECT, v.charValue());
	}
	
	public Character decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		return Character.valueOf(readCharData(ctx, parameterizedJmfType));
	}

	public void encodePrimitive(OutputContext ctx, int v) throws IOException {
		writeCharData(ctx, JMF_CHARACTER, v);
	}
	
	public char decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		return readCharData(ctx, parameterizedJmfType);
	}
	
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
		case JMF_CHARACTER:
			ctx.indentPrintLn("char: '" + escape(readCharData(ctx, parameterizedJmfType)) + "'");
			break;
		case JMF_CHARACTER_OBJECT:
			ctx.indentPrintLn(Character.class.getName() + ": '" + escape(readCharData(ctx, parameterizedJmfType)) + "'");
			break;
		default:
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
	
	protected void writeCharData(OutputContext ctx, int jmfType, int v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		if (v <= 0x00FF) {
			os.write(jmfType);
			os.write(v);
		}
		else {	
			os.write(0x80 | jmfType);
			os.write(v >>> 8);
			os.write(v);
		}
	}
	
	protected char readCharData(InputContext ctx, int parameterizedJmfType) throws IOException {
		char v = (char)ctx.safeRead();
		
		if ((parameterizedJmfType & 0x80) != 0)
			v = (char)((v << 8) | ctx.safeRead());
		
		return v;
	}
}
