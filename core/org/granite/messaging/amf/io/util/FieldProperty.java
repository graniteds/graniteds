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

package org.granite.messaging.amf.io.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.granite.messaging.amf.io.convert.Converters;

/**
 * @author Franck WOLFF
 */
public class FieldProperty extends Property {

    private final Field field;

    public FieldProperty(Converters converters, Field field) {
        super(converters, field.getName());
        this.field = field;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, boolean recursive) {
        return field.isAnnotationPresent(annotationClass);
    }

    @Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass, boolean recursive) {
		return field.getAnnotation(annotationClass);
	}

	@Override
    public Type getType() {
        return field.getGenericType();
    }

    @Override
    public void setProperty(Object instance, Object value, boolean convert) {
        try {
            field.setAccessible(true);
            field.set(instance, convert ? convert(value) : value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getProperty(Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
