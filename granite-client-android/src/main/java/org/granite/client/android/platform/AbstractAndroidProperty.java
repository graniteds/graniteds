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
package org.granite.client.android.platform;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.persistence.collection.UnsafePersistentCollection;
import org.granite.messaging.reflect.Property;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractAndroidProperty implements AndroidProperty {
	
	private static final ObservableWrapperDef observableListWrapperDef = ObservableWrapperDef.init("org.granite.binding.collection.ObservableListWrapper", "wrappedList", List.class,
			"org.granite.client.persistence.collection.observable.ObservablePersistentList", PersistentList.class);
	private static final ObservableWrapperDef observableSetWrapperDef = ObservableWrapperDef.init("org.granite.binding.collection.ObservableSetWrapper", "wrappedSet", Set.class,
			"org.granite.client.persistence.collection.observable.ObservablePersistentSet", PersistentSet.class);
	private static final ObservableWrapperDef observableMapWrapperDef = ObservableWrapperDef.init("org.granite.binding.collection.ObservableMapWrapper", "wrappedMap", Map.class,
			"org.granite.client.persistence.collection.observable.ObservablePersistentMap", PersistentMap.class);
	
	protected final Property property;
	
	public AbstractAndroidProperty(Property property) {
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
		return property.getType();
	}

	@Override
	public boolean getBoolean(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getBoolean(holder);
	}

	@Override
	public char getChar(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getChar(holder);
	}

	@Override
	public byte getByte(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getByte(holder);
	}

	@Override
	public short getShort(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getShort(holder);
	}

	@Override
	public int getInt(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getInt(holder);
	}

	@Override
	public long getLong(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getLong(holder);
	}

	@Override
	public float getFloat(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getFloat(holder);
	}

	@Override
	public double getDouble(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getDouble(holder);
	}
	
	@Override
	public Object getRawObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getObject(holder);
	}

	@Override
	public Object getObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object value = property.getObject(holder);
		
		if (value instanceof UnsafePersistentCollection)
			return ((UnsafePersistentCollection<?>)value).internalPersistentCollection();
		if (observableListWrapperDef != null && observableListWrapperDef.isWrapper(value))
			return observableListWrapperDef.getWrappedValue(value);
		if (observableSetWrapperDef != null && observableSetWrapperDef.isWrapper(value))
			return observableSetWrapperDef.getWrappedValue(value);
		if (observableMapWrapperDef != null && observableMapWrapperDef.isWrapper(value))
			return observableMapWrapperDef.getWrappedValue(value);
		
		return value;
	}

	@Override
	public void setBoolean(Object holder, boolean value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setBoolean(holder, value);
	}

	@Override
	public void setChar(Object holder, char value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setChar(holder, value);
	}

	@Override
	public void setByte(Object holder, byte value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setByte(holder, value);
	}

	@Override
	public void setShort(Object holder, short value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setShort(holder, value);
	}

	@Override
	public void setInt(Object holder, int value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setInt(holder, value);
	}

	@Override
	public void setLong(Object holder, long value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setLong(holder, value);
	}

	@Override
	public void setFloat(Object holder, float value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setFloat(holder, value);
	}

	@Override
	public void setDouble(Object holder, double value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setDouble(holder, value);
	}

	@Override
	public void setObject(Object holder, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?> fieldType = property.getType();
		
		if (List.class.isAssignableFrom(fieldType) && observableListWrapperDef != null) {
			value = observableListWrapperDef.newWrapper(value);
		}
		else if (Set.class.isAssignableFrom(fieldType)) {
			value = observableSetWrapperDef.newWrapper(value);
		}
		else if (Map.class.isAssignableFrom(fieldType)) {
			value = observableMapWrapperDef.newWrapper(value);
		}
		
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
	
	
	private static final class ObservableWrapperDef {
		public final Class<?> wrapperClass;
		public final Constructor<?> wrapperConstructor;
		public final Field wrapperField;
		@SuppressWarnings("unused")
		public final Class<?> persistentClass;
		public final Constructor<?> persistentConstructor;
		
		public ObservableWrapperDef(Class<?> wrapperClass, Constructor<?> wrapperConstructor, Field wrapperField, 
				Class<?> persistentClass, Constructor<?> persistentConstructor) {
			this.wrapperClass = wrapperClass;
			this.wrapperConstructor = wrapperConstructor;
			this.wrapperField = wrapperField;
			this.persistentClass = persistentClass;
			this.persistentConstructor = persistentConstructor;
		}
		
		public static ObservableWrapperDef init(String wrapperClassName, String wrapperFieldName, Class<?> wrapperConstructorArgType, 
				String persistentClassName, Class<?> persistentConstructorArgType) {
			try {
				Class<?> wrapperClass = TypeUtil.forName(wrapperClassName);
				Constructor<?> wrapperConstructor = wrapperClass.getConstructor(wrapperConstructorArgType);
				Field wrapperField = wrapperClass.getDeclaredField(wrapperFieldName);
				wrapperField.setAccessible(true);
				Class<?> persistentClass = TypeUtil.forName(persistentClassName);
				Constructor<?> persistentConstructor = persistentClass.getConstructor(persistentConstructorArgType);
				return new ObservableWrapperDef(wrapperClass, wrapperConstructor, wrapperField, persistentClass, persistentConstructor);
			}
			catch (ClassNotFoundException e) {
				return null;
			}
			catch (Throwable t) {
				throw new ExceptionInInitializerError(t);
			}
		}
		
		public Object newWrapper(Object collection) {
			try {
				if (collection instanceof PersistentCollection)
					return persistentConstructor.newInstance(collection);
				return wrapperConstructor.newInstance(collection);
			} 
			catch (Exception e) {
				throw new RuntimeException("Could not create wrapper", e);
			}
		}
		
		public boolean isWrapper(Object collection) {
			return wrapperClass.isInstance(collection);
		}
		
		public Object getWrappedValue(Object collection) {
			try {
				return wrapperField.get(collection);
			} 
			catch (Exception e) {
				throw new RuntimeException("Could not get wrapped value", e);
			}
		}
	}

}
