/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.config;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.Reverter;
import org.granite.util.TypeUtil;

public class CustomDateConverter extends Converter implements Reverter {

	public CustomDateConverter(Converters converters) {
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
        return new Long(date.getTime());

	}

	@Override
	public boolean canRevert(Object value) {
		return value instanceof Long;
	}

	@Override
	public Object revert(Object value) {
		return new Date((Long)value);
	}
	
}