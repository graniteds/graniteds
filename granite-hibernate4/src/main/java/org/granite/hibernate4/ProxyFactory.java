/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.hibernate4;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;

import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.service.ServiceException;
import org.granite.util.TypeUtil;
import org.granite.util.Introspector;
import org.granite.util.PropertyDescriptor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.CompositeType;

/**
 * @author Franck WOLFF
 */
public class ProxyFactory {

    private static final Class<?>[] INTERFACES = new Class<?>[]{HibernateProxy.class};
    private static final Class<?>[] SINGLE_OBJECT_PARAMS = new Class<?>[]{Object.class};
    private static final Method OBJECT_EQUALS;
    static {
    	try {
    		OBJECT_EQUALS = Object.class.getMethod("equals", SINGLE_OBJECT_PARAMS);
    	}
    	catch (Exception e) {
    		throw new ExceptionInInitializerError(e);
    	}
    }

    protected final ConcurrentHashMap<Class<?>, Object[]> identifierInfos = new ConcurrentHashMap<Class<?>, Object[]>();

    private final Method getProxyFactory;
    private final Method getProxy;
    private final boolean classOverridesEqualsParameter;

    public ProxyFactory(String initializerClassName) {
        try {
            // Get proxy methods: even if CGLIB/Javassist LazyInitializer implementations share a common
        	// superclass, getProxyFactory/getProxy methods are declared as static in each inherited
        	// class with the same signature.
            Class<?> initializerClass = TypeUtil.forName(initializerClassName);
            getProxyFactory = initializerClass.getMethod("getProxyFactory", new Class[] { Class.class, Class[].class });
            
            // Hibernate 4.0.1 has an extra boolean parameter in last position: classOverridesEquals.
            Method getProxy = null;
            boolean classOverridesEqualsParameter = false;
            try {
	            getProxy = initializerClass.getMethod("getProxy", new Class[]{
	                Class.class, String.class, Class.class, Class[].class, Method.class, Method.class,
	                CompositeType.class, Serializable.class, SessionImplementor.class
	            });
            }
            catch (NoSuchMethodException e) {
                getProxy = initializerClass.getMethod("getProxy", new Class[]{
                	Class.class, String.class, Class.class, Class[].class, Method.class, Method.class,
                    CompositeType.class, Serializable.class, SessionImplementor.class, Boolean.TYPE
                });
                classOverridesEqualsParameter = true;
            }
            this.getProxy = getProxy;
            this.classOverridesEqualsParameter = classOverridesEqualsParameter;
        } 
        catch (Exception e) {
            throw new ServiceException("Could not introspect initializer class: " + initializerClassName, e);
        }
    }

    public HibernateProxy getProxyInstance(String persistentClassName, String entityName, Serializable id) {
        try {
            // Get ProxyFactory.
            Class<?> persistentClass = TypeUtil.forName(persistentClassName);
            Class<?> factory = (Class<?>)getProxyFactory.invoke(null, new Object[] { persistentClass, INTERFACES });

            // Convert id (if necessary).
            Object[] identifierInfo = getIdentifierInfo(persistentClass);
            Type identifierType = (Type)identifierInfo[0];
            Method identifierGetter = (Method)identifierInfo[1];
            if (id == null || !identifierType.equals(id.getClass())) {
                GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
                id = (Serializable)config.getConverters().convert(id, identifierType);
            }

            // Get Proxy (with or without the extra parameter classOverridesEquals)
            if (classOverridesEqualsParameter) {
                return (HibernateProxy)getProxy.invoke(null, new Object[]{
                	factory, entityName, persistentClass, INTERFACES, identifierGetter, null, null, id, null, overridesEquals(persistentClass)});
            }
            return (HibernateProxy)getProxy.invoke(null, new Object[] { factory, entityName, persistentClass, INTERFACES, identifierGetter, null, null, id, null });
        } 
        catch (Exception e) {
            throw new ServiceException("Error with proxy description: " + persistentClassName + '/' + entityName + " and id: " + id, e);
        }
    }
    
    protected boolean overridesEquals(Class<?> persistentClass) {
    	try {
    		return !OBJECT_EQUALS.equals(persistentClass.getMethod("equals", SINGLE_OBJECT_PARAMS));
    	}
    	catch (Exception e) {
    		return false; // should never happen unless persistentClass is an interface...
    	}
    }

    protected Object[] getIdentifierInfo(Class<?> persistentClass) {

        Object[] info = identifierInfos.get(persistentClass);
        if (info != null)
            return info;

        Type type = null;
        Method getter = null;
        for (Class<?> clazz = persistentClass; clazz != Object.class && clazz != null; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                    type = field.getGenericType();
                    break;
                }
            }
        }

        if (type == null) {
            PropertyDescriptor[] propertyDescriptors = Introspector.getPropertyDescriptors(persistentClass);

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method method = propertyDescriptor.getReadMethod();
                if (method != null && (
                        method.isAnnotationPresent(Id.class) ||
                        method.isAnnotationPresent(EmbeddedId.class))) {
                    type = method.getGenericReturnType();
                    getter = method;
                    break;
                }
                method = propertyDescriptor.getWriteMethod();
                if (method != null && (
                        method.isAnnotationPresent(Id.class) ||
                        method.isAnnotationPresent(EmbeddedId.class))) {
                    type = method.getGenericParameterTypes()[0];
                    break;
                }
            }
        }

        if (type != null) {
        	info = new Object[] { type, getter };
            Object[] previousInfo = identifierInfos.putIfAbsent(persistentClass, info);
            if (previousInfo != null)
                info = previousInfo; // should be the same...
            return info;
        }

        throw new IllegalArgumentException("Could not find id in: " + persistentClass);
    }
}
