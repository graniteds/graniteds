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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.granite.messaging.annotations.Include;
import org.granite.messaging.annotations.Exclude;

/**
 * @author Franck WOLFF
 */
public class Reflection {
	
	protected static final int STATIC_TRANSIENT_MASK = Modifier.STATIC | Modifier.TRANSIENT;
	protected static final int STATIC_PRIVATE_PROTECTED_MASK = Modifier.STATIC | Modifier.PRIVATE | Modifier.PROTECTED;

	protected final ClassLoader classLoader;
	protected final ConstructorFactory constructorFactory;
	protected final Comparator<Property> lexicalPropertyComparator;
	protected final ConcurrentMap<Class<?>, List<Property>> serializableFieldsCache;
	
	public Reflection(ClassLoader classLoader) {
		this.classLoader = classLoader;
		
		this.constructorFactory = new SunConstructorFactory();
		
		this.lexicalPropertyComparator = new Comparator<Property>() {
			public int compare(Property p1, Property p2) {
				return p1.getName().compareTo(p2.getName());
			}
		};
		
		this.serializableFieldsCache = new ConcurrentHashMap<Class<?>, List<Property>>();
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
	
	public List<Property> findSerializableFields(Class<?> cls) throws SecurityException {
		List<Property> serializableFields = serializableFieldsCache.get(cls);
		
		if (serializableFields == null) {
			List<Class<?>> hierarchy = new ArrayList<Class<?>>();
			for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass())
				hierarchy.add(c);
			
			serializableFields = new ArrayList<Property>();
			for (int i = hierarchy.size() - 1; i >= 0; i--) {
				Class<?> c = hierarchy.get(i);
				serializableFields.addAll(findSerializableDeclaredFields(c));
			}
			serializableFields = Collections.unmodifiableList(serializableFields);
			List<Property> previous = serializableFieldsCache.putIfAbsent(cls, serializableFields);
			if (previous != null)
				serializableFields = previous;
		}
		
		return serializableFields;
	}

	protected List<Property> findSerializableDeclaredFields(Class<?> cls) throws SecurityException {
		
		if (!isRegularClass(cls))
			throw new IllegalArgumentException("Not a regular class: " + cls);

		Field[] declaredFields = cls.getDeclaredFields();
		List<Property> serializableFields = new ArrayList<Property>(declaredFields.length);
		for (Field field : declaredFields) {
			int modifiers = field.getModifiers();
			if ((modifiers & STATIC_TRANSIENT_MASK) == 0 && !field.isAnnotationPresent(Exclude.class)) {
				field.setAccessible(true);
				serializableFields.add(new FieldProperty(field));
			}
		}
		
		Method[] declaredMethods = cls.getDeclaredMethods();
		for (Method method : declaredMethods) {
			int modifiers = method.getModifiers();
			if ((modifiers & STATIC_PRIVATE_PROTECTED_MASK) == 0 &&
				method.isAnnotationPresent(Include.class) &&
				method.getParameterTypes().length == 0 &&
				method.getReturnType() != Void.TYPE) {
				
				String name = method.getName();
				if (name.startsWith("get")) {
					if (name.length() <= 3)
						continue;
					name = name.substring(3, 4).toLowerCase() + name.substring(4);
				}
				else if (name.startsWith("is") &&
					(method.getReturnType() == Boolean.class || method.getReturnType() == Boolean.TYPE)) {
					if (name.length() <= 2)
						continue;
					name = name.substring(2, 3).toLowerCase() + name.substring(3);
				}
				else
					continue;
				
				serializableFields.add(new MethodProperty(method, name));
			}
		}
		
		Collections.sort(serializableFields, lexicalPropertyComparator);
		
		return serializableFields;
	}
	
	public boolean isRegularClass(Class<?> cls) {
		return cls != Class.class && !cls.isAnnotation() && !cls.isArray() &&
			!cls.isEnum() && !cls.isInterface() && !cls.isPrimitive();
	}
}

