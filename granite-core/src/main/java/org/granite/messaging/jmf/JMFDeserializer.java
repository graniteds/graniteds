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

package org.granite.messaging.jmf;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.annotations.Include;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.reflect.NoopWritableProperty;
import org.granite.messaging.reflect.Property;
import org.granite.messaging.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class JMFDeserializer implements InputContext {
	
	///////////////////////////////////////////////////////////////////////////
	// Fields
	
	protected final List<String> storedStrings = new ArrayList<String>(256);
	protected final List<Object> storedObjects = new ArrayList<Object>(256);
	
    protected final InputStream inputStream;
    protected final SharedContext context;
    
    protected final CodecRegistry codecRegistry;
	
	///////////////////////////////////////////////////////////////////////////
	// Initialization
	
	public JMFDeserializer(InputStream is, SharedContext context) {
		this.inputStream = is;
		this.context = context;
		this.codecRegistry = context.getCodecRegistry();
		
		this.storedStrings.addAll(context.getDefaultStoredStrings());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ObjectInput implementation

	public boolean readBoolean() throws IOException {
		return codecRegistry.getBooleanCodec().decodePrimitive(this);
	}

	public byte readByte() throws IOException {
		return codecRegistry.getByteCodec().decodePrimitive(this);
	}

	public int readUnsignedByte() throws IOException {
		return readByte() & 0xFF;
	}

	public short readShort() throws IOException {
		return codecRegistry.getShortCodec().decodePrimitive(this);
	}

	public int readUnsignedShort() throws IOException {
		return readShort() & 0xFFFF;
	}

	public char readChar() throws IOException {
		return codecRegistry.getCharacterCodec().decodePrimitive(this);
	}

	public int readInt() throws IOException {
		return codecRegistry.getIntegerCodec().decodePrimitive(this);
	}

	public long readLong() throws IOException {
		return codecRegistry.getLongCodec().decodePrimitive(this);
	}

	public float readFloat() throws IOException {
		return codecRegistry.getFloatCodec().decodePrimitive(this);
	}

	public double readDouble() throws IOException {
		return codecRegistry.getDoubleCodec().decodePrimitive(this);
	}

	public String readUTF() throws IOException {
		int parameterizedJmfType = safeRead();
		
		if (parameterizedJmfType == JMF_NULL)
			return (String)codecRegistry.getNullCodec().decode(this, parameterizedJmfType);
		
		return codecRegistry.getStringCodec().decode(this, parameterizedJmfType);
	}

	public Object readObject() throws ClassNotFoundException, IOException {
		int parameterizedJmfType = safeRead();
		int jmfType = codecRegistry.extractJmfType(parameterizedJmfType);
		
		StandardCodec<?> codec = codecRegistry.getCodec(jmfType);
		if (codec == null)
			throw new JMFEncodingException("Unsupported JMF type: " + jmfType);
		
		try {
			return codec.decode(this, parameterizedJmfType);
		}
		catch (InvocationTargetException e) {
			throw new IOException(e.getTargetException());
		}
		catch (IllegalAccessException e) {
			throw new IOException(e);
		}
		catch (InstantiationException e) {
			throw new IOException(e);
		}
		catch (NoSuchMethodException e) {
			throw new IOException(e);
		}
	}

	public int available() throws IOException {
		return inputStream.available();
	}

	public void close() throws IOException {
		inputStream.close();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ObjectInput implementation (unsupported, marked at deprecated)

	@Deprecated
	public int read() throws IOException {
		throw new UnsupportedOperationException("Use readByte()");
	}

	@Deprecated
	public int read(byte[] b) throws IOException {
		throw new UnsupportedOperationException("Use (byte[])readObject()");
	}

	@Deprecated
	public int read(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException("Use (byte[])readObject()");
	}
	
	@Deprecated
	public void readFully(byte[] b) throws IOException {
		throw new UnsupportedOperationException("Use (byte[])readObject()");
	}

	@Deprecated
	public void readFully(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException("Use (byte[])readObject()");
	}
	
	@Deprecated
	public String readLine() throws IOException {
		throw new UnsupportedOperationException("Use readUTF()");
	}

	@Deprecated
	public int skipBytes(int n) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public long skip(long n) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// InputContext implementation

	public SharedContext getSharedContext() {
		return context;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}

	public int safeRead() throws IOException {
		int b = inputStream.read();
		if (b == -1)
			throw new EOFException();
		return b;
	}
	
	public void safeReadFully(byte[] b) throws IOException {
		safeReadFully(b, 0, b.length);
	}

	public void safeReadFully(byte[] b, int off, int len) throws IOException {
		if (off < 0 || len < 0 || off + len > b.length)
			throw new IndexOutOfBoundsException("b.length=" + b.length + ", off=" + off + ", len" + len);
		
		if (len == 0)
			return;
		
		do {
			int read = inputStream.read(b, off, len);
			if (read == -1)
				throw new EOFException();
			off += read;
			len -= read;
		}
		while (len > 0);
	}

	public void safeSkip(long n) throws IOException {
		while (n > 0) {
			if (inputStream.read() == -1)
				throw new EOFException();
			n--;
		}
	}

	public int addSharedString(String s) {
		int index = storedStrings.size();
		storedStrings.add(index, s);
		return index;
	}
	
	public String getSharedString(int index) {
		return storedStrings.get(index);
	}
	
	public int addSharedObject(Object o) {
		int index = storedObjects.size();
		storedObjects.add(index, o);
		return index;
	}
	
	public Object getSharedObject(int index) {
		Object o = storedObjects.get(index);
		if (o instanceof UnresolvedSharedObject)
			throw new JMFUnresolvedSharedObjectException("Unresolved shared object: " + o);
		return o;
	}
	
	public int addUnresolvedSharedObject(String className) {
		int index = storedObjects.size();
		storedObjects.add(index, new UnresolvedSharedObject(className, index));
		return index;
	}
	
	public Object setUnresolvedSharedObject(int index, Object o) {
		Object uso = storedObjects.set(index, o);
		if (!(uso instanceof UnresolvedSharedObject))
			throw new JMFUnresolvedSharedObjectException("Not an unresolved shared object: " + uso);
		return uso;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ExtendedObjectInput implementation

	public Reflection getReflection() {
		return context.getReflection();
	}

	public String getAlias(String remoteAlias) {
		return context.getClassName(remoteAlias);
	}

	public void readAndSetProperty(Object obj, Property property) throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		if (property.isAnnotationPresent(Include.class) && !property.isWritable())
			property = new NoopWritableProperty(property.getName(), property.getType());
		
		if (property.getType().isPrimitive())
			codecRegistry.getPrimitivePropertyCodec(property.getType()).decodePrimitive(this, obj, property);
		else
			property.setObject(obj, readObject());
	}
	
	static class UnresolvedSharedObject {
		
		private final String className;
		private final int index;

		public UnresolvedSharedObject(String className, int index) {
			this.className = className;
			this.index = index;
		}

		public String getClassName() {
			return className;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public String toString() {
			return UnresolvedSharedObject.class.getName() + " {className=" + className + ", index=" + index + "}";
		}
	}
}
