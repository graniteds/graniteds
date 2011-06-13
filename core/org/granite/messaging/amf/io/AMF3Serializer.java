/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

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

package org.granite.messaging.amf.io;

import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.granite.config.flex.Channel;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.AMF3Constants;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.amf.io.util.DefaultJavaClassDescriptor;
import org.granite.messaging.amf.io.util.IndexedJavaClassDescriptor;
import org.granite.messaging.amf.io.util.JavaClassDescriptor;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.util.ClassUtil;
import org.granite.util.XMLUtil;
import org.w3c.dom.Document;

import flex.messaging.io.ArrayCollection;

/**
 * @author Franck WOLFF
 */
public class AMF3Serializer extends DataOutputStream implements ObjectOutput, AMF3Constants {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    protected static final Logger log = Logger.getLogger(AMF3Serializer.class);
    protected static final Logger logMore = Logger.getLogger(AMF3Serializer.class.getName() + "_MORE");

    protected final Map<String, Integer> storedStrings = new HashMap<String, Integer>();
    protected final Map<Object, Integer> storedObjects = new IdentityHashMap<Object, Integer>();
    protected final Map<String, IndexedJavaClassDescriptor> storedClassDescriptors
    	= new HashMap<String, IndexedJavaClassDescriptor>();

    protected final GraniteContext context = GraniteContext.getCurrentInstance();
    protected final Converters converters = context.getGraniteConfig().getConverters();

    protected final boolean externalizeLong
		= (context.getGraniteConfig().getExternalizer(Long.class.getName()) != null);
    protected final boolean externalizeBigInteger
		= (context.getGraniteConfig().getExternalizer(BigInteger.class.getName()) != null);
    protected final boolean externalizeBigDecimal
    	= (context.getGraniteConfig().getExternalizer(BigDecimal.class.getName()) != null);

    protected final XMLUtil xmlUtil = new XMLUtil();
    
    protected final boolean warnOnChannelIdMissing;

    protected final boolean debug = log.isDebugEnabled();
    protected final boolean debugMore = logMore.isDebugEnabled();

    protected Channel channel = null;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public AMF3Serializer(OutputStream out) {
    	this(out, true);
    }

    public AMF3Serializer(OutputStream out, boolean warnOnChannelMissing) {
        super(out);

        this.warnOnChannelIdMissing = warnOnChannelMissing;

        if (debugMore) logMore.debug("new AMF3Serializer(out=%s)", out);
    }

    ///////////////////////////////////////////////////////////////////////////
    // ObjectOutput implementation.

    public void writeObject(Object o) throws IOException {
        if (debugMore) logMore.debug("writeObject(o=%s)", o);

        try {
	        if (o == null)
	            write(AMF3_NULL);
	        else if (!(o instanceof Externalizable)) {
	
	            if (converters.hasReverters())
	                o = converters.revert(o);
	
	            if (o instanceof String || o instanceof Character)
	                writeAMF3String(o.toString());
	            else if (o instanceof Boolean)
	                write(((Boolean)o).booleanValue() ? AMF3_BOOLEAN_TRUE : AMF3_BOOLEAN_FALSE);
	            else if (o instanceof Number) {
	                if (o instanceof Integer || o instanceof Short || o instanceof Byte)
	                    writeAMF3Integer(((Number)o).intValue());
	                else if (externalizeLong && o instanceof Long)
	                	writeAMF3Object(o);
	                else if (externalizeBigInteger && o instanceof BigInteger)
	                	writeAMF3Object(o);
	                else if (externalizeBigDecimal && o instanceof BigDecimal)
	                	writeAMF3Object(o);
	                else
	                    writeAMF3Number(((Number)o).doubleValue());
	            }
	            else if (o instanceof Date)
	                writeAMF3Date((Date)o);
	            else if (o instanceof Calendar)
	                writeAMF3Date(((Calendar)o).getTime());
	            else if (o instanceof Document)
	                writeAMF3Xml((Document)o);
	            else if (o instanceof Collection<?>)
	                writeAMF3Collection((Collection<?>)o);
	            else if (o.getClass().isArray()) {
	                if (o.getClass().getComponentType() == Byte.TYPE)
	                    writeAMF3ByteArray((byte[])o);
	                else
	                    writeAMF3Array(o);
	            }
	            else
	                writeAMF3Object(o);
	        } else
	            writeAMF3Object(o);
        }
        catch (IOException e) {
        	throw e;
        }
        catch (Exception e) {
        	throw new AMF3SerializationException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // AMF3 serialization.

    protected void writeAMF3Integer(int i) throws IOException {
        if (debugMore) logMore.debug("writeAMF3Integer(i=%d)", i);

        if (i < AMF3_INTEGER_MIN || i > AMF3_INTEGER_MAX) {
            if (debugMore) logMore.debug("writeAMF3Integer() - %d is out of AMF3 int range, writing it as a Number", i);
            writeAMF3Number(i);
        } else {
            write(AMF3_INTEGER);
            writeAMF3IntegerData(i);
        }
    }

    protected void writeAMF3IntegerData(int i) throws IOException {
        if (debugMore) logMore.debug("writeAMF3IntegerData(i=%d)", i);

        if (i < AMF3_INTEGER_MIN || i > AMF3_INTEGER_MAX)
            throw new IllegalArgumentException("Integer out of range: " + i);

        if (i < 0 || i >= 0x200000) {
            write(((i >> 22) & 0x7F) | 0x80);
            write(((i >> 15) & 0x7F) | 0x80);
            write(((i >> 8) & 0x7F) | 0x80);
            write(i & 0xFF);
        } else {
            if (i >= 0x4000)
                write(((i >> 14) & 0x7F) | 0x80);
            if (i >= 0x80)
                write(((i >> 7) & 0x7F) | 0x80);
            write(i & 0x7F);
        }
    }

    protected void writeAMF3Number(double d) throws IOException {
        if (debugMore) logMore.debug("writeAMF3Number(d=%f)", d);

        write(AMF3_NUMBER);
        writeDouble(d);
    }

    protected void writeAMF3String(String s) throws IOException {
        if (debugMore) logMore.debug("writeAMF3String(s=%s)", s);

        write(AMF3_STRING);
        writeAMF3StringData(s);
    }

    protected void writeAMF3StringData(String s) throws IOException {
        if (debugMore) logMore.debug("writeAMF3StringData(s=%s)", s);

        if (s.length() == 0) {
            write(0x01);
            return;
        }

        int index = indexOfStoredStrings(s);

        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredStrings(s);

            final int sLength = s.length();

            // Compute and write modified UTF-8 string length.
            int uLength = 0;
            for (int i = 0; i < sLength; i++) {
                int c = s.charAt(i);
                if ((c >= 0x0001) && (c <= 0x007F))
                    uLength++;
                else if (c > 0x07FF)
                    uLength += 3;
                else
                    uLength += 2;
            }
            writeAMF3IntegerData((uLength << 1) | 0x01);

            // Write modified UTF-8 bytes.
            for (int i = 0; i < sLength; i++) {
                int c = s.charAt(i);
                if ((c >= 0x0001) && (c <= 0x007F)) {
                    write(c);
                } else if (c > 0x07FF) {
                    write(0xE0 | ((c >> 12) & 0x0F));
                    write(0x80 | ((c >>  6) & 0x3F));
                    write(0x80 | ((c >>  0) & 0x3F));
                } else {
                    write(0xC0 | ((c >>  6) & 0x1F));
                    write(0x80 | ((c >>  0) & 0x3F));
                }
            }
        }
    }

    protected void writeAMF3Xml(Document doc) throws IOException {
        if (debugMore) logMore.debug("writeAMF3Xml(doc=%s)", doc);

        byte xmlType = AMF3_XMLSTRING;
        Channel channel = getChannel();
        if (channel != null && channel.isLegacyXmlSerialization())
            xmlType = AMF3_XML;
        write(xmlType);

        int index = indexOfStoredObjects(doc);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(doc);

            byte[] bytes = xmlUtil.toString(doc).getBytes("UTF-8");
            writeAMF3IntegerData((bytes.length << 1) | 0x01);
            write(bytes);
        }
    }

    protected void writeAMF3Date(Date date) throws IOException {
        if (debugMore) logMore.debug("writeAMF3Date(date=%s)", date);

        write(AMF3_DATE);

        int index = indexOfStoredObjects(date);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(date);
            writeAMF3IntegerData(0x01);
            writeDouble(date.getTime());
        }
    }

    protected void writeAMF3Array(Object array) throws IOException {
        if (debugMore) logMore.debug("writeAMF3Array(array=%s)", array);

        write(AMF3_ARRAY);

        int index = indexOfStoredObjects(array);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(array);

            int length = Array.getLength(array);
            writeAMF3IntegerData(length << 1 | 0x01);
            write(0x01);
            for (int i = 0; i < length; i++)
                writeObject(Array.get(array, i));
        }
    }

    protected void writeAMF3ByteArray(byte[] bytes) throws IOException {
        if (debugMore) logMore.debug("writeAMF3ByteArray(bytes=%s)", bytes);

        write(AMF3_BYTEARRAY);

        int index = indexOfStoredObjects(bytes);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(bytes);

            writeAMF3IntegerData(bytes.length << 1 | 0x01);
            //write(bytes);

            for (int i = 0; i < bytes.length; i++)
            	out.write(bytes[i]);
        }
    }

    protected void writeAMF3Collection(Collection<?> c) throws IOException {
        if (debugMore) logMore.debug("writeAMF3Collection(c=%s)", c);

        Channel channel = getChannel();
        if (channel != null && channel.isLegacyCollectionSerialization())
            writeAMF3Array(c.toArray());
        else {
            ArrayCollection ac = (c instanceof ArrayCollection ? (ArrayCollection)c : new ArrayCollection(c));
            writeAMF3Object(ac);
        }
    }

    protected void writeAMF3Object(Object o) throws IOException {
        if (debug) log.debug("writeAMF3Object(o=%s)...", o);

        write(AMF3_OBJECT);

        int index = indexOfStoredObjects(o);
        if (index >= 0)
            writeAMF3IntegerData(index << 1);
        else {
            addToStoredObjects(o);

            ClassGetter classGetter = context.getGraniteConfig().getClassGetter();
            if (debug) log.debug("writeAMF3Object() - classGetter=%s", classGetter);

            Class<?> oClass = classGetter.getClass(o);
            if (debug) log.debug("writeAMF3Object() - oClass=%s", oClass);

            JavaClassDescriptor desc = null;

            // write class description.
            IndexedJavaClassDescriptor iDesc = getFromStoredClassDescriptors(oClass);
            if (iDesc != null) {
                desc = iDesc.getDescriptor();
                writeAMF3IntegerData(iDesc.getIndex() << 2 | 0x01);
            }
            else {
                iDesc = addToStoredClassDescriptors(oClass);
                desc = iDesc.getDescriptor();

                writeAMF3IntegerData((desc.getPropertiesCount() << 4) | (desc.getEncoding() << 2) | 0x03);
                writeAMF3StringData(desc.getName());

                for (int i = 0; i < desc.getPropertiesCount(); i++)
                    writeAMF3StringData(desc.getPropertyName(i));
            }
            if (debug) log.debug("writeAMF3Object() - desc=%s", desc);

            // write object content.
            if (desc.isExternalizable()) {
                Externalizer externalizer = desc.getExternalizer();

                if (externalizer != null) {
                    if (debug) log.debug("writeAMF3Object() - using externalizer=%s", externalizer);
                    try {
                        externalizer.writeExternal(o, this);
                    } catch (IOException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException("Could not externalize object: " + o, e);
                    }
                }
                else {
                    if (debug) log.debug("writeAMF3Object() - legacy Externalizable=%s", o);
                    ((Externalizable)o).writeExternal(this);
                }
            }
            else {
                if (debug) log.debug("writeAMF3Object() - writing defined properties...");
                for (int i = 0; i < desc.getPropertiesCount(); i++) {
                    Object obj = desc.getPropertyValue(i, o);
                    if (debug) log.debug("writeAMF3Object() - writing defined property: %s=%s", desc.getPropertyName(i), obj);
                    writeObject(obj);
                }

                if (desc.isDynamic()) {
                    if (debug) log.debug("writeAMF3Object() - writing dynamic properties...");
                    Map<?, ?> oMap = (Map<?, ?>)o;
                    for (Map.Entry<?, ?> entry : oMap.entrySet()) {
                        Object key = entry.getKey();
                        if (key != null) {
                            String propertyName = key.toString();
                            if (propertyName.length() > 0) {
                                if (debug) log.debug(
                                    "writeAMF3Object() - writing dynamic property: %s=%s",
                                    propertyName, entry.getValue()
                                );
                                writeAMF3StringData(propertyName);
                                writeObject(entry.getValue());
                            }
                        }
                    }
                    writeAMF3StringData("");
                }
            }
        }

        if (debug) log.debug("writeAMF3Object(o=%s) - Done", o);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cached objects methods.

    protected void addToStoredStrings(String s) {
        if (!storedStrings.containsKey(s)) {
            Integer index = Integer.valueOf(storedStrings.size());
            if (debug) log.debug("addToStoredStrings(s=%s) at index=%d", s, index);
            storedStrings.put(s, index);
        }
    }

    protected int indexOfStoredStrings(String s) {
        Integer index = storedStrings.get(s);
        if (debug) log.debug("indexOfStoredStrings(s=%s) -> %d", s, (index != null ? index : -1));
        return (index != null ? index : -1);
    }

    protected void addToStoredObjects(Object o) {
        if (o != null && !storedObjects.containsKey(o)) {
            Integer index = Integer.valueOf(storedObjects.size());
            if (debug) log.debug("addToStoredObjects(o=%s) at index=%d", o, index);
            storedObjects.put(o, index);
        }
    }

    protected int indexOfStoredObjects(Object o) {
        Integer index = storedObjects.get(o);
        if (debug) log.debug("indexOfStoredObjects(o=%s) -> %d", o, (index != null ? index : -1));
        return (index != null ? index : -1);
    }

    protected IndexedJavaClassDescriptor addToStoredClassDescriptors(Class<?> clazz) {
        final String name = JavaClassDescriptor.getClassName(clazz);

        if (debug) log.debug("addToStoredClassDescriptors(clazz=%s)", clazz);

        if (storedClassDescriptors.containsKey(name))
            throw new RuntimeException(
                "Descriptor of \"" + name + "\" is already stored at index: " +
                getFromStoredClassDescriptors(clazz).getIndex()
            );

        // find custom class descriptor and instantiate if any
        JavaClassDescriptor desc = null;

        Class<? extends JavaClassDescriptor> descriptorType
            = context.getGraniteConfig().getJavaDescriptor(clazz.getName());
        if (descriptorType != null) {
            Class<?>[] argsDef = new Class[]{Class.class};
            Object[] argsVal = new Object[]{clazz};
            try {
                desc = ClassUtil.newInstance(descriptorType, argsDef, argsVal);
            } catch (Exception e) {
                throw new RuntimeException("Could not instantiate Java descriptor: " + descriptorType);
            }
        }

        if (desc == null)
            desc = new DefaultJavaClassDescriptor(clazz);

        IndexedJavaClassDescriptor iDesc = new IndexedJavaClassDescriptor(storedClassDescriptors.size(), desc);

        if (debug) log.debug("addToStoredClassDescriptors() - putting: name=%s, iDesc=%s", name, iDesc);

        storedClassDescriptors.put(name, iDesc);

        return iDesc;
    }

    protected IndexedJavaClassDescriptor getFromStoredClassDescriptors(Class<?> clazz) {
        if (debug) log.debug("getFromStoredClassDescriptors(clazz=%s)", clazz);

        String name = JavaClassDescriptor.getClassName(clazz);
        IndexedJavaClassDescriptor iDesc = storedClassDescriptors.get(name);

        if (debug) log.debug("getFromStoredClassDescriptors() -> %s", iDesc);

        return iDesc;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utilities.

    protected Channel getChannel() {
        if (channel == null) {
            String channelId = context.getAMFContext().getChannelId();
            if (channelId != null) {
                channel = context.getServicesConfig().findChannelById(channelId);
                if (channel == null)
                    log.warn("Could not get channel for channel id: %s", channelId);
            }
            else if (warnOnChannelIdMissing)
                log.warn("Could not get channel id for message: %s", context.getAMFContext().getRequest());
        }
        return channel;
    }
}
