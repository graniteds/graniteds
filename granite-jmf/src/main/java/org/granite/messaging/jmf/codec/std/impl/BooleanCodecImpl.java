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

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.BooleanCodec;

/**
 * @author Franck WOLFF
 */
public class BooleanCodecImpl extends AbstractStandardCodec<Boolean> implements BooleanCodec {

	public int getObjectType() {
		return JMF_BOOLEAN_OBJECT;
	}

	public Class<?> getObjectClass() {
		return Boolean.class;
	}

	public int getPrimitiveType() {
		return JMF_BOOLEAN;
	}

	public Class<?> getPrimitiveClass() {
		return boolean.class;
	}

	public void encode(OutputContext ctx, Boolean v) throws IOException {
		if (v.booleanValue())
			ctx.getOutputStream().write(0x80 | JMF_BOOLEAN_OBJECT);
		else
			ctx.getOutputStream().write(JMF_BOOLEAN_OBJECT);
	}
	
	public Boolean decode(InputContext ctx, int parameterizedJmfType) throws IOException {
		return Boolean.valueOf(((parameterizedJmfType & 0x80) != 0));
	}

	public void encodePrimitive(OutputContext ctx, boolean v) throws IOException {
		if (v)
			ctx.getOutputStream().write(0x80 | JMF_BOOLEAN);
		else
			ctx.getOutputStream().write(JMF_BOOLEAN);
	}
	
	public boolean decodePrimitive(InputContext ctx) throws IOException {
		int parameterizedJmfType = ctx.safeRead();
		return ((parameterizedJmfType & 0x80) != 0);
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		switch (jmfType) {
			case JMF_BOOLEAN:
				ctx.indentPrintLn("boolean: " + ((parameterizedJmfType & 0x80) != 0));
				break;
			case JMF_BOOLEAN_OBJECT:
				ctx.indentPrintLn(Boolean.class.getName() + ": " + ((parameterizedJmfType & 0x80) != 0));
				break;
			default:
				throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		}
	}
}
