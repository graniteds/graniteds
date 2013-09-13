
package org.granite.test.amf.convert.seam;

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
import org.granite.util.XMLUtilFactory;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import flex.messaging.io.ArrayCollection;

/**
 * @author Franck WOLFF
 */
@SuppressWarnings({ "unused", "boxing" })
public class TestSeamConverters {
	    
    @SuppressWarnings("all")
    private List<ValueExpression> listOfValueExpressions = null;
    private Field listOfValueExpressionsField = null;
    

    ///////////////////////////////////////////////////////////////////////////
    // Initialization

    private Converters converters = null;

    @Before
    public void setUp() throws Exception {
        try {
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
