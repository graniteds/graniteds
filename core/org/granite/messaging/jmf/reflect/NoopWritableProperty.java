package org.granite.messaging.jmf.reflect;

import java.lang.reflect.InvocationTargetException;

public class NoopWritableProperty extends NullProperty {

	private final Class<?> type;
	
	public NoopWritableProperty(Class<?> type) {
		this.type = type;
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
}
