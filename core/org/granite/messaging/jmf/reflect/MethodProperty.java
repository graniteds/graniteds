package org.granite.messaging.jmf.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodProperty implements Property {

	private final Method method;
	private final String name;
	
	public MethodProperty(Method method, String name) {
		this.method = method;
		this.name = name;
	}

	public Class<?> getType() {
		return method.getReturnType();
	}

	public String getName() {
		return name;
	}

	public boolean getBoolean(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Boolean)method.invoke(holder)).booleanValue();
	}

	public char getChar(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Character)method.invoke(holder)).charValue();
	}

	public byte getByte(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Byte)method.invoke(holder)).byteValue();
	}

	public short getShort(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Short)method.invoke(holder)).shortValue();
	}

	public int getInt(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Integer)method.invoke(holder)).intValue();
	}

	public long getLong(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Long)method.invoke(holder)).intValue();
	}

	public float getFloat(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Float)method.invoke(holder)).floatValue();
	}

	public double getDouble(Object holder) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return ((Double)method.invoke(holder)).doubleValue();
	}

	public Object getObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return method.invoke(holder);
	}

	public void setBoolean(Object holder, boolean value) throws IllegalArgumentException, IllegalAccessException {
	}

	public void setChar(Object holder, char value) throws IllegalArgumentException, IllegalAccessException {
	}

	public void setByte(Object holder, byte value) throws IllegalArgumentException, IllegalAccessException {
	}

	public void setShort(Object holder, short value) throws IllegalArgumentException, IllegalAccessException {
	}

	public void setInt(Object holder, int value) throws IllegalArgumentException, IllegalAccessException {
	}

	public void setLong(Object holder, long value) throws IllegalArgumentException, IllegalAccessException {
	}

	public void setFloat(Object holder, float value) throws IllegalArgumentException, IllegalAccessException {
	}

	public void setDouble(Object holder, double value) throws IllegalArgumentException, IllegalAccessException {
	}

	public void setObject(Object holder, Object value) throws IllegalArgumentException, IllegalAccessException {
	}

	@Override
	public int hashCode() {
		return method.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof MethodProperty && ((MethodProperty)obj).method.equals(method));
	}

	@Override
	public String toString() {
		return method.toString();
	}
}
