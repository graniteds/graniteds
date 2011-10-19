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
*/

package org.granite.tide.cdi;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;


/**
 * @author William DRAI
 */
@ApplicationScoped
public class TideInstrumentedBeans implements Serializable {

    private static final long serialVersionUID = 1L;
    
    
    private Map<Type, Bean<?>> beans = null;
    private Map<Object, Type> producedBeans = null;
    
    public void setBeans(Map<Type, Bean<?>> beans) {
    	this.beans = beans;
    }
    public Bean<?> getBean(Type type) {
    	Bean<?> bean = beans.get(type);
    	if (bean != null)
    		return bean;
    	if (type instanceof Class<?>) {
    		Class<?> clazz = (Class<?>)type;
    		if (clazz.getSuperclass() != null)
    			bean = beans.get(clazz.getSuperclass());
    		if (bean != null)
    			return bean;
    		for (Class<?> i : clazz.getInterfaces()) {
    			bean = beans.get(i);
    			if (bean != null)
    				return bean;
    		}
    	}
    	return null;
    }
    
    
    public void setProducedBeans(Map<Object, Type> producedBeans) {
    	this.producedBeans = new HashMap<Object, Type>();
    	for (Entry<Object, Type> me : producedBeans.entrySet()) {
    		if (beans.containsKey(me.getValue()))
    			this.producedBeans.put(me.getKey(), me.getValue());
    	}
    }
    public boolean isProducedBy(Object name, Type clazz) {
    	Class<?> beanClass = ((Class<?>)producedBeans.get(name)); 
    	return beanClass != null ? beanClass.isAssignableFrom((Class<?>)clazz) : false;
    }
}
