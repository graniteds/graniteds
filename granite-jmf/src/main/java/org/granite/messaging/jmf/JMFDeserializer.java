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
package org.granite.messaging.jmf;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.annotations.Include;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.reflect.ClassDescriptor;
import org.granite.messaging.reflect.NoopWritableProperty;
import org.granite.messaging.reflect.Property;
import org.granite.messaging.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class JMFDeserializer implements InputContext {
	
	///////////////////////////////////////////////////////////////////////////
	// Fields
	
	protected final List<String> classNames = new ArrayList<String>(256);
	protected final List<String> strings = new ArrayList<String>(256);
	protected final List<Object> objects = new ArrayList<Object>(256);
	
	protected final Map<String, ClassDescriptor> classDescriptors = new HashMap<String, ClassDescriptor>(256);

    protected final InputStream inputStream;
    protected final SharedContext context;
    
    protected final CodecRegistry codecRegistry;
	
	///////////////////////////////////////////////////////////////////////////
	// Initialization
	
	public JMFDeserializer(InputStream is, SharedContext context) {
		this.inputStream = is;
		this.context = context;
		this.codecRegistry = context.getCodecRegistry();
		
		this.classNames.addAll(context.getInitialClassNameDictionary());
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
		
		StandardCodec<?> codec = codecRegistry.getCodec(parameterizedJmfType);
		if (codec == null)
			throw new JMFEncodingException("Unsupported JMF type: " + codecRegistry.extractJmfType(parameterizedJmfType));
		
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
		return readByte();
	}

	@Deprecated
	public int read(byte[] b) throws IOException {
		try {
			byte[] bytes = (byte[])readObject();
			int count = Math.min(bytes.length, b.length);
			System.arraycopy(bytes, 0, b, 0, count);
			return count;
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException("Could not read byte array", e);
		}
	}

	@Deprecated
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			byte[] bytes = (byte[])readObject();
			int count = Math.min(bytes.length, len);
			System.arraycopy(bytes, 0, b, off, count);
			return count;
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException("Could not read byte array", e);
		}
	}
	
	@Deprecated
	public void readFully(byte[] b) throws IOException {
		try {
			byte[] bytes = (byte[])readObject();
			if (bytes.length < b.length)
				throw new IOException("Could not fully read byte array of length: " + b.length);
			System.arraycopy(bytes, 0, b, 0, b.length);
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException("Could not read byte array", e);
		}
	}

	@Deprecated
	public void readFully(byte[] b, int off, int len) throws IOException {
		try {
			byte[] bytes = (byte[])readObject();
			if (bytes.length < len)
				throw new IOException("Could not fully read byte array of length: " + len);
			System.arraycopy(bytes, 0, b, off, len);
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException("Could not read byte array", e);
		}
	}
	
	@Deprecated
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
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

	@SuppressWarnings("cast")
	public long safeReadLong() throws IOException {
		return (long)safeRead();
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

	@Override
	public int addToClassNames(String cn) {
		int index = classNames.size();
		classNames.add(index, cn);
		return index;
	}

	@Override
	public String getClassName(int index) {
		return classNames.get(index);
	}

	@Override
	public ClassDescriptor getClassDescriptor(String className) throws ClassNotFoundException {
		ClassDescriptor desc = classDescriptors.get(className);
		if (desc == null) {
			Class<?> cls = context.getReflection().loadClass(className);
			desc = context.getReflection().getDescriptor(cls);
			classDescriptors.put(className, desc);
		}
		return desc;
	}

	@Override
	public ClassDescriptor getClassDescriptor(Class<?> cls) {
		ClassDescriptor desc = classDescriptors.get(cls.getName());
		if (desc == null) {
			desc = context.getReflection().getDescriptor(cls);
			classDescriptors.put(cls.getName(), desc);
		}
		return desc;
	}

	public int addToStrings(String s) {
		int index = strings.size();
		strings.add(index, s);
		return index;
	}
	
	public String getString(int index) {
		return strings.get(index);
	}
	
	public int addToObjects(Object o) {
		int index = objects.size();
		objects.add(index, o);
		return index;
	}
	
	public Object getObject(int index) {
		Object o = objects.get(index);
		if (o instanceof UnresolvedSharedObject)
			throw new JMFUnresolvedSharedObjectException("Unresolved shared object: " + o);
		return o;
	}
	
	public int addToUnresolvedObjects(String className) {
		int index = objects.size();
		objects.add(index, new UnresolvedSharedObject(className, index));
		return index;
	}
	
	public Object setUnresolvedObject(int index, Object o) {
		Object uso = objects.set(index, o);
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
	
	///////////////////////////////////////////////////////////////////////////
	// Debug

	public String toDumpString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(" {\n");
		
		sb.append("    storedClassNames=[\n");
		for (int i = 0; i < classNames.size(); i++)
			sb.append("        ").append(i).append(": \"").append(classNames.get(i)).append("\"\n");
		sb.append("    ],\n");
		
		
		sb.append("    storedStrings=[\n");
		for (int i = 0; i < strings.size(); i++)
			sb.append("        ").append(i).append(": \"").append(strings.get(i)).append("\"\n");
		sb.append("    ],\n");
		
		sb.append("    storedObjects=[\n");
		for (int i = 0; i < objects.size(); i++) {
			Object o = objects.get(i);
			sb.append("        ").append(i).append(": ").append(o.getClass().getName())
			  .append("@").append(System.identityHashCode(o)).append("\n");
		}
		sb.append("    ],\n");
		
		sb.append("}");
		return sb.toString();
	}
}
