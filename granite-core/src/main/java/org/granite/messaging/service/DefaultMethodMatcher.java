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

package org.granite.messaging.service;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.service.annotations.IgnoredMethod;
import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.util.StringUtil;
import org.granite.util.TypeUtil;

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
        ParameterizedType[] serviceDeclaringTypes = TypeUtil.getDeclaringTypes(serviceClass);

        MatchingMethod match = null;
        if (params == null || params.length == 0)
            match = new MatchingMethod(serviceClass.getMethod(methodName, (Class[])null), null);
        else {
            List<MatchingMethod> matchingMethods = new ArrayList<MatchingMethod>();
            
            List<Method> methods = new ArrayList<Method>();
            for (Class<?> serviceInterface : serviceClass.getInterfaces())
                methods.addAll(Arrays.asList(serviceInterface.getMethods()));
            
            methods.addAll(Arrays.asList(serviceClass.getMethods()));
            
            for (Method method : methods) {

                if (!methodName.equals(method.getName()))
                    continue;

                Type[] paramTypes = method.getGenericParameterTypes();
                if (paramTypes.length != params.length)
                    continue;
                
                // Methods marked with @IgnoredMethod cannot be called remotely
                if (method.isAnnotationPresent(IgnoredMethod.class))
                	continue;
                
                findAndChange(paramTypes, method.getDeclaringClass(), serviceDeclaringTypes);

                Converter[] convertersArray = getConvertersArray(converters, params, paramTypes);
                if (convertersArray != null)
                    matchingMethods.add(new MatchingMethod(method, convertersArray));
            }
            
            if (matchingMethods.size() == 1)
            	match = matchingMethods.get(0);
            else if (matchingMethods.size() > 1) {
                // Multiple matches found
                match = resolveMatchingMethod(matchingMethods, serviceClass);
            }
        }

        if (match == null)
            throw new NoSuchMethodException(serviceClass.getName() + '.' + methodName + StringUtil.toString(params));

        params = convert(match.convertersArray, params, match.serviceMethod.getGenericParameterTypes());

        return new ServiceInvocationContext(message, destination, service, match.serviceMethod, params);
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
    
    protected MatchingMethod resolveMatchingMethod(List<MatchingMethod> methods, Class<?> serviceClass) {

        // Prefer methods of interfaces/classes marked with @RemoteDestination
        for (MatchingMethod m : methods) {
            if (m.serviceMethod.getDeclaringClass().isAnnotationPresent(RemoteDestination.class))
                return m;
        }
        
        // Then prefer method declared by the serviceClass (with EJBs, we have always 2 methods, one in the interface,
        // the other in the proxy, and the @RemoteDestination annotation cannot be find on the proxy class even
        // it is present on the original class).
        List<MatchingMethod> serviceClassMethods = new ArrayList<MatchingMethod>();
        for (MatchingMethod m : methods) {
            if (m.serviceMethod.getDeclaringClass().equals(serviceClass))
            	serviceClassMethods.add(m);
        }
        if (serviceClassMethods.size() == 1)
        	return serviceClassMethods.get(0);
        
        log.warn("Ambiguous method match for " + methods.get(0).serviceMethod.getName() + ", selecting first found " + methods.get(0).serviceMethod);        
        return methods.get(0);
    }
    
    /**
     * If there is only one TypeVariable in method's argument list, it will be replaced
     * by the type of the superclass of the service.
     */
    protected void findAndChange(Type[] paramTypes, Class<?> declaringClass, ParameterizedType[] declaringTypes) {
        for (int j = 0; j < paramTypes.length; j++)
            paramTypes[j] = TypeUtil.resolveTypeVariable(paramTypes[j], declaringClass, declaringTypes);
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
    
    private static class MatchingMethod {
    	
    	public final Method serviceMethod;
    	public final Converter[] convertersArray;

    	public MatchingMethod(Method serviceMethod, Converter[] convertersArray) {
			this.serviceMethod = serviceMethod;
			this.convertersArray = convertersArray;
		}

		@Override
		public String toString() {
			return "MatchingMethod {serviceMethod=" + serviceMethod + ", convertersArray=" + (convertersArray != null ? Arrays.toString(convertersArray) : "[]") + "}";
		}
    }
}
