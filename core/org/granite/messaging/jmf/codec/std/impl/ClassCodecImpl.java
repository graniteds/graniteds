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

	public boolean accept(Object v) {
		return (v instanceof Class);
	}

	public void encode(OutputContext ctx, Object v) throws IOException, IllegalAccessException {
		writeString(ctx, ((Class<?>)v).getName(), TYPE_HANDLER);
	}

	public Object decode(InputContext ctx, int parameterizedJmfType)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_CLASS)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		String className = readString(ctx, parameterizedJmfType, TYPE_HANDLER);
		return ctx.getReflection().loadClass(className);
	}

	public void dump(DumpContext ctx, int parameterizedJmfType) throws IOException {
		int jmfType = ctx.getSharedContext().getCodecRegistry().extractJmfType(parameterizedJmfType);
		
		if (jmfType != JMF_CLASS)
			throw newBadTypeJMFEncodingException(jmfType, parameterizedJmfType);
		
		String className = readString(ctx, parameterizedJmfType, TYPE_HANDLER);
		ctx.indentPrintLn(Class.class.getName() + ": " + className + ".class");
	}
}
