/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

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
  
  
  SLSB: This class and all the modifications to use it are marked with the 'SLSB' tag.
 */

package org.granite.generator.as3.reflect;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.reflect.JavaMethod.MethodType;
import org.granite.generator.util.GenericTypeUtil;
import org.granite.messaging.service.annotations.IgnoredMethod;
import org.granite.messaging.service.annotations.RemoteDestination;

/**
 * @author Franck WOLFF
 */
public class JavaRemoteDestination extends JavaAbstractType {

	// /////////////////////////////////////////////////////////////////////////
	// Fields.

	protected final Set<JavaImport> imports = new HashSet<JavaImport>();
	protected final JavaType superclass;
	protected final List<JavaMethod> methods;
    protected final Map<String, JavaMethodProperty> properties;
	protected final String destinationName;
	protected final String channelId;
	
	// /////////////////////////////////////////////////////////////////////////
	// Constructor.

	public JavaRemoteDestination(JavaTypeFactory provider, Class<?> type, URL url) {
		super(provider, type, url);

		// Find superclass (controller filtered).
		this.superclass = provider.getJavaTypeSuperclass(type);

		// Collect methods.
		this.methods = Collections.unmodifiableList(initMethods());

        // Collect bean properties.
        this.properties = Collections.unmodifiableMap(initProperties());

		// Collect imports.
		if (superclass != null)
			addToImports(provider.getJavaImport(superclass.getType()));
		
		RemoteDestination rd = type.getAnnotation(RemoteDestination.class);
		if (rd != null) {
			destinationName = rd.id();
			channelId = rd.channel();
		}
		else {
			destinationName = null;
			channelId = null;
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// Properties.

	public Set<JavaImport> getImports() {
		return imports;
	}

	protected void addToImports(JavaImport javaImport) {
		if (javaImport != null)
			imports.add(javaImport);
	}

	protected void addToImports(Set<JavaImport> javaImports) {
		if (javaImports != null)
			imports.addAll(javaImports);
	}

	public boolean hasSuperclass() {
		return superclass != null;
	}

	public JavaType getSuperclass() {
		return superclass;
	}
	
    public Collection<JavaMethod> getMethods() {
        return methods;
    }
	
    public Collection<JavaMethodProperty> getProperties() {
        return properties.values();
    }

	public String getDestinationName() {
		return destinationName;
	}

	public String getChannelId() {
		return channelId;
	}
	
	public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
		return type.isAnnotationPresent(annotation);
	}

	// /////////////////////////////////////////////////////////////////////////
	// Utilities.

	protected List<JavaMethod> initMethods() {
		List<JavaMethod> methodList = new ArrayList<JavaMethod>();

		// Get all methods for interfaces: normally, even if it is possible in Java
		// to override a method into a inherited interface, there is no meaning
		// to do so (we just ignore potential compilation issues with generated AS3
		// classes for this case since it is always possible to remove the method
		// re-declaration in the child interface).
		Method[] methods = null;
		ParameterizedType[] declaringTypes = GenericTypeUtil.getDeclaringTypes(type);
		
		if (type.isInterface())
			methods = filterOverrides(type.getMethods());
		else
			methods = type.getDeclaredMethods();
		
		for (Method method : methods) {
			if (shouldGenerateMethod(method)) {
				JavaMethod javaMethod = new JavaMethod(method, MethodType.OTHER, this.provider, declaringTypes);
				
				for (Class<?> clazz : javaMethod.getParameterTypes()) {
					if (clazz.isMemberClass() && !clazz.isEnum()) {
						throw new UnsupportedOperationException(
							"Inner classes are not supported (except enums): " + clazz
						);
					}
				}
				for (ClientType paramType : javaMethod.getClientParameterTypes())
					addToImports(provider.getJavaImports(paramType, false));
				
				Class<?> clazz = javaMethod.getReturnType();
				if (clazz.isMemberClass() && !clazz.isEnum()) {
					throw new UnsupportedOperationException(
						"Inner classes are not supported (except enums): " + clazz
					);
				}
				if (!clazz.equals(Void.class) && !clazz.equals(void.class))
					addToImports(provider.getJavaImports(javaMethod.getClientReturnType(), false));
				
				methodList.add(javaMethod);
			}
		}
		
		Collections.sort(methodList, new Comparator<JavaMethod>() {
			public int compare(JavaMethod m1, JavaMethod m2) {
				if (m1.getName().equals(m2.getName())) {
					if (m1.getMember().getDeclaringClass() != m2.getMember().getDeclaringClass()) {
						if (m1.getMember().getDeclaringClass().isAssignableFrom(m2.getMember().getDeclaringClass()))
							return -1;
						if (m2.getMember().getDeclaringClass().isAssignableFrom(m1.getMember().getDeclaringClass()))
							return 1;
					}
					
					if (m1.getParameterTypes().length < m2.getParameterTypes().length)
						return -1;
					else if (m1.getParameterTypes().length > m2.getParameterTypes().length)
						return 1;
					
					for (int i = 0; i < m1.getParameterTypes().length; i++) {
						if (m1.getParameterTypes()[i] == m2.getParameterTypes()[i])
							continue;
						if (m1.getParameterTypes()[i].isAssignableFrom(m2.getParameterTypes()[i]))
							return -1;
						else if (m2.getParameterTypes()[i].isAssignableFrom(m1.getParameterTypes()[i]))
							return 1;
						return m1.getParameterTypes()[i].toString().compareTo(m2.getParameterTypes()[i].toString());
					}
				}
				
				return m1.getName().compareTo(m2.getName());
			}
		});

		return methodList;
	}
	
	protected boolean shouldGenerateMethod(Method method) {
		if (!Modifier.isPublic(method.getModifiers())
			|| Modifier.isStatic(method.getModifiers())
			|| method.isAnnotationPresent(IgnoredMethod.class))
			return false;
		
		return true;
	}	

	protected Map<String, JavaMethodProperty> initProperties() {
        Map<String, JavaMethodProperty> propertyMap = new LinkedHashMap<String, JavaMethodProperty>();

		// Get all methods for interfaces: normally, even if it is possible in Java
		// to override a method into a inherited interface, there is no meaning
		// to do so (we just ignore potential compilation issues with generated AS3
		// classes for this case since it is always possible to remove the method
		// re-declaration in the child interface).
		Method[] methods = null;
		if (type.isInterface())
			methods = type.getMethods();
		else
			methods = type.getDeclaredMethods();
		
		List<Object[]> tmp = new ArrayList<Object[]>();
		
		for (Method method : methods) {
			if (shouldGenerateProperty(method)) {				
				for (Class<?> clazz : method.getParameterTypes())
					addToImports(provider.getJavaImport(clazz));
				addToImports(provider.getJavaImport(method.getReturnType()));
				
				String propertyName = Introspector.decapitalize(method.getName().startsWith("is") ? method.getName().substring(2) : method.getName().substring(3));

				Object[] property = null;
				for (Object[] mp : tmp) {
					if (mp[0].equals(propertyName)) {
						property = mp;
						break;
					}
				}
				if (property == null) {
					property = new Object[] { propertyName, null, null };
					tmp.add(property);
				}
				if (method.getName().startsWith("set"))
					property[2] = method;
				else
					property[1] = method;
			}
		}
		
		for (Object[] property : tmp) {
			JavaMethod readMethod = property[1] != null ? new JavaMethod((Method)property[1], MethodType.GETTER) : null;
			JavaMethod writeMethod = property[2] != null ? new JavaMethod((Method)property[2], MethodType.SETTER) : null;
			propertyMap.put((String)property[0], new JavaMethodProperty(provider, (String)property[0], readMethod, writeMethod));
		}
		
		return propertyMap;
	}
	
	protected boolean shouldGenerateProperty(Method method) {
		return false;
	}	

	
	public JavaInterface convertToJavaInterface() {
		return new JavaInterface(getProvider(), getType(), getUrl());
	}
	
	
	public static Method[] filterOverrides(Method[] methods) {
		List<Method> filteredMethods = new ArrayList<Method>();
		for (Method method : methods) {
			// Lookup an override in subinterfaces
			boolean foundOverride = false;
			for (Method m : methods) {
				if (method == m)
					continue;
				if (m.getName().equals(method.getName()) && Arrays.equals(m.getParameterTypes(), method.getParameterTypes()) &&
					method.getDeclaringClass().isAssignableFrom(m.getDeclaringClass())) {
					foundOverride = true;
					break;
				}
			}
			if (!foundOverride)
				filteredMethods.add(method);
		}
		return filteredMethods.toArray(new Method[filteredMethods.size()]);
	}
}
