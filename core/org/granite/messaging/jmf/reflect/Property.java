package org.granite.messaging.jmf.reflect;

import java.lang.reflect.InvocationTargetException;

public interface Property {

	Class<?> getType();
	String getName();
	
	boolean getBoolean(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	char getChar(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	byte getByte(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	short getShort(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	int getInt(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	long getLong(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	float getFloat(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	double getDouble(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	Object getObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	
	void setBoolean(Object holder, boolean value) throws IllegalArgumentException, IllegalAccessException;
	void setChar(Object holder, char value) throws IllegalArgumentException, IllegalAccessException;
	void setByte(Object holder, byte value) throws IllegalArgumentException, IllegalAccessException;
	void setShort(Object holder, short value) throws IllegalArgumentException, IllegalAccessException;
	void setInt(Object holder, int value) throws IllegalArgumentException, IllegalAccessException;
	void setLong(Object holder, long value) throws IllegalArgumentException, IllegalAccessException;
	void setFloat(Object holder, float value) throws IllegalArgumentException, IllegalAccessException;
	void setDouble(Object holder, double value) throws IllegalArgumentException, IllegalAccessException;
	void setObject(Object holder, Object value) throws IllegalArgumentException, IllegalAccessException;
}
