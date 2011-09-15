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

package org.granite.generator.as3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.granite.generator.as3.As3Type;
import org.granite.messaging.service.annotations.Param;
import org.granite.tide.data.Lazy;

/**
 * @author Franck WOLFF
 */
public class JavaMethod extends JavaMember<Method> {

    public enum MethodType {
        GETTER,
        SETTER,
        OTHER
    }

    private final String name;
    private final boolean override;
    private final MethodType type;
    private final String options;
    private final Class<?>[] parameterTypes;
    private final As3Type[] as3ParameterTypes;
    private final String[] as3ParameterNames;
    private final String[] as3ParameterOptions;
    
    public JavaMethod(Method method, MethodType type) {
    	this(method, type, null);
    }
    
    public JavaMethod(Method method, MethodType type, JavaTypeFactory provider) {
        super(method);

        Class<?> objectClass = Object.class;
        try {
        	objectClass = method.getDeclaringClass().getClassLoader().loadClass(Object.class.getCanonicalName());
        }
        catch (Exception e) {
        }
        
        this.name = method.getName();

        // This part figure out if an ActionScript3 accessor should be marked as override.
        Class<?> superclass = method.getDeclaringClass().getSuperclass();
        boolean override = false;
        if (superclass != null && superclass != objectClass) {
            try {
                Method superMethod = superclass.getMethod(method.getName(), method.getParameterTypes());

                // if the super method is declared by an interface, check if we have a superclass that
                // implements this interface.
                if (superMethod.getDeclaringClass().isInterface())
                	override = superMethod.getDeclaringClass().isAssignableFrom(superclass);
                // if the super method is declared by a class, check if its declaring class implements,
                // directly or not, an interface with a method with the same signature.
                else {
                	for (Class<?> sc = superMethod.getDeclaringClass(); sc != null; sc = sc.getSuperclass()) {
	                	for (Class<?> interfaze : sc.getInterfaces()) {
	        				try {
	        					interfaze.getMethod(method.getName(), method.getParameterTypes());
	        					override = true;
	        					break;
	        				}
	        				catch (NoSuchMethodException e) {
	        					// continue...
	        				}
	        			}
	                	if (override)
	                		break;
                	}
                }
            } catch (NoSuchMethodException e) {
                // continue...
            }
        }
        this.override = override;

        this.type = type;
        
        if (method.isAnnotationPresent(Lazy.class))
        	this.options = "Lazy";
        else
        	this.options = null;
        
		if (type == MethodType.OTHER && provider != null) {
			this.parameterTypes = method.getParameterTypes();
			this.as3ParameterTypes = new As3Type[this.parameterTypes.length];
			this.as3ParameterNames = new String[this.parameterTypes.length];
			this.as3ParameterOptions = new String[this.parameterTypes.length];
			for (int i = 0; i < this.parameterTypes.length; i++) {
				as3ParameterNames[i] = getParamName(method, i);
				if (Map.class.isAssignableFrom(parameterTypes[i]))
					as3ParameterTypes[i] = As3Type.OBJECT;
				else
					as3ParameterTypes[i] = provider.getAs3Type(this.parameterTypes[i]);
				
				Annotation[] annotations = method.getParameterAnnotations()[i];
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().equals(Lazy.class)) {
						as3ParameterOptions[i] = "Lazy";
						break;
					}
				}				
			}
		} 
		else {
			this.parameterTypes = null;
			this.as3ParameterTypes = null;
			this.as3ParameterNames = null;
			this.as3ParameterOptions = null;
		}
    }
    
    private String getParamName(Method method, int paramIndex) {
    	Annotation[][] annotations = method.getParameterAnnotations();
    	if (annotations != null && annotations.length > paramIndex && annotations[paramIndex] != null) {
    		for (Annotation annotation : annotations[paramIndex]) {
    			if (annotation.annotationType().equals(Param.class))
    				return ((Param)annotation).value();
    		}
    	}
    	return "arg" + paramIndex;
    }

    public boolean isOverride() {
        return override;
    }

    public MethodType getType() {
        return type;
    }
    
    public String getOptions() {
    	return options;
    }

    public String getTypeName() {
        return type.name();
    }
    
    @Override
	public String getName() {
        return name;
    }
   
    public Class<?>[] getParameterTypes() {
    	return parameterTypes;
    }

	public As3Type[] getAs3ParameterTypes() {
		return as3ParameterTypes;
	}

	public String[] getAs3ParameterNames() {
		return as3ParameterNames;
	}

	public String[] getAs3ParameterOptions() {
		return as3ParameterOptions;
	}
}
