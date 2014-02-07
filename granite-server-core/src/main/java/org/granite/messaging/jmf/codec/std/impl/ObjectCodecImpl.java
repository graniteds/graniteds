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

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.JMFEncodingException;
import org.granite.messaging.jmf.JMFObjectInputStream;
import org.granite.messaging.jmf.JMFObjectOutputStream;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.ObjectCodec;
import org.granite.messaging.reflect.ClassDescriptor;
import org.granite.messaging.reflect.Property;

/**
 * @author Franck WOLFF
 */
public class ObjectCodecImpl extends AbstractIntegerStringCodec<Object> implements ObjectCodec {

	protected static final StringTypeHandler TYPE_HANDLER = new StringTypeHandler() {

		public int type(IntegerComponents ics, boolean reference) {
			if (reference)
				return 0x40 | (ics.length << 4) | JMF_OBJECT;
			return (ics.length << 4) | JMF_OBJECT;
		}

		public int indexOrLengthBytesCount(int parameterizedJmfType) {
			return (parameterizedJmfType >> 4) & 0x03;
		}

		public boolean isReference(int parameterizedJmfType) {
			return (parameterizedJmfType & 0x40) != 0;
		}
	};

	public int getObjectType() {
		return JMF_OBJECT;
	}

	public boolean canEncode(Object v) {
		Class<?> cls = v.getClass();
		return !cls.isArray() && !cls.isEnum() && !(v instanceof Class);
	}

	public void encode(OutputContext ctx, Object v) throws IOException, IllegalAccessException, InvocationTargetException {
		final OutputStream os = ctx.getOutputStream();
		
		int indexOfStoredObject = ctx.indexOfStoredObjects(v);
		if (indexOfStoredObject >= 0) {
			IntegerComponents ics = intComponents(indexOfStoredObject);
			os.write(0x80 | (ics.length << 4) | JMF_OBJECT);
			writeIntData(ctx, ics);
		}
		else {			
			if (!(v instanceof Serializable))
				throw new NotSerializableException(v.getClass().getName());
			
			ctx.addToStoredObjects(v);

			ExtendedObjectCodec extendedCodec = ctx.getSharedContext().getCodecRegistry().findExtendedEncoder(ctx, v);
			if (extendedCodec != null) {
				writeString(ctx, extendedCodec.getEncodedClassName(ctx, v), TYPE_HANDLER);
				extendedCodec.encode(ctx, v);
				os.write(JMF_OBJECT_END);
			}
			else {
				ClassDescriptor desc = ctx.getReflection().getDescriptor(v.getClass());
				
				while (desc != null && desc.hasWriteReplaceMethod()) {
					Object replacement = desc.invokeWriteReplaceMethod(v);
					if (replacement == null)
						throw new JMFEncodingException(desc.getCls() + ".writeReplace() method returned null");
					if (replacement.getClass() == v.getClass())
						throw new JMFEncodingException(desc.getCls() + ".writeReplace() method returned an instance of the same class");
					
					ClassDescriptor replacementDesc = ctx.getReflection().getDescriptor(replacement.getClass());
					if (replacementDesc == null || !replacementDesc.hasReadResolveMethod()) {
						throw new JMFEncodingException(
							desc.getCls() +
							".writeReplace() method returned an object that has no readResolve() method: " +
							(replacementDesc == null ? "null" : replacementDesc.getCls())
						);
					}
					
					v = replacement;
					desc = replacementDesc;
				}

				writeString(ctx, ctx.getAlias(v.getClass().getName()), TYPE_HANDLER);
				if (v instanceof Externalizable && !Proxy.isProxyClass(v.getClass()))
					((Externalizable)v).writeExternal(ctx);
				else
					encodeSerializable(ctx, (Serializable)v, desc);
				os.write(JMF_OBJECT_END);
			}
		}
	}
	
	protected void encodeSerializable(OutputContext ctx, Serializable v, ClassDescriptor desc)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		ClassDescriptor parentDesc = desc.getParent(); 
		
		if (parentDesc != null)
			encodeSerializable(ctx, v, parentDesc);
		
		if (desc.hasWriteObjectMethod())
			desc.invokeWriteObjectMethod(new JMFObjectOutputStream(ctx, desc, v), v);
		else {
			for (Property property : desc.getSerializableProperties())
				ctx.getAndWriteProperty(v, property);
		}
	}
	
	public Object decode(InputContext ctx, int parameterizedJmfType)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, SecurityException, NoSuchMethodException {
		
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		if (jmfType != JMF_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		Object v = null;

		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 4) & 0x03, false);
		if ((parameterizedJmfType & 0x80) != 0)
			v = ctx.getSharedObject(indexOrLength);
		else {
			String className = readString(ctx, parameterizedJmfType, indexOrLength, TYPE_HANDLER);
			
			ExtendedObjectCodec extendedCodec = codecRegistry.findExtendedDecoder(ctx, className);
			if (extendedCodec != null) {
				className = extendedCodec.getDecodedClassName(ctx, className);
				int index = ctx.addUnresolvedSharedObject(className);
				v = extendedCodec.newInstance(ctx, className);
				ctx.setUnresolvedSharedObject(index, v);
				extendedCodec.decode(ctx, v);
			}
			else {
				className = ctx.getAlias(className);
				Class<?> cls = ctx.getSharedContext().getReflection().loadClass(className);
				
				if (!Serializable.class.isAssignableFrom(cls))
					throw new NotSerializableException(cls.getName());
				
				v = ctx.getReflection().newInstance(cls);

				ClassDescriptor desc = ctx.getReflection().getDescriptor(cls);
				if (desc == null || !desc.hasReadResolveMethod()) {
					ctx.addSharedObject(v);
					
					if (Externalizable.class.isAssignableFrom(cls))
						((Externalizable)v).readExternal(ctx);
					else
						decodeSerializable(ctx, (Serializable)v);
				}
				else {
					int index = ctx.addUnresolvedSharedObject(className);
					
					if (Externalizable.class.isAssignableFrom(cls))
						((Externalizable)v).readExternal(ctx);
					else
						decodeSerializable(ctx, (Serializable)v);
					
					do {
						Object resolved = desc.invokeReadResolveMethod(v);
						if (resolved == null)
							throw new JMFEncodingException(desc.getCls() + ".readResolve() method returned null");
						if (resolved.getClass() == v.getClass())
							throw new JMFEncodingException(desc.getCls() + ".readResolve() method returned an instance of the same class");
						
						ClassDescriptor resolvedDesc = ctx.getReflection().getDescriptor(resolved.getClass());
						if (resolvedDesc == null || !resolvedDesc.hasWriteReplaceMethod()) {
							throw new JMFEncodingException(
								desc.getCls() +
								".readResolve() method returned an object that has no writeReplace() method: " +
								(resolvedDesc == null ? "null" : resolvedDesc.getCls())
							);
						}
						
						v = resolved;
						desc = resolvedDesc;
					}
					while (desc.hasReadResolveMethod());
					
					ctx.setUnresolvedSharedObject(index, v);
				}
			}
			
			int mark = ctx.safeRead();
			if (mark != JMF_OBJECT_END)
				throw new JMFEncodingException("Not a Object end marker: " + mark);
		}
		
		return v;
	}

	protected void decodeSerializable(InputContext ctx, Serializable v)
		throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {

		ClassDescriptor desc = ctx.getReflection().getDescriptor(v.getClass());
		decodeSerializable(ctx, v, desc);
	}
	
	protected void decodeSerializable(InputContext ctx, Serializable v, ClassDescriptor desc)
		throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {

		ClassDescriptor parentDesc = desc.getParent(); 
		
		if (parentDesc != null)
			decodeSerializable(ctx, v, parentDesc);
		
		if (desc.hasReadObjectMethod())
			desc.invokeReadObjectMethod(new JMFObjectInputStream(ctx, desc, v), v);
		else {
			for (Property property : desc.getSerializableProperties())
				ctx.readAndSetProperty(v, property);
		}
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();

		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_OBJECT)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);

		int indexOrLength = readIntData(ctx, (parameterizedJmfType >> 4) & 0x03, false);
		
		if ((parameterizedJmfType & 0x80) != 0) {
			String className = (String)ctx.getSharedObject(indexOrLength);
			ctx.indentPrintLn("<" + className + "@" + indexOrLength + ">");
		}
		else {
			String className = readString(ctx, parameterizedJmfType, indexOrLength, TYPE_HANDLER);
			int indexOfStoredObject = ctx.addSharedObject(className);
			ctx.indentPrintLn(className + "@" + indexOfStoredObject + " {");
			ctx.incrIndent(1);
			
			while ((parameterizedJmfType = ctx.safeRead()) != JMF_OBJECT_END) {
				jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
				StandardCodec<?> codec = codecRegistry.getCodec(jmfType);
				
				if (codec == null)
					throw new JMFEncodingException("No codec for JMF type: " + jmfType);
				
				codec.dump(ctx, parameterizedJmfType);
			}
			
			ctx.incrIndent(-1);
			ctx.indentPrintLn("}");
		}
	}
}
