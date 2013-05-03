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

package org.granite.messaging.jmf.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Franck WOLFF
 */
public class Reflection {
	
	protected static final int STATIC_TRANSIENT_MASK = Modifier.STATIC | Modifier.TRANSIENT;

	protected final ClassLoader classLoader;
	protected final ConstructorFactory constructorFactory;
	protected final Comparator<Field> lexicalFieldComparator;
	protected final ConcurrentMap<Class<?>, List<Field>> serializableFieldsCache;
	
	public Reflection(ClassLoader classLoader) {
		this.classLoader = classLoader;
		
		this.constructorFactory = new SunConstructorFactory();
		
		this.lexicalFieldComparator = new Comparator<Field>() {
			public int compare(Field f1, Field f2) {
				return f1.getName().compareTo(f2.getName());
			}
		};
		
		this.serializableFieldsCache = new ConcurrentHashMap<Class<?>, List<Field>>();
	}
	
	public ClassLoader getClassLoader() {
		return (classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader());
	}

	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return getClassLoader().loadClass(className);
	}
	
	public <T> T newInstance(Class<T> cls)
		throws InstantiationException, IllegalAccessException, IllegalArgumentException,
		InvocationTargetException, SecurityException, NoSuchMethodException {
		
		return findDefaultContructor(cls).newInstance();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T newInstance(String className)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
		InvocationTargetException, SecurityException, NoSuchMethodException {
		
		return newInstance((Class<T>)loadClass(className));
	}
	

	@SuppressWarnings("unchecked")
	public <T> Constructor<T> findDefaultContructor(Class<T> cls) throws SecurityException, NoSuchMethodException {
		
		try {
			return cls.getConstructor();
		}
		catch (NoSuchMethodException e) {
			return (Constructor<T>)constructorFactory.newConstructorForSerialization(cls);
		}
	}
	
	public List<Field> findSerializableFields(Class<?> cls) throws SecurityException {
		List<Field> serializableFields = serializableFieldsCache.get(cls);
		
		if (serializableFields == null) {
			List<Class<?>> hierarchy = new ArrayList<Class<?>>();
			for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass())
				hierarchy.add(c);
			
			serializableFields = new ArrayList<Field>();
			for (int i = hierarchy.size() - 1; i >= 0; i--) {
				Class<?> c = hierarchy.get(i);
				serializableFields.addAll(findSerializableDeclaredFields(c));
			}
			serializableFields = Collections.unmodifiableList(serializableFields);
			List<Field> previous = serializableFieldsCache.putIfAbsent(cls, serializableFields);
			if (previous != null)
				serializableFields = previous;
		}
		
		return serializableFields;
	}

	protected List<Field> findSerializableDeclaredFields(Class<?> cls) throws SecurityException {
		
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
		
		Collections.sort(serializableFields, lexicalFieldComparator);
		
		return serializableFields;
	}
	
	public boolean isRegularClass(Class<?> cls) {
		return cls != Class.class && !cls.isAnnotation() && !cls.isArray() &&
			!cls.isEnum() && !cls.isInterface() && !cls.isPrimitive();
	}
}

