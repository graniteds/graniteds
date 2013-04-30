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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.granite.messaging.jmf.codec.StandardCodec;

/**
 * @author Franck WOLFF
 */
public class JMFSerializer implements OutputContext {
	
	///////////////////////////////////////////////////////////////////////////
	// Fields

	protected final Map<String, Integer> storedStrings = new HashMap<String, Integer>(256);
	protected final Map<Object, Integer> storedObjects = new IdentityHashMap<Object, Integer>(256);
	
    protected final OutputStream outputStream;
    protected final SharedContext context;
    
    protected final CodecRegistry codecRegistry;
	
	///////////////////////////////////////////////////////////////////////////
	// Initialization

	public JMFSerializer(OutputStream outputStream, SharedContext context) {
		this.outputStream = outputStream;
		this.codecRegistry = context.getCodecRegistry();
		this.context = context;
		
		for (String s : context.getDefaultStoredStrings())
			addToStoredStrings(s);
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

	public void addToStoredStrings(String s) {
        if (s != null && !storedStrings.containsKey(s)) {
            Integer index = Integer.valueOf(storedStrings.size());
            storedStrings.put(s, index);
        }
    }

	public int indexOfStoredStrings(String s) {
    	if (s != null) {
	        Integer index = storedStrings.get(s);
	        if (index != null)
	        	return index.intValue();
    	}
    	return -1;
    }

	public void addToStoredObjects(Object o) {
        if (o != null && !storedObjects.containsKey(o)) {
            Integer index = Integer.valueOf(storedObjects.size());
            storedObjects.put(o, index);
        }
    }

	public int indexOfStoredObjects(Object o) {
    	if (o != null) {
	        Integer index = storedObjects.get(o);
	        if (index != null)
	        	return index.intValue();
    	}
		return -1;
    }
	
	///////////////////////////////////////////////////////////////////////////
	// ExtendedObjectOutput implementation

	public ClassLoader getClassLoader() {
		return context.getClassLoader();
	}

	public void getAndWriteField(Object obj, Field field) throws IOException, IllegalAccessException {
		if (field.getType().isPrimitive())
			codecRegistry.getPrimitiveFieldCodec(field.getType()).encodePrimitive(this, obj, field);
		else
			writeObject(field.get(obj));
	}
}
