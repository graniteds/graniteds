/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.messaging.amf.io.convert.impl;

import java.lang.reflect.Type;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.Reverter;
import org.granite.util.TypeUtil;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


public class GAEKeyConverter extends Converter implements Reverter {
	
	private static String KEY_CLASS = "com.google.appengine.api.datastore.Key";
	
    public GAEKeyConverter(Converters converters) {
        super(converters);
    }

	@Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        Class<?> targetClass = TypeUtil.classOfType(targetType);
        return (
            targetClass != Object.class && KEY_CLASS.equals(targetClass.getName()) &&
            (value instanceof String || value == null)
        );
	}

	@Override
	protected Object internalConvert(Object value, Type targetType) {
        if (value == null)
            return null;
        return KeyFactory.stringToKey((String)value);
	}

	public boolean canRevert(Object value) {
		return value != null && KEY_CLASS.equals(value.getClass().getName());
	}

	public Object revert(Object value) {
		return KeyFactory.keyToString((Key)value);
	}

}
