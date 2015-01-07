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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.granite.messaging.annotations.Exclude;
import org.granite.messaging.annotations.Include;
import org.granite.messaging.annotations.Serialized;

/**
 * @author Franck WOLFF
 */
public class ClassDescriptor {
	
	private static final Class<?>[] WRITE_OBJECT_PARAMS = new Class<?>[]{ ObjectOutputStream.class };
	private static final Class<?>[] READ_OBJECT_PARAMS = new Class<?>[]{ ObjectInputStream.class };
	
	private final Class<?> cls;
	private final Instantiator instantiator;
	private final List<Property> properties;
	private final Method writeObjectMethod;
	private final Method readObjectMethod;
	private final Method writeReplaceMethod;
	private final Method readResolveMethod;
	
	private final ClassDescriptor parent;

	private volatile SoftReference<List<Property>> inheritedProperties = new SoftReference<List<Property>>(null);
	
	public ClassDescriptor(Reflection reflection, Class<?> cls) {
		this.cls = cls;
		
		this.instantiator = getInstantiator(reflection, cls);
		
		if (!Externalizable.class.isAssignableFrom(cls)) {
			this.writeObjectMethod = getPrivateMethod(cls, "writeObject", WRITE_OBJECT_PARAMS, Void.TYPE);
			this.readObjectMethod = getPrivateMethod(cls, "readObject", READ_OBJECT_PARAMS, Void.TYPE);
		}
		else {
			this.writeObjectMethod = null;
			this.readObjectMethod = null;
		}
		
		this.writeReplaceMethod = getInheritedMethod(cls, "writeReplace", null, Object.class);
		this.readResolveMethod = getInheritedMethod(cls, "readResolve", null, Object.class);
		
		this.properties = getSerializableProperties(reflection, cls);

		this.parent = reflection.getDescriptor(cls.getSuperclass());
	}
	
	private static Instantiator getInstantiator(Reflection reflection, Class<?> cls) {
		try {
			return new ConstructorInstantiator(cls.getConstructor());
		}
		catch (NoSuchMethodException e) {
			// Fallback...
		}
		
		try {
			return reflection.instanceFactory.newInstantiator(cls);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public Class<?> getCls() {
		return cls;
	}
	
	public Object newInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return instantiator.newInstance();
	}

	public List<Property> getSerializableProperties() {
		return properties;
	}
	
	public List<Property> getInheritedSerializableProperties() {
		List<Property> inheritedSerializableProperties = inheritedProperties.get();
		if (inheritedSerializableProperties == null) {
			if (parent == null)
				inheritedSerializableProperties = properties;
			else {
				List<Property> parentProperties = parent.getInheritedSerializableProperties();
				if (!parentProperties.isEmpty()) {
					if (!properties.isEmpty()) {
						inheritedSerializableProperties = new ArrayList<Property>(parentProperties.size() + properties.size());
						inheritedSerializableProperties.addAll(parentProperties);
						inheritedSerializableProperties.addAll(properties);
						inheritedSerializableProperties = Collections.unmodifiableList(inheritedSerializableProperties);
					}
					else
						inheritedSerializableProperties = parentProperties;
				}
				else
					inheritedSerializableProperties = properties;
			}
			inheritedProperties = new SoftReference<List<Property>>(inheritedSerializableProperties);
		}
		return inheritedSerializableProperties;
	}
	
	public boolean hasWriteObjectMethod() {
		return writeObjectMethod != null;
	}

	public void invokeWriteObjectMethod(ObjectOutputStream oos, Object v) throws IOException {
		if (writeObjectMethod == null)
			throw new UnsupportedOperationException("No writeObject(...) method in " + cls);
		
		try {
			writeObjectMethod.invoke(v, oos);
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof IOException)
				throw (IOException)t;
			throw new IOException(t);
		}
		catch (IllegalAccessException e) {
			throw new InternalError();
		}
	}
	
	public boolean hasReadObjectMethod() {
		return readObjectMethod != null;
	}
	
	public void invokeReadObjectMethod(ObjectInputStream ois, Object v) throws ClassNotFoundException, IOException {
		if (readObjectMethod == null)
			throw new UnsupportedOperationException("No readObject(...) method in " + cls);
		
		try {
			readObjectMethod.invoke(v, ois);
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof ClassNotFoundException)
				throw (ClassNotFoundException)t;
			if (t instanceof IOException)
				throw (IOException)t;
			throw new IOException(t);
		}
		catch (IllegalAccessException e) {
			throw new InternalError();
		}
	}
	
	public boolean hasWriteReplaceMethod() {
		return writeReplaceMethod != null;
	}

	public Object invokeWriteReplaceMethod(Object v) throws IOException {
		if (writeReplaceMethod == null)
			throw new UnsupportedOperationException("No writeReplace() method in " + cls);
		
		try {
			return writeReplaceMethod.invoke(v);
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof IOException)
				throw (IOException)t;
			throw new IOException(t);
		}
		catch (IllegalAccessException e) {
			throw new InternalError();
		}
	}

	public boolean hasReadResolveMethod() {
		return readResolveMethod != null;
	}

	public Object invokeReadResolveMethod(Object v) throws IOException {
		if (readResolveMethod == null)
			throw new UnsupportedOperationException("No readResolve() method in " + cls);
		
		try {
			return readResolveMethod.invoke(v);
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof IOException)
				throw (IOException)t;
			throw new IOException(t);
		}
		catch (IllegalAccessException e) {
			throw new InternalError();
		}
	}

	public ClassDescriptor getParent() {
		return parent;
	}
	
	protected static Method getInheritedMethod(Class<?> cls, String name, Class<?>[] params, Class<?> ret) {
		try {
			Method method = cls.getDeclaredMethod(name, params);
			method.setAccessible(true);
			return (
				method.getReturnType() == ret && ((Modifier.STATIC | Modifier.ABSTRACT) & method.getModifiers()) == 0 ?
				method :
				null
			);
		}
		catch (NoSuchMethodException e) {
		}
		
		final Class<?> root = cls;
		while ((cls = cls.getSuperclass()) != null) {
			try {
				Method method = cls.getDeclaredMethod(name, params);
				method.setAccessible(true);
				
				if (method.getReturnType() != ret)
					return null;
				
				final int modifiers = method.getModifiers();
				
				if (((Modifier.STATIC | Modifier.ABSTRACT | Modifier.PRIVATE) & modifiers) != 0)
					return null;
				
				if (((Modifier.PUBLIC | Modifier.PROTECTED) & modifiers) != 0)
					return method;
				
				return root.getPackage() == cls.getPackage() ? method : null;
			}
			catch (NoSuchMethodException e) {
			}
		}
		
		return null;
	}
	
	protected static Method getPrivateMethod(Class<?> cls, String name, Class<?>[] params, Class<?> ret) {
		try {
			Method method = cls.getDeclaredMethod(name, params);
			method.setAccessible(true);
			if (method.getReturnType() == ret && (method.getModifiers() & (Modifier.STATIC | Modifier.PRIVATE)) == Modifier.PRIVATE)
				return method;
		}
		catch (NoSuchMethodException e) {
		}
		return null;
	}
	
	protected static List<Property> getSerializableProperties(Reflection reflection, Class<?> cls) {
		Field[] declaredFields = cls.getDeclaredFields();
		
		List<Property> serializableProperties = new ArrayList<Property>(declaredFields.length);
		for (Field field : declaredFields) {
			if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0 &&
				!field.isAnnotationPresent(Exclude.class)) {
				field.setAccessible(true);
				serializableProperties.add(reflection.newFieldProperty(field));
			}
		}
		
		Method[] declaredMethods = cls.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if ((method.getModifiers() & (Modifier.STATIC | Modifier.PRIVATE | Modifier.PROTECTED)) == 0 &&
				method.getReturnType() != Void.TYPE &&
				method.isAnnotationPresent(Include.class) &&
				method.getParameterTypes().length == 0) {
				
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
				
				serializableProperties.add(reflection.newMethodProperty(method, null, name));
			}
		}
		
		Serialized serialized = cls.getAnnotation(Serialized.class);
		if (serialized != null && serialized.propertiesOrder().length > 0) {
			String[] value = serialized.propertiesOrder();
			
			if (value.length != serializableProperties.size())
				throw new ReflectionException("Illegal @Serialized(propertiesOrder) value: " + serialized + " on: " + cls.getName() + " (bad length)");
			
			for (int i = 0; i < value.length; i++) {
				String propertyName = value[i];
				
				boolean found = false;
				for (int j = i; j < value.length; j++) {
					Property property = serializableProperties.get(j);
					if (property.getName().equals(propertyName)) {
						found = true;
						if (i != j) {
							serializableProperties.set(j, serializableProperties.get(i));
							serializableProperties.set(i, property);
						}
						break;
					}
				}
				if (!found)
					throw new ReflectionException("Illegal @Serialized(propertiesOrder) value: " + serialized + " on: " + cls.getName() + " (\"" + propertyName + "\" isn't a property name)");
			}
		}
		else
			Collections.sort(serializableProperties, reflection.getLexicalPropertyComparator());
		
		return Collections.unmodifiableList(serializableProperties);
	}
}
