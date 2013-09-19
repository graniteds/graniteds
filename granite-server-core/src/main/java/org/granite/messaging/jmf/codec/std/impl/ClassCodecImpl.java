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
import java.lang.reflect.InvocationTargetException;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.ClassCodec;

public class ClassCodecImpl extends AbstractIntegerStringCodec<Object> implements ClassCodec {

	protected static final StringTypeHandler TYPE_HANDLER = new StringTypeHandler() {

		public int type(IntegerComponents ics, boolean reference) {
			return (reference ? (0x40 | (ics.length << 4) | JMF_CLASS) : ((ics.length << 4) | JMF_CLASS));
		}

		public int indexOrLengthBytesCount(int parameterizedJmfType) {
			return (parameterizedJmfType >> 4) & 0x03;
		}

		public boolean isReference(int parameterizedJmfType) {
			return (parameterizedJmfType & 0x40) != 0;
		}
	};

	public int getObjectType() {
		return JMF_CLASS;
	}

	public boolean canEncode(Object v) {
		return (v instanceof Class);
	}

	public void encode(OutputContext ctx, Object v) throws IOException, IllegalAccessException {
		writeString(ctx, ctx.getAlias(((Class<?>)v).getName()), TYPE_HANDLER);
	}

	public Object decode(InputContext ctx, int parameterizedJmfType)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_CLASS)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		String className = readString(ctx, parameterizedJmfType, TYPE_HANDLER);
		className = ctx.getAlias(className);
		return ctx.getReflection().loadClass(className);
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_CLASS)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		String className = readString(ctx, parameterizedJmfType, TYPE_HANDLER);
		className = ctx.getAlias(className);
		ctx.indentPrintLn(Class.class.getName() + ": " + className + ".class");
	}
}
