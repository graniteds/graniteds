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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.granite.client.tide.Context;
import org.granite.client.tide.InstanceStore;
import org.granite.client.tide.server.Component;

/**
 * @author William DRAI
 */
public class SimpleInstanceStore implements InstanceStore {
    
	private final Context context;
	private static final String TYPED = "__TYPED__";
    private Map<String, Object> instances = new HashMap<String, Object>();
    
    public SimpleInstanceStore(Context context) {
    	this.context = context;
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
        instances.remove(name);
    }
    
    @Override
    public void clear() {
    	instances.clear();
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

    @SuppressWarnings("unchecked")
    public <T> T byName(String name, Context context) {
        return (T)instances.get(name);
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
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] allByType(Class<T> type, Context context, boolean create) {
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

}
