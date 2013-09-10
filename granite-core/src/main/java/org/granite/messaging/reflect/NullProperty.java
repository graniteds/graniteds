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
