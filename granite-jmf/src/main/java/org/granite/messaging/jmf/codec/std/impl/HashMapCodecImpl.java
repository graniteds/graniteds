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
import java.util.HashMap;
import java.util.Map;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.JMFEncodingException;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.HashMapCodec;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;

/**
 * @author Franck WOLFF
 */
public class HashMapCodecImpl extends AbstractStandardCodec<HashMap<?, ?>> implements HashMapCodec {

	protected static final int INDEX_OR_LENGTH_BYTE_COUNT_OFFSET = 5;

	public int getObjectType() {
		return JMF_HASH_MAP;
	}

	public Class<?> getObjectClass() {
		return HashMap.class;
	}

	public void encode(OutputContext ctx, HashMap<?, ?> v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		int indexOfStoredObject = ctx.indexOfObject(v);
		if (indexOfStoredObject >= 0) {
			int count = IntegerUtil.significantIntegerBytesCount0(indexOfStoredObject);
			os.write(0x80 | (count << INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) | JMF_HASH_MAP);
			IntegerUtil.encodeInteger(ctx, indexOfStoredObject, count);
		}
		else {
			ctx.addToObjects(v);
			
			Map.Entry<?, ?>[] snapshot = v.entrySet().toArray(new Map.Entry<?, ?>[0]);
			
			int count = IntegerUtil.significantIntegerBytesCount0(snapshot.length);
			os.write((count << INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) | JMF_HASH_MAP);
			IntegerUtil.encodeInteger(ctx, snapshot.length, count);
			
			for (Map.Entry<?, ?> entry : snapshot) {
				ctx.writeObject(entry.getKey());
				ctx.writeObject(entry.getValue());
			}
		}
	}

	public HashMap<?, ?> decode(InputContext ctx, int parameterizedJmfType) throws IOException, ClassNotFoundException {
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0)
			return (HashMap<?, ?>)ctx.getObject(indexOrLength);

		HashMap<Object, Object> v = new HashMap<Object, Object>(indexOrLength);
		ctx.addToObjects(v);
		
		for (int index = 0; index < indexOrLength; index++) {
			Object key = ctx.readObject();
			Object value = ctx.readObject();
			v.put(key, value);
		}
				
		return v;
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_HASH_MAP)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0) {
			String v = (String)ctx.getObject(indexOrLength);
			ctx.indentPrintLn("<" + v + "@" + indexOrLength + ">");
			return;
		}

		String v = HashMap.class.getName() + "[" + indexOrLength + "]";
		int indexOfStoredObject = ctx.addToObjects(v);
		ctx.indentPrintLn(v + "@" + indexOfStoredObject + " {");
		ctx.incrIndent(1);
		
		for (int index = 0; index < indexOrLength; index++) {
			parameterizedJmfType = ctx.safeRead();
			jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
			StandardCodec<?> codec = codecRegistry.getCodec(jmfType);
			
			if (codec == null)
				throw new JMFEncodingException("No codec for JMF type: " + jmfType);
			
			codec.dump(ctx, parameterizedJmfType);

			ctx.incrIndent(1);
			parameterizedJmfType = ctx.safeRead();
			jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
			codec = codecRegistry.getCodec(jmfType);
			
			if (codec == null)
				throw new JMFEncodingException("No codec for JMF type: " + jmfType);
			
			codec.dump(ctx, parameterizedJmfType);
			ctx.incrIndent(-1);
		}
			
		ctx.incrIndent(-1);
		ctx.indentPrintLn("}");
	}
}
