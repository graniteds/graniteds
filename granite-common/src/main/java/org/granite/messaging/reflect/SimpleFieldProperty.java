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
package org.granite.messaging.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Franck WOLFF
 */
public class SimpleFieldProperty implements FieldProperty {

	private final Field field;
	
	public SimpleFieldProperty(Field field) {
		if (field == null)
			throw new NullPointerException("field cannot be null");
		
		field.setAccessible(true);
		
		this.field = field;
	}

	public Field getField() {
		return field;
	}

	public Class<?> getType() {
		return field.getType();
	}

	public String getName() {
		return field.getName();
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return field.isAnnotationPresent(annotationClass);
	}

	public boolean isReadable() {
		return true;
	}

	public boolean isWritable() {
		return true;
	}

	public boolean getBoolean(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.getBoolean(holder);
	}

	public char getChar(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.getChar(holder);
	}

	public byte getByte(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.getByte(holder);
	}

	public short getShort(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.getShort(holder);
	}

	public int getInt(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.getInt(holder);
	}

	public long getLong(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.getLong(holder);
	}

	public float getFloat(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.getFloat(holder);
	}

	public double getDouble(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.getDouble(holder);
	}

	public Object getObject(Object holder) throws IllegalArgumentException, IllegalAccessException {
		return field.get(holder);
	}

	public Object getRawObject(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return field.get(holder);
	}

	public void setBoolean(Object holder, boolean value) throws IllegalArgumentException, IllegalAccessException {
		field.setBoolean(holder, value);
	}

	public void setChar(Object holder, char value) throws IllegalArgumentException, IllegalAccessException {
		field.setChar(holder, value);
	}

	public void setByte(Object holder, byte value) throws IllegalArgumentException, IllegalAccessException {
		field.setByte(holder, value);
	}

	public void setShort(Object holder, short value) throws IllegalArgumentException, IllegalAccessException {
		field.setShort(holder, value);
	}

	public void setInt(Object holder, int value) throws IllegalArgumentException, IllegalAccessException {
		field.setInt(holder, value);
	}

	public void setLong(Object holder, long value) throws IllegalArgumentException, IllegalAccessException {
		field.setLong(holder, value);
	}

	public void setFloat(Object holder, float value) throws IllegalArgumentException, IllegalAccessException {
		field.setFloat(holder, value);
	}

	public void setDouble(Object holder, double value) throws IllegalArgumentException, IllegalAccessException {
		field.setDouble(holder, value);
	}

	public void setObject(Object holder, Object value) throws IllegalArgumentException, IllegalAccessException {
		field.set(holder, value);
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof FieldProperty && ((FieldProperty)obj).getField().equals(field));
	}

	@Override
	public String toString() {
		return field.toString();
	}
}
