/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.granite.client.tide.Context;
import org.granite.client.tide.Factory;


/**
 * @author William DRAI
 */
public class InstanceFactory {
	
	private List<Factory<?>> initializers = new ArrayList<Factory<?>>();
	
    private Map<String, Factory<?>> factoriesByName = new HashMap<String, Factory<?>>();
    private Map<Class<?>, List<Factory<?>>> factoriesByType = new HashMap<Class<?>, List<Factory<?>>>();
    
    
    public void initModules(Set<Class<?>> moduleClasses) {
    	for (Class<?> moduleClass : moduleClasses)
    		setupModule(moduleClass);
    }
    
    private void setupModule(Class<?> moduleClass) {
    	for (Method method : moduleClass.getMethods()) {
    		if (method.getDeclaringClass() != moduleClass || !Modifier.isStatic(method.getModifiers()))
    			continue;
    		
    		Class<?> type = method.getReturnType();
    		
    		if (type == Void.class || type == void.class) {
    			initializers.add(new MethodFactory<Void>(method));
    		}
    		else {
	    		if (method.isAnnotationPresent(Named.class)) {
	    			String name = method.getAnnotation(Named.class).value();
	    			if ("".equals(name))
	    				name = method.getName();
	    			registerFactory(name, new MethodFactory<Object>(method));
	    		}
	    		else {
	    			registerFactory(type, new MethodFactory<Object>(method));
	    		}
    		}
    	}
    }
    
    public void registerFactory(String name, Factory<?> factory) {
    	factoriesByName.put(name, factory);
    }
    
    public void registerFactory(Class<?> type, Factory<?> factory) {
		List<Factory<?>> factories = factoriesByType.get(type);
		if (factories == null) {
			factories = new ArrayList<Factory<?>>();
			factoriesByType.put(type, factories);
		}
		else {
			if (factories.get(0).isSingleton() != factory.isSingleton())
				throw new IllegalStateException("Cannot define different scopes for factories of type " + type);
		}
		factories.add(factory);
    }
    
    public void initContext(Context context) {
        for (Factory<?> factory : initializers) {
        	if (factory.isSingleton() == context.isGlobal())
        		factory.create(context);
        }
    }
	
	public Factory<?> forName(String name, boolean singleton) {
		Factory<?> factory = factoriesByName.get(name);
		if (factory == null)
			return null;
		return factory.isSingleton() || !singleton ? factory : null;
	}
	
	public List<Factory<?>> forType(Class<?> type, boolean singleton) {
    	List<Factory<?>> factories = new ArrayList<Factory<?>>();
    	for (Entry<Class<?>, List<Factory<?>>> me : factoriesByType.entrySet()) {
    		if (type.isAssignableFrom(me.getKey())) {
    			for (Factory<?> factory : me.getValue()) {
    				if (factory.isSingleton() || !singleton)
    					factories.add(factory);
    			}
    		}
    	}
    	return factories;
	}
	
	
	private static class MethodFactory<T> implements Factory<T> {
		
		private final Method method;
		private final boolean singleton;
		
		public MethodFactory(Method method) {
			this.method = method;
			this.singleton = method.isAnnotationPresent(Singleton.class);
		}
		
		public boolean isSingleton() {
			return singleton;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T create(Context context) {
			final Class<?>[] parametersTypes = method.getParameterTypes();
			final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
			
			Object[] args = new Object[parametersTypes.length];
			
			for (int i = 0; i < parametersTypes.length; i++) {
				if (parametersTypes[i] == Context.class) {
					args[i] = context;
					continue;
				}
				
				String name = null;
				for (Annotation annotation : parameterAnnotations[i]) {
					if (annotation.annotationType() == Named.class) {
						name = ((Named)annotation).value();
						if ("".equals(name))
							throw new RuntimeException("Can not inject @Named value without explicit name for method " + method.getName() + " argument " + i);
						break;
					}
				}
				if (name != null) {
					args[i] = context.byName(name);
					if (args[i] == null)
						throw new RuntimeException("Cannot find injected value named " + name + " for arg " + i + " of method " + method.toGenericString());
				}
 				else {
					args[i] = context.byType(parametersTypes[i]);
					if (args[i] == null)
						throw new RuntimeException("Cannot find injected value of type " + parametersTypes[i] + " for arg " + i + " of method " + method.toGenericString());
 				}
			}
			
			try {
				return (T)method.invoke(null, args);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not create instance with method " + method.toGenericString(), e);
			}
		}
		
	}
}
