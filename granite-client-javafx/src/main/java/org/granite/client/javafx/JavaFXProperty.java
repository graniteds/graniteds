/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.javafx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableListValue;
import javafx.beans.value.WritableMapValue;
import javafx.beans.value.WritableSetValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;

import org.granite.client.javafx.persistence.collection.FXPersistentCollections;
import org.granite.client.persistence.collection.PersistentBag;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.persistence.collection.PersistentSortedMap;
import org.granite.client.persistence.collection.PersistentSortedSet;
import org.granite.client.persistence.collection.UnsafePersistentCollection;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.Property;
import org.granite.util.TypeUtil;
import org.granite.util.TypeUtil.DeclaredAnnotation;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.ObservableMapWrapper;
import com.sun.javafx.collections.ObservableSetWrapper;

/**
 * @author William DRAI
 */
public class JavaFXProperty extends Property {
	
	private static final Field observableListWrapperField;
	static {
		try {
			observableListWrapperField = ObservableListWrapper.class.getDeclaredField("backingList");
			observableListWrapperField.setAccessible(true);
		}
		catch (Throwable t) {
			throw new ExceptionInInitializerError("Could not find backingList field in: " + ObservableListWrapper.class.getName());
		}
	}
	
	private static final Field observableSetWrapperField;
	static {
		try {
			observableSetWrapperField = ObservableSetWrapper.class.getDeclaredField("backingSet");
			observableSetWrapperField.setAccessible(true);
		}
		catch (Throwable t) {
			throw new ExceptionInInitializerError("Could not find backingSet field in: " + ObservableSetWrapper.class.getName());
		}
	}
	
	private static final Field observableMapWrapperField;
	static {
		try {
			observableMapWrapperField = ObservableMapWrapper.class.getDeclaredField("backingMap");
			observableMapWrapperField.setAccessible(true);
		}
		catch (Throwable t) {
			throw new ExceptionInInitializerError("Could not find backingMap field in: " + ObservableMapWrapper.class.getName());
		}
	}
	
	private final Field field;
	private final Method setter;
	private final Method getter;
	
    public JavaFXProperty(Converters converters, String name, Field field, Method getter, Method setter) {
        super(converters, name);
        this.field = field;
        field.setAccessible(true);
        this.setter = setter;
        this.getter = getter;
    }

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Object instance, Object value, boolean convert) {
		try {
			Class<?> fieldType = field.getType();
			Object convertedValue = value;
			
			if (WritableValue.class.isAssignableFrom(fieldType)) {
				WritableValue<Object> writableValue = (WritableValue<Object>)field.get(instance);
				
				if (writableValue instanceof WritableListValue) {
					if (value instanceof PersistentBag)
						convertedValue = FXPersistentCollections.observablePersistentBag((PersistentBag<Object>)value);
					else if (value instanceof PersistentList)
						convertedValue = FXPersistentCollections.observablePersistentList((PersistentList<Object>)value);
					else
						convertedValue = FXCollections.observableList((List<Object>)value);
				}
				else if (writableValue instanceof WritableSetValue) {
					if (value instanceof PersistentSortedSet)
						convertedValue = FXPersistentCollections.observablePersistentSortedSet((PersistentSortedSet<Object>)value);
					else if (value instanceof PersistentSet)
						convertedValue = FXPersistentCollections.observablePersistentSet((PersistentSet<Object>)value);
					else
						convertedValue = FXCollections.observableSet((Set<Object>)value);
				}
				else if (writableValue instanceof WritableMapValue) {
					if (value instanceof PersistentSortedMap)
						convertedValue = FXPersistentCollections.observablePersistentSortedMap((PersistentSortedMap<Object, Object>)value);
					else if (value instanceof PersistentMap)
						convertedValue = FXPersistentCollections.observablePersistentMap((PersistentMap<Object, Object>)value);
					else
						convertedValue = FXCollections.observableMap((Map<Object, Object>)value);
				}
				else
					convertedValue = convert ? convert(value) : value;
				
				writableValue.setValue(convertedValue);
			}
			else {
				convertedValue = convert ? convert(value) : value;
				field.set(instance, convertedValue);
			}
		} 
		catch (Exception e) {
			throw new RuntimeException("Could not set value of property " + field, e);
		}
	}

	@Override
	public Object getValue(Object instance) {
		try {
			Object fieldValue = field.get(instance);
			
			if (fieldValue instanceof ObservableValue) {
				Object wrappedValue = ((ObservableValue<?>)fieldValue).getValue();
				
				if (wrappedValue instanceof UnsafePersistentCollection)
					return ((UnsafePersistentCollection<?>)wrappedValue).internalPersistentCollection();
				if (wrappedValue instanceof ObservableListWrapper)
					return observableListWrapperField.get(wrappedValue);
				if (wrappedValue instanceof ObservableSetWrapper)
					return observableSetWrapperField.get(wrappedValue);
				if (wrappedValue instanceof ObservableMapWrapper)
					return observableMapWrapperField.get(wrappedValue);
				
				return wrappedValue;
			}
			
			return fieldValue;
		} 
		catch (Exception e) {
			throw new RuntimeException("Could not get value of property " + field, e);
		}
	}

	@Override
	public Class<?> getDeclaringClass() {
		if (getter != null)
			return getter.getDeclaringClass();
		if (setter != null)
			return setter.getDeclaringClass();
		
		return field.getDeclaringClass();
	}

	@Override
	public Type getType() {
		Class<?> type = field.getType();
		
		if (ObservableValue.class.isAssignableFrom(type)) {
			
			if (getter != null)
				return getter.getGenericReturnType();
			if (setter != null)
				return setter.getGenericParameterTypes()[0];
			
			if (ObservableBooleanValue.class.isAssignableFrom(type))
				return Boolean.TYPE;
			if (ObservableIntegerValue.class.isAssignableFrom(type))
				return Integer.TYPE;
			if (ObservableLongValue.class.isAssignableFrom(type))
				return Long.TYPE;
			if (ObservableDoubleValue.class.isAssignableFrom(type))
				return Double.TYPE;
			if (ObservableFloatValue.class.isAssignableFrom(type))
				return Float.TYPE;
			if (ObservableStringValue.class.isAssignableFrom(type))
			    return String.class;
			
			if (field.getGenericType() instanceof ParameterizedType)
				return ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
		}
		
		return type;
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, boolean recursive) {
		if (field != null) {
            if (field.isAnnotationPresent(annotationClass))
                return true;
            if (recursive && TypeUtil.isAnnotationPresent(field, annotationClass))
            	return true;
		}
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
    	if (field != null) {
    		annotation = field.getAnnotation(annotationClass);
    		if (annotation == null && recursive) {
    			DeclaredAnnotation<T> declaredAnnotation = TypeUtil.getAnnotation(field, annotationClass);
    			if (declaredAnnotation != null)
    				annotation = declaredAnnotation.annotation;
    		}
    	}
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
		return null;
	}

}
