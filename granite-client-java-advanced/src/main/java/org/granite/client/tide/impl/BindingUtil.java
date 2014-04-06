/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.granite.client.util.WeakIdentityHashMap;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class BindingUtil {
	
    private static final Logger log = Logger.getLogger(BindingUtil.class);
    
	private static final ConcurrentHashMap<Class<?>, Method[]> changeListenerMethodsMap = new ConcurrentHashMap<Class<?>, Method[]>();
	private static final WeakIdentityHashMap<Object, PropertyChangeSupport> pcsMap = new WeakIdentityHashMap<Object, PropertyChangeSupport>();
	
    private static Method[] getPropertyChangeListeners(Class<?> clazz) throws NoSuchMethodException {
    	Method[] m = changeListenerMethodsMap.get(clazz);
    	if (m == null) {
    		try {
	    		Method add1 = clazz.getMethod("addPropertyChangeListener", PropertyChangeListener.class);
	    		Method rem1 = clazz.getMethod("removePropertyChangeListener", PropertyChangeListener.class);
	    		try {
		    		Method add2 = clazz.getMethod("addPropertyChangeListener", String.class, PropertyChangeListener.class);
		    		Method rem2 = clazz.getMethod("removePropertyChangeListener", String.class, PropertyChangeListener.class);
		    		m = new Method[] { add1, rem1, add2, rem2 };
	    		}
	    		catch (NoSuchMethodException e) {
	    			m = new Method[] { add1, rem1 };
	    		}
    		}
    		catch (NoSuchMethodException e) {
    			m = new Method[0];
    		}
    		changeListenerMethodsMap.put(clazz, m);
    	}
    	return m;
    }
    
    private static PropertyChangeSupport getProperyChangeSupport(Object bean) {
    	PropertyChangeSupport pcs = pcsMap.get(bean);
    	if (pcs == null) {
    		pcs = new PropertyChangeSupport(bean);
    		pcsMap.put(bean, pcs);
    	}
    	return pcs;
    }
	
    public static void addPropertyChangeListener(Object bean, PropertyChangeListener listener) {
    	try {
    		Method[] m = getPropertyChangeListeners(bean.getClass());
    		if (m.length > 0)
    			m[0].invoke(bean, listener);
    		else
    			getProperyChangeSupport(bean).addPropertyChangeListener(listener);
    	}
    	catch (Exception e) {
    		log.debug("Could not add property change listener on %s", bean);
    	}
    }
	
    public static void addPropertyChangeListener(Object bean, String propertyName, PropertyChangeListener listener) {
    	try {
    		Method[] m = getPropertyChangeListeners(bean.getClass());
    		if (m.length > 2)
    			m[2].invoke(bean, listener);
    		else
    			getProperyChangeSupport(bean).addPropertyChangeListener(propertyName, listener);
    	}
    	catch (Exception e) {
    		log.debug("Could not add property change listener %s on %s", propertyName, bean);
    	}
    }
	
    public static void removePropertyChangeListener(Object bean, PropertyChangeListener listener) {
    	try {
    		Method[] m = getPropertyChangeListeners(bean.getClass());
    		if (m.length > 0)
    			m[1].invoke(bean, listener);
    		else
    			getProperyChangeSupport(bean).removePropertyChangeListener(listener);
    	}
    	catch (Exception e) {
    		log.debug("Could not remove property change listener on %s", bean);
    	}
    }
	
    public static void removePropertyChangeListener(Object bean, String propertyName, PropertyChangeListener listener) {
    	try {
    		Method[] m = getPropertyChangeListeners(bean.getClass());
    		if (m.length > 2)
    			m[3].invoke(bean, listener);
    		else
    			getProperyChangeSupport(bean).removePropertyChangeListener(propertyName, listener);
    	}
    	catch (Exception e) {
    		log.debug("Could not remove property change listener %s on %s", propertyName, bean);
    	}
    }
}
