/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.reflect.Property;
import org.granite.messaging.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class JMFSerializer implements OutputContext {
	
	///////////////////////////////////////////////////////////////////////////
	// Fields

	protected final Map<String, Integer> classNames = new HashMap<String, Integer>(256);
	protected final Map<String, Integer> strings = new HashMap<String, Integer>(256);
	protected final Map<Object, Integer> objects = new IdentityHashMap<Object, Integer>(256);
	
    protected final OutputStream outputStream;
    protected final SharedContext context;
    
    protected final CodecRegistry codecRegistry;
	
	///////////////////////////////////////////////////////////////////////////
	// Initialization

	public JMFSerializer(OutputStream outputStream, SharedContext context) {
		this.outputStream = outputStream;
		this.codecRegistry = context.getCodecRegistry();
		this.context = context;
		
		for (String s : context.getInitialClassNameDictionary())
			addToClassNames(s);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ObjectOutput implementation

	public void writeBoolean(boolean v) throws IOException {
		codecRegistry.getBooleanCodec().encodePrimitive(this, v);
	}

	public void writeByte(int v) throws IOException {
		codecRegistry.getByteCodec().encodePrimitive(this, v);
	}

	public void writeShort(int v) throws IOException {
		codecRegistry.getShortCodec().encodePrimitive(this, v);
	}

	public void writeChar(int v) throws IOException {
		codecRegistry.getCharacterCodec().encodePrimitive(this, v);
	}
	
	public void writeInt(int v) throws IOException {
		codecRegistry.getIntegerCodec().encodePrimitive(this, v);
	}

	public void writeLong(long v) throws IOException {
		codecRegistry.getLongCodec().encodePrimitive(this, v);
	}

	public void writeFloat(float v) throws IOException {
		codecRegistry.getFloatCodec().encodePrimitive(this, v);
	}

	public void writeDouble(double v) throws IOException {
		codecRegistry.getDoubleCodec().encodePrimitive(this, v);
	}

	public void writeUTF(String s) throws IOException {
		if (s == null)
			codecRegistry.getNullCodec().encode(this, s);
		else
			codecRegistry.getStringCodec().encode(this, s);
	}

	public void writeObject(Object obj) throws IOException {
		StandardCodec<Object> codec = codecRegistry.getCodec(obj);
		if (codec == null)
			throw new JMFEncodingException("Unsupported Java class: " + obj);
		
		try {
			codec.encode(this, obj);
		}
		catch (IllegalAccessException e) {
			throw new IOException(e);
		}
		catch (InvocationTargetException e) {
			throw new IOException(e);
		}
	}

	public void flush() throws IOException {
		outputStream.flush();
	}

	public void close() throws IOException {
		outputStream.close();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ObjectOutput implementation (unsupported, marked at deprecated)

	@Deprecated
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException("Use writeByte(b)");
	}

	@Deprecated
	public void write(byte[] b) throws IOException {
		throw new UnsupportedOperationException("Use writeObject(b)");
	}

	@Deprecated
	public void write(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException("Use writeObject(Arrays.copyOfRange(b, off, off+len))");
	}

	@Deprecated
	public void writeBytes(String s) throws IOException {
		throw new UnsupportedOperationException("Use writeUTF(s)");
	}

	@Deprecated
	public void writeChars(String s) throws IOException {
		throw new UnsupportedOperationException("Use writeUTF(s)");
	}
	
	///////////////////////////////////////////////////////////////////////////
	// OutputContext implementation

	public SharedContext getSharedContext() {
		return context;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public void addToClassNames(String className) {
        if (className != null && !classNames.containsKey(className)) {
            Integer index = Integer.valueOf(classNames.size());
            classNames.put(className, index);
        }
	}

	@Override
	public int indexOfClassName(String className) {
    	if (className != null) {
	        Integer index = classNames.get(className);
	        if (index != null)
	        	return index.intValue();
    	}
    	return -1;
	}

	public void addToStrings(String s) {
        if (s != null && !strings.containsKey(s)) {
            Integer index = Integer.valueOf(strings.size());
            strings.put(s, index);
        }
    }

	public int indexOfString(String s) {
    	if (s != null) {
	        Integer index = strings.get(s);
	        if (index != null)
	        	return index.intValue();
    	}
    	return -1;
    }

	public void addToObjects(Object o) {
        if (o != null && !objects.containsKey(o)) {
            Integer index = Integer.valueOf(objects.size());
            objects.put(o, index);
        }
    }

	public int indexOfObject(Object o) {
    	if (o != null) {
	        Integer index = objects.get(o);
	        if (index != null)
	        	return index.intValue();
    	}
		return -1;
    }
	
	///////////////////////////////////////////////////////////////////////////
	// ExtendedObjectOutput implementation

	public Reflection getReflection() {
		return context.getReflection();
	}

	public String getAlias(String className) {
		return context.getRemoteAlias(className);
	}

	public void getAndWriteProperty(Object obj, Property property) throws IOException, IllegalAccessException, InvocationTargetException {
		if (property.getType().isPrimitive())
			codecRegistry.getPrimitivePropertyCodec(property.getType()).encodePrimitive(this, obj, property);
		else
			writeObject(property.getObject(obj));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Debug

	public String toDumpString() {
		final Comparator<Map.Entry<?, Integer>> comparator = new Comparator<Map.Entry<?, Integer>>() {
			@Override
			public int compare(Entry<?, Integer> o1, Entry<?, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		};
		
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(" {\n");
		
		TreeSet<Map.Entry<String, Integer>> setStringInteger = new TreeSet<Map.Entry<String, Integer>>(comparator);
		setStringInteger.addAll(classNames.entrySet());
		sb.append("    classNames=[\n");
		for (Map.Entry<String, Integer> entry : setStringInteger)
			sb.append("        ").append(entry.getValue()).append(": \"").append(entry.getKey()).append("\"\n");
		sb.append("    ],\n");
		
		setStringInteger = new TreeSet<Map.Entry<String, Integer>>(comparator);
		setStringInteger.addAll(strings.entrySet());
		sb.append("    strings=[\n");
		for (Map.Entry<String, Integer> entry : setStringInteger)
			sb.append("        ").append(entry.getValue()).append(": \"").append(entry.getKey()).append("\"\n");
		sb.append("    ],\n");
		
		TreeSet<Map.Entry<Object, Integer>> setObjectInteger = new TreeSet<Map.Entry<Object, Integer>>(comparator);
		setObjectInteger.addAll(objects.entrySet());
		sb.append("    objects=[\n");
		for (Map.Entry<Object, Integer> entry : setObjectInteger) {
			sb.append("        ").append(entry.getValue()).append(": ").append(entry.getKey().getClass().getName())
			  .append("@").append(System.identityHashCode(entry.getKey())).append("\n");
		}
		sb.append("    ]\n");
		
		sb.append("}");
		return sb.toString();
	}
}
