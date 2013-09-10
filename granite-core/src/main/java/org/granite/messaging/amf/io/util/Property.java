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
import java.lang.reflect.Type;

import org.granite.messaging.amf.io.convert.Converters;

/**
 * @author Franck WOLFF
 */
public abstract class Property {

    private final String name;
    private final Converters converters;

    protected Property(Converters converters, String name) {
        this.name = name;
        this.converters = converters;
    }

    public String getName() {
        return name;
    }

    protected Converters getConverters() {
        return converters;
    }

    public void setProperty(Object instance, Object value) {
        setProperty(instance, value, true);
    }

    public abstract void setProperty(Object instance, Object value, boolean convert);
    public abstract Object getProperty(Object instance);
    public abstract Type getType();
    public abstract Class<?> getDeclaringClass();
    
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    	return isAnnotationPresent(annotationClass, false);
    }
    public abstract boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, boolean recursive);

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    	return getAnnotation(annotationClass, false);
    }
    public abstract <T extends Annotation> T getAnnotation(Class<T> annotationClass, boolean recursive);

    protected Object convert(Object value) {
        return converters.convert(value, getType());
    }

	@Override
	public String toString() {
		return name;
	}
}
