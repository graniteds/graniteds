package org.granite.messaging.jmf.codec.std.impl;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.SortedSet;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DumpContext;
import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.JMFEncodingException;
import org.granite.messaging.jmf.OutputContext;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.GenericCollectionCodec;
import org.granite.messaging.jmf.codec.std.impl.util.ClassNameUtil;
import org.granite.messaging.jmf.codec.std.impl.util.IntegerUtil;
import org.granite.messaging.reflect.ClassDescriptor;

public class GenericCollectionCodecImpl extends AbstractStandardCodec<Object> implements GenericCollectionCodec {
	
	protected static final int INDEX_OR_LENGTH_BYTE_COUNT_OFFSET = 5;

	@Override
	public int getObjectType() {
		return JMF_GENERIC_COLLECTION;
	}

	@Override
	public boolean canEncode(Object v) {
		return (v instanceof Collection) && !(v instanceof EnumSet);
	}

	@Override
	public void encode(OutputContext ctx, Object v) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		int indexOfStoredObject = ctx.indexOfObject(v);
		if (indexOfStoredObject >= 0) {
			int count = IntegerUtil.significantIntegerBytesCount0(indexOfStoredObject);
			os.write(0x80 | (count << INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) | JMF_GENERIC_COLLECTION);
			IntegerUtil.encodeInteger(ctx, indexOfStoredObject, count);
		}
		else {
			ctx.addToObjects(v);
			
			Collection<?> coll = (Collection<?>)v;
			Object[] snapshot = coll.toArray();
			
			int count = IntegerUtil.significantIntegerBytesCount0(snapshot.length);
			os.write((count << INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) | JMF_GENERIC_COLLECTION);
			IntegerUtil.encodeInteger(ctx, snapshot.length, count);

			String className = ctx.getAlias(
				SortedSet.class.isAssignableFrom(v.getClass())
				? HashSet.class.getName()
				: v.getClass().getName()
			);
			ClassNameUtil.encodeClassName(ctx, className);

			for (Object element : snapshot)
				ctx.writeObject(element);
		}
	}

	@Override
	public Object decode(InputContext ctx, int parameterizedJmfType)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, SecurityException, NoSuchMethodException {
		
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0)
			return ctx.getObject(indexOrLength);

		String className = ctx.getAlias(ClassNameUtil.decodeClassName(ctx));
		
		ClassDescriptor desc = ctx.getClassDescriptor(className);
		Class<?> cls = desc.getCls();
		
		if (!Serializable.class.isAssignableFrom(cls))
			throw new NotSerializableException(cls.getName());

		@SuppressWarnings("unchecked")
		Collection<Object> v = (Collection<Object>)desc.newInstance();
		ctx.addToObjects(v);
		
		for (int index = 0; index < indexOrLength; index++)
			v.add(ctx.readObject());

		return v;
	}

	@Override
	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		final CodecRegistry codecRegistry = ctx.getSharedContext().getCodecRegistry();
		
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_GENERIC_COLLECTION)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		int indexOrLength = IntegerUtil.decodeInteger(ctx, (parameterizedJmfType >>> INDEX_OR_LENGTH_BYTE_COUNT_OFFSET) & 0x03);
		if ((parameterizedJmfType & 0x80) != 0) {
			String v = (String)ctx.getObject(indexOrLength);
			ctx.indentPrintLn("<" + v + "@" + indexOrLength + ">");
			return;
		}
		
		String className = ClassNameUtil.decodeClassName(ctx);
		className = ctx.getAlias(className);
		
		String v = className + "[" + indexOrLength + "]";
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

		}
			
		ctx.incrIndent(-1);
		ctx.indentPrintLn("}");
	}
}
