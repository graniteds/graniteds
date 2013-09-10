/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.granite.util;

import java.lang.reflect.Method;


public class PropertyDescriptor {
	
	private String name;
    private Method getter;
    private Method setter;

    public PropertyDescriptor(String propertyName, Method getter, Method setter) {
        this.name = propertyName;
        setReadMethod(getter);
        setWriteMethod(setter);
    }

    public PropertyDescriptor(String propertyName, Class<?> beanClass) {
        this.name = propertyName;
        try {
            setReadMethod(beanClass, createDefaultMethodName(propertyName, "is"));
        } 
        catch (Exception e) {
            setReadMethod(beanClass, createDefaultMethodName(propertyName, "get"));
        }

        setWriteMethod(beanClass, createDefaultMethodName(propertyName, "set"));
    }
    
    public String getName() {
    	return this.name;
    }

    public void setWriteMethod(Method setter) {
        this.setter = setter;
    }

    public void setReadMethod(Method getter) {
        this.getter = getter;
    }

    public Method getWriteMethod() {
        return setter;
    }

    public Method getReadMethod() {
        return getter;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PropertyDescriptor))
        	return false;

        PropertyDescriptor pd = (PropertyDescriptor)object;
        if (!((this.getter == null && pd.getter == null) 
        		|| (this.getter != null && this.getter.equals(pd.getter))))
        	return false;
        
        if (!((this.setter == null && pd.setter == null) 
        		|| (this.setter != null && this.setter.equals(pd.setter))))
        	return false;
        
        return this.getPropertyType() == pd.getPropertyType();
    }

    @Override
    public int hashCode() {
    	int hashCode = getter != null ? getter.hashCode() : 0;
    	if (setter != null)
    		hashCode = hashCode*31 + setter.hashCode();
    	if (getPropertyType() != null)
    		hashCode = hashCode*31 + getPropertyType().hashCode();
    	return hashCode;
    }

    public Class<?> getPropertyType() {
        if (getter != null)
            return getter.getReturnType();
        if (setter != null) {
            Class<?>[] parameterTypes = setter.getParameterTypes();
            return parameterTypes[0];
        }
        return null;
    }

    private String createDefaultMethodName(String propertyName, String prefix) {
        return prefix + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    private void setReadMethod(Class<?> beanClass, String getterName) {
        try {
            Method readMethod = beanClass.getMethod(getterName, new Class[] {});
            setReadMethod(readMethod);
        } 
        catch (Exception e) {
            throw new RuntimeException("Introspection exception", e);
        }
    }

    private void setWriteMethod(Class<?> beanClass, String setterName) {
        Method writeMethod = null;
        try {
            if (getter != null) {
                writeMethod = beanClass.getMethod(setterName, new Class[] { getter.getReturnType() });
            } 
            else {
                Class<?> clazz = beanClass;
                Method[] methods = null;
                while (clazz != null && writeMethod == null) {
                    methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (setterName.equals(method.getName())) {
                            if (method.getParameterTypes().length == 1) {
                                writeMethod = method;
                                break;
                            }
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            }
        } 
        catch (Exception e) {
            throw new RuntimeException("Introspection exception", e);
        }
        if (writeMethod == null)
            throw new RuntimeException("Could not find setter for property " + name);
        
        setWriteMethod(writeMethod);
    }
}
