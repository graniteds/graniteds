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

package org.granite.messaging.amf.io.convert.impl;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Map;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.IllegalConverterArgumentException;
import org.granite.util.TypeUtil;
import org.granite.util.MapUtil;

/**
 * @author Franck WOLFF
 */
public class Map2Map extends Converter {

    public Map2Map(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {

        Type[] targetComponentTypes = MapUtil.getComponentTypes(targetType);
        if (targetComponentTypes == null)
            return false; // not a map.

        if (value == null)
            return true;

        if (!(value instanceof Map<?, ?>))
            return false;

        Type keyType = targetComponentTypes[0];
        Type valueType = targetComponentTypes[1];

        if ((keyType.equals(Object.class) || keyType instanceof WildcardType) &&
            (valueType.equals(Object.class) || valueType instanceof WildcardType))
            return true;

        Converter keyConverter = null;
        Converter valueConverter = null;
        for (Map.Entry<?, ?> item : ((Map<?, ?>)value).entrySet()) {

            if (keyConverter == null)
                keyConverter = converters.getConverter(item.getKey(), keyType);
            else if (!keyConverter.canConvert(item.getKey(), keyType))
                keyConverter = converters.getConverter(item.getKey(), keyType);
            if (keyConverter == null)
                return false;

            if (valueConverter == null)
                valueConverter = converters.getConverter(item.getValue(), valueType);
            else if (!valueConverter.canConvert(item.getValue(), valueType))
                valueConverter = converters.getConverter(item.getValue(), valueType);
            if (valueConverter == null)
                return false;
        }

        return true;
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {

        if (value == null)
            return null;

        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>)value;

            Type[] targetComponentTypes = MapUtil.getComponentTypes(targetType);
            if (targetComponentTypes != null) {
                Type keyType = targetComponentTypes[0];
                Type valueType = targetComponentTypes[1];

                Class<?> targetClass = TypeUtil.classOfType(targetType);
                if (targetClass.isInstance(value) &&
                    (keyType.equals(Object.class) || keyType instanceof WildcardType) &&
                    (valueType.equals(Object.class) || valueType instanceof WildcardType))
                    return value;

                Map<Object, Object> targetInstance = null;
                try {
                    targetInstance = MapUtil.newMap(targetClass, map.size());
                } catch (Exception e) {
                    throw new IllegalConverterArgumentException(this, value, targetType, e);
                }

                Converter keyConverter = null;
                Converter valueConverter = null;
                for (Map.Entry<?, ?> item : ((Map<?, ?>)value).entrySet()) {

                    if (keyConverter == null)
                        keyConverter = converters.getConverter(item.getKey(), keyType);
                    else if (!keyConverter.canConvert(item.getKey(), keyType))
                        keyConverter = converters.getConverter(item.getKey(), keyType);
                    if (keyConverter == null)
                        throw new IllegalConverterArgumentException(this, value, targetType);

                    if (valueConverter == null)
                        valueConverter = converters.getConverter(item.getValue(), valueType);
                    else if (!valueConverter.canConvert(item.getValue(), valueType))
                        valueConverter = converters.getConverter(item.getValue(), valueType);
                    if (valueConverter == null)
                        throw new IllegalConverterArgumentException(this, value, targetType);

                    targetInstance.put(
                        keyConverter.convert(item.getKey(), keyType),
                        valueConverter.convert(item.getValue(), valueType)
                    );
                }

                return targetInstance;
            }
        }

        throw new IllegalConverterArgumentException(this, value, targetType);
    }
}
