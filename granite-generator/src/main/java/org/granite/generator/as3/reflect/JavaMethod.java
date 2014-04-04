/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

package org.granite.generator.as3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.PropertyType;
import org.granite.generator.util.GenericTypeUtil;
import org.granite.messaging.service.annotations.Param;
import org.granite.tide.data.Lazy;
import org.granite.util.ClassUtil;

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
    private final Class<?> returnType;
    private final Class<?>[] parameterTypes;
    private final ClientType clientReturnType;
    private final ClientType[] clientParameterTypes;
    private final String[] clientParameterNames;
    private final String[] clientParameterOptions;
    private final Set<ClientType> clientAnnotationTypes = new HashSet<ClientType>();
    
    public JavaMethod(Method method, MethodType type) {
    	this(method, type, null, null);
    }
    
    public JavaMethod(Method method, MethodType type, JavaTypeFactory provider) {
    	this(method, type, provider, null);
    }
    
    public JavaMethod(Method method, MethodType type, JavaTypeFactory provider, ParameterizedType[] declaringTypes) {
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
        
        if (method.isAnnotationPresent(Lazy.class)) {
        	this.options = "Lazy";
        	this.clientAnnotationTypes.add(provider.getClientType(Lazy.class, null, null, null));
        }
        else
        	this.options = null;
        
		if (type == MethodType.OTHER && provider != null) {
			if (method.getReturnType() == void.class) {
				this.returnType = Void.class;
				this.clientReturnType = provider.getClientType(Void.class, null, null, PropertyType.SIMPLE);
			}
			else {
				Type genericType = GenericTypeUtil.resolveTypeVariable(method.getGenericReturnType(), method.getDeclaringClass(), declaringTypes);
				genericType = GenericTypeUtil.primitiveToWrapperType(genericType);
		    	this.returnType = ClassUtil.classOfType(genericType);
		    	
				ClientType returnType = provider.getClientType(genericType, method.getDeclaringClass(), declaringTypes, PropertyType.SIMPLE);
				if (returnType == null)
					returnType = provider.getAs3Type(this.returnType);
				clientReturnType = returnType;
			}

			this.parameterTypes = method.getParameterTypes();			
			this.clientParameterTypes = new ClientType[this.parameterTypes.length];
			this.clientParameterNames = new String[this.parameterTypes.length];
			this.clientParameterOptions = new String[this.parameterTypes.length];
			for (int i = 0; i < this.parameterTypes.length; i++) {
				clientParameterNames[i] = getParamName(method, i);
				if (Map.class.isAssignableFrom(parameterTypes[i]))
					clientParameterTypes[i] = provider.getClientType(Object.class, null, null, PropertyType.SIMPLE);
				else {
					Type genericType = GenericTypeUtil.resolveTypeVariable(method.getGenericParameterTypes()[i], method.getDeclaringClass(), declaringTypes);
			    	parameterTypes[i] = ClassUtil.classOfType(genericType);
			    	
					ClientType paramType = provider.getClientType(genericType, method.getDeclaringClass(), declaringTypes, PropertyType.SIMPLE);
					if (paramType == null)
						paramType = provider.getAs3Type(parameterTypes[i]);
					clientParameterTypes[i] = paramType;
				}
				
				Annotation[] annotations = method.getParameterAnnotations()[i];
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().equals(Lazy.class)) {
						clientParameterOptions[i] = "Lazy";
			        	this.clientAnnotationTypes.add(provider.getClientType(Lazy.class, null, null, null));
						break;
					}
				}				
			}
		} 
		else {
			this.returnType = null;
			this.parameterTypes = null;
			this.clientReturnType = null;
			this.clientParameterTypes = null;
			this.clientParameterNames = null;
			this.clientParameterOptions = null;
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
    
    public Class<?> getReturnType() {
    	return returnType;
    }
   
    public Class<?>[] getParameterTypes() {
    	return parameterTypes;
    }

	public ClientType[] getAs3ParameterTypes() {
		return clientParameterTypes;
	}

	public String[] getAs3ParameterNames() {
		return clientParameterNames;
	}

	public String[] getAs3ParameterOptions() {
		return clientParameterOptions;
	}
	
	public ClientType getClientReturnType() {
		return clientReturnType;
	}

	public ClientType[] getClientParameterTypes() {
		return clientParameterTypes;
	}

	public String[] getClientParameterNames() {
		return clientParameterNames;
	}

	public String[] getClientParameterOptions() {
		return clientParameterOptions;
	}
	
	public Set<ClientType> getClientAnnotationTypes() {
		return clientAnnotationTypes;
	}
}
