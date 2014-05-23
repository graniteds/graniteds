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
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.granite.client.tide.Context;
import org.granite.client.tide.Factory;
import org.granite.client.tide.InstanceStore;
import org.granite.client.tide.server.Component;

/**
 * @author William DRAI
 */
public class SimpleInstanceStore implements InstanceStore {
    
	private final Context context;
    private final InstanceFactory instanceFactory;
	private static final String TYPED = "__TYPED__";
    private Map<String, Object> instances = new LinkedHashMap<String, Object>();
    private Set<Factory<?>> appliedFactories = new HashSet<Factory<?>>();
    
    
    public SimpleInstanceStore(Context context, InstanceFactory instanceFactory) {
    	this.context = context;
    	this.instanceFactory = instanceFactory;
    }
    
    public <T> T set(String name, T instance) {
    	context.initInstance(instance, name);
        instances.put(name, instance);
        return instance;
    }
    
    private int NUM_TYPED_INSTANCE = 1;
    
    public <T> T set(T instance) {
    	if (instance == null)
    		throw new NullPointerException("Cannot register null component instance");
    	
    	context.initInstance(instance, null);
    	if (!instances.containsValue(instance))
    		instances.put(TYPED + (NUM_TYPED_INSTANCE++), instance);
    	return instance;
    }

    @Override
    public void remove(String name) {
    	context.destroyInstance(instances.remove(name));
    }
	
	@Override
	public void remove(Object instance) {
		for (Iterator<Entry<String, Object>> ie = instances.entrySet().iterator(); ie.hasNext(); ) {
			Entry<String, Object> e = ie.next();
			if (e.getValue() == instance) {
				ie.remove();
				break;
			}
		}
		context.destroyInstance(instance);
	}
    
    @Override
    public void clear() {
    	for (Object instance : instances.values())
    		context.destroyInstance(instance);
    	instances.clear();
    	appliedFactories.clear();
	}
    
    public List<String> allNames() {
    	List<String> names = new ArrayList<String>(instances.size());
    	for (String name : instances.keySet()) {
    		if (!name.startsWith(TYPED))
    			names.add(name);
    	}
    	return names;
    }

    @SuppressWarnings("unchecked")
    public <T> T getNoProxy(String name, Context context) {
        Object instance = instances.get(name);
        if (instance instanceof Component)
            return null;
        return (T)instance;
    }
    
    public boolean exists(String name) {
    	return instances.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T byName(String name, Context context) {
    	T instance = (T)instances.get(name);
    	if (instance == null) {
    		Factory<?> factory = instanceFactory.forName(name, context.isGlobal());
    		if (factory != null) {
    			if (factory.isSingleton() && !context.isGlobal())
    				return context.getContextManager().getContext().byName(name);  
    			
    			instance = (T)factory.create(context);
    	    	context.initInstance(instance, name);
    			instances.put(name, instance);
    		}
    	}
        return instance;
    }
    
    protected Object createInstance() {
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T byType(Class<T> type, Context context) {
        T instance = null;
        for (Object i : instances.values()) {
            if (type.isInstance(i)) {
                if (instance == null)
                    instance = (T)i;
                else
                    throw new RuntimeException("Ambiguous component definition for class " + type);
            }
        }
        if (instance == null) {
        	List<Factory<?>> factories = instanceFactory.forType(type, context.isGlobal());        	
    		if (factories.size() > 1)
                throw new RuntimeException("Ambiguous component definition for class " + type);
    		else if (!factories.isEmpty()) {
    			if (factories.get(0).isSingleton() && !context.isGlobal())
    				return context.getContextManager().getContext().byType(type);     			
    			
    			if (appliedFactories.contains(factories.get(0)))
    				throw new IllegalStateException("Instance for type " + type + " already created by factory but not found");
    			instance = (T)factories.get(0).create(context);
    	    	if (!instances.containsValue(instance)) {
        	    	context.initInstance(instance, null);
    	    		String name = TYPED + (NUM_TYPED_INSTANCE++);
    	    		instances.put(name, instance);
    	    		appliedFactories.add(factories.get(0));
    	    	}
    		}
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] allByType(Class<T> type, Context context, boolean create) {
    	List<Factory<?>> factories = instanceFactory.forType(type, context.isGlobal());
    	if (!factories.isEmpty() && !context.isGlobal() && factories.get(0).isSingleton())
    		return context.getContextManager().getContext().allByType(type, create);
    	
    	if (create) {
    		for (Factory<?> factory : factories) {
    			if (appliedFactories.contains(factory))
    				continue;
	    		String name = TYPED + (NUM_TYPED_INSTANCE++);
    			Object instance = factory.create(context);
    			context.initInstance(instance, null);
    			instances.put(name, instance);
    			appliedFactories.add(factory);
    		}
    	}
    	
        List<T> list = new ArrayList<T>();
        for (Object instance : instances.values()) {
            if (type.isInstance(instance))
                list.add((T)instance);
        }
        T[] all = (T[])Array.newInstance(type, list.size());
        return list.size() > 0 ? list.toArray(all) : null;
    }

    @Override
	public Map<String, Object> allByAnnotatedWith(Class<? extends Annotation> annotationClass, Context context) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Entry<String, Object> entry : instances.entrySet()) {
            if (entry.getValue().getClass().isAnnotationPresent(annotationClass))
                map.put(entry.getKey(), entry.getValue());
        }
        return map.isEmpty() ? null : map;
    }
    
    
    public void inject(Object target, String componentName, Map<String, Object> properties) {
    	if (target == null)
    		throw new IllegalArgumentException("Cannot inject null object");
    	
    	Class<?> c = target.getClass();
    	while (c != null && c != Object.class) {
    		for (Field f : c.getDeclaredFields()) {
    			if (f.isAnnotationPresent(Inject.class)) {
    				f.setAccessible(true);
    				if (f.isAnnotationPresent(Named.class)) {
    					String name = f.getAnnotation(Named.class).value();
    					if ("".equals(name))
    						name = f.getName();
    					try {
    						Object value = context.byName(name);
    						if (value != null)
    							f.set(target, value);
						} 
    					catch (Exception e) {
    						throw new RuntimeException("Cannot inject field " + f.getName(), e);
						}
    				}
    				else {
    					try {
	    					if (f.getType().isArray()) {
	    						Object values = context.allByType(f.getType().getComponentType());
	    						if (values != null)
	    							f.set(target, values);
	    					}
	    					else {
	    						Object value = context.byType(f.getType());
	    						if (value != null)
	    							f.set(target, value);
	    					}
						} 
						catch (Exception e) {
							throw new RuntimeException("Cannot inject field " + f.getName(), e);
						}
    				}
    			}
    		}
    		c = c.getSuperclass();
    	}
    	
    	if (componentName == null)
    		return;
        
    	for (String key : properties.keySet()) {
    		int idx = key.indexOf(".");
    		if (idx < 0)
    			continue;
    		
    		if (!componentName.equals(key.substring(0, idx)))
    			continue;
    		
    		String propertyName = key.substring(idx+1);
    		Object value = properties.get(key);
    		
    		String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    		Method setter = null;
    		for (Method s : target.getClass().getMethods()) {
    			if (s.getName().equals(setterName)) {
    				setter = s;
    				break;
    			}
    		}
    		if (setter == null)
    			throw new RuntimeException("No setter found for " + key);
    		
    		try {
    			setter.invoke(target, value);
    		}
    		catch (Exception e) {
    			throw new RuntimeException("Could not set value for bundle property " + key);
    		}
        }
    }

}
