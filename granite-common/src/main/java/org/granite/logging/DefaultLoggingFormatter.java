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
package org.granite.logging;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Formattable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public class DefaultLoggingFormatter implements LoggingFormatter {

    private static final int MAX_COLLECTION_ITEMS = 100;

    private final int maxItems;

    public DefaultLoggingFormatter() {
        this(MAX_COLLECTION_ITEMS);
    }

    public DefaultLoggingFormatter(int maxItems) {
        this.maxItems = maxItems;
    }

    public String format(String message, Object... args) {
        try {
            for (int i = 0; i < args.length; i++)
                args[i] = convert(args[i]);

            return String.format(message, args);
        } catch (Exception e) {
            try {
                return "[FORMATTER ERROR] \"" + message + "\"(" + Arrays.toString(args) + ") - " + e;
            } catch (Exception f) {
                return "[FORMATTER ERROR] \"" + message + "\"(" + f + ") - " + e;
            }
        }
    }

    protected Object convert(Object o) {
        if (o == null ||
            o instanceof Formattable ||
            o instanceof Number ||
            o instanceof Character ||
            o instanceof Boolean ||
            o instanceof Date ||
            o instanceof Calendar)
            return o;

        try {
            if (o instanceof String) {
                o = new StringBuilder().append('"').append(o).append('"').toString();
            }
            else if (o.getClass().isArray()) {
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
            else {
                o = o.toString();
            }

            return o;
        } catch (Exception e) {
            return o.getClass().getName() + " (exception: " + e.toString() + ")";
        }
    }
}
