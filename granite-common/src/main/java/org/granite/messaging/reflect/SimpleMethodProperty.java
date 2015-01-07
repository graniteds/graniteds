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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Franck WOLFF
 */
public class SimpleMethodProperty implements MethodProperty {

	private final Method getter;
	private final Method setter;
	private final String name;
	
	public SimpleMethodProperty(Method getter, Method setter, String name) {
		if (getter == null || name == null)
			throw new NullPointerException("getter and name cannot be null");
		
		this.getter = getter;
		this.setter = setter;
		this.name = name;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	public Class<?> getType() {
		return getter.getReturnType();
	}

	public String getName() {
		return name;
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getter.isAnnotationPresent(annotationClass) || (setter != null && setter.isAnnotationPresent(annotationClass));
	}

	public boolean isReadable() {
		return getter != null;
	}

	public boolean isWritable() {
		return setter != null;
	}

	public boolean getBoolean(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Boolean)getter.invoke(holder)).booleanValue();
	}

	public char getChar(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Character)getter.invoke(holder)).charValue();
	}

	public byte getByte(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Byte)getter.invoke(holder)).byteValue();
	}

	public short getShort(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Short)getter.invoke(holder)).shortValue();
	}

	public int getInt(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Integer)getter.invoke(holder)).intValue();
	}

	public long getLong(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Long)getter.invoke(holder)).intValue();
	}

	public float getFloat(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Float)getter.invoke(holder)).floatValue();
	}

	public double getDouble(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Double)getter.invoke(holder)).doubleValue();
	}

	public Object getObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return getter.invoke(holder);
	}

	public Object getRawObject(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return getter.invoke(holder);
	}

	public void setBoolean(Object holder, boolean value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	public void setChar(Object holder, char value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	public void setByte(Object holder, byte value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	public void setShort(Object holder, short value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	public void setInt(Object holder, int value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	public void setLong(Object holder, long value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	public void setFloat(Object holder, float value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	public void setDouble(Object holder, double value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	public void setObject(Object holder, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (setter == null)
			throw new IllegalAccessException("Property " + this + " isn't writable");
		setter.invoke(holder, value);
	}

	@Override
	public int hashCode() {
		return getter.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof MethodProperty && ((MethodProperty)obj).getGetter().equals(getter));
	}

	@Override
	public String toString() {
		return getter.toString();
	}
}
