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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Reflection provider
 *
 * @author Franck WOLFF
 */
public class Reflection {
	
	protected static final int STATIC_TRANSIENT_MASK = Modifier.STATIC | Modifier.TRANSIENT;
	protected static final int STATIC_PRIVATE_PROTECTED_MASK = Modifier.STATIC | Modifier.PRIVATE | Modifier.PROTECTED;
	protected static final Property NULL_PROPERTY = new NullProperty();

	protected final ClassLoader classLoader;
	protected final BypassConstructorAllocator instanceFactory;
	protected final Comparator<Property> lexicalPropertyComparator;
	
	protected final ConcurrentMap<Class<?>, ClassDescriptor> descriptorCache;
	protected final ConcurrentMap<SinglePropertyKey, Property> singlePropertyCache;
	
	public Reflection(ClassLoader classLoader) {
		this(classLoader, null);
	}
	
	public Reflection(ClassLoader classLoader, BypassConstructorAllocator instanceFactory) {
		this.classLoader = classLoader;
		
		if (instanceFactory != null)
			this.instanceFactory = instanceFactory;
		else {
			try {
				this.instanceFactory = new SunBypassConstructorAllocator();
			}
			catch (Exception e) {
				throw new RuntimeException("Could not instantiate BypassConstructorAllocator", e);
			}
		}
		
		this.lexicalPropertyComparator = new Comparator<Property>() {
			public int compare(Property p1, Property p2) {
				return p1.getName().compareTo(p2.getName());
			}
		};
		
		this.descriptorCache = new ConcurrentHashMap<Class<?>, ClassDescriptor>();
		this.singlePropertyCache = new ConcurrentHashMap<SinglePropertyKey, Property>();
	}
	
	public ClassLoader getClassLoader() {
		return (classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader());
	}

	public BypassConstructorAllocator getInstanceFactory() {
		return instanceFactory;
	}

	public Comparator<Property> getLexicalPropertyComparator() {
		return lexicalPropertyComparator;
	}

	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return getClassLoader().loadClass(className);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<T> cls)
		throws InstantiationException, IllegalAccessException, IllegalArgumentException,
		InvocationTargetException, SecurityException {
		
		ClassDescriptor desc = descriptorCache.get(cls);
		if (desc != null)
			return (T)desc.newInstance();
	    
		try {
	        Constructor<T> constructor = cls.getConstructor();
	        return constructor.newInstance();
	    }
	    catch (NoSuchMethodException e) {
	        return (T)instanceFactory.newInstantiator(cls).newInstance();
	    }
	}
	
	@SuppressWarnings("unchecked")
	public <T> T newInstance(String className)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
		InvocationTargetException, SecurityException {
		
		return newInstance((Class<T>)loadClass(className));
	}
	
	public Property findSerializableProperty(Class<?> cls, String name) throws SecurityException {
		List<Property> properties = findSerializableProperties(cls);
		for (Property property : properties) {
			if (name.equals(property.getName()))
				return property;
		}
		return null;
	}
	
	public ClassDescriptor getDescriptor(Class<?> cls) {
		if (cls == null || cls == Object.class || !isRegularClass(cls))
			return null;

		ClassDescriptor descriptor = descriptorCache.get(cls);
		if (descriptor == null) {
			descriptor = new ClassDescriptor(this, cls);
			ClassDescriptor previousDescriptor = descriptorCache.putIfAbsent(cls, descriptor);
			if (previousDescriptor != null)
				descriptor = previousDescriptor;
		}
		return descriptor;
	}
	
	public List<Property> findSerializableProperties(Class<?> cls) throws SecurityException {
		ClassDescriptor descriptor = getDescriptor(cls);
		if (descriptor == null)
			return Collections.emptyList();
		return descriptor.getInheritedSerializableProperties();
	}
	
	protected FieldProperty newFieldProperty(Field field) {
		return new SimpleFieldProperty(field);
	}
	
	protected MethodProperty newMethodProperty(Method getter, Method setter, String name) {
		return new SimpleMethodProperty(getter, setter, name);
	}
	
	public boolean isRegularClass(Class<?> cls) {
		return cls != Class.class && !cls.isAnnotation() && !cls.isArray() &&
			!cls.isEnum() && !cls.isInterface() && !cls.isPrimitive();
	}
	
	public Property findProperty(Class<?> cls, String name, Class<?> type) {
		NameTypePropertyKey key = new NameTypePropertyKey(cls, name, type);
		
		Property property = singlePropertyCache.get(key);
		
		if (property == null) {
			Field field = null;
			
			for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
				try {
					field = c.getDeclaredField(name);
				}
				catch (NoSuchFieldException e) {
					continue;
				}
				
				if (field.getType() != type)
					continue;
				
				field.setAccessible(true);
				break;
			}
			
			if (field == null)
				property = NULL_PROPERTY;
			else
				property = newFieldProperty(field);
			
			Property previous = singlePropertyCache.putIfAbsent(key, property);
			if (previous != null)
				property = previous;
		}
		
		return (property != NULL_PROPERTY ? property : null);
	}
	
	public Property findProperty(Class<?> cls, Class<? extends Annotation> annotationClass) {
		AnnotatedPropertyKey key = new AnnotatedPropertyKey(cls, annotationClass);
		
		Property property = singlePropertyCache.get(key);
		
		if (property == null) {
			boolean searchFields = false;
			boolean searchMethods = false;
			
			if (!annotationClass.isAnnotationPresent(Target.class))
				searchFields = searchMethods = true;
			else {
				Target target = annotationClass.getAnnotation(Target.class);
				for (ElementType targetType : target.value()) {
					if (targetType == ElementType.FIELD)
						searchFields = true;
					else if (targetType == ElementType.METHOD)
						searchMethods = true;
				}
			}
			
			if (searchFields == false && searchMethods == false)
				return null;
			
			final int modifierMask = Modifier.PUBLIC | Modifier.STATIC;
			
			classLoop:
			for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
				if (searchMethods) {
					for (Method method : c.getDeclaredMethods()) {
						if ((method.getModifiers() & modifierMask) != Modifier.PUBLIC ||
							!method.isAnnotationPresent(annotationClass))
							continue;
						
						if (method.getReturnType() == Void.TYPE) {
							if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
								String name = method.getName().substring(3);
								
								if (name.length() == 0)
									continue;
								
								Method getter = null;
								try {
									getter = cls.getMethod("get" + name);
								}
								catch (NoSuchMethodException e) {
									try {
										getter = cls.getMethod("is" + name);
									}
									catch (Exception f) {
									}
								}
								
								if (getter != null && (getter.getModifiers() & Modifier.STATIC) != 0 &&
									getter.getReturnType() != method.getParameterTypes()[0])
									getter = null;
								
								if (getter == null)
									continue;
								
								name = name.substring(0, 1).toLowerCase() + name.substring(1);
								property = newMethodProperty(getter, method, name);
								break classLoop;
							}
						}
						else if (method.getParameterTypes().length == 0 && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
							String name;
							if (method.getName().startsWith("get"))
								name = method.getName().substring(3);
							else
								name = method.getName().substring(2);
							
							if (name.length() == 0)
								continue;
							
							Method setter = null;
							try {
								setter = cls.getMethod("set" + name);
							}
							catch (NoSuchMethodException e) {
							}
							
							if (setter != null && (setter.getModifiers() & Modifier.STATIC) != 0 &&
								method.getReturnType() != setter.getParameterTypes()[0])
								setter = null;
							
							name = name.substring(0, 1).toLowerCase() + name.substring(1);
							property = newMethodProperty(method, setter, name);
							break classLoop;
						}
					}
				}
				
				if (searchFields) {
					for (Field field : c.getDeclaredFields()) {
						if ((field.getModifiers() & Modifier.STATIC) == 0 && field.isAnnotationPresent(annotationClass)) {
							property = newFieldProperty(field);
							break classLoop;
						}
					}
				}
			}
			
			if (property == null)
				property = NULL_PROPERTY;
			
			Property previous = singlePropertyCache.putIfAbsent(key, property);
			if (previous != null)
				property = previous;
		}
		
		return (property != NULL_PROPERTY ? property : null);
	}
	
	protected static abstract class SinglePropertyKey {

		protected final Class<?> cls;
		
		public SinglePropertyKey(Class<?> cls) {
			this.cls = cls;
		}
	}
	
	protected static class AnnotatedPropertyKey extends SinglePropertyKey {
		
		private final Class<? extends Annotation> annotationClass;
		
		public AnnotatedPropertyKey(Class<?> cls, Class<? extends Annotation> annotationClass) {
			super(cls);
			this.annotationClass = annotationClass;
		}

		public Class<?> getCls() {
			return cls;
		}

		public Class<? extends Annotation> getAnnotationClass() {
			return annotationClass;
		}

		@Override
		public int hashCode() {
			return (31 * cls.hashCode()) + annotationClass.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof AnnotatedPropertyKey))
				return false;
			AnnotatedPropertyKey key = (AnnotatedPropertyKey)obj;
			return cls.equals(key.cls) && annotationClass.equals(key.annotationClass);
		}
	}
	
	protected static class NameTypePropertyKey extends SinglePropertyKey {
		
		private final String name;
		private final Class<?> type;

		public NameTypePropertyKey(Class<?> cls, String name, Class<?> type) {
			super(cls);
			this.name = name;
			this.type = type;
		}

		public Class<?> getCls() {
			return cls;
		}

		public String getName() {
			return name;
		}

		public Class<?> getType() {
			return type;
		}

		@Override
		public int hashCode() {
			return (31 * (31 * cls.hashCode()) + name.hashCode()) + type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof NameTypePropertyKey))
				return false;
			NameTypePropertyKey key = (NameTypePropertyKey)obj;
			return cls.equals(key.cls) && name.equals(key.name) && type.equals(key.type);
		}
	}
}

