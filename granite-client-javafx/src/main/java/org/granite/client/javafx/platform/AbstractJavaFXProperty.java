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
package org.granite.client.javafx.platform;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import javafx.beans.value.WritableBooleanValue;
import javafx.beans.value.WritableDoubleValue;
import javafx.beans.value.WritableFloatValue;
import javafx.beans.value.WritableIntegerValue;
import javafx.beans.value.WritableListValue;
import javafx.beans.value.WritableLongValue;
import javafx.beans.value.WritableMapValue;
import javafx.beans.value.WritableSetValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.granite.client.javafx.persistence.collection.FXPersistentCollections;
import org.granite.client.persistence.collection.PersistentBag;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.persistence.collection.PersistentSortedMap;
import org.granite.client.persistence.collection.PersistentSortedSet;
import org.granite.client.persistence.collection.UnsafePersistentCollection;
import org.granite.messaging.reflect.Property;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.ObservableMapWrapper;
import com.sun.javafx.collections.ObservableSetWrapper;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractJavaFXProperty implements JavaFXProperty {
	
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
	
	protected final Property property;

	public AbstractJavaFXProperty(Property property) {
		this.property = property;
	}

	@Override
	public String getName() {
		return property.getName();
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return property.isAnnotationPresent(annotationClass);
	}

	@Override
	public boolean isReadable() {
		return property.isReadable();
	}

	@Override
	public boolean isWritable() {
		return property.isWritable();
	}

	@Override
	public Class<?> getType() {
		Class<?> type = property.getType();
		
		if (ObservableValue.class.isAssignableFrom(type)) {
			
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
			
			return Object.class;
		}
			
		return type;
	}

	@Override
	public boolean getBoolean(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (ObservableBooleanValue.class.isAssignableFrom(property.getType()))
			return ((ObservableBooleanValue)property.getObject(holder)).get();
		return property.getBoolean(holder);
	}

	@Override
	public char getChar(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (ObservableValue.class.isAssignableFrom(property.getType()))
			throw new UnsupportedOperationException("JavaFX doesn't support char properties");
		return property.getChar(holder);
	}

	@Override
	public byte getByte(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (ObservableValue.class.isAssignableFrom(property.getType()))
			throw new UnsupportedOperationException("JavaFX doesn't support byte properties");
		return property.getByte(holder);
	}

	@Override
	public short getShort(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (ObservableValue.class.isAssignableFrom(property.getType()))
			throw new UnsupportedOperationException("JavaFX doesn't support short properties");
		return property.getShort(holder);
	}

	@Override
	public int getInt(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (ObservableIntegerValue.class.isAssignableFrom(property.getType()))
			return ((ObservableIntegerValue)property.getObject(holder)).get();
		return property.getInt(holder);
	}

	@Override
	public long getLong(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (ObservableLongValue.class.isAssignableFrom(property.getType()))
			return ((ObservableLongValue)property.getObject(holder)).get();
		return property.getLong(holder);
	}

	@Override
	public float getFloat(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (ObservableFloatValue.class.isAssignableFrom(property.getType()))
			return ((ObservableFloatValue)property.getObject(holder)).get();
		return property.getFloat(holder);
	}

	@Override
	public double getDouble(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (ObservableDoubleValue.class.isAssignableFrom(property.getType()))
			return ((ObservableDoubleValue)property.getObject(holder)).get();
		return property.getDouble(holder);
	}
	
	@Override
	public Object getRawObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object fieldValue = property.getObject(holder);
		
		if (fieldValue instanceof ObservableValue)
			return ((ObservableValue<?>)fieldValue).getValue();
		
		return fieldValue;
	}

	@Override
	public Object getObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object fieldValue = property.getObject(holder);
		
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

	@Override
	public void setBoolean(Object holder, boolean value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (WritableBooleanValue.class.isAssignableFrom(property.getType()))
			((WritableBooleanValue)property.getObject(holder)).set(value);
		else
			property.setBoolean(holder, value);
	}

	@Override
	public void setChar(Object holder, char value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (WritableValue.class.isAssignableFrom(property.getType()))
			throw new UnsupportedOperationException("JavaFX doesn't support char properties");
		property.setChar(holder, value);
	}

	@Override
	public void setByte(Object holder, byte value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (WritableValue.class.isAssignableFrom(property.getType()))
			throw new UnsupportedOperationException("JavaFX doesn't support byte properties");
		property.setByte(holder, value);
	}

	@Override
	public void setShort(Object holder, short value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (WritableValue.class.isAssignableFrom(property.getType()))
			throw new UnsupportedOperationException("JavaFX doesn't support short properties");
		property.setShort(holder, value);
	}

	@Override
	public void setInt(Object holder, int value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (WritableIntegerValue.class.isAssignableFrom(property.getType()))
			((WritableIntegerValue)property.getObject(holder)).set(value);
		else
			property.setInt(holder, value);
	}

	@Override
	public void setLong(Object holder, long value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (WritableLongValue.class.isAssignableFrom(property.getType()))
			((WritableLongValue)property.getObject(holder)).set(value);
		else
			property.setLong(holder, value);
	}

	@Override
	public void setFloat(Object holder, float value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (WritableFloatValue.class.isAssignableFrom(property.getType()))
			((WritableFloatValue)property.getObject(holder)).set(value);
		else
			property.setFloat(holder, value);
	}

	@Override
	public void setDouble(Object holder, double value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (WritableDoubleValue.class.isAssignableFrom(property.getType()))
			((WritableDoubleValue)property.getObject(holder)).set(value);
		else
			property.setDouble(holder, value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setObject(Object holder, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?> fieldType = property.getType();
		
		if (WritableValue.class.isAssignableFrom(fieldType)) {
			WritableValue<Object> writableValue = (WritableValue<Object>)property.getObject(holder);
			
			if (writableValue instanceof WritableListValue) {
				if (value instanceof PersistentBag)
					value = FXPersistentCollections.observablePersistentBag((PersistentBag<Object>)value);
				else if (value instanceof PersistentList)
					value = FXPersistentCollections.observablePersistentList((PersistentList<Object>)value);
				else if (value != null && !(value instanceof ObservableList<?>))
					value = FXCollections.observableList((List<Object>)value);
			}
			else if (writableValue instanceof WritableSetValue) {
				if (value instanceof PersistentSortedSet)
					value = FXPersistentCollections.observablePersistentSortedSet((PersistentSortedSet<Object>)value);
				else if (value instanceof PersistentSet)
					value = FXPersistentCollections.observablePersistentSet((PersistentSet<Object>)value);
				else if (value != null && !(value instanceof ObservableSet<?>))
					value = FXCollections.observableSet((Set<Object>)value);
			}
			else if (writableValue instanceof WritableMapValue) {
				if (value instanceof PersistentSortedMap)
					value = FXPersistentCollections.observablePersistentSortedMap((PersistentSortedMap<Object, Object>)value);
				else if (value instanceof PersistentMap)
					value = FXPersistentCollections.observablePersistentMap((PersistentMap<Object, Object>)value);
				else if (value != null && !(value instanceof ObservableMap<?, ?>))
					value = FXCollections.observableMap((Map<Object, Object>)value);
			}
			
			/**
			* 
			* A bound property cannot be set!!!
			* 
			* 
			*/
			if(!((javafx.beans.property.Property)writableValue).isBound())
				writableValue.setValue(value);
		}
		else
			property.setObject(holder, value);
	}

	@Override
	public boolean equals(Object obj) {
		return property.equals(obj);
	}

	@Override
	public int hashCode() {
		return property.hashCode();
	}

	@Override
	public String toString() {
		return property.toString();
	}
}
