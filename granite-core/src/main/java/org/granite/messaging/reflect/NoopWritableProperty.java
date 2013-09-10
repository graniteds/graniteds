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
