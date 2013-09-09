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
/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.client.platform.android;

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

	public boolean getBoolean(Object arg0) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getBoolean(arg0);
	}

	public byte getByte(Object arg0) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getByte(arg0);
	}

	public char getChar(Object arg0) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getChar(arg0);
	}

	public double getDouble(Object arg0) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getDouble(arg0);
	}

	public float getFloat(Object arg0) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getFloat(arg0);
	}

	public int getInt(Object arg0) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getInt(arg0);
	}

	public long getLong(Object arg0) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getLong(arg0);
	}

	public short getShort(Object arg0) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return property.getShort(arg0);
	}

	public void setBoolean(Object arg0, boolean arg1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setBoolean(arg0, arg1);
	}

	public void setByte(Object arg0, byte arg1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setByte(arg0, arg1);
	}

	public void setChar(Object arg0, char arg1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setChar(arg0, arg1);
	}

	public void setDouble(Object arg0, double arg1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setDouble(arg0, arg1);
	}

	public void setFloat(Object arg0, float arg1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setFloat(arg0, arg1);
	}

	public void setInt(Object arg0, int arg1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setInt(arg0, arg1);
	}

	public void setLong(Object arg0, long arg1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setLong(arg0, arg1);
	}

	public void setShort(Object arg0, short arg1) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		property.setShort(arg0, arg1);
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
