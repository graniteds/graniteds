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

package org.granite.test.amf.convert;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.Reverter;
import org.granite.messaging.amf.io.convert.impl.Array2Array;
import org.granite.messaging.amf.io.convert.impl.Array2Collection;
import org.granite.messaging.amf.io.convert.impl.Boolean2Boolean;
import org.granite.messaging.amf.io.convert.impl.Character2Character;
import org.granite.messaging.amf.io.convert.impl.Collection2Array;
import org.granite.messaging.amf.io.convert.impl.Collection2Collection;
import org.granite.messaging.amf.io.convert.impl.Compatibility;
import org.granite.messaging.amf.io.convert.impl.Date2Date;
import org.granite.messaging.amf.io.convert.impl.Map2Map;
import org.granite.messaging.amf.io.convert.impl.Number2BigDecimal;
import org.granite.messaging.amf.io.convert.impl.Number2BigInteger;
import org.granite.messaging.amf.io.convert.impl.Number2Byte;
import org.granite.messaging.amf.io.convert.impl.Number2Double;
import org.granite.messaging.amf.io.convert.impl.Number2Float;
import org.granite.messaging.amf.io.convert.impl.Number2Integer;
import org.granite.messaging.amf.io.convert.impl.Number2Long;
import org.granite.messaging.amf.io.convert.impl.Number2Short;
import org.granite.messaging.amf.io.convert.impl.String2Char;
import org.granite.messaging.amf.io.convert.impl.String2CharArray;
import org.granite.messaging.amf.io.convert.impl.String2Document;
import org.granite.messaging.amf.io.convert.impl.String2Locale;
import org.granite.messaging.amf.io.convert.impl.String2URI;
import org.granite.seam21.ValueExpressionConverter;
import org.granite.util.XMLUtil;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import flex.messaging.io.ArrayCollection;

/**
 * @author Franck WOLFF
 */
@SuppressWarnings({ "unchecked", "unused", "boxing", "rawtypes" })
public class DefaultConvertersTest {
	
    private Number[] arrayOfNumbers = new Number[] {
        Byte.valueOf((byte)0),
        Short.valueOf((short)0),
        Integer.valueOf(0),
        Long.valueOf(0L),
        Float.valueOf((float)0.0),
        Double.valueOf(0.0),
        BigDecimal.valueOf(0.0),
        BigDecimal.ZERO,
        BigInteger.ZERO,

        Byte.valueOf(Byte.MIN_VALUE),
        Byte.valueOf(Byte.MAX_VALUE),
        Short.valueOf(Short.MIN_VALUE),
        Short.valueOf(Short.MAX_VALUE),
        Integer.valueOf(Integer.MIN_VALUE),
        Integer.valueOf(Integer.MAX_VALUE),
        Long.valueOf(Long.MIN_VALUE),
        Long.valueOf(Long.MAX_VALUE),
        Float.valueOf(Float.MIN_VALUE),
        Float.valueOf(Float.MAX_VALUE),
        Double.valueOf(Double.MIN_VALUE),
        Double.valueOf(Double.MAX_VALUE),
        BigDecimal.valueOf(Double.MIN_VALUE),
        BigDecimal.valueOf(Double.MAX_VALUE),
        BigInteger.valueOf(Long.MIN_VALUE),
        BigInteger.valueOf(Long.MAX_VALUE)
    };

    private ArrayList<?> arrayListOfWildcard = new ArrayList();
    private Field arrayListOfWildcardField = null;

    private HashMap<?, ?> hashMapOfWildcards = new HashMap();
    private Field hashMapOfWildcardsField = null;

    private Map<String, Integer> mapOfStringInteger = null;
    private Field mapOfStringIntegerField = null;

    private List<?> listOfWildcard = null;
    private Field listOfWildcardField = null;

    private Set<?> setOfWildcard = null;
    private Field setOfWildcardField = null;

    private char primitiveChar = '\0';
    private Field primitiveCharField = null;

    private char[] arrayOfChar = new char[] {'a', 'b', 'c'};
    private Field arrayOfCharField = null;

    private Character[] arrayOfCharacter = new Character[] {'a', 'b', 'c'};
    private Field arrayOfCharacterField = null;

    private Object[] arrayOfObjectString = new Object[]{"abc", null, "def", "", "ghi"};
    private Object[] arrayOfObjectNumber = new Object[]{Integer.valueOf(45), null, Byte.valueOf((byte)12), BigDecimal.TEN, Double.valueOf(6.56)};
    private Long[] arrayOfLong = new Long[]{Long.valueOf(45L), null, Long.valueOf(12L), Long.valueOf(10L), Long.valueOf(6L)};
    private long[] arrayOfPrimitiveLong = new long[]{45L, 0L, 12L, 10L, 6L};

    private List<Object> listOfObject = null;
    private Field listOfObjectField = null;

    private List<Set<String>> listOfSetOfString = null;
    private Field listOfSetOfStringField = null;

    private Set<Long> setOfLong = null;
    private Field setOfLongField = null;
    
    private List<ValueExpression> listOfValueExpressions = null;
    private Field listOfValueExpressionsField = null;
    

    ///////////////////////////////////////////////////////////////////////////
    // Initialization

    private Converters converters = null;

    @Before
    public void setUp() throws Exception {
        try {
            arrayListOfWildcardField = getClass().getDeclaredField("arrayListOfWildcard");
            arrayListOfWildcardField.setAccessible(true);

            hashMapOfWildcardsField = getClass().getDeclaredField("hashMapOfWildcards");
            hashMapOfWildcardsField.setAccessible(true);

            primitiveCharField = getClass().getDeclaredField("primitiveChar");
            primitiveCharField.setAccessible(true);

            arrayOfCharField = getClass().getDeclaredField("arrayOfChar");
            arrayOfCharField.setAccessible(true);

            arrayOfCharacterField = getClass().getDeclaredField("arrayOfCharacter");
            arrayOfCharacterField.setAccessible(true);

            listOfObjectField = getClass().getDeclaredField("listOfObject");
            listOfObjectField.setAccessible(true);

            listOfSetOfStringField = getClass().getDeclaredField("listOfSetOfString");
            listOfSetOfStringField.setAccessible(true);

            setOfLongField = getClass().getDeclaredField("setOfLong");
            setOfLongField.setAccessible(true);

            listOfWildcardField = getClass().getDeclaredField("listOfWildcard");
            listOfWildcardField.setAccessible(true);

            setOfWildcardField = getClass().getDeclaredField("setOfWildcard");
            setOfWildcardField.setAccessible(true);

            mapOfStringIntegerField = getClass().getDeclaredField("mapOfStringInteger");
            mapOfStringIntegerField.setAccessible(true);

            mapOfStringIntegerField = getClass().getDeclaredField("mapOfStringInteger");
            mapOfStringIntegerField.setAccessible(true);

            listOfValueExpressionsField = getClass().getDeclaredField("listOfValueExpressions");
            listOfValueExpressionsField.setAccessible(true);
        } catch (Exception e) {
            fail("Coud not get fields: " + e.toString());
        }

        List<Class<? extends Converter>> converterClasses = new ArrayList<Class<? extends Converter>>();

        converterClasses.add(Number2Integer.class);
        converterClasses.add(Number2Long.class);
        converterClasses.add(Number2Double.class);
        converterClasses.add(Boolean2Boolean.class);
        converterClasses.add(Collection2Collection.class);
        converterClasses.add(Collection2Array.class);
        converterClasses.add(Array2Array.class);
        converterClasses.add(Array2Collection.class);
        converterClasses.add(Date2Date.class);
        converterClasses.add(Map2Map.class);
        converterClasses.add(String2CharArray.class);
        converterClasses.add(Number2BigDecimal.class);
        converterClasses.add(Number2BigInteger.class);
        converterClasses.add(Number2Byte.class);
        converterClasses.add(Number2Float.class);
        converterClasses.add(Number2Short.class);
        converterClasses.add(String2Char.class);
        converterClasses.add(String2Locale.class);
        converterClasses.add(String2URI.class);
        converterClasses.add(Character2Character.class);
        converterClasses.add(Compatibility.class);
        converterClasses.add(String2Document.class);
        converterClasses.add(ValueExpressionConverter.class);

        this.converters = new Converters(converterClasses);
    }

    @After
    public void tearDown() throws Exception {
        converters = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Boolean2Boolean

    @Test
    public void testBoolean2Boolean() {
        testBoolean2Boolean(Boolean.TRUE, Boolean.class);
        testBoolean2Boolean(Boolean.FALSE, Boolean.class);
        testBoolean2Boolean(null, Boolean.class);

        testBoolean2Boolean(Boolean.TRUE, Boolean.TYPE);
        testBoolean2Boolean(Boolean.FALSE, Boolean.TYPE);
        testBoolean2Boolean(null, Boolean.TYPE);

        Converter converter = new Boolean2Boolean(converters);
        assertTrue(converter.canConvert(null, Boolean.class));
        assertEquals(null, converter.convert(null, Boolean.class));
        assertTrue(converter.canConvert(Boolean.TRUE, Boolean.class));
        assertEquals(Boolean.TRUE, converter.convert(Boolean.TRUE, Boolean.class));
        assertTrue(converter.canConvert(Boolean.FALSE, Boolean.class));
        assertEquals(Boolean.FALSE, converter.convert(Boolean.FALSE, Boolean.class));
        assertTrue(converter.canConvert(null, Boolean.TYPE));
        assertEquals(Boolean.FALSE, converter.convert(null, Boolean.TYPE));
        assertTrue(converter.canConvert(Boolean.TRUE, Boolean.TYPE));
        assertEquals(Boolean.TRUE, converter.convert(Boolean.TRUE, Boolean.TYPE));
        assertTrue(converter.canConvert(Boolean.FALSE, Boolean.class));
        assertEquals(Boolean.FALSE, converter.convert(Boolean.FALSE, Boolean.TYPE));
    }
    private void testBoolean2Boolean(Object value, Type targetType) {
        Converter converter = converters.getConverter(value, targetType);
        assertNotNull(converter);
        assertEquals(Boolean2Boolean.class, converter.getClass());
        assertTrue(converter.canConvert(value, targetType));
        if (targetType == Boolean.class || value != null)
            assertEquals(value, converter.convert(value, targetType));
        else
            assertEquals(Boolean.FALSE, converter.convert(value, targetType));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Number2<Number>

    @Test
    public void testNumber2Number() {
        testNumber2Number(new Number2Byte(converters), Byte.class, Byte.TYPE, new DirectConverter() {
            public Object convert(Object o) {
                return o instanceof Byte ? o : Byte.valueOf(((Number)o).byteValue());
            }
        });
        testNumber2Number(new Number2Short(converters), Short.class, Short.TYPE, new DirectConverter() {
            public Object convert(Object o) {
                return o instanceof Short ? o : Short.valueOf(((Number)o).shortValue());
            }
        });
        testNumber2Number(new Number2Integer(converters), Integer.class, Integer.TYPE, new DirectConverter() {
            public Object convert(Object o) {
                return o instanceof Integer ? o : Integer.valueOf(((Number)o).intValue());
            }
        });
        testNumber2Number(new Number2Long(converters), Long.class, Long.TYPE, new DirectConverter() {
            public Object convert(Object o) {
                return o instanceof Long ? o : Long.valueOf(((Number)o).longValue());
            }
        });

        testNumber2Number(new Number2Float(converters), Float.class, Float.TYPE, new DirectConverter() {
            public Object convert(Object o) {
                return o instanceof Float ? o : Float.valueOf(((Number)o).floatValue());
            }
        });
        testNumber2Number(new Number2Double(converters), Double.class, Double.TYPE, new DirectConverter() {
            public Object convert(Object o) {
                return o instanceof Double ? o : Double.valueOf(((Number)o).doubleValue());
            }
        });
        testNumber2Number(new Number2BigDecimal(converters), BigDecimal.class, null, new DirectConverter() {
            public Object convert(Object o) {
                return o instanceof BigDecimal ? o : BigDecimal.valueOf(((Number)o).doubleValue());
            }
        });
        testNumber2Number(new Number2BigInteger(converters), BigInteger.class, null, new DirectConverter() {
            public Object convert(Object o) {
                return o instanceof BigInteger ? o : BigInteger.valueOf(((Number)o).longValue());
            }
        });
    }
    private void testNumber2Number(Converter numberConverter, Type targetType, Type targetPrimitiveType, DirectConverter directConverter) {
        // Test for various numbers.
        for (Number number : arrayOfNumbers) {
            Converter converter = converters.getConverter(number, targetType);
            assertNotNull(converter);
            assertEquals(numberConverter.getClass(), converter.getClass());
            assertTrue(converter.canConvert(number, targetType));
            Object result = converter.convert(number, targetType);
            assertEquals(targetType, result.getClass());
            assertEquals(directConverter.convert(number), result);
        }

        // Test for null (with primitive conversion if any).
        assertTrue(numberConverter.canConvert(null, targetType));
        assertEquals(null, numberConverter.convert(null, targetType));
        if (targetPrimitiveType != null) {
            assertTrue(numberConverter.canConvert(null, targetPrimitiveType));
            Object result = numberConverter.convert(null, targetPrimitiveType);
            assertEquals(targetType, result.getClass());
            assertEquals(directConverter.convert(Integer.valueOf(0)), result);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // String2Char

    @Test
    public void testString2Char() {
        Converter converter = converters.getConverter("a", Character.class);
        assertNotNull(converter);
        assertEquals(String2Char.class, converter.getClass());
        assertTrue(converter.canConvert("a", Character.class));
        assertEquals(Character.valueOf('a'), converter.convert("a", Character.class));

        converter = converters.getConverter("", Character.class);
        assertNotNull(converter);
        assertEquals(String2Char.class, converter.getClass());
        assertTrue(converter.canConvert("", Character.class));
        assertEquals(null, converter.convert("", Character.class));

        converter = converters.getConverter("a", Character.TYPE);
        assertNotNull(converter);
        assertEquals(String2Char.class, converter.getClass());
        assertTrue(converter.canConvert("a", Character.TYPE));
        assertEquals(Character.valueOf('a'), converter.convert("a", Character.TYPE));

        converter = converters.getConverter("", Character.TYPE);
        assertNotNull(converter);
        assertEquals(String2Char.class, converter.getClass());
        assertTrue(converter.canConvert("", Character.TYPE));
        assertEquals(Character.valueOf('\0'), converter.convert("", Character.TYPE));

        converter = new String2Char(converters);

        assertTrue(converter.canConvert(null, Character.class));
        assertEquals(null, converter.convert(null, Character.class));

        assertTrue(converter.canConvert(null, Character.TYPE));
        Object result = converter.convert(null, Character.TYPE);
        assertEquals(Character.valueOf('\0'), result);

        try {
            primitiveCharField.set(this, result);
        } catch (Exception e) {
            fail("Cannot set value of: " + primitiveCharField + " - " + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // String2CharArray

    @Test
    public void testString2CharArray() {
        // Character[]
        Converter converter = converters.getConverter("abc", Character[].class);
        assertNotNull(converter);
        assertEquals(String2CharArray.class, converter.getClass());
        assertTrue(converter.canConvert("abc", Character[].class));
        Object result = converter.convert("abc", Character[].class);
        assertEquals(Character[].class, result.getClass());
        assertArrayEquals(arrayOfCharacter, (Character[])result);
        try {
            arrayOfCharacterField.set(this, result);
        } catch (Exception e) {
            fail("Cannot set value of: " + arrayOfCharacterField + " - " + e.toString());
        }

        // char[]
        converter = converters.getConverter("abc", char[].class);
        assertNotNull(converter);
        assertEquals(String2CharArray.class, converter.getClass());
        assertTrue(converter.canConvert("abc", char[].class));
        result = converter.convert("abc", char[].class);
        assertEquals(char[].class, result.getClass());
        assertArrayEquals(arrayOfChar, (char[])result);
        try {
            arrayOfCharField.set(this, result);
        } catch (Exception e) {
            fail("Cannot set value of: " + arrayOfCharField + " - " + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // String2Locale

    @Test
    public void testString2Locale() {
        Converter converter = converters.getConverter("en", Locale.class);
        assertNotNull(converter);
        assertEquals(String2Locale.class, converter.getClass());
        assertTrue(converter.canConvert("en", Locale.class));
        Object result = converter.convert("en", Locale.class);
        assertEquals(Locale.class, result.getClass());
        assertEquals(Locale.ENGLISH, result);
        assertTrue(((Reverter)converter).canRevert(result));
        assertEquals("en", ((Reverter)converter).revert(result));
        assertEquals("en", converters.revert(result));
        
        converter = converters.getConverter("en_US", Locale.class);
        assertNotNull(converter);
        assertEquals(String2Locale.class, converter.getClass());
        assertTrue(converter.canConvert("en_US", Locale.class));
        result = converter.convert("en_US", Locale.class);
        assertEquals(Locale.class, result.getClass());
        assertEquals(result, Locale.US);
        assertTrue(((Reverter)converter).canRevert(result));
        assertEquals("en_US", ((Reverter)converter).revert(result));
        assertEquals("en_US", converters.revert(result));

        converter = converters.getConverter("es_ES_Traditional_WIN", Locale.class);
        assertNotNull(converter);
        assertEquals(String2Locale.class, converter.getClass());
        assertTrue(converter.canConvert("es_ES_Traditional_WIN", Locale.class));
        result = converter.convert("es_ES_Traditional_WIN", Locale.class);
        assertEquals(Locale.class, result.getClass());
        assertEquals(result, new Locale("es", "ES", "Traditional_WIN"));
        assertTrue(((Reverter)converter).canRevert(result));
        assertEquals("es_ES_Traditional_WIN", ((Reverter)converter).revert(result));
        assertEquals("es_ES_Traditional_WIN", converters.revert(result));
    }

    ///////////////////////////////////////////////////////////////////////////
    // String2URI

    @Test
    public void testString2URI() {
        Converter converter = converters.getConverter("http://www.graniteds.org", URI.class);
        assertNotNull(converter);
        assertEquals(String2URI.class, converter.getClass());
        assertTrue(converter.canConvert("http://www.graniteds.org", URI.class));
        Object result = converter.convert("http://www.graniteds.org", URI.class);
        assertEquals(URI.class, result.getClass());
        try {
        	assertEquals(new URI("http://www.graniteds.org"), result);
        } catch (Exception e) {
        	fail(e.toString());
        }
        assertTrue(((Reverter)converter).canRevert(result));
        assertEquals("http://www.graniteds.org", ((Reverter)converter).revert(result));
        assertEquals("http://www.graniteds.org", converters.revert(result));

        converter = converters.getConverter("http://www.graniteds.org", URL.class);
        assertNotNull(converter);
        assertEquals(String2URI.class, converter.getClass());
        assertTrue(converter.canConvert("http://www.graniteds.org", URL.class));
        result = converter.convert("http://www.graniteds.org", URL.class);
        assertEquals(URL.class, result.getClass());
        try {
        	assertEquals(new URL("http://www.graniteds.org"), result);
        } catch (Exception e) {
        	fail(e.toString());
        }
        assertTrue(((Reverter)converter).canRevert(result));
        assertEquals("http://www.graniteds.org", ((Reverter)converter).revert(result));
        assertEquals("http://www.graniteds.org", converters.revert(result));
    }

    ///////////////////////////////////////////////////////////////////////////
    // String2Document

    @Test
    public void testString2Document() {
    	
    	final String xmlString =
    		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    		"<doc attr=\"attr value\">\n" +
    		"    <elt>blabla</elt>\n" +
    		"</doc>";
    	
    	final XMLUtil xmlUtil = new XMLUtil();
    	final Document doc = xmlUtil.buildDocument(xmlString);
    	
        Converter converter = converters.getConverter(xmlString, Document.class);
        assertNotNull(converter);
        assertEquals(String2Document.class, converter.getClass());
        assertTrue(converter.canConvert(xmlString, Document.class));
        Object result = converter.convert(xmlString, Document.class);
        assertTrue(result instanceof Document);
        assertTrue(doc.isEqualNode((Document)result));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Array2Array

    @Test
    public void testArray2Array() {
        // Object[] -> String[]
        Converter converter = converters.getConverter(arrayOfObjectString, String[].class);
        assertNotNull(converter);
        assertEquals(Array2Array.class, converter.getClass());
        assertTrue(converter.canConvert(arrayOfObjectString, String[].class));
        Object result = converter.convert(arrayOfObjectString, String[].class);
        assertEquals(String[].class, result.getClass());
        assertArrayEquals(arrayOfObjectString, (String[])result);

        // Object[] -> Long[]
        converter = converters.getConverter(arrayOfObjectNumber, Long[].class);
        assertNotNull(converter);
        assertEquals(Array2Array.class, converter.getClass());
        assertTrue(converter.canConvert(arrayOfObjectNumber, Long[].class));
        result = converter.convert(arrayOfObjectNumber, Long[].class);
        assertEquals(Long[].class, result.getClass());
        assertArrayEquals(arrayOfLong, (Long[])result);

        // Object[] -> long[]
        converter = converters.getConverter(arrayOfObjectNumber, long[].class);
        assertNotNull(converter);
        assertEquals(Array2Array.class, converter.getClass());
        assertTrue(converter.canConvert(arrayOfObjectNumber, long[].class));
        result = converter.convert(arrayOfObjectNumber, long[].class);
        assertEquals(long[].class, result.getClass());
        assertArrayEquals(arrayOfPrimitiveLong, (long[])result);

        // Object[][] -> String[][]
        Object[][] objects = new Object[][] {
            {"abc", "def", ""},
            {null, "yz"}
        };
        converter = converters.getConverter(objects, String[][].class);
        assertNotNull(converter);
        assertEquals(Array2Array.class, converter.getClass());
        assertTrue(converter.canConvert(objects, String[][].class));
        result = converter.convert(objects, String[][].class);
        assertEquals(String[][].class, result.getClass());
        assertArrayEquals(objects[0], ((String[][])result)[0]);
        assertArrayEquals(objects[1], ((String[][])result)[1]);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Collection2Array

    @Test
    public void testCollection2Array() {
        // List<?> -> String[]
        listOfWildcard = Arrays.asList(arrayOfObjectString);
        Converter converter = converters.getConverter(listOfWildcard, String[].class);
        assertNotNull(converter);
        assertEquals(Collection2Array.class, converter.getClass());
        assertTrue(converter.canConvert(listOfWildcard, String[].class));
        Object result = converter.convert(listOfWildcard, String[].class);
        assertEquals(String[].class, result.getClass());
        assertArrayEquals(arrayOfObjectString, (String[])result);

        // List<?> -> Long[]
        listOfWildcard = Arrays.asList(arrayOfObjectNumber);
        converter = converters.getConverter(listOfWildcard, Long[].class);
        assertNotNull(converter);
        assertEquals(Collection2Array.class, converter.getClass());
        assertTrue(converter.canConvert(listOfWildcard, Long[].class));
        result = converter.convert(listOfWildcard, Long[].class);
        assertEquals(Long[].class, result.getClass());
        assertArrayEquals(arrayOfLong, (Long[])result);

        // List<?> -> long[]
        converter = converters.getConverter(listOfWildcard, long[].class);
        assertNotNull(converter);
        assertEquals(Collection2Array.class, converter.getClass());
        assertTrue(converter.canConvert(listOfWildcard, long[].class));
        result = converter.convert(listOfWildcard, long[].class);
        assertEquals(long[].class, result.getClass());
        assertArrayEquals(arrayOfPrimitiveLong, (long[])result);

        // Set<?> -> String[]
        setOfWildcard = new HashSet<Object>(Arrays.asList(arrayOfObjectString));
        converter = converters.getConverter(setOfWildcard, String[].class);
        assertNotNull(converter);
        assertEquals(Collection2Array.class, converter.getClass());
        assertTrue(converter.canConvert(setOfWildcard, String[].class));
        result = converter.convert(setOfWildcard, String[].class);
        assertEquals(String[].class, result.getClass());
        assertTrue(sameElements(arrayOfObjectString, (String[])result));

        // Set<?> -> Long[]
        setOfWildcard = new HashSet<Object>(Arrays.asList(arrayOfObjectNumber));
        converter = converters.getConverter(setOfWildcard, Long[].class);
        assertNotNull(converter);
        assertEquals(Collection2Array.class, converter.getClass());
        assertTrue(converter.canConvert(setOfWildcard, Long[].class));
        result = converter.convert(setOfWildcard, Long[].class);
        assertEquals(Long[].class, result.getClass());
        assertTrue(sameElements(arrayOfLong, (Long[])result));

        // Set<?> -> long[]
        converter = converters.getConverter(setOfWildcard, long[].class);
        assertNotNull(converter);
        assertEquals(Collection2Array.class, converter.getClass());
        assertTrue(converter.canConvert(setOfWildcard, long[].class));
        result = converter.convert(setOfWildcard, long[].class);
        assertEquals(long[].class, result.getClass());
        assertTrue(sameElements(arrayOfPrimitiveLong, (long[])result));

        // List<Set<Object>> -> String[][]
        List<Set<Object>> list = new ArrayList<Set<Object>>();
        list.add(new HashSet<Object>(Arrays.asList(new Object[]{"abc", "def", ""})));
        list.add(new HashSet<Object>(Arrays.asList(new Object[]{null, "yz"})));
        converter = converters.getConverter(list, String[][].class);
        assertNotNull(converter);
        assertEquals(Collection2Array.class, converter.getClass());
        assertTrue(converter.canConvert(list, String[][].class));
        result = converter.convert(list, String[][].class);
        assertEquals(String[][].class, result.getClass());
        assertTrue(sameElements(list.get(0).toArray(), ((String[][])result)[0]));
        assertTrue(sameElements(list.get(1).toArray(), ((String[][])result)[1]));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Collection2Collection

    @Test
    public void testCollection2Collection() {
        // List<?> -> Collection (no conversion, same instance)
        listOfWildcard = Arrays.asList(arrayOfObjectString);
        Converter converter = converters.getConverter(listOfWildcard, Collection.class);
        assertNotNull(converter);
        assertEquals(Collection2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(listOfWildcard, Collection.class));
        Object result = converter.convert(listOfWildcard, Collection.class);
        assertTrue(listOfWildcard == result);
        assertEquals(Arrays.asList(arrayOfObjectString), result);

        // List<?> -> List (no conversion, same instance)
        listOfWildcard = Arrays.asList(arrayOfObjectString);
        converter = converters.getConverter(listOfWildcard, List.class);
        assertNotNull(converter);
        assertEquals(Collection2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(listOfWildcard, List.class));
        result = converter.convert(listOfWildcard, List.class);
        assertTrue(listOfWildcard == result);
        assertEquals(Arrays.asList(arrayOfObjectString), result);

        // Set<?> -> Collection (no conversion, same instance)
        setOfWildcard = new HashSet<Object>(Arrays.asList(arrayOfObjectString));
        converter = converters.getConverter(setOfWildcard, Collection.class);
        assertNotNull(converter);
        assertEquals(Collection2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(setOfWildcard, Collection.class));
        result = converter.convert(setOfWildcard, Collection.class);
        assertTrue(setOfWildcard == result);
        assertEquals(new HashSet<Object>(Arrays.asList(arrayOfObjectString)), result);

        // List<?> -> List<Object> (no conversion, same instance)
        Type targetType = listOfObjectField.getGenericType();
        listOfWildcard = Arrays.asList(arrayOfObjectString);
        converter = converters.getConverter(listOfWildcard, targetType);
        assertNotNull(converter);
        assertEquals(Collection2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(listOfWildcard, targetType));
        result = converter.convert(listOfWildcard, targetType);
        assertTrue(listOfWildcard == result);
        assertEquals(Arrays.asList(arrayOfObjectString), result);

        // ArrayList<?> -> List<?> (no conversion, same instance)
        targetType = listOfWildcardField.getGenericType();
        listOfWildcard = Arrays.asList(arrayOfObjectString);
        converter = converters.getConverter(listOfWildcard, targetType);
        assertNotNull(converter);
        assertEquals(Collection2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(listOfWildcard, targetType));
        result = converter.convert(listOfWildcard, targetType);
        assertTrue(listOfWildcard == result);
        assertEquals(Arrays.asList(arrayOfObjectString), result);

        // List<?> -> Set<Long> (collection + items conversions)
        targetType = setOfLongField.getGenericType();
        listOfWildcard = Arrays.asList(arrayOfObjectNumber);
        converter = converters.getConverter(listOfWildcard, targetType);
        assertNotNull(converter);
        assertEquals(Collection2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(listOfWildcard, targetType));
        result = converter.convert(listOfWildcard, targetType);
        assertEquals(HashSet.class, result.getClass());
        assertEquals(new HashSet<Long>(Arrays.asList(arrayOfLong)), result);

        // Set<List<Object>> -> List<Set<String>>
        Set<List<Object>> set = new HashSet<List<Object>>();
        set.add(new ArrayList<Object>(Arrays.asList(new Object[]{"abc", "def", ""})));
        set.add(new ArrayList<Object>(Arrays.asList(new Object[]{null, "yz"})));
        targetType = listOfSetOfStringField.getGenericType();
        converter = converters.getConverter(set, targetType);
        assertNotNull(converter);
        assertEquals(Collection2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(set, targetType));
        result = converter.convert(set, targetType);
        assertEquals(ArrayList.class, result.getClass());
        assertEquals(HashSet.class, ((List<?>)result).get(0).getClass());
        assertEquals(HashSet.class, ((List<?>)result).get(1).getClass());
        boolean first = true;
        int iMatch = 0;
        for (List<?> list : set) {
            if (first) {
                first = false;
                if (sameElements(list.toArray(), ((List<Set<?>>)result).get(0).toArray()))
                    iMatch = 0;
                else if (sameElements(list.toArray(), ((List<Set<?>>)result).get(1).toArray()))
                    iMatch = 1;
                else
                    fail("Cannot find a match between " + result + " and " + set);
            }
            else if (!sameElements(list.toArray(), ((List<Set<?>>)result).get((iMatch + 1) % 2).toArray()))
                fail("Cannot find a match between " + result + " and " + set);
        }
        try {
            listOfSetOfStringField.set(this, result);
            for (Set<String> st : listOfSetOfString) {
                for (String s : st)
                    assertTrue(s == null || s.length() >= 0);
            }
        } catch (Exception e) {
            fail("Cannot set value of: " + listOfSetOfStringField + " - " + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Array2Collection

    @Test
    public void testArray2Collection() {
        // Object[] -> Collection
        Converter converter = converters.getConverter(arrayOfObjectString, Collection.class);
        assertNotNull(converter);
        assertEquals(Array2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(arrayOfObjectString, Collection.class));
        Object result = converter.convert(arrayOfObjectString, Collection.class);
        assertEquals(ArrayList.class, result.getClass());
        assertEquals(Arrays.asList(arrayOfObjectString), result);

        // String[] -> SortedSet
        final String[] strings = {"f", "d", "e", "abc", "", "gds"};
        converter = converters.getConverter(strings, SortedSet.class);
        assertNotNull(converter);
        assertEquals(Array2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(strings, SortedSet.class));
        result = converter.convert(strings, SortedSet.class);
        assertEquals(TreeSet.class, result.getClass());
        assertEquals(new TreeSet<Object>(Arrays.asList(strings)), result);

        // Object[] -> Set<Long> (collection + items conversions)
        Type targetType = setOfLongField.getGenericType();
        converter = converters.getConverter(arrayOfObjectNumber, targetType);
        assertNotNull(converter);
        assertEquals(Array2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(arrayOfObjectNumber, targetType));
        result = converter.convert(arrayOfObjectNumber, targetType);
        assertEquals(HashSet.class, result.getClass());
        assertEquals(new HashSet<Long>(Arrays.asList(arrayOfLong)), result);

        // Object[][] -> List<Set<String>>
        Object[][] objects = new Object[][]{
            {"abc", "def", ""},
            {null, "yz"}
        };
        targetType = listOfSetOfStringField.getGenericType();
        converter = converters.getConverter(objects, targetType);
        assertNotNull(converter);
        assertEquals(Array2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(objects, targetType));
        result = converter.convert(objects, targetType);
        assertEquals(ArrayList.class, result.getClass());
        assertEquals(HashSet.class, ((List<?>)result).get(0).getClass());
        assertEquals(HashSet.class, ((List<?>)result).get(1).getClass());
        boolean first = true;
        int iMatch = 0;
        for (Object[] oo : objects) {
            if (first) {
                first = false;
                if (sameElements(oo, ((List<Set<?>>)result).get(0).toArray()))
                    iMatch = 0;
                else if (sameElements(oo, ((List<Set<?>>)result).get(1).toArray()))
                    iMatch = 1;
                else
                    fail("Cannot find a match between " + result + " and " + Arrays.toString(objects));
            }
            else if (!sameElements(oo, ((List<Set<?>>)result).get((iMatch + 1) % 2).toArray()))
                fail("Cannot find a match between " + result + " and " + Arrays.toString(objects));
        }
        try {
            listOfSetOfStringField.set(this, result);
            for (Set<String> st : listOfSetOfString) {
                for (String s : st)
                    assertTrue(s == null || s.length() >= 0);
            }
        } catch (Exception e) {
            fail("Cannot set value of: " + listOfSetOfStringField + " - " + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Map2Map

    @Test
    public void testMap2Map() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("one", Byte.valueOf((byte)1));
        map.put("ten", BigDecimal.TEN);
        map.put(null, null);
        map.put("one hundred twenty three", Integer.valueOf(123));
        map.put("zero", BigInteger.ZERO);

        Type targetType = mapOfStringIntegerField.getGenericType();
        Converter converter = converters.getConverter(map, targetType);
        assertNotNull(converter);
        assertEquals(Map2Map.class, converter.getClass());
        assertTrue(converter.canConvert(map, targetType));
        Object result = converter.convert(map, targetType);
        assertEquals(HashMap.class, result.getClass());
        for (Map.Entry<?, ?> entry : ((Map<?, ?>)result).entrySet()) {
            assertTrue(entry.getKey() == null || entry.getKey() instanceof String);
            assertTrue(entry.getValue() == null || entry.getValue() instanceof Integer);
        }
        try {
            mapOfStringIntegerField.set(this, result);
        } catch (Exception e) {
            fail("Cannot set value of: " + mapOfStringIntegerField + " - " + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Compatibility

    public static interface I {/**/}
    public static class A implements I {/**/}
    public static class B extends A {/**/}
    public static class C<T> {C<? extends T> c = this;}
    public static class D<T> {D<T> d = this;}

	public static abstract class AbstractParent<T extends AbstractParent> {
		T parent;

		public T getParent() {
			return parent;
		}

		public void setParent(T parent) {
			this.parent = parent;
		}
	}

	public static class Child extends AbstractParent {/**/}
	
	public static class SubChild extends AbstractParent<Child> {/**/}

    @Test
    public void testCompatibility() {
        A a = new A();
        B b = new B();
        C<A> ca = new C<A>();
        D<A> da = new D<A>();

        Converter converter = converters.getConverter(b, A.class);
        assertNotNull(converter);
        assertEquals(Compatibility.class, converter.getClass());
        assertTrue(converter.canConvert(b, A.class));
        Object result = converter.convert(b, A.class);
        assertTrue(b == result);
        
        SubChild c = new SubChild();
        c.setParent(new Child());
        Type targetType = null;
        try {
        	targetType = c.getClass().getSuperclass().getDeclaredField("parent").getGenericType();
        } catch (Exception e) {
        	fail("Could not get 'parent' field type in: " + c);
        }
        converter = converters.getConverter(c, targetType);
        assertNotNull(converter);
        assertEquals(Compatibility.class, converter.getClass());
        assertTrue(converter.canConvert(c, targetType));
        result = converter.convert(c, targetType);
        assertTrue(result == c);
        
        // test whether Compatibility converter is found for wildcarded generic field C.c
        try {
        	targetType = C.class.getDeclaredField("c").getGenericType();
        }
        catch (NoSuchFieldException e) {
        	fail("Could not get field 'c' in class: " + C.class);
        }
        converter = converters.getConverter(ca.c, targetType);
        assertNotNull(converter);
        assertEquals(Compatibility.class, converter.getClass());
        result = converter.convert(ca.c, targetType);
        assertTrue(result == ca.c);

        // test whether Compatibility converter is found for generic field D.d
        try {
	        targetType = D.class.getDeclaredField("d").getGenericType();
	    }
	    catch (NoSuchFieldException e) {
	    	fail("Could not get field 'd' in class: " + D.class);
	    }
        converter = converters.getConverter(da.d, targetType);
        assertNotNull(converter);
        assertEquals(Compatibility.class, converter.getClass());
        result = converter.convert(da.d, targetType);
        assertTrue(result == da.d);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utilities.

    private static boolean sameElements(Object[] a1, Object[] a2) {
        if (a1.length != a2.length)
            return false;
        List<?> l2 = new ArrayList<Object>(Arrays.asList(a2));
        for (Object o1 : a1) {
            if (!l2.remove(o1))
                return false;
        }
        return l2.isEmpty(); // cannot be false, anyway...
    }

    @SuppressWarnings("boxing")
    private static boolean sameElements(long[] a1, long[] a2) {
        if (a1.length != a2.length)
            return false;
        long[] aa2 = new long[a2.length];
        System.arraycopy(a2, 0, aa2, 0, a2.length);
        for (long o1 : a1) {
            boolean found = false;
            for (int i = 0; i < aa2.length; i++) {
                if (o1 == aa2[i]) {
                    long[] tmp = new long[aa2.length - 1];
                    if (i > 0)
                        System.arraycopy(aa2, 0, tmp, 0, i);
                    if (i < tmp.length)
                        System.arraycopy(aa2, i+1, tmp, i, tmp.length - i);
                    aa2 = tmp;
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;
        }
        return aa2.length == 0; // cannot be false, anyway...
    }

    private static interface DirectConverter {
        public Object convert(Object o);
    }

    ///////////////////////////////////////////////////////////////////////////
    // List<ValueExpression> to ArrayCollection<String>

    @Test
    public void testVE2String() {
        ArrayCollection exprs = new ArrayCollection();
        exprs.add("p.firstName like #{examplePerson.firstName}");
        exprs.add("p.lastName like #{examplePerson.lastName}");
        exprs.add("ppp");

        Type targetType = listOfValueExpressionsField.getGenericType();
        Converter converter = converters.getConverter(exprs, targetType);
        assertNotNull(converter);
        assertEquals(Collection2Collection.class, converter.getClass());
        assertTrue(converter.canConvert(exprs, targetType));
        Object result = converter.convert(exprs, targetType);
        assertTrue(List.class.isAssignableFrom(result.getClass()));
        for (Object elt : ((List<?>)result))
            assertTrue(elt instanceof ValueExpression);
        
        try {
            listOfValueExpressionsField.set(this, result);
        } catch (Exception e) {
            fail("Cannot set value of: " + listOfValueExpressionsField + " - " + e.toString());
        }
    }
}
