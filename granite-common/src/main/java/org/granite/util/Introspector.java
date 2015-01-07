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
package org.granite.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

/**
 * 	Basic bean introspector
 *  Required for Android environment which does not include java.beans.Intropector
 */
public class Introspector {

    private static Map<Class<?>, PropertyDescriptor[]> descriptorCache = Collections.synchronizedMap(new WeakHashMap<Class<?>, PropertyDescriptor[]>(128));

    /**
     * Decapitalizes a given string according to the rule:
     * <ul>
     * <li>If the first or only character is Upper Case, it is made Lower Case
     * <li>UNLESS the second character is also Upper Case, when the String is
     * returned unchanged <eul>
     * 
     * @param name -
     *            the String to decapitalize
     * @return the decapitalized version of the String
     */
    public static String decapitalize(String name) {

        if (name == null)
            return null;
        // The rule for decapitalize is that:
        // If the first letter of the string is Upper Case, make it lower case
        // UNLESS the second letter of the string is also Upper Case, in which case no
        // changes are made.
        if (name.length() == 0 || (name.length() > 1 && Character.isUpperCase(name.charAt(1)))) {
            return name;
        }
        
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * Flushes all <code>BeanInfo</code> caches.
     *  
     */
    public static void flushCaches() {
        // Flush the cache by throwing away the cache HashMap and creating a
        // new empty one
        descriptorCache.clear();
    }

    /**
     * Flushes the <code>BeanInfo</code> caches of the specified bean class
     * 
     * @param clazz
     *            the specified bean class
     */
    public static void flushFromCaches(Class<?> clazz) {
        if (clazz == null)
            throw new NullPointerException();
        
        descriptorCache.remove(clazz);
    }

    /**
	 * Gets the <code>BeanInfo</code> object which contains the information of
	 * the properties, events and methods of the specified bean class.
	 * 
	 * <p>
	 * The <code>Introspector</code> will cache the <code>BeanInfo</code>
	 * object. Subsequent calls to this method will be answered with the cached
	 * data.
	 * </p>
	 * 
	 * @param beanClass
	 *            the specified bean class.
	 * @return the <code>BeanInfo</code> of the bean class.
	 * @throws IntrospectionException
	 */
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> beanClass) {
        PropertyDescriptor[] descriptor = descriptorCache.get(beanClass);
        if (descriptor == null) {
        	descriptor = new BeanInfo(beanClass).getPropertyDescriptors();
            descriptorCache.put(beanClass, descriptor);
        }
        return descriptor;
    }
    
    
    private static class BeanInfo {

        private Class<?> beanClass;
        private PropertyDescriptor[] properties = null;

        
        public BeanInfo(Class<?> beanClass) {
            this.beanClass = beanClass;

            if (properties == null)
                properties = introspectProperties();
        }

        public PropertyDescriptor[] getPropertyDescriptors() {
            return properties;
        }

        /**
         * Introspects the supplied class and returns a list of the Properties of
         * the class
         * 
         * @return The list of Properties as an array of PropertyDescriptors
         * @throws IntrospectionException
         */
        private PropertyDescriptor[] introspectProperties() {

        	Method[] methods = beanClass.getMethods();
        	List<Method> methodList = new ArrayList<Method>();
        	
        	for (Method method : methods) {
        		if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers()))
        			continue;
        		methodList.add(method);
        	}

            Map<String, Map<String, Object>> propertyMap = new HashMap<String, Map<String, Object>>(methodList.size());

            // Search for methods that either get or set a Property
            for (Method method : methodList) {
                introspectGet(method, propertyMap);
                introspectSet(method, propertyMap);
            }

            // fix possible getter & setter collisions
            fixGetSet(propertyMap);
            
            // Put the properties found into the PropertyDescriptor array
            List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();

            for (Map.Entry<String, Map<String, Object>> entry : propertyMap.entrySet()) {
                String propertyName = entry.getKey();
                Map<String, Object> table = entry.getValue();
                if (table == null)
                    continue;
                
                Method getter = (Method)table.get("getter");
                Method setter = (Method)table.get("setter");

                PropertyDescriptor propertyDesc = new PropertyDescriptor(propertyName, getter, setter);
                propertyList.add(propertyDesc);
            }

            PropertyDescriptor[] properties = new PropertyDescriptor[propertyList.size()];
            propertyList.toArray(properties);
            return properties;
        }

        @SuppressWarnings("unchecked")
        private static void introspectGet(Method method, Map<String, Map<String, Object>> propertyMap) {
            String methodName = method.getName();
            
            if (!(method.getName().startsWith("get") || method.getName().startsWith("is")))
            	return;
            
            if (method.getParameterTypes().length > 0 || method.getReturnType() == void.class)
            	return;
            
            if (method.getName().startsWith("is") && method.getReturnType() != boolean.class)
            	return;

            String propertyName = method.getName().startsWith("get") ? methodName.substring(3) : methodName.substring(2);
            propertyName = decapitalize(propertyName);

            Map<String, Object> table = propertyMap.get(propertyName);
            if (table == null) {
                table = new HashMap<String, Object>();
                propertyMap.put(propertyName, table);
            }

            List<Method> getters = (List<Method>)table.get("getters");
            if (getters == null) {
                getters = new ArrayList<Method>();
                table.put("getters", getters);
            }
            getters.add(method);
        }

        @SuppressWarnings("unchecked")
        private static void introspectSet(Method method, Map<String, Map<String, Object>> propertyMap) {
            String methodName = method.getName();
            
            if (!method.getName().startsWith("set"))
            	return;
            
            if (method.getParameterTypes().length != 1 || method.getReturnType() != void.class)
            	return;

            String propertyName = decapitalize(methodName.substring(3));

            Map<String, Object> table = propertyMap.get(propertyName);
            if (table == null) {
                table = new HashMap<String, Object>();
                propertyMap.put(propertyName, table);
            }

            List<Method> setters = (List<Method>)table.get("setters");
            if (setters == null) {
                setters = new ArrayList<Method>();
                table.put("setters", setters);
            }

            // add new setter
            setters.add(method);
        }

        /**
         * Checks and fixs all cases when several incompatible checkers / getters
         * were specified for single property.
         * 
         * @param propertyTable
         * @throws IntrospectionException
         */
        private void fixGetSet(Map<String, Map<String, Object>> propertyMap) {
            if (propertyMap == null)
                return;

            for (Entry<String, Map<String, Object>> entry : propertyMap.entrySet()) {
                Map<String, Object> table = entry.getValue();
                @SuppressWarnings("unchecked")
				List<Method> getters = (List<Method>)table.get("getters");
                @SuppressWarnings("unchecked")
				List<Method> setters = (List<Method>)table.get("setters");
                if (getters == null)
                    getters = new ArrayList<Method>();
                if (setters == null)
                    setters = new ArrayList<Method>();

                Method definedGetter = getters.isEmpty() ? null : getters.get(0);
                Method definedSetter = null;

                if (definedGetter != null) {
    	            Class<?> propertyType = definedGetter.getReturnType();
    	
    	            for (Method setter : setters) {
    	                if (setter.getParameterTypes().length == 1 && propertyType.equals(setter.getParameterTypes()[0])) {
    	                    definedSetter = setter;
    	                    break;
    	                }
    	            }
    	            if (definedSetter != null && !setters.isEmpty())
    	            	definedSetter = setters.get(0);
                } 
                else if (!setters.isEmpty()) {
                	definedSetter = setters.get(0);
                }

                table.put("getter", definedGetter);
                table.put("setter", definedSetter);
            }
        }
    }
}


