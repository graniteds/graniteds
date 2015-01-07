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
package org.granite.messaging.amf.io.convert.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.IllegalConverterArgumentException;
import org.granite.util.ArrayUtil;

/**
 * @author Franck WOLFF
 */
public class Collection2Array extends Converter {

    public Collection2Array(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        Type targetComponentType = ArrayUtil.getComponentType(targetType);

        if (targetComponentType == null)
            return false; // not an array.

        if (value == null)
            return true;

        if (!(value instanceof Collection<?>))
            return false;

        if (targetComponentType.equals(Object.class))
            return true;

        Converter itemConverter = null;
        for (Object item : (Collection<?>)value) {

            if (itemConverter == null)
                itemConverter = converters.getConverter(item, targetComponentType);
            else if (!itemConverter.canConvert(item, targetComponentType))
                itemConverter = converters.getConverter(item, targetComponentType);

            if (itemConverter == null)
                return false;
        }

        return true;
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {

        if (value == null)
            return null;

        if (value instanceof Collection<?>) {
            Collection<?> c = (Collection<?>)value;

            Type targetComponentType = ArrayUtil.getComponentType(targetType);
            if (targetComponentType != null) {
                Converter itemConverter = null;
                final Object array = ArrayUtil.newArray(targetComponentType, c.size());
                int i = 0;
                for (Object item : c) {

                	if (item != null && item.getClass() == targetComponentType) {
                		Array.set(array, i++, item);
                		continue;
                	}

                	if (itemConverter == null)
                        itemConverter = converters.getConverter(item, targetComponentType);
                    else if (!itemConverter.canConvert(item, targetComponentType))
                        itemConverter = converters.getConverter(item, targetComponentType);

                    if (itemConverter == null)
                        throw new IllegalConverterArgumentException(this, value, targetType);

                    Array.set(array, i++, itemConverter.convert(item, targetComponentType));
                }
                return array;
            }
        }

        throw new IllegalConverterArgumentException(this, value, targetType);
    }
}
