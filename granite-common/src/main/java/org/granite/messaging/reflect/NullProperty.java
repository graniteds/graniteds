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

public class NullProperty implements Property {
	
	public Class<?> getType() {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		throw new UnsupportedOperationException();
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		throw new UnsupportedOperationException();
	}

	public boolean isReadable() {
		throw new UnsupportedOperationException();
	}

	public boolean isWritable() {
		throw new UnsupportedOperationException();
	}

	public boolean getBoolean(Object holder)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public char getChar(Object holder) throws IllegalArgumentException,
		IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public byte getByte(Object holder) throws IllegalArgumentException,
		IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public short getShort(Object holder) throws IllegalArgumentException,
		IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public int getInt(Object holder) throws IllegalArgumentException,
		IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public long getLong(Object holder) throws IllegalArgumentException,
		IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public float getFloat(Object holder) throws IllegalArgumentException,
		IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public double getDouble(Object holder) throws IllegalArgumentException,
		IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public Object getObject(Object holder) throws IllegalArgumentException,
		IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public Object getRawObject(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setBoolean(Object holder, boolean value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setChar(Object holder, char value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setByte(Object holder, byte value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setShort(Object holder, short value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setInt(Object holder, int value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setLong(Object holder, long value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setFloat(Object holder, float value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setDouble(Object holder, double value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	public void setObject(Object holder, Object value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return NullProperty.class.getSimpleName();
	}
}
