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
package org.granite.messaging.amf.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;

import org.granite.config.ConvertersConfig;
import org.granite.config.ExternalizersConfig;
import org.granite.config.flex.ChannelConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.amf.io.util.DefaultJavaClassDescriptor;
import org.granite.messaging.amf.io.util.IndexedJavaClassDescriptor;
import org.granite.messaging.amf.io.util.JavaClassDescriptor;
import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.messaging.amf.types.AMFDictionaryValue;
import org.granite.messaging.amf.types.AMFSpecialValue;
import org.granite.messaging.amf.types.AMFVectorIntValue;
import org.granite.messaging.amf.types.AMFVectorNumberValue;
import org.granite.messaging.amf.types.AMFVectorObjectValue;
import org.granite.messaging.amf.types.AMFVectorUintValue;
import org.granite.util.ObjectIndexedCache;
import org.granite.util.StringIndexedCache;
import org.granite.util.TypeUtil;
import org.granite.util.XMLUtil;
import org.granite.util.XMLUtilFactory;
import org.w3c.dom.Document;

import flex.messaging.io.ArrayCollection;

/**
 * @author Franck WOLFF
 */
public class AMF3Serializer implements ObjectOutput, AMF3Constants {
	
    ///////////////////////////////////////////////////////////////////////////
    // Static AMF3 writers.
	
	protected static final AMF3Writer AMF3_STRING_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3String(o.toString());
		}
	};
	
	protected static final AMF3Writer AMF3_BOOLEAN_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3Boolean(((Boolean)o).booleanValue());
		}
	};
	
	protected static final AMF3Writer AMF3_INTEGER_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3Integer(((Number)o).intValue());
		}
	};
	
	protected static final AMF3Writer AMF3_NUMBER_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3Number(((Number)o).doubleValue());
		}
	};
	
	protected static final AMF3Writer AMF3_DATE_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3Date((Date)o);
		}
	};
	
	protected static final AMF3Writer AMF3_CALENDAR_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3Date(((Calendar)o).getTime());
		}
	};
	
	protected static final AMF3Writer AMF3_DOCUMENT_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3Xml((Document)o);
		}
	};
	
	protected static final AMF3Writer AMF3_COLLECTION_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3Collection((Collection<?>)o);
		}
	};
	
	protected static final AMF3Writer AMF3_BOOLEAN_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3BooleanArray((boolean[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_CHAR_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3CharArray((char[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_CHAR_OBJECT_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3CharObjectArray((Character[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_BYTE_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3ByteArray((byte[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_BYTE_OBJECT_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3ByteObjectArray((Byte[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_SHORT_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3ShortArray((short[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_INT_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3IntArray((int[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_LONG_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3LongArray((long[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_FLOAT_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3FloatArray((float[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_DOUBLE_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3DoubleArray((double[])o);
		}
	};
	
	protected static final AMF3Writer AMF3_OBJECT_ARRAY_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3ObjectArray(o);
		}
	};
	
	protected static final AMF3Writer AMF3_SPECIAL_VALUE_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3SpecialValue((AMFSpecialValue<?>)o);
		}
	};
	
	protected static final AMF3Writer AMF3_OBJECT_WRITER = new AMF3Writer() {
		@Override
		public void write(AMF3Serializer serializer, Object o) throws IOException {
			serializer.writeAMF3Object(o);
		}
	};
	
    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.
    
	protected final OutputStream out;
	protected final byte[] buffer;
	protected int position;

	protected final StringIndexedCache storedStrings;
	protected final ObjectIndexedCache storedObjects;
    protected final Map<Class<?>, IndexedJavaClassDescriptor> storedClassDescriptors;
    protected final Map<Class<?>, AMF3Writer> writersCache;

    protected final Converters converters;
    protected final ClassGetter classGetter;
    protected final XMLUtil xmlUtil;

    protected final ExternalizersConfig externalizersConfig;
    protected final boolean externalizeLong;
    protected final boolean externalizeBigInteger;
    protected final boolean externalizeBigDecimal;
    protected final boolean legacyXmlSerialization;
    protected final boolean legacyCollectionSerialization;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public AMF3Serializer(OutputStream out) {
        this(out, 1024);
    }

    public AMF3Serializer(OutputStream out, int capacity) {
    	this.out = out;
        this.buffer = new byte[capacity];
        this.position = 0;
        
        this.storedStrings = new StringIndexedCache();
        this.storedObjects = new ObjectIndexedCache();
        this.storedClassDescriptors = new IdentityHashMap<Class<?>, IndexedJavaClassDescriptor>();
        this.writersCache = new IdentityHashMap<Class<?>, AMF3Writer>(64);
        
        GraniteContext context = GraniteContext.getCurrentInstance();
        
        ConvertersConfig convertersConfig = (ConvertersConfig)context.getGraniteConfig();
        this.converters = convertersConfig.getConverters();
        this.classGetter = convertersConfig.getClassGetter();
        this.xmlUtil = XMLUtilFactory.getXMLUtil();

        this.externalizersConfig = (ExternalizersConfig)context.getGraniteConfig();
        this.externalizeLong = (externalizersConfig.getExternalizer(Long.class.getName()) != null);
        this.externalizeBigInteger = (externalizersConfig.getExternalizer(BigInteger.class.getName()) != null);
        this.externalizeBigDecimal = (externalizersConfig.getExternalizer(BigDecimal.class.getName()) != null);
        
        String channelId = context.getAMFContext().getChannelId();
        ChannelConfig channelConfig = context.getServicesConfig();
        this.legacyXmlSerialization = getChannelProperty(channelId, channelConfig, "legacyXmlSerialization");
        this.legacyCollectionSerialization = getChannelProperty(channelId, channelConfig, "legacyCollectionSerialization");
    }
    
    protected static boolean getChannelProperty(String channelId, ChannelConfig channelConfig, String name) {
    	if (channelId != null && channelConfig != null)
    		return channelConfig.getChannelProperty(channelId, name);
    	return false;
    }
    
    public void reset() {
    	this.storedStrings.clear();
    	this.storedObjects.clear();
    	this.storedClassDescriptors.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // AMF3 entry point: ObjectOutput.writeObject(...) implementation.

    @Override
    public void writeObject(Object o) throws IOException {
    	if (o != null)
    		o = converters.revert(o);
    	if (o == null)
    		writeAMF3Null();
    	else {
    		Class<?> cls = o.getClass();
    		
    		if (cls == String.class) {
    			writeAMF3String((String)o);
    			return;
    		}
    		if (cls == Integer.class) {
    			writeAMF3Integer(((Integer)o).intValue());
    			return;
    		}
    		if (cls == Boolean.class) {
    			writeAMF3Boolean(((Boolean)o).booleanValue());
    			return;
    		}
    		
    		AMF3Writer writer = writersCache.get(cls);
    		if (writer == null) {
    			writer = getWriter(cls);
    			writersCache.put(cls, writer);
    		}
    		writer.write(this, o);
    	}
    }

    ///////////////////////////////////////////////////////////////////////////
    // AMF3 serialization methods.

    protected void writeAMF3Null() throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_NULL;
    }

    protected void writeAMF3Boolean(boolean value) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = (value ? AMF3_BOOLEAN_TRUE : AMF3_BOOLEAN_FALSE);
    }

    protected void writeAMF3Integer(int i) throws IOException {
        if (i < AMF3_INTEGER_MIN || i > AMF3_INTEGER_MAX)
            writeAMF3Number(i);
        else {
        	ensureCapacity(1);
        	buffer[position++] = AMF3_INTEGER;
        	
        	writeAMF3UnsignedIntegerData(i & AMF3_SIGNED_INTEGER_MASK);
        }
    }
    
    protected void writeAMF3UnsignedIntegerData(int i) throws IOException {
    	if (i < 0)
    		throw new AMF3SerializationException("Negative unsigned int: " + i);
    	
    	ensureCapacity(4);
    	
    	final byte[] buffer = this.buffer;
    	int position = this.position;

    	if (i < 0x4000) {
    		if (i < 0x80)
    			buffer[position++] = (byte)i;
    		else {
    			buffer[position++] = (byte)((i >>> 7) | 0x80);
    			buffer[position++] = (byte)(i & 0x7F);
    		}
    	}
    	else if (i < 0x40000000) {
    		if (i < 0x200000) {
    			buffer[position++] = (byte)((i >>> 14) | 0x80);
    			buffer[position++] = (byte)((i >>> 7) | 0x80);
    			buffer[position++] = (byte)(i & 0x7F);
    		}
    		else {
    			buffer[position++] = (byte)((i >>> 22) | 0x80);
    			buffer[position++] = (byte)((i >>> 15) | 0x80);
    			buffer[position++] = (byte)((i >>> 8) | 0x80);
    			buffer[position++] = (byte)i;
    		}
    	}
    	else
    		throw new AMF3SerializationException("Unsigned int out of range: " + i);
        
    	this.position = position;
    }
    
    protected void writeAMF3Number(double d) throws IOException {
    	ensureCapacity(9);
    	
    	buffer[position++] = AMF3_NUMBER;
    	position = writeLongData(buffer, position, Double.doubleToLongBits(d));
    }

    protected void writeAMF3String(String s) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_STRING;
       
    	writeAMF3StringData(s);
    }
    
    protected void writeAMF3StringData(String s) throws IOException {
    	final int length = s.length();

    	if (length == 0) {
        	ensureCapacity(1);
        	buffer[position++] = 0x01;
            return;
        }

        int index = storedStrings.putIfAbsent(s);

        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            final int count = utfByteCount(s);
            writeAMF3UnsignedIntegerData((count << 1) | 0x01);
            
        	final byte[] buffer = this.buffer;
        	final int bufferLength = buffer.length;
            
        	int position = this.position;
            
            // String chars are in [0x0000, 0x007F]: write them directly as bytes.
            if (count == length) {
                if (length <= bufferLength - position) {
                	for (int i = 0; i < length; i++)
                		buffer[position++] = (byte)s.charAt(i);
                	this.position = position;
                }
                else {
    	        	
    	        	int i = 0;
    	        	while (position < bufferLength)
    	        		buffer[position++] = (byte)s.charAt(i++);
    	        	this.position = position;
    	        	
    	        	do {
    		        	flushBuffer();
    		        	position = 0;

    		        	int max = Math.min(bufferLength, length - i);
    		        	while (position < max)
    		        		buffer[position++] = (byte)s.charAt(i++);
    		        	this.position = position;
    	        	}
    	        	while (i < length);
                }
            }
            // We have at least one char > 0x007F but enough buffer to write them all.
            else if (count <= bufferLength - position) {
            	
            	for (int i = 0; i < length; i++) {
                	char c = s.charAt(i);
                	if (c <= 0x007F)
                    	buffer[position++] = (byte)c;
                    else if (c > 0x07FF) {
                    	buffer[position++] = (byte)(0xE0 | (c >>> 12));
                    	buffer[position++] = (byte)(0x80 | ((c >>> 6) & 0x3F));
                    	buffer[position++] = (byte)(0x80 | (c & 0x3F));
                    }
                    else {
                    	buffer[position++] = (byte)(0xC0 | ((c >>> 6) & 0x1F));
                    	buffer[position++] = (byte)(0x80 | (c & 0x3F));
                    }
                }
                
                this.position = position;
            }
            // We have at least one char > 0x007F and not enough buffer to write them all.
            else {
	        	final int bufferLengthMinus3 = buffer.length - 3;
            	
            	int i = 0, total = 0;
	        	do {
	            	flushBuffer();

	            	position = 0;
	            	final int max = Math.min(count - total, bufferLengthMinus3);
	            	
	            	while (position < max) {
	            		char c = s.charAt(i++);
		            	if (c <= 0x007F)
		                	buffer[position++] = (byte)c;
		                else if (c > 0x07FF) {
		                	buffer[position++] = (byte)(0xE0 | (c >>> 12));
		                	buffer[position++] = (byte)(0x80 | ((c >>> 6) & 0x3F));
		                	buffer[position++] = (byte)(0x80 | (c & 0x3F));
		                }
		                else {
		                	buffer[position++] = (byte)(0xC0 | ((c >>> 6) & 0x1F));
		                	buffer[position++] = (byte)(0x80 | (c & 0x3F));
		                }
	            	}
	            	
	            	total += position;
	            	this.position = position;
	        	}
	        	while (total < count);
            }
        }
    }

    protected void writeAMF3Xml(Document doc) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = (legacyXmlSerialization ? AMF3_XML : AMF3_XMLSTRING);

        int index = storedObjects.putIfAbsent(doc);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            byte[] bytes = xmlUtil.toString(doc).getBytes("UTF-8");
            writeAMF3UnsignedIntegerData((bytes.length << 1) | 0x01);
            flushBuffer();
            out.write(bytes, 0, bytes.length);
        }
    }

    protected void writeAMF3Date(Date date) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_DATE;

        int index = storedObjects.putIfAbsent(date);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
        	writeAMF3UnsignedIntegerData(0x01);
            
            ensureCapacity(8);
            position = writeLongData(buffer, position, Double.doubleToRawLongBits((double)date.getTime()));
        }
    }
    
    protected void writeAMF3BooleanArray(boolean[] array) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_ARRAY;

        int index = storedObjects.putIfAbsent(array);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            writeAMF3UnsignedIntegerData(array.length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = 0x01;

        	for (boolean i : array)
        		writeAMF3Boolean(i);
        }
    }
    
    protected void writeAMF3CharArray(char[] array) throws IOException {
    	writeAMF3String(new String(array));
    }
    
    protected void writeAMF3CharObjectArray(Character[] array) throws IOException {
    	final int length = array.length;
    	
    	char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            Character c = array[i];
            if (c == null)
                chars[i] = 0;
            else
                chars[i] = c.charValue();
        }
    	
    	writeAMF3String(new String(chars));
    }

    protected void writeAMF3ByteArray(byte[] bytes) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_BYTEARRAY;

        int index = storedObjects.putIfAbsent(bytes);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
        	writeAMF3UnsignedIntegerData(bytes.length << 1 | 0x01);
            
            flushBuffer();
            out.write(bytes, 0, bytes.length);
        }
    }
    
    protected void writeAMF3ByteObjectArray(Byte[] array) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_BYTEARRAY;

        int index = storedObjects.putIfAbsent(array);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
        	final int length = array.length;
        	
            writeAMF3UnsignedIntegerData(length << 1 | 0x01);
            
            byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++) {
            	Byte b = array[i];
            	if (b == null)
            		bytes[i] = 0;
            	else
            		bytes[i] = b.byteValue();
            }
            
            flushBuffer();
            out.write(bytes, 0, length);
        }
    }
    
    protected void writeAMF3ShortArray(short[] array) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_ARRAY;

        int index = storedObjects.putIfAbsent(array);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            writeAMF3UnsignedIntegerData(array.length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = 0x01;

        	for (short i : array)
        		writeAMF3Integer(i);
        }
    }
    
    protected void writeAMF3IntArray(int[] array) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_ARRAY;

        int index = storedObjects.putIfAbsent(array);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            writeAMF3UnsignedIntegerData(array.length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = 0x01;

        	for (int i : array)
        		writeAMF3Integer(i);
        }
    }
    
    protected void writeAMF3LongArray(long[] array) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_ARRAY;

        int index = storedObjects.putIfAbsent(array);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            writeAMF3UnsignedIntegerData(array.length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = 0x01;

        	for (long i : array)
        		writeAMF3Number(i);
        }
    }
    
    protected void writeAMF3FloatArray(float[] array) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_ARRAY;

        int index = storedObjects.putIfAbsent(array);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            writeAMF3UnsignedIntegerData(array.length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = 0x01;

        	for (float i : array)
        		writeAMF3Number(i);
        }
    }
    
    protected void writeAMF3DoubleArray(double[] array) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_ARRAY;

        int index = storedObjects.putIfAbsent(array);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            writeAMF3UnsignedIntegerData(array.length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = 0x01;

        	for (double i : array)
        		writeAMF3Number(i);
        }
    }

    protected void writeAMF3ObjectArray(Object array) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_ARRAY;

        int index = storedObjects.putIfAbsent(array);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            int length = Array.getLength(array);
            writeAMF3UnsignedIntegerData(length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = 0x01;

            for (int i = 0; i < length; i++)
                writeObject(Array.get(array, i));
        }
    }

    protected void writeAMF3Collection(Collection<?> c) throws IOException {
        if (legacyCollectionSerialization)
            writeAMF3ObjectArray(c.toArray());
        else {
        	ensureCapacity(1);
        	buffer[position++] = AMF3_OBJECT;
            
        	int index = storedObjects.putIfAbsent(c);
            if (index >= 0)
            	writeAMF3UnsignedIntegerData(index << 1);
            else {
            	writeAndGetAMF3Descriptor(ArrayCollection.class);
            	
            	ensureCapacity(1);
            	buffer[position++] = AMF3_ARRAY;
            	
            	// Add an arbitrary object in the dictionary instead of the
            	// array obtained via c.toArray(): c.toArray() must return a
            	// new instance each time it is called, there is no way to
            	// find the same instance later...
            	storedObjects.putIfAbsent(new Object());
            	
            	writeAMF3UnsignedIntegerData(c.size() << 1 | 0x01);
                
                ensureCapacity(1);
            	buffer[position++] = 0x01;
            	
            	for (Object o : c)
            		writeObject(o);
            }
        }
    }

    protected void writeAMF3Object(Object o) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_OBJECT;

        int index = storedObjects.putIfAbsent(o);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            Class<?> oClass = classGetter.getClass(o);

            JavaClassDescriptor desc = writeAndGetAMF3Descriptor(oClass);
            if (desc.isExternalizable()) {
                Externalizer externalizer = desc.getExternalizer();

                if (externalizer != null) {
                    try {
                        externalizer.writeExternal(o, this);
                    }
                    catch (IOException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Could not externalize object: " + o, e);
                    }
                }
                else
                    ((Externalizable)o).writeExternal(this);
            }
            else {
            	final int count = desc.getPropertiesCount();
            	for (int i = 0; i < count; i++) {
            		Property property = desc.getProperty(i);
            		writeObject(property.getValue(o));
            	}

                if (desc.isDynamic()) {
                    Map<?, ?> oMap = (Map<?, ?>)o;
                    for (Map.Entry<?, ?> entry : oMap.entrySet()) {
                        Object key = entry.getKey();
                        if (key != null) {
                            String propertyName = key.toString();
                            if (propertyName.length() > 0) {
                                writeAMF3StringData(propertyName);
                                writeObject(entry.getValue());
                            }
                        }
                    }
                	ensureCapacity(1);
                	buffer[position++] = 0x01;
                }
            }
        }
    }
    
    protected void writeAMF3SpecialValue(AMFSpecialValue<?> value) throws IOException {
        switch (value.type) {
        case AMF3_DICTIONARY:
			writeAMF3Dictionary((AMFDictionaryValue)value);
			break;
        case AMF3_VECTOR_INT:
			writeAMF3VectorInt((AMFVectorIntValue)value);
        	break;
        case AMF3_VECTOR_NUMBER:
			writeAMF3VectorNumber((AMFVectorNumberValue)value);
        	break;
        case AMF3_VECTOR_UINT:
			writeAMF3VectorUint((AMFVectorUintValue)value);
        	break;
        case AMF3_VECTOR_OBJECT:
			writeAMF3VectorObject((AMFVectorObjectValue)value);
        	break;
        default:
			throw new RuntimeException("Unsupported AMF special value: " + value);
        }
    }
    
    protected void writeAMF3VectorObject(AMFVectorObjectValue value) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_VECTOR_OBJECT;

        Object o = value.value;
        
        int index = storedObjects.putIfAbsent(o);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            int length = getArrayOrCollectionLength(o);
            writeAMF3UnsignedIntegerData(length << 1 | 0x01);

            ensureCapacity(1);
        	buffer[position++] = (byte)(value.fixed ? 0x01 : 0x00);
            
        	writeAMF3StringData(value.type);

            if (o.getClass().isArray()) {
                for (int i = 0; i < length; i++)
                    writeObject(Array.get(o, i));
            }
            else {
            	for (Object item : (Collection<?>)o)
            		writeObject(item);
            }
        }
    }
    
    protected void writeAMF3VectorInt(AMFVectorIntValue value) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_VECTOR_INT;

        Object o = value.value;

        int index = storedObjects.putIfAbsent(o);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            int length = getArrayOrCollectionLength(o);
            writeAMF3UnsignedIntegerData(length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = (byte)(value.fixed ? 0x01 : 0x00);

            if (o.getClass().isArray()) {
                for (int i = 0; i < length; i++) {
                	ensureCapacity(4);
                	position = writeIntData(buffer, position, ((Number)Array.get(o, i)).intValue());
                }
            }
            else {
            	for (Object item : (Collection<?>)o) {
                	ensureCapacity(4);
                	position = writeIntData(buffer, position, ((Number)item).intValue());
            	}
            }
        }
    }
    
    protected void writeAMF3VectorNumber(AMFVectorNumberValue value) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_VECTOR_NUMBER;

        Object o = value.value;

        int index = storedObjects.putIfAbsent(o);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            int length = getArrayOrCollectionLength(o);
            writeAMF3UnsignedIntegerData(length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = (byte)(value.fixed ? 0x01 : 0x00);

            if (o.getClass().isArray()) {
                for (int i = 0; i < length; i++) {
                	ensureCapacity(8);
                	position = writeLongData(buffer, position, Double.doubleToLongBits(((Number)Array.get(o, i)).doubleValue()));
                }
            }
            else {
            	for (Object item : (Collection<?>)o) {
                	ensureCapacity(8);
                	position = writeLongData(buffer, position, Double.doubleToLongBits(((Number)item).doubleValue()));
            	}
            }
        }
    }
    
    protected void writeAMF3VectorUint(AMFVectorUintValue value) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_VECTOR_UINT;

        Object o = value.value;

        int index = storedObjects.putIfAbsent(o);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            int length = getArrayOrCollectionLength(o);
            writeAMF3UnsignedIntegerData(length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = (byte)(value.fixed ? 0x01 : 0x00);

            if (o.getClass().isArray()) {
                for (int i = 0; i < length; i++) {
                	ensureCapacity(4);
                	position = writeIntData(buffer, position, ((Number)Array.get(o, i)).intValue());
                }
            }
            else {
            	for (Object item : (Collection<?>)o) {
                	ensureCapacity(4);
                	position = writeIntData(buffer, position, ((Number)item).intValue());
            	}
            }
        }
    }

    protected void writeAMF3Dictionary(AMFDictionaryValue value) throws IOException {
    	ensureCapacity(1);
    	buffer[position++] = AMF3_DICTIONARY;

        Map<?, ?> o = value.value;

        int index = storedObjects.putIfAbsent(o);
        if (index >= 0)
        	writeAMF3UnsignedIntegerData(index << 1);
        else {
            int length = o.size();
            writeAMF3UnsignedIntegerData(length << 1 | 0x01);
        	
            ensureCapacity(1);
        	buffer[position++] = (byte)(value.weakKeys ? 0x01 : 0x00);

            for (Map.Entry<?, ?> entry : o.entrySet()) {
            	writeObject(entry.getKey());
            	writeObject(entry.getValue());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utilities.
    
    protected int getArrayOrCollectionLength(Object o) {
    	if (o.getClass().isArray())
    		return Array.getLength(o);
    	return ((Collection<?>)o).size();
    }
    
    protected AMF3Writer getWriter(Class<?> cls) {
    	
    	if (cls.isPrimitive())
    		throw new RuntimeException("Illegal primitive class: " + cls);
    	
    	if (Externalizable.class.isAssignableFrom(cls))
    		return AMF3_OBJECT_WRITER;
		
    	if (cls == String.class || cls == Character.class)
			return AMF3_STRING_WRITER;
		
    	if (Number.class.isAssignableFrom(cls)) {
			if (cls == Integer.class || cls == Short.class || cls == Byte.class)
				return AMF3_INTEGER_WRITER;
			if (externalizeLong && cls == Long.class)
				return AMF3_OBJECT_WRITER;
			if (externalizeBigInteger && BigInteger.class.isAssignableFrom(cls))
				return AMF3_OBJECT_WRITER;
			if (externalizeBigDecimal && BigDecimal.class.isAssignableFrom(cls))
				return AMF3_OBJECT_WRITER;
			return AMF3_NUMBER_WRITER;
		}
		
    	if (Collection.class.isAssignableFrom(cls))
			return AMF3_COLLECTION_WRITER;
		
    	if (cls.isArray()) {
			Class<?> componentType = cls.getComponentType();
			if (componentType == boolean.class)
				return AMF3_BOOLEAN_ARRAY_WRITER;
			if (componentType == char.class)
				return AMF3_CHAR_ARRAY_WRITER;
			if (componentType == Character.class)
				return AMF3_CHAR_OBJECT_ARRAY_WRITER;
			if (componentType == byte.class)
				return AMF3_BYTE_ARRAY_WRITER;
			if (componentType == Byte.class)
				return AMF3_BYTE_OBJECT_ARRAY_WRITER;
			if (componentType == short.class)
				return AMF3_SHORT_ARRAY_WRITER;
			if (componentType == int.class)
				return AMF3_INT_ARRAY_WRITER;
			if (componentType == long.class)
				return AMF3_LONG_ARRAY_WRITER;
			if (componentType == float.class)
				return AMF3_FLOAT_ARRAY_WRITER;
			if (componentType == double.class)
				return AMF3_DOUBLE_ARRAY_WRITER;
			return AMF3_OBJECT_ARRAY_WRITER;
		}
		
    	if (cls == Boolean.class)
			return AMF3_BOOLEAN_WRITER;
		
    	if (Date.class.isAssignableFrom(cls))
			return AMF3_DATE_WRITER;
		if (Calendar.class.isAssignableFrom(cls))
			return AMF3_CALENDAR_WRITER;
		
		if (Document.class.isAssignableFrom(cls))
			return AMF3_DOCUMENT_WRITER;
		
		if (AMFSpecialValue.class.isAssignableFrom(cls))
			return AMF3_SPECIAL_VALUE_WRITER;
		
		return AMF3_OBJECT_WRITER;
    }
    
    protected JavaClassDescriptor writeAndGetAMF3Descriptor(Class<?> cls) throws IOException {
    	JavaClassDescriptor desc = null;
    	
        IndexedJavaClassDescriptor iDesc = storedClassDescriptors.get(cls);
        if (iDesc != null) {
            desc = iDesc.getDescriptor();
            writeAMF3UnsignedIntegerData(iDesc.getIndex() << 2 | 0x01);
        }
        else {
            iDesc = addToStoredClassDescriptors(cls);
            desc = iDesc.getDescriptor();
            
            final int count = desc.getPropertiesCount();

            writeAMF3UnsignedIntegerData((count << 4) | (desc.getEncoding() << 2) | 0x03);
            writeAMF3StringData(desc.getName());

            for (int i = 0; i < count; i++)
            	writeAMF3StringData(desc.getPropertyName(i));
        }
    	
        return desc;
    }

    protected IndexedJavaClassDescriptor addToStoredClassDescriptors(Class<?> clazz) {
        JavaClassDescriptor desc = externalizersConfig.getJavaDescriptorsCache().get(clazz.getName());
        
        if (desc == null) {
            
        	// find custom class descriptor and instantiate it if any.
	        Class<? extends JavaClassDescriptor> descriptorType = externalizersConfig.getJavaDescriptor(clazz.getName());
	        if (descriptorType != null) {
	            try {
	                desc = TypeUtil.newInstance(descriptorType, new Class[]{Class.class}, new Object[]{clazz});
	            }
	            catch (Exception e) {
	                throw new RuntimeException("Could not instantiate Java descriptor: " + descriptorType);
	            }
	        }
	        else
	            desc = new DefaultJavaClassDescriptor(clazz);
	        
	        externalizersConfig.getJavaDescriptorsCache().putIfAbsent(clazz.getName(), desc);
        }

        IndexedJavaClassDescriptor iDesc = new IndexedJavaClassDescriptor(storedClassDescriptors.size(), desc);
        storedClassDescriptors.put(clazz, iDesc);
        return iDesc;
    }

    protected void ensureCapacity(int capacity) throws IOException {
		if (buffer.length - position < capacity)
			flushBuffer();
	}
	
    protected void flushBuffer() throws IOException {
		if (position > 0) {
			out.write(buffer, 0, position);
			position = 0;
		}
	}
    
    protected static int utfByteCount(String s) {
    	final int length = s.length();
    	
    	int count = length;
        for (int i = 0; i < length; i++) {
        	char c = s.charAt(i);
        	if (c > 0x007F) {
        		if (c > 0x07FF)
        			count += 2;
        		else
        			count++;
        	}
        }
        return count;
    }
	
    protected static int writeIntData(byte[] buffer, int position, int i) {
    	buffer[position++] = (byte)(i >>> 24);
    	buffer[position++] = (byte)(i >>> 16);
    	buffer[position++] = (byte)(i >>> 8);
    	buffer[position++] = (byte)i;
    	return position;
    }
	
    protected static int writeLongData(byte[] buffer, int position, long l) {
    	buffer[position++] = (byte)(l >>> 56);
    	buffer[position++] = (byte)(l >>> 48);
    	buffer[position++] = (byte)(l >>> 40);
    	buffer[position++] = (byte)(l >>> 32);
    	buffer[position++] = (byte)(l >>> 24);
    	buffer[position++] = (byte)(l >>> 16);
    	buffer[position++] = (byte)(l >>> 8);
    	buffer[position++] = (byte)l;
    	return position;
    }
	
	///////////////////////////////////////////////////////////////////////////
	// ObjectOutput implementation (except writeObject): not optimized as these
    // methods are very unlikely used with AMF3...
	
	@Override
	public void writeBoolean(boolean v) throws IOException {
		flushBuffer();
		out.write(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) throws IOException {
		flushBuffer();
		out.write(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		flushBuffer();
		out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
	}

	@Override
	public void writeChar(int v) throws IOException {
		flushBuffer();
		out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
	}

	@Override
	public void writeInt(int v) throws IOException {
		flushBuffer();
		out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>>  8) & 0xFF);
        out.write((v >>>  0) & 0xFF);
	}

	@Override
	public void writeLong(long v) throws IOException {
		flushBuffer();
		
		byte writeBuffer[] = new byte[8];
        writeBuffer[0] = (byte)(v >>> 56);
        writeBuffer[1] = (byte)(v >>> 48);
        writeBuffer[2] = (byte)(v >>> 40);
        writeBuffer[3] = (byte)(v >>> 32);
        writeBuffer[4] = (byte)(v >>> 24);
        writeBuffer[5] = (byte)(v >>> 16);
        writeBuffer[6] = (byte)(v >>>  8);
        writeBuffer[7] = (byte)(v >>>  0);
        out.write(writeBuffer, 0, 8);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		flushBuffer();
		writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(double v) throws IOException {
		flushBuffer();
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeBytes(String s) throws IOException {
		flushBuffer();
        
		final int len = s.length();
        for (int i = 0 ; i < len ; i++)
            out.write((byte)s.charAt(i));
	}

	@Override
	public void writeChars(String s) throws IOException {
		flushBuffer();
        
		int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
            out.write((v >>> 8) & 0xFF);
            out.write((v >>> 0) & 0xFF);
        }
	}

	@Override
	public void writeUTF(String s) throws IOException {
		flushBuffer();

		final int strlen = s.length();
        int utflen = 0;
        int c, count = 0;

        for (int i = 0; i < strlen; i++) {
            c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");

        byte[] bytearr = new byte[utflen+2];

        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i=0;
        for (i=0; i<strlen; i++) {
           c = s.charAt(i);
           if (!((c >= 0x0001) && (c <= 0x007F)))
        	   break;
           bytearr[count++] = (byte) c;
        }

        for (;i < strlen; i++){
            c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        
        out.write(bytearr, 0, utflen+2);
	}

	@Override
	public void write(int b) throws IOException {
		flushBuffer();
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		flushBuffer();
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		flushBuffer();
		out.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		flushBuffer();
		out.flush();
	}

	@Override
	public void close() throws IOException {
		flushBuffer();
		out.close();
	}

	///////////////////////////////////////////////////////////////////////////
	// General interface for specific AMF3 writers (see static writers above).
	
	protected interface AMF3Writer {
		
		public void write(AMF3Serializer serializer, Object o) throws IOException;
	}
}
