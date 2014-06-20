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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.granite.client.tide.Context;

/**
 * @author William DRAI
 */
public class SimpleInjectInstanceStore extends SimpleInstanceStore {
    
    public SimpleInjectInstanceStore(Context context, InstanceFactory instanceFactory) {
    	super(context, instanceFactory);
    }
    
    
    @Override
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
