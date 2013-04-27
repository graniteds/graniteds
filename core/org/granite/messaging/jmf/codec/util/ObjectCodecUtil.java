/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.messaging.jmf.codec.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Franck WOLFF
 */
public class ObjectCodecUtil {
	
	private static final int STATIC_TRANSIENT_MASK = Modifier.STATIC | Modifier.TRANSIENT;
	
	private static final DefaultConstructorFactory defaultConstructorFactory;
	static {
		try {
			defaultConstructorFactory = new SunDefaultConstructorFactory(ObjectCodecUtil.class.getClassLoader());
		}
		catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
	public static final Comparator<Field> LEXICAL_FIELD_COMPARATOR = new Comparator<Field>() {
		public int compare(Field f1, Field f2) {
			return f1.getName().compareTo(f2.getName());
		}
	};
	
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> findDefaultContructor(Class<T> cls) throws SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		try {
			return cls.getConstructor();
		}
		catch (NoSuchMethodException e) {
			return (Constructor<T>)defaultConstructorFactory.findDefaultConstructor(cls);
		}
	}
	
	public static List<Field> findSerializableFields(Class<?> cls) throws SecurityException {
		return findSerializableFields(cls, LEXICAL_FIELD_COMPARATOR);
	}
	
	public static List<Field> findSerializableFields(Class<?> cls, Comparator<Field> fieldComparator) throws SecurityException {
		List<Field> serializableFields = new ArrayList<Field>();
		for (Class<?> c = cls; c != Object.class; c = c.getSuperclass())
			serializableFields.addAll(findSerializableDeclaredFields(c, fieldComparator));
		return serializableFields;
	}
	
	public static List<Field> findSerializableDeclaredFields(Class<?> cls) throws SecurityException {
		return findSerializableDeclaredFields(cls, LEXICAL_FIELD_COMPARATOR);
	}

	public static List<Field> findSerializableDeclaredFields(Class<?> cls, Comparator<Field> fieldComparator) throws SecurityException {
		if (!isRegularClass(cls))
			throw new IllegalArgumentException("Not a regular class: " + cls);

		Field[] declaredFields = cls.getDeclaredFields();
		List<Field> serializableFields = new ArrayList<Field>(declaredFields.length);
		for (int i = 0; i < declaredFields.length; i++) {
			Field field = declaredFields[i];
			
			int modifiers = field.getModifiers();
			if ((modifiers & STATIC_TRANSIENT_MASK) == 0) {
				declaredFields[i].setAccessible(true);
				serializableFields.add(field);
			}
		}
		
		if (fieldComparator != null)
			Collections.sort(serializableFields, fieldComparator);
		
		return serializableFields;
	}
	
	public static boolean isRegularClass(Class<?> cls) {
		return !cls.isAnnotation() && !cls.isArray() && !cls.isEnum() && !cls.isInterface() && !cls.isPrimitive();
	}
}

interface DefaultConstructorFactory {
	
	public Constructor<?> findDefaultConstructor(Class<?> cls)
	    	throws NoSuchMethodException, SecurityException,
	    	IllegalAccessException, InvocationTargetException;
}

class SunDefaultConstructorFactory implements DefaultConstructorFactory {

    private final Object reflectionFactory;
    private final Method newConstructorForSerialization;

    public SunDefaultConstructorFactory(ClassLoader classLoader)
    	throws ClassNotFoundException, NoSuchMethodException, SecurityException,
    	IllegalAccessException, InvocationTargetException {
        
    	Class<?> factoryClass = classLoader.loadClass("sun.reflect.ReflectionFactory");
        Method getReflectionFactory = factoryClass.getDeclaredMethod("getReflectionFactory");
        reflectionFactory = getReflectionFactory.invoke(null);
        newConstructorForSerialization = factoryClass.getDeclaredMethod(
            "newConstructorForSerialization",
            new Class[]{Class.class, Constructor.class}
        );
    }
    
    public Constructor<?> findDefaultConstructor(Class<?> cls)
    	throws NoSuchMethodException, SecurityException,
    	IllegalAccessException, InvocationTargetException {

    	Constructor<?> constructor = Object.class.getDeclaredConstructor();
        constructor = (Constructor<?>)newConstructorForSerialization.invoke(
            reflectionFactory,
            new Object[]{cls, constructor}
        );
        
        constructor.setAccessible(true);
        
        return constructor;
    }
}

