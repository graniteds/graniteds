package org.granite.generator.as3;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.granite.util.ClassUtil;
import org.w3c.dom.Document;

public class LCDSAs3TypeFactory implements As3TypeFactory {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private final Map<Class<?>, As3Type> java2As3Type = new HashMap<Class<?>, As3Type>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public LCDSAs3TypeFactory() {
        java2As3Type.put(Double.class, As3Type.NUMBER);
        java2As3Type.put(Double.TYPE, As3Type.NUMBER);
        java2As3Type.put(Float.class, As3Type.NUMBER);
        java2As3Type.put(Float.TYPE, As3Type.NUMBER);
        java2As3Type.put(Long.class, As3Type.NUMBER);
        java2As3Type.put(Long.TYPE, As3Type.NUMBER);
        java2As3Type.put(Integer.class, As3Type.NUMBER);
        java2As3Type.put(Integer.TYPE, As3Type.INT);
        java2As3Type.put(Short.class, As3Type.NUMBER);
        java2As3Type.put(Short.TYPE, As3Type.INT);
        java2As3Type.put(Byte.class, As3Type.NUMBER);
        java2As3Type.put(Byte.TYPE, As3Type.INT);

        java2As3Type.put(Long.class, As3Type.NUMBER);
        java2As3Type.put(Long.TYPE, As3Type.NUMBER);
        java2As3Type.put(BigInteger.class, As3Type.NUMBER);
        java2As3Type.put(BigDecimal.class, As3Type.NUMBER);
        java2As3Type.put(MathContext.class, As3Type.NUMBER);
        java2As3Type.put(RoundingMode.class, As3Type.NUMBER);

        java2As3Type.put(Boolean.class, As3Type.BOOLEAN);
        java2As3Type.put(Boolean.TYPE, As3Type.BOOLEAN);

        java2As3Type.put(String.class, As3Type.STRING);
        java2As3Type.put(Character.class, As3Type.STRING);
        java2As3Type.put(Character.TYPE, As3Type.STRING);
        java2As3Type.put(Locale.class, As3Type.STRING);
        java2As3Type.put(URL.class, As3Type.STRING);
        java2As3Type.put(URI.class, As3Type.STRING);

        java2As3Type.put(Object.class, As3Type.OBJECT);
        java2As3Type.put(Serializable.class, As3Type.OBJECT);

        java2As3Type.put(Enum.class, As3Type.STRING);
    }

	public void configure(boolean externalizeLong, boolean externalizeBigInteger, boolean externalizeBigDecimal) {
	}

	public As3Type getAs3Type(Class<?> jType) {
        As3Type as3Type = getFromCache(jType);

        if (as3Type == null) {
            if (Date.class.isAssignableFrom(jType) || Calendar.class.isAssignableFrom(jType)) {
                as3Type = As3Type.DATE;
            }
            else if (Number.class.isAssignableFrom(jType)) {
                as3Type = As3Type.NUMBER;
            }
            else if (Document.class.isAssignableFrom(jType)) {
                as3Type = As3Type.XML;
            }
            else if (jType.isEnum()) {
            	as3Type = As3Type.STRING;
            }
            else if ("javassist.bytecode.ByteArray".equals(jType.getName())) {
            	as3Type = As3Type.BYTE_ARRAY;
            }
            else if (jType.isArray()) {
                Class<?> componentType = jType.getComponentType();
                if (Byte.class.equals(componentType) || Byte.TYPE.equals(componentType))
                    as3Type = As3Type.BYTE_ARRAY;
                else if (Character.class.equals(componentType) || Character.TYPE.equals(componentType))
                    as3Type = As3Type.STRING;
                else
                    as3Type = As3Type.ARRAY;
            }
            else if (Collection.class.isAssignableFrom(jType)) {
                as3Type = As3Type.ARRAY_COLLECTION;
            }
            else if (Map.class.isAssignableFrom(jType)) {
                as3Type = As3Type.OBJECT;
            }
            else {
                as3Type = createAs3Type(jType);
            }

            putInCache(jType, as3Type);
        }

        return as3Type;
	}

    protected As3Type createAs3Type(Class<?> jType) {
    	String name = jType.getSimpleName();
    	if (jType.isMemberClass())
    		name = jType.getEnclosingClass().getSimpleName() + '$' + jType.getSimpleName();
        return new As3Type(ClassUtil.getPackageName(jType), name);
    }

    protected As3Type getFromCache(Class<?> jType) {
        if (jType == null)
            throw new NullPointerException("jType must be non null");
        return java2As3Type.get(jType);
    }

    protected void putInCache(Class<?> jType, As3Type as3Type) {
        if (jType == null || as3Type == null)
            throw new NullPointerException("jType and as3Type must be non null");
        java2As3Type.put(jType, as3Type);
    }
}
