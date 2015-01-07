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
package org.granite.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public class StringUtil {
	
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public static char[] bytesToHexChars(byte[] bytes) {
    	return bytesToHexChars(bytes, new char[bytes.length * 2], 0);
    }

    public static char[] bytesToHexChars(byte[] bytes, char[] chars, int off) {
    	if (chars.length - off < bytes.length * 2)
    		throw new IllegalArgumentException("Unsufficient capacity in 'chars' parameter");
    	
    	for (int i = off, j = 0; i < bytes.length; ) {
    		int b = bytes[i++] & 0xFF;
    		chars[j++] = HEX_CHARS[b >> 4];
    		chars[j++] = HEX_CHARS[b & 0x0F];
    	}
    	
    	return chars;
    }

    public static byte[] hexCharsToBytes(char[] chars) {
    	return hexCharsToBytes(chars, 0);
    }
    
    public static byte[] hexCharsToBytes(char[] chars, int off) {
    	final int len = chars.length;
    	if (((len - off) % 2) != 0)
    		throw new IllegalArgumentException("(chars.length - off) must be even");
    	
    	byte[] bytes = new byte[(len - off) / 2];
    	for (int i = off, j = 0; i < len; ) {
    		int b = 0;
    		char c = chars[i++];
    		b |= ((c - (c < 'A' ? '0' : ('A' - 10))) << 4);
    		c = chars[i++];
    		b |= (c - (c < 'A' ? '0' : ('A' - 10)));
    		bytes[j++] = (byte)b;
    	}
    	return bytes;
    }

    public static byte[] hexStringToBytes(String s) {
    	return hexStringToBytes(s, 0);
    }
    
    public static byte[] hexStringToBytes(String s, int off) {
    	final int len = s.length();
    	if (((len - off) % 2) != 0)
    		throw new IllegalArgumentException("(s.length() - off) must be even");
    	
    	byte[] bytes = new byte[(len - off) / 2];
    	for (int i = off, j = 0; i < len; ) {
    		int b = 0;
    		char c = s.charAt(i++);
    		b |= ((c - (c < 'A' ? '0' : ('A' - 10))) << 4);
    		c = s.charAt(i++);
    		b |= (c - (c < 'A' ? '0' : ('A' - 10)));
    		bytes[j++] = (byte)b;
    	}
    	return bytes;
    }
    
    public static String toHexString(Number n) {
        if (n == null)
            return "null";

        byte[] bytes = new byte[8];
        ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes);
        LongBuffer longBuffer = bytesBuffer.asLongBuffer();
        longBuffer.put(0, n.longValue());

        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            if (b != 0 || sb.length() > 0 || i == (bytes.length - 1))
                sb.append(HEX_CHARS[b >> 4]).append(HEX_CHARS[b & 0x0F]);
        }
        return sb.toString();
    }
	
	public static String removeSpaces(String s) {
		if (s == null)
			return null;
    	String[] tokens = s.split("\\s", -1);
    	if (tokens.length == 0)
    		return "";
    	if (tokens.length == 1)
    		return tokens[0];
    	StringBuilder sb = new StringBuilder();
    	for (String token : tokens)
    		sb.append(token);
    	return sb.toString();
    }
	
	public static String[] toStringArray(String s) {
		if (s == null)
			return new String[0];

		List<String> lines = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new StringReader(s));
			String line = null;
			while ((line = reader.readLine()) != null)
				lines.add(line);
		}
		catch (IOException e) {
			// can't happen...
		}
		return lines.toArray(new String[lines.size()]);
	}

    public static String toString(Object o) {
        return toString(o, -1);
    }

    public static String toString(Object o, int maxItems) {
        if (o == null)
            return "null";

        if (o instanceof String)
            return ("\"" + o + "\"");

        if (o instanceof Character || o.getClass() == Character.TYPE)
            return ("'" + o + "'");

        if (o instanceof Number) {
            if (o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long)
                return o + " <0x" + toHexString((Number)o) + ">";
            return String.valueOf(o);
        }

        // Enclose code in try catch block for uninitialized proxy exceptions (and the like).
        try {
            if (o.getClass().isArray()) {
                Class<?> type = o.getClass().getComponentType();

                if (maxItems < 0) {
                    if (type.isPrimitive()) {
                        if (type == Byte.TYPE)
                            return Arrays.toString((byte[])o);
                        if (type == Character.TYPE)
                            return Arrays.toString((char[])o);
                        if (type == Integer.TYPE)
                             return Arrays.toString((int[])o);
                        if (type == Double.TYPE)
                            return Arrays.toString((double[])o);
                        if (type == Long.TYPE)
                            return Arrays.toString((long[])o);
                        if (type == Float.TYPE)
                            return Arrays.toString((float[])o);
                        if (type == Short.TYPE)
                            return Arrays.toString((short[])o);
                        if (type == Boolean.TYPE)
                            return Arrays.toString((boolean[])o);
                        return "[Array of unknown primitive type: " + type + "]"; // Should never append...
                    }
                    return Arrays.toString((Object[])o);
                }

                final int max = Math.min(maxItems, Array.getLength(o));
                List<Object> list = new ArrayList<Object>(max);

                for (int i = 0; i < max; i++)
                    list.add(Array.get(o, i));
                if (max < Array.getLength(o))
                    list.add("(first " + max + '/' + Array.getLength(o) + " elements only...)");

                o = list;
            }
            else if (o instanceof Collection<?> && maxItems >= 0) {

                Collection<?> coll = (Collection<?>)o;
                final int max = Math.min(maxItems, coll.size());
                List<Object> list = new ArrayList<Object>(max);

                int i = 0;
                for (Object item : coll) {
                    if (i >= max) {
                        list.add("(first " + max + '/' + coll.size() + " elements only...)");
                        break;
                    }
                    list.add(item);
                    i++;
                }

                o = list;
            }
            else if (o instanceof Map<?, ?> && maxItems >= 0) {
                Map<?, ?> map = (Map<?, ?>)o;
                final int max = Math.min(maxItems, map.size());
                Map<Object, Object> copy = new HashMap<Object, Object>(max);

                int i = 0;
                for (Map.Entry<?, ?> item : map.entrySet()) {
                    if (i >= max) {
                        copy.put("(first " + max + '/' + map.size() + " elements only...)", "...");
                        break;
                    }
                    copy.put(item.getKey(), item.getValue());
                    i++;
                }

                o = copy;
            }

            return String.valueOf(o);
        }
        catch (Exception e) {
            return o.getClass().getName() + " (exception: " + e.toString() + ")";
        }
    }
}
