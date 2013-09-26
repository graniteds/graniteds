/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.android.platform;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import org.granite.client.persistence.collection.UnsafePersistentCollection;
import org.granite.messaging.reflect.Property;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractAndroidProperty implements AndroidProperty {
	
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
		Class<?> type = property.getType();
		return type;
	}

	@Override
	public Object getRawObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object fieldValue = property.getRawObject(holder);
		return fieldValue;
	}

	@Override
	public Object getObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object fieldValue = property.getObject(holder);
		
		if (fieldValue instanceof UnsafePersistentCollection)
			return ((UnsafePersistentCollection<?>)fieldValue).internalPersistentCollection();
		
		return fieldValue;
	}

	@Override
	public void setObject(Object holder, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setObject(holder, value);
	}

	public boolean getBoolean(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getBoolean(holder);
	}

	public byte getByte(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getByte(holder);
	}

	public char getChar(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getChar(holder);
	}

	public double getDouble(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getDouble(holder);
	}

	public float getFloat(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getFloat(holder);
	}

	public int getInt(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getInt(holder);
	}

	public long getLong(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getLong(holder);
	}

	public short getShort(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getShort(holder);
	}

	public void setBoolean(Object holder, boolean value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setBoolean(holder, value);
	}

	public void setByte(Object holder, byte value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setByte(holder, value);
	}

	public void setChar(Object holder, char value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setChar(holder, value);
	}

	public void setDouble(Object holder, double value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setDouble(holder, value);
	}

	public void setFloat(Object holder, float value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setFloat(holder, value);
	}

	public void setInt(Object holder, int value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setInt(holder, value);
	}

	public void setLong(Object holder, long value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setLong(holder, value);
	}

	public void setShort(Object holder, short value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setShort(holder, value);
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
