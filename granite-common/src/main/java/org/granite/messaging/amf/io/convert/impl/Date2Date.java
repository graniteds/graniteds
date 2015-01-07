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

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.IllegalConverterArgumentException;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class Date2Date extends Converter {

    public Date2Date(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        Class<?> targetClass = TypeUtil.classOfType(targetType);
        return (
            targetClass.isAssignableFrom(Date.class) ||
            targetClass.isAssignableFrom(Calendar.class) ||
            targetClass.isAssignableFrom(java.sql.Timestamp.class) ||
            targetClass.isAssignableFrom(java.sql.Time.class) ||
            targetClass.isAssignableFrom(java.sql.Date.class)
        ) && (
            value == null || value instanceof Date
        );
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {

        if (value == null)
            return null;

        Date date = (Date)value;
        Class<?> targetClass = TypeUtil.classOfType(targetType);
        if (targetClass.isAssignableFrom(Date.class))
            return value;

        if (targetClass.isAssignableFrom(Calendar.class)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }

        if (targetClass.isAssignableFrom(java.sql.Timestamp.class))
            return new java.sql.Timestamp(date.getTime());

        if (targetClass.isAssignableFrom(java.sql.Date.class))
            return new java.sql.Date(date.getTime());

        if (targetClass.isAssignableFrom(java.sql.Time.class))
            return new java.sql.Time(date.getTime());

        throw new IllegalConverterArgumentException(this, value, targetType);
    }
}
