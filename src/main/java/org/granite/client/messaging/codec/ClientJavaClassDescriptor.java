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
package org.granite.client.messaging.codec;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.granite.messaging.amf.io.util.DefaultActionScriptClassDescriptor;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class ClientJavaClassDescriptor extends DefaultActionScriptClassDescriptor {

	private static final SunConstructorFactory factory = new SunConstructorFactory();
    private static final ConcurrentHashMap<String, Constructor<?>> constructors =
        new ConcurrentHashMap<String, Constructor<?>>();
    
    
	public ClientJavaClassDescriptor(String type, byte encoding) {
		super(type, encoding);
	}
	
	@Override
	public Object newJavaInstance() {
		try {
			return super.newJavaInstance();
		}
		catch (RuntimeException e) {
			if (e.getCause() instanceof InstantiationException)
				return findDefaultConstructor();
			throw e;
		}
	}
	
	private Object findDefaultConstructor() {
		try {
			Constructor<?> defaultContructor = constructors.get(type);
			if (defaultContructor == null) {
				defaultContructor = factory.findDefaultConstructor(TypeUtil.forName(type));
	            Constructor<?> previousConstructor = constructors.putIfAbsent(type, defaultContructor);
	            if (previousConstructor != null)
	            	defaultContructor = previousConstructor; // Should be the same instance, anyway...
			}
			return defaultContructor.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not create Proxy for: " + type);
		}
	}
}
class SunConstructorFactory {

    private final Object reflectionFactory;
    private final Method newConstructorForSerialization;

    public SunConstructorFactory() {
        try {
            Class<?> factoryClass = TypeUtil.forName("sun.reflect.ReflectionFactory");
            Method getReflectionFactory = factoryClass.getDeclaredMethod("getReflectionFactory");
            reflectionFactory = getReflectionFactory.invoke(null);
            newConstructorForSerialization = factoryClass.getDeclaredMethod(
                "newConstructorForSerialization",
                new Class[]{Class.class, Constructor.class}
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not create Sun Factory", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Constructor<T> findDefaultConstructor(Class<T> clazz) {
        try {
            Constructor<?> constructor = Object.class.getDeclaredConstructor();
            constructor = (Constructor<?>)newConstructorForSerialization.invoke(
                reflectionFactory,
                new Object[]{clazz, constructor}
            );
            constructor.setAccessible(true);
            return (Constructor<T>)constructor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}