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
package org.granite.messaging.amf.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.granite.config.AMF3Config;
import org.granite.config.ExternalizersConfig;
import org.granite.config.api.AliasRegistryConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.AliasRegistry;
import org.granite.messaging.amf.io.util.ActionScriptClassDescriptor;
import org.granite.messaging.amf.io.util.DefaultActionScriptClassDescriptor;
import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.messaging.amf.io.util.instantiator.AbstractInstantiator;
import org.granite.util.TypeUtil;
import org.granite.util.XMLUtil;
import org.granite.util.XMLUtilFactory;
import org.w3c.dom.Document;

/**
 * @author Franck WOLFF
 */
public class AMF3Deserializer implements ObjectInput, AMF3Constants {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    protected final List<String> storedStrings;
    protected final List<Object> storedObjects;
    protected final List<ActionScriptClassDescriptor> storedClassDescriptors;
    
    protected Map<String, Document> documentCache;

    protected final AliasRegistry aliasRegistry;
    protected final ExternalizersConfig externalizersConfig;
    protected final AMF3DeserializerSecurizer securizer;
    protected final XMLUtil xmlUtil;

    private final InputStream in;
	private final byte[] buffer;
	private int position;
	private int size;
	
    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

	public AMF3Deserializer(InputStream in) {
		this(in, 1024);
	}
	
    public AMF3Deserializer(InputStream in, int capacity) {
        this.in = in;
        this.buffer = new byte[capacity];
        this.position = 0;
        this.size = 0;
        
        this.storedStrings = new ArrayList<String>(64);
        this.storedObjects = new ArrayList<Object>(64);
        this.storedClassDescriptors = new ArrayList<ActionScriptClassDescriptor>();
        this.documentCache = null; // created on demand.
        
        GraniteContext context = GraniteContext.getCurrentInstance();
        this.aliasRegistry = ((AliasRegistryConfig)context.getGraniteConfig()).getAliasRegistry();
        this.externalizersConfig = (ExternalizersConfig)context.getGraniteConfig();
        this.securizer = ((AMF3Config)context.getGraniteConfig()).getAmf3DeserializerSecurizer();
        this.xmlUtil = XMLUtilFactory.getXMLUtil();
    }
    
    public void reset() {
        this.storedStrings.clear();
        this.storedObjects.clear();
        this.storedClassDescriptors.clear();
        this.documentCache = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // ObjectInput implementation.

    public Object readObject() throws IOException {
    	ensureAvailable(1);
        int type = buffer[position++];
        
        return readObject(type);
    }

    ///////////////////////////////////////////////////////////////////////////
    // AMF3 deserialization methods.

    protected Object readObject(int type) throws IOException {

        switch (type) {
        case AMF3_UNDEFINED: // 0x00;
        case AMF3_NULL: // 0x01;
            return null;
        case AMF3_BOOLEAN_FALSE: // 0x02;
            return Boolean.FALSE;
        case AMF3_BOOLEAN_TRUE: // 0x03;
            return Boolean.TRUE;
        case AMF3_INTEGER: // 0x04;
            return Integer.valueOf(readAMF3Integer());
        case AMF3_NUMBER: // 0x05;
            return readAMF3Double();
        case AMF3_STRING: // 0x06;
            return readAMF3String();
        case AMF3_XML: // 0x07;
            return readAMF3Xml();
        case AMF3_DATE: // 0x08;
            return readAMF3Date();
        case AMF3_ARRAY: // 0x09;
            return readAMF3Array();
        case AMF3_OBJECT: // 0x0A;
            return readAMF3Object();
        case AMF3_XMLSTRING: // 0x0B;
            return readAMF3XmlString();
        case AMF3_BYTEARRAY: // 0x0C;
            return readAMF3ByteArray();
        case AMF3_VECTOR_INT: // 0x0D;
        	return readAMF3VectorInt();
        case AMF3_VECTOR_UINT: // 0x0E;
        	return readAMF3VectorUint();
        case AMF3_VECTOR_NUMBER: // 0x0F;
        	return readAMF3VectorNumber();
        case AMF3_VECTOR_OBJECT: // 0x10;
        	return readAMF3VectorObject();
        case AMF3_DICTIONARY: // 0x11;
        	return readAMF3Dictionary();

        default:
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    protected int readAMF3Integer() throws IOException {
    	return ((readAMF3UnsignedInteger() << 3) >> 3);
    }
    
    protected int readAMF3UnsignedInteger() throws IOException {
        ensureAvailable(1);
        byte b = buffer[position++];
        if (b >= 0)
        	return b;
        
        ensureAvailable(1);
        int result = (b & 0x7F) << 7; 
        b = buffer[position++];
        if (b >= 0)
        	return (result | b);
        
        ensureAvailable(1);
        result = (result | (b & 0x7F)) << 7; 
        b = buffer[position++];
        if (b >= 0)
        	return (result | b);
        
        ensureAvailable(1);
        return (((result | (b & 0x7F)) << 8) | (buffer[position++] & 0xFF));
    }

    protected Double readAMF3Double() throws IOException {
    	ensureAvailable(8);
    	
    	double d = Double.longBitsToDouble(readLongData(buffer, position));
    	position += 8;
    	return Double.isNaN(d) ? null : d;
    }

    protected String readAMF3String() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;

        if ((type & 0x01) == 0) // stored string
            return storedStrings.get(lengthOrIndex);
        
        if (lengthOrIndex == 0)
        	return "";
        
        String result;
        if (lengthOrIndex <= buffer.length) {
        	ensureAvailable(lengthOrIndex);
        	result = new String(buffer, position, lengthOrIndex, UTF8);
        	position += lengthOrIndex;
        }
        else {
	        byte[] bytes = new byte[lengthOrIndex];
	        readFully(bytes, 0, lengthOrIndex);
	        result = new String(bytes, UTF8);
        }
        storedStrings.add(result);
        return result;
    }


    protected Date readAMF3Date() throws IOException {
        final int type = readAMF3UnsignedInteger();

        if ((type & 0x01) == 0) // stored Date
            return (Date)storedObjects.get(type >>> 1);

    	ensureAvailable(8);
    	Date result = new Date((long)Double.longBitsToDouble(readLongData(buffer, position)));
    	position += 8;
    	storedObjects.add(result);
        return result;
    }

    protected Object readAMF3Array() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;

        if ((type & 0x01) == 0) // stored array.
            return storedObjects.get(lengthOrIndex);

        String key = readAMF3String();
        if (key.length() == 0) {
            Object[] objects = new Object[lengthOrIndex];
            storedObjects.add(objects);

            for (int i = 0; i < lengthOrIndex; i++)
                objects[i] = readObject();

            return objects;
        }

        Map<Object, Object> map = new HashMap<Object, Object>(lengthOrIndex);
        storedObjects.add(map);
        while (key.length() > 0) {
            map.put(key, readObject());
            key = readAMF3String();
        }
        for (int i = 0; i < lengthOrIndex; i++)
            map.put(Integer.valueOf(i), readObject());

        return map;
    }

	protected int[] readAMF3VectorInt() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;

        if ((type & 0x01) == 0) // stored vector.
        	return (int[])storedObjects.get(lengthOrIndex);

        readByte(); // fixed flag: unused...

        int[] vector = new int[lengthOrIndex];
        storedObjects.add(vector);
        for (int i = 0; i < lengthOrIndex; i++)
        	vector[i] = readInt();
        return vector;
    }

	protected long[] readAMF3VectorUint() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;
        
        if ((type & 0x01) == 0) // stored vector.
        	return (long[])storedObjects.get(lengthOrIndex);

        readByte(); // fixed flag: unused...

        long[] vector = new long[lengthOrIndex];
        storedObjects.add(vector);
        for (int i = 0; i < lengthOrIndex; i++)
        	vector[i] = (readInt() & 0xffffffffL);
        return vector;
    }

	protected double[] readAMF3VectorNumber() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;
        
        if ((type & 0x01) == 0) // stored vector.
        	return (double[])storedObjects.get(lengthOrIndex);

        readByte(); // fixed flag: unused...

        double[] vector = new double[lengthOrIndex];
        storedObjects.add(vector);
        for (int i = 0; i < lengthOrIndex; i++)
        	vector[i] = readDouble();
        return vector;
    }

	@SuppressWarnings("unchecked")
	protected List<Object> readAMF3VectorObject() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;
        
        if ((type & 0x01) == 0) // stored vector.
        	return (List<Object>)storedObjects.get(lengthOrIndex);

        readByte(); // fixed flag: unused...
        readAMF3String(); // component class name: unused...
        
        List<Object> vector = new ArrayList<Object>(lengthOrIndex);
        storedObjects.add(vector);
        for (int i = 0; i < lengthOrIndex; i++)
        	vector.add(readObject());
        return vector;
    }
    
    @SuppressWarnings("unchecked")
	protected Map<Object, Object> readAMF3Dictionary() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;

        if ((type & 0x01) == 0) // stored dictionary.
        	return (Map<Object, Object>)storedObjects.get(lengthOrIndex);

        readByte(); // weak keys flag: unused...
        
    	// AS3 Dictionary doesn't have a strict Java equivalent: use an HashMap, which
    	// could (unlikely) lead to duplicated keys collision...
        Map<Object, Object> dictionary = new HashMap<Object, Object>(lengthOrIndex);
        storedObjects.add(dictionary);
        for (int i = 0; i < lengthOrIndex; i++) {
        	Object key = readObject();
        	Object value = readObject();
        	dictionary.put(key, value);
        }
        return dictionary;
    }

    protected Document readAMF3Xml() throws IOException {
        String xml = readAMF3XmlString();
        if (documentCache == null)
        	documentCache = new IdentityHashMap<String, Document>(32);
        Document doc = documentCache.get(xml);
        if (doc == null) {
        	doc = xmlUtil.buildDocument(xml);
        	documentCache.put(xml, doc);
        }
        return doc;
    }

    protected String readAMF3XmlString() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;

        if ((type & 0x01) == 0) // stored object
            return (String)storedObjects.get(lengthOrIndex);
        
        byte[] bytes = new byte[lengthOrIndex];
        readFully(bytes, 0, lengthOrIndex);
        String result = new String(bytes, UTF8);
        storedObjects.add(result);
        return result;
    }

    protected byte[] readAMF3ByteArray() throws IOException {
        final int type = readAMF3UnsignedInteger();
        final int lengthOrIndex = type >>> 1;
        
        if ((type & 0x01) == 0) // stored object.
            return (byte[])storedObjects.get(lengthOrIndex);
        
        byte[] result = new byte[lengthOrIndex];
        readFully(result, 0, lengthOrIndex);
        storedObjects.add(result);
        return result;
    }

    protected Object readAMF3Object() throws IOException {
        final int type = readAMF3UnsignedInteger();
        
        if ((type & 0x01) == 0) // stored object.
        	return storedObjects.get(type >>> 1);
        
        ActionScriptClassDescriptor desc = readActionScriptClassDescriptor(type);
        Object result = newInstance(desc);
        
        final int index = storedObjects.size();
        storedObjects.add(result);
        
        // Entity externalizers (eg. OpenJPA) may return null values for non-null AS3 objects (ie. proxies).
        if (result == null)
        	return null;

        if (desc.isExternalizable())
        	readExternalizable(desc, result);
        else
        	readStandard(desc, result);

        if (result instanceof AbstractInstantiator<?>) {
            try {
                result = ((AbstractInstantiator<?>)result).resolve();
            }
            catch (Exception e) {
                throw new RuntimeException("Could not instantiate object: " + result, e);
            }
            storedObjects.set(index, result);
        }

        return result;
    }
    
    protected void readExternalizable(ActionScriptClassDescriptor desc, Object result) throws IOException {
    	Externalizer externalizer = desc.getExternalizer();
        if (externalizer != null) {
            try {
                externalizer.readExternal(result, this);
            }
            catch (IOException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException("Could not read externalized object: " + result, e);
            }
        }
        else {
        	if (!(result instanceof Externalizable)) {
        		throw new RuntimeException(
        			"The ActionScript3 class bound to " + result.getClass().getName() +
        			" (ie: [RemoteClass(alias=\"" + result.getClass().getName() + "\")])" +
        			" implements flash.utils.IExternalizable but this Java class neither" +
        			" implements java.io.Externalizable nor is in the scope of a configured" +
        			" externalizer (please fix your granite-config.xml)"
        		);
        	}
            try {
                ((Externalizable)result).readExternal(this);
            }
            catch (IOException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException("Could not read externalizable object: " + result, e);
            }
        }
    }
    
    protected void readStandard(ActionScriptClassDescriptor desc, Object result) throws IOException {
        // defined values...
    	final int count = desc.getPropertiesCount();
        for (int i = 0; i < count; i++) {
        	Property property = desc.getProperty(i);
            Object value = readObject(readUnsignedByte());
            
            if (value != null && value.getClass() == property.getType())
            	property.setValue(result, value, false);
            else
            	property.setValue(result, value, true);
        }

        // dynamic values...
        if (desc.isDynamic()) {
            while (true) {
                String name = readAMF3String();
                if (name.length() == 0)
                    break;

                Object value = readObject(readUnsignedByte());
                desc.setPropertyValue(name, result, value);
            }
        }
    }
    
    protected Object newInstance(ActionScriptClassDescriptor desc) {
        Externalizer externalizer = desc.getExternalizer();
        if (externalizer == null)
        	return desc.newJavaInstance();
        
        try {
            return externalizer.newInstance(desc.getType(), this);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not instantiate type: " + desc.getType(), e);
        }
    }
    
    protected ActionScriptClassDescriptor readActionScriptClassDescriptor(int flags) throws IOException {
    	if ((flags & 0x02) == 0)
    		return storedClassDescriptors.get(flags >>> 2);
    	return readInlineActionScriptClassDescriptor(flags);
    }
    
    protected ActionScriptClassDescriptor readInlineActionScriptClassDescriptor(int flags) throws IOException {
        final int propertiesCount = flags >>> 4;
        final byte encoding = (byte)((flags >>> 2) & 0x03);
        
        String alias = readAMF3String();
        String className = aliasRegistry.getTypeForAlias(alias);
        
        // Check if the class is allowed to be instantiated.
        if (securizer != null && !securizer.allowInstantiation(className))
        	throw new SecurityException("Illegal attempt to instantiate class: " + className + ", securizer: " + securizer.getClass());

        // Find custom AS3 class descriptor if any.
        Class<? extends ActionScriptClassDescriptor> descriptorType = null;
        if (!"".equals(className))
            descriptorType = externalizersConfig.getActionScriptDescriptor(className);

        ActionScriptClassDescriptor desc = null;

        if (descriptorType != null) {
            // instantiate descriptor
            try {
                desc = TypeUtil.newInstance(
                	descriptorType,
                	new Class[]{String.class, byte.class},
                	new Object[]{className, Byte.valueOf(encoding)}
                );
            }
            catch (Exception e) {
                throw new RuntimeException("Could not instantiate AS descriptor: " + descriptorType, e);
            }
        }
        
        if (desc == null)
            desc = new DefaultActionScriptClassDescriptor(className, encoding);

        for (int i = 0; i < propertiesCount; i++) {
            String name = readAMF3String();
            desc.defineProperty(name);
        }
        
        storedClassDescriptors.add(desc);
    	
        return desc;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cached objects methods.

    protected void addToStoredClassDescriptors(ActionScriptClassDescriptor desc) {
        storedClassDescriptors.add(desc);
    }

    protected ActionScriptClassDescriptor getFromStoredClassDescriptors(int index) {
        return storedClassDescriptors.get(index);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utilities.
    
    private static long readLongData(byte[] buffer, int position) {
        return
        	(buffer[position++] & 0xFFL) << 56 |
        	(buffer[position++] & 0xFFL) << 48 |
        	(buffer[position++] & 0xFFL) << 40 |
        	(buffer[position++] & 0xFFL) << 32 |
        	(buffer[position++] & 0xFFL) << 24 |
        	(buffer[position++] & 0xFFL) << 16 |
        	(buffer[position++] & 0xFFL) << 8 |
        	(buffer[position] & 0xFFL);
    }
	
	private void ensureAvailable(int count) throws IOException {
		if (size - position < count)
			ensureAvailable0(count);
	}

	private void ensureAvailable0(final int count) throws IOException {
		if (count > buffer.length)
			throw new IllegalArgumentException("Count=" + count + " cannot be larger than buffer.length=" + buffer.length);
		
		if (position > 0) {
			size -= position;
			System.arraycopy(buffer, position, buffer, 0, size);
			position = 0;
		}
		
		do {
			int read = in.read(buffer, size, buffer.length - size);
			if (read <= 0) {
				if (read == -1)
					throw new EOFException("Count not read " + count + " bytes from input stream");
				throw new RuntimeException("Internal error: buffer.length=" + buffer.length + ", size=" + size + ", count=" + count);
			}
			size += read;
		}
		while (size < count);
	}
	
	///////////////////////////////////////////////////////////////////////////
	//

	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		if (b == null)
			throw new NullPointerException();
		if (off < 0 || len < 0 || len > b.length - off)
			throw new IndexOutOfBoundsException();
        if (len == 0)
        	return;

        final int left = size - position;
		if (len <= left) {
			System.arraycopy(buffer, position, b, off, len);
			position += len;
		}
		else {
			if (left > 0) {
				System.arraycopy(buffer, position, b, off, left);
				off += left;
				len -= left;
				position = size;
			}
			
			while (len > 0) {
				int count = in.read(b, off, len);
	            if (count <= 0)
	                throw new EOFException();
	            off += count;
	            len -= count;
			}
		}
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int)skip(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		ensureAvailable(1);
		return (buffer[position++] != 0);
	}

	@Override
	public byte readByte() throws IOException {
		ensureAvailable(1);
		return buffer[position++];
	}

	@Override
	public int readUnsignedByte() throws IOException {
		ensureAvailable(1);
		return (buffer[position++] & 0xFF);
	}

	@Override
	public short readShort() throws IOException {
		ensureAvailable(2);
		return (short)(((buffer[position++] & 0xFF) << 8) | (buffer[position++] & 0xFF));
	}

	@Override
	public int readUnsignedShort() throws IOException {
		ensureAvailable(2);
		return (((buffer[position++] & 0xFF) << 8) | (buffer[position++] & 0xFF));
	}

	@Override
	public char readChar() throws IOException {
		ensureAvailable(2);
		return (char)(((buffer[position++] & 0xFF) << 8) | (buffer[position++] & 0xFF));
	}

	@Override
	public int readInt() throws IOException {
		ensureAvailable(4);
		
		final byte[] buffer = this.buffer;
		int position = this.position;

		int i = (
			((buffer[position++] & 0xFF) << 24) |
			((buffer[position++] & 0xFF) << 16) |
			((buffer[position++] & 0xFF) <<  8) |
			((buffer[position++] & 0xFF)      )
		);
		
		this.position = position;
		
		return i;
	}

	@Override
	public long readLong() throws IOException {
		ensureAvailable(8);
		
		final byte[] buffer = this.buffer;
		int position = this.position;
		
		long l = (
			((buffer[position++] & 0xFFL) << 56) |
			((buffer[position++] & 0xFFL) << 48) |
			((buffer[position++] & 0xFFL) << 40) |
			((buffer[position++] & 0xFFL) << 32) |
			((buffer[position++] & 0xFFL) << 24) |
			((buffer[position++] & 0xFFL) << 16) |
			((buffer[position++] & 0xFFL) <<  8) |
			((buffer[position++] & 0xFFL)      )
		);
		
		this.position = position;
		
		return l;
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	@Deprecated
	@SuppressWarnings("resource")
	public String readLine() throws IOException {
		// Highly inefficient, but readLine() should never be used
		// when deserializing AMF3 data...
		return new DataInputStream(new InputStream() {
			@Override
			public int read() throws IOException {
				ensureAvailable(1);
				return buffer[position++];
			}
		}).readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return DataInputStream.readUTF(this);
	}

	@Override
	public int read() throws IOException {
		ensureAvailable(1);
		return buffer[position++];
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null)
			throw new NullPointerException();
		if (off < 0 || len < 0 || len > b.length - off)
			throw new IndexOutOfBoundsException();
        if (len == 0)
        	return 0;
		
        ensureAvailable(1);
        
        int count = Math.min(size - position, len);
        System.arraycopy(buffer, position, b, off, count);
        position += count;
		return count;
	}

	@Override
	public long skip(long n) throws IOException {
		if (n <= 0)
			return 0;
		
		final int left = size - position;
		if (n <= left) {
			position += n;
			return n;
		}
		
		position = size;
		
		long total = left;
		while (total < n) {
			long count = in.skip(n - total);
			if (count <= 0)
				return total;
			total += count;
		}
		
		return total;
	}

	@Override
	public int available() throws IOException {
		return (size - position) + in.available();
	}

	@Override
	public void close() throws IOException {
		position = size;
		in.close();
	}
}
