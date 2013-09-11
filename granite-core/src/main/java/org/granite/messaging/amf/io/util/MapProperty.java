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

package org.granite.messaging.amf.io.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import org.granite.messaging.amf.io.convert.Converters;

/**
 * @author Franck WOLFF
 */
public class MapProperty extends Property {

    public MapProperty(Converters converters, String name) {
        super(converters, name);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, boolean recursive) {
        return false;
    }

    @Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass, boolean recursive) {
		return null;
	}

	@Override
    public Type getType() {
        return Object.class;
    }
	
	@Override
    public Class<?> getDeclaringClass() {
        return Object.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setProperty(Object instance, Object value, boolean convert) {
        ((Map<Object, Object>)instance).put(getName(), value);
    }

    @Override
    public Object getProperty(Object instance) {
        return ((Map<?, ?>)instance).get(getName());
    }
}
