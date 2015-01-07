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
import java.lang.reflect.InvocationTargetException;

import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.ClassCodec;
import org.granite.messaging.jmf.codec.std.impl.util.ClassNameUtil;

/**
 * @author Franck WOLFF
 */
public class ClassCodecImpl extends AbstractStandardCodec<Object> implements ClassCodec {

	public int getObjectType() {
		return JMF_CLASS;
	}

	public boolean canEncode(Object v) {
		return (v instanceof Class);
	}

	public void encode(OutputContext ctx, Object v) throws IOException, IllegalAccessException {
		String className = ((Class<?>)v).getName();
		className = ctx.getAlias(className);

		ctx.getOutputStream().write(JMF_CLASS);
		ClassNameUtil.encodeClassName(ctx, className);
	}

	public Object decode(InputContext ctx, int parameterizedJmfType)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {

		String className = ClassNameUtil.decodeClassName(ctx);
		className = ctx.getAlias(className);
		return ctx.getReflection().loadClass(className);
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_CLASS)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		String className = ClassNameUtil.decodeClassName(ctx);
		className = ctx.getAlias(className);
		ctx.indentPrintLn(Class.class.getName() + ": " + className + ".class");
	}
}
