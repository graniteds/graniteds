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

import java.lang.reflect.InvocationTargetException;

public class NoopWritableProperty extends NullProperty {

	private final String name;
	private final Class<?> type;
	
	public NoopWritableProperty(String name, Class<?> type) {
		if(name == null || type == null)
			throw new NullPointerException("name and type cannot be null");
		
		this.name = name;
		this.type = type;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setBoolean(Object holder, boolean value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public void setChar(Object holder, char value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public void setByte(Object holder, byte value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public void setShort(Object holder, short value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public void setInt(Object holder, int value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public void setLong(Object holder, long value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public void setFloat(Object holder, float value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public void setDouble(Object holder, double value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public void setObject(Object holder, Object value)
		throws IllegalArgumentException, IllegalAccessException,
		InvocationTargetException {
	}

	@Override
	public int hashCode() {
		return name.hashCode() + type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof NoopWritableProperty))
			return false;
		return ((NoopWritableProperty)obj).name.equals(name) && ((NoopWritableProperty)obj).type.equals(type);
	}

	@Override
	public String toString() {
		return "NoopWritableProperty {name=" + name + ", type=" + type + "}";
	}
}
