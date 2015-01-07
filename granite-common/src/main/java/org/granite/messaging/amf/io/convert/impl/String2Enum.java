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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class String2Enum extends Converter {

    public String2Enum(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        return Enum.class.isAssignableFrom(TypeUtil.classOfType(targetType)) && (value == null || value instanceof String || value instanceof Enum);
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {
        if (value == null)
            return null;
        Class<?> enumType = TypeUtil.classOfType(targetType);
        String enumValue = value instanceof String ? (String)value : ((Enum<?>)value).name();
        try {
	        Method m = enumType.getClass().getMethod("valueOf", enumType, String.class);
	        return m.invoke(enumType, enumValue);
        }
        catch (Exception e) {
        	throw new RuntimeException("Cound not convert " + value + " to enum of type " + targetType, e);
        }
    }

	public boolean canRevert(Object value) {
		return (value == null) || (value instanceof Locale);
	}

	public Object revert(Object value) {
		if (value != null)
			value = value.toString();
		return value;
	}
}
