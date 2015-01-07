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

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.std.EnumCodec;
import org.granite.messaging.jmf.codec.std.impl.util.ClassNameUtil;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;

/**
 * @author Franck WOLFF
 */
public class EnumCodecImpl extends AbstractStandardCodec<Object> implements EnumCodec {

	protected static final int ORDINAL_BYTE_COUNT_OFFSET = 6;

	public int getObjectType() {
		return JMF_ENUM;
	}

	public boolean canEncode(Object v) {
		return v.getClass().isEnum();
	}

	public void encode(OutputContext ctx, Object v) throws IOException {
		String className = ctx.getAlias(v.getClass().getName());
		int ordinal = ((Enum<?>)v).ordinal();
		int count = IntegerUtil.significantIntegerBytesCount0(ordinal);
		
		ctx.getOutputStream().write((count << ORDINAL_BYTE_COUNT_OFFSET) | JMF_ENUM);
		IntegerUtil.encodeInteger(ctx, ordinal, count);
		ClassNameUtil.encodeClassName(ctx, className);
	}

	public Object decode(InputContext ctx, int parameterizedJmfType) throws IOException, ClassNotFoundException {
		int count = (parameterizedJmfType >>> ORDINAL_BYTE_COUNT_OFFSET) /* & 0x03 */;
		int ordinal = IntegerUtil.decodeInteger(ctx, count);
		String className = ctx.getAlias(ClassNameUtil.decodeClassName(ctx));
		Class<?> cls = ctx.getSharedContext().getReflection().loadClass(className);

		return cls.getEnumConstants()[ordinal];
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_ENUM)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		int count = (parameterizedJmfType >>> ORDINAL_BYTE_COUNT_OFFSET) /* & 0x03 */;
		int ordinal = IntegerUtil.decodeInteger(ctx, count);
		String className = ctx.getAlias(ClassNameUtil.decodeClassName(ctx));
		
		ctx.indentPrintLn(className + ": <" + ordinal + ">");
	}
}
