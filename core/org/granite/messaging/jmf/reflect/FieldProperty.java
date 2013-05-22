package org.granite.messaging.jmf.reflect;

import java.lang.reflect.Field;

public class FieldProperty implements Property {

	private final Field field;
	
	public FieldProperty(Field field) {
		this.field = field;
	}

	public Class<?> getType() {
		return field.getType();
	}

	public String getName() {
		return field.getName();
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
}
