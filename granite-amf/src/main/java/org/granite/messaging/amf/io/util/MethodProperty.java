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
package org.granite.messaging.amf.io.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.types.AMFSpecialValueFactory.SpecialValueFactory;
import org.granite.util.TypeUtil;
import org.granite.util.TypeUtil.DeclaredAnnotation;

/**
 * @author Franck WOLFF
 */
public class MethodProperty extends Property {

    private final Method setter;
    private final Method getter;
    private final Type type;
    private final SpecialValueFactory<?> factory;

    public MethodProperty(Converters converters, String name, Method setter, Method getter) {
        super(converters, name);
        this.setter = setter;
        this.getter = getter;
        this.type = getter != null ? getter.getGenericReturnType() : setter.getParameterTypes()[0];
        
        this.factory = specialValueFactory.getValueFactory(this);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, boolean recursive) {
        if (getter != null) {
            if (getter.isAnnotationPresent(annotationClass))
                return true;
            if (recursive && TypeUtil.isAnnotationPresent(getter, annotationClass))
            	return true;
        }
        if (setter != null) {
            if (setter.isAnnotationPresent(annotationClass))
            	return true;
            if (recursive && TypeUtil.isAnnotationPresent(setter, annotationClass))
            	return true;
        }
        return false;
    }

    @Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass, boolean recursive) {
    	T annotation = null;
    	if (getter != null) {
    		annotation = getter.getAnnotation(annotationClass);
    		if (annotation == null && recursive) {
    			DeclaredAnnotation<T> declaredAnnotation = TypeUtil.getAnnotation(getter, annotationClass);
    			if (declaredAnnotation != null)
    				annotation = declaredAnnotation.annotation;
    		}
    	}
    	if (annotation == null && setter != null) {
    		annotation = setter.getAnnotation(annotationClass);
    		if (annotation == null && recursive) {
    			DeclaredAnnotation<T> declaredAnnotation = TypeUtil.getAnnotation(setter, annotationClass);
    			if (declaredAnnotation != null)
    				annotation = declaredAnnotation.annotation;
    		}
    	}
		return annotation;
	}

	@Override
    public Type getType() {
        return type;
    }
	
	@Override
	public Class<?> getDeclaringClass() {
		return getter != null ? getter.getDeclaringClass() : setter.getDeclaringClass();
	}

    @Override
    public void setValue(Object instance, Object value, boolean convert) {
        try {
            setter.invoke(instance, convert ? convert(value) : value);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public Object getValue(Object instance) {
        try {
            Object o = getter.invoke(instance);
            if (factory != null)
            	o = factory.create(o);
            return o;
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
