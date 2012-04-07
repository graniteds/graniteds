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

package org.granite.messaging.service;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.util.StringUtil;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 * @author Pedro GONCALVES
 */
public class DefaultMethodMatcher implements MethodMatcher {
    
    private static final Logger log = Logger.getLogger(DefaultMethodMatcher.class);

    public ServiceInvocationContext findServiceMethod(
        Message message,
        Destination destination,
        Object service,
        String methodName,
        Object[] params) throws NoSuchMethodException {

        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        Converters converters = config.getConverters();

        Class<?> serviceClass = service.getClass();
        Type serviceClassGenericType = getGenericType(serviceClass);

        Converter[] convertersArray = null;
        Method serviceMethod = null;
        if (params == null || params.length == 0)
            serviceMethod = serviceClass.getMethod(methodName, (Class[])null);
        else {
            for (Method method : serviceClass.getMethods()) {

                if (!methodName.equals(method.getName()))
                    continue;

                Type[] paramTypes = method.getGenericParameterTypes();
                if (paramTypes.length != params.length)
                    continue;
                
                if (serviceClassGenericType != null)
                    findAndChange(paramTypes, serviceClassGenericType);

                convertersArray = getConvertersArray(converters, params, paramTypes);
                if (convertersArray != null) {
                    serviceMethod = method;
                    break;
                }
            }
        }

        if (serviceMethod == null)
            throw new NoSuchMethodException(serviceClass.getName() + '.' + methodName + StringUtil.toString(params));

        params = convert(convertersArray, params, serviceMethod.getGenericParameterTypes());

        return new ServiceInvocationContext(message, destination, service, serviceMethod, params);
    }

    protected Converter[] getConvertersArray(Converters converters, Object[] values, Type[] targetTypes) {
        Converter[] convertersArray = new Converter[values.length];
        for (int i = 0; i < values.length; i++) {
            convertersArray[i] = converters.getConverter(values[i], targetTypes[i]);
            if (convertersArray[i] == null)
                return null;
        }
        return convertersArray;
    }

    protected Object[] convert(Converter[] convertersArray, Object[] values, Type[] targetTypes) {
        if (values.length > 0) {
            for (int i = 0; i < convertersArray.length; i++)
                values[i] = convertersArray[i].convert(values[i], targetTypes[i]);
        }
        return values;
    }
    
    protected Method resolveMatchingMethod(List<Method> methods) {
        Method method = null;
        // Prefer methods of interfaces/classes marked with @RemoteDestination
        for (Method m : methods) {
            if (m.getDeclaringClass().isAnnotationPresent(RemoteDestination.class)) {
                method = m;
                break;
            }
        }
        if (method != null)
            return method;
        
        log.warn("Ambiguous method match for " + methods.get(0).getName() + ", selecting first found " + methods.get(0));        
        return methods.get(0);
    }

    /**
     * If there is only one TypeVariable in method's argument list, it will be replaced
     * by the type of the superclass of the service.
     */
    protected boolean findAndChange(Type[] paramTypes, Type superType) {
        int idx = -1;
        boolean find = false;
        for (int j = 0; j < paramTypes.length; j++) {
            Type type = paramTypes[j];
           
            if (type instanceof TypeVariable<?>) {
                if (!find) {
                    idx = j;
                    find = true;
                } else {
                    throw new RuntimeException("There's two variable types.");
                }
            }
        }
       
        if (find)
            paramTypes[idx] = superType;
       
        return find;
    }
   
    /**
     * Returns actual type argument of a given class.
     */
    protected Type getGenericType(Class<?> clazz) {
        try {
            ParameterizedType genericSuperclass = (ParameterizedType)clazz.getGenericSuperclass();
            Type[] actualTypeArguments = genericSuperclass.getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length == 1)
                return actualTypeArguments[0];
        } catch (Exception e) {
        	// fallback...
        }
        return null;
    }
}
