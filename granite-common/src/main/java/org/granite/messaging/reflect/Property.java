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

/**
 * @author Franck WOLFF
 */
public interface Property {
	
	Class<?> getType();
	String getName();
	
	boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
	
	boolean isReadable();
	boolean isWritable();
	
	boolean getBoolean(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	char getChar(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	byte getByte(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	short getShort(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	int getInt(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	long getLong(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	float getFloat(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	double getDouble(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	Object getObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	
	Object getRawObject(Object holder) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	
	void setBoolean(Object holder, boolean value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	void setChar(Object holder, char value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	void setByte(Object holder, byte value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	void setShort(Object holder, short value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	void setInt(Object holder, int value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	void setLong(Object holder, long value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	void setFloat(Object holder, float value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	void setDouble(Object holder, double value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	void setObject(Object holder, Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
}
