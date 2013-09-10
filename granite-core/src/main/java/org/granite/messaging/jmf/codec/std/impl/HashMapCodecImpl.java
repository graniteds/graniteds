/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
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

/**
 * @author Franck WOLFF
 */
public class HashMapCodecImpl extends AbstractIntegerStringCodec<HashMap<?, ?>> implements HashMapCodec {

	public int getObjectType() {
		return JMF_HASH_MAP;
	}

	public Class<?> getObjectClass() {
		return HashMap.class;
	}

	public void encode(OutputContext ctx, HashMap<?, ?> v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		int indexOfStoredObject = ctx.indexOfStoredObjects(v);
		if (indexOfStoredObject >= 0) {
			IntegerComponents ics = intComponents(indexOfStoredObject);
			os.write(0x80 | (ics.length << 5) | JMF_HASH_MAP);
			writeIntData(ctx, ics);
		}
		else {
			ctx.addToStoredObjects(v);
			
			Map.Entry<?, ?>[] snapshot = v.entrySet().toArray(new Map.Entry<?, ?>[0]);
			
			IntegerComponents ics = intComponents(snapshot.length);
			os.write((ics.length << 5) | JMF_HASH_MAP);
			writeIntData(ctx, ics);
			
			for (Map.Entry<?, ?> entry : snapshot) {
				ctx.writeObject(entry.getKey());
				ctx.writeObject(entry.getValue());
			}
		}
	}

	public HashMap<?, ?> decode(InputContext ctx, int parameterizedJmfType) throws IOException, ClassNotFoundException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_HASH_MAP)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		final int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 5) & 0x03, false);
		if ((parameterizedJmfType & 0x80) != 0)
			return (HashMap<?, ?>)ctx.getSharedObject(indexOrLength);

		HashMap<Object, Object> v = new HashMap<Object, Object>(indexOrLength);
		ctx.addSharedObject(v);
		
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
		
		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 5) & 0x03, false);
		if ((parameterizedJmfType & 0x80) != 0) {
			String v = (String)ctx.getSharedObject(indexOrLength);
			ctx.indentPrintLn("<" + v + "@" + indexOrLength + ">");
			return;
		}

		String v = HashMap.class.getName() + "[" + indexOrLength + "]";
		int indexOfStoredObject = ctx.addSharedObject(v);
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
