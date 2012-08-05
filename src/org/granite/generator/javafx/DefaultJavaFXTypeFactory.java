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

package org.granite.generator.javafx;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.ClientType;
import org.granite.generator.util.GenericTypeUtil;
import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 */
public class DefaultJavaFXTypeFactory implements As3TypeFactory {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private final Map<Type, ClientType> simpleJava2JavaFXType = new HashMap<Type, ClientType>();
    private final Map<Type, ClientType> propertyJava2JavaFXType = new HashMap<Type, ClientType>();
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public DefaultJavaFXTypeFactory() {
    	simpleJava2JavaFXType.put(Boolean.TYPE, JavaFXType.BOOLEAN);
    	simpleJava2JavaFXType.put(Integer.TYPE, JavaFXType.INT);
    	simpleJava2JavaFXType.put(Long.TYPE, JavaFXType.LONG);
    	simpleJava2JavaFXType.put(Float.TYPE, JavaFXType.FLOAT);
    	simpleJava2JavaFXType.put(Double.TYPE, JavaFXType.DOUBLE);
    	simpleJava2JavaFXType.put(String.class, JavaFXType.STRING);
    	
    	propertyJava2JavaFXType.put(Boolean.TYPE, JavaFXType.BOOLEAN_PROPERTY);
    	propertyJava2JavaFXType.put(Double.TYPE, JavaFXType.DOUBLE_PROPERTY);
    	propertyJava2JavaFXType.put(Float.TYPE, JavaFXType.FLOAT_PROPERTY);
    	propertyJava2JavaFXType.put(Long.TYPE, JavaFXType.LONG_PROPERTY);
    	propertyJava2JavaFXType.put(Integer.TYPE, JavaFXType.INT_PROPERTY);
    	propertyJava2JavaFXType.put(String.class, JavaFXType.STRING_PROPERTY);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    public void configure(boolean externalizeLong, boolean externalizeBigInteger, boolean externalizeBigDecimal) {
	}

	public ClientType getClientType(Type jType, Class<?> declaringClass, ParameterizedType[] declaringTypes, boolean property) {
        ClientType javafxType = getFromCache(jType, property);

        if (javafxType == null) {
        	if (jType instanceof GenericArrayType) {
        		Type componentType = ((GenericArrayType)jType).getGenericComponentType();
        		javafxType = getClientType(componentType, declaringClass, declaringTypes, false).toArrayType();
        	}
        	else if (jType instanceof Class<?> && ((Class<?>)jType).isArray()) {
        		javafxType = getClientType(((Class<?>)jType).getComponentType(), declaringClass, declaringTypes, false).toArrayType();
        	}
        	else {
	        	Class<?> jClass = ClassUtil.classOfType(jType);
	        	String genericType = "";
	        	if (jType instanceof ParameterizedType) {
	        		for (Type t : ((ParameterizedType)jType).getActualTypeArguments()) {
	        			if (genericType.length() > 0)
	        				genericType += ", ";
	        			genericType += ClassUtil.classOfType(t).getSimpleName();
	        		}
	        		genericType = "<" + genericType + ">";
	        	}
	            if (property && List.class.isAssignableFrom(jClass)) {
	                javafxType = new JavaFXType("javafx.collections", "ObservableList" + genericType, null, "org.granite.client.persistence.javafx.PersistentList" + genericType, null);
	            }
	            else if (property && Set.class.isAssignableFrom(jClass)) {
	                javafxType = new JavaFXType("javafx.collections", "ObservableList" + genericType, null, "org.granite.client.persistence.javafx.PersistentSet" + genericType, null);
	            }
	            else if (property && Map.class.isAssignableFrom(jClass)) {
	                javafxType = new JavaFXType("javafx.collections", "ObservableMap" + genericType, null, "org.granite.client.persistence.javafx.PersistentMap" + genericType, null);
	            }
	            else if (jClass.getName().equals("com.google.appengine.api.datastore.Key")) {
	            	javafxType = JavaFXType.STRING;
	            }
	            else if (jClass.getName().equals("org.springframework.data.domain.Page")) {
	            	javafxType = new JavaFXType("org.granite.tide.data.model", "Page" + genericType, null);
	            }
	            else if (jClass.getName().equals("org.springframework.data.domain.Pageable")) {
	            	javafxType = JavaFXType.PAGE_INFO;
	            }
	            else if (jClass.getName().equals("org.springframework.data.domain.Sort")) {
	            	javafxType = JavaFXType.SORT_INFO;
	            }
	            else {
	            	javafxType = createJavaFXType(jType, declaringClass, declaringTypes, property);
	            }
        	}

            putInCache(jType, property, javafxType);
        }

        return javafxType;
    }

	public ClientType getAs3Type(Class<?> jType) {
		return getClientType(jType, null, null, false);
	}

    protected JavaFXType createJavaFXType(Type jType, Class<?> declaringClass, ParameterizedType[] declaringTypes, boolean property) {
    	Class<?> jClass = ClassUtil.classOfType(jType);
    	String name = jClass.getSimpleName();
    	if (jClass.isMemberClass())
    		name = jClass.getEnclosingClass().getSimpleName() + '$' + jClass.getSimpleName();
    	else if (jType instanceof ParameterizedType) {
    		ParameterizedType type = (ParameterizedType)jType;
    		name = name + "<";
    		boolean first = true;
    		for (Type ata : type.getActualTypeArguments()) {
    			if (first)
    				first = false;
    			else
    				name = name + ", ";
    			if (ata instanceof TypeVariable<?>) {
    				Type resolved = GenericTypeUtil.resolveTypeVariable(ata, declaringClass, declaringTypes);
    				if (resolved instanceof TypeVariable<?>)
    					name = name + "?";
    				else
    					name = name + ClassUtil.classOfType(resolved).getSimpleName();
    			}
    			else if (ata instanceof WildcardType) {
    				name = name + "?";
    				if (((WildcardType)ata).getLowerBounds().length > 0) {
    					String bounds = "";
	    				for (Type t : ((WildcardType)ata).getLowerBounds()) {
	        				Type resolved = GenericTypeUtil.resolveTypeVariable(t, declaringClass, declaringTypes);
	        				if (resolved instanceof TypeVariable<?>) {
	        					bounds = "";
	        					break;
	        				}
        					if (bounds.length() > 0)
        						bounds = bounds + ", ";
        					bounds = bounds + ClassUtil.classOfType(resolved).getSimpleName();
	    				}
	    				if (bounds.length() > 0)
	    					name = name + " super " + bounds;
    				}
    				if (((WildcardType)ata).getUpperBounds().length > 0) {
    					String bounds = "";
	    				for (Type t : ((WildcardType)ata).getUpperBounds()) {
	        				Type resolved = GenericTypeUtil.resolveTypeVariable(t, declaringClass, declaringTypes);
	        				if (resolved instanceof TypeVariable<?>) {
	        					bounds = "";
	        					break;
	        				}
        					if (bounds.length() > 0)
        						bounds = bounds + ", ";
        					bounds = bounds + ClassUtil.classOfType(resolved).getSimpleName();
	    				}
	    				if (bounds.length() > 0)
	    					name = name + " extends " + bounds;
    				}
    			}
    			else
    				name = name + ClassUtil.classOfType(ata).getSimpleName();
    		}
    		name = name + ">";
    	}
    	
    	if (property)
    		return new JavaFXType(ClassUtil.getPackageName(jClass), name, "javafx.beans.property.ObjectProperty<" + name + ">", "javafx.beans.property.SimpleObjectProperty<" + name + ">", null);
    	return new JavaFXType(ClassUtil.getPackageName(jClass), name, null);
    }

    protected ClientType getFromCache(Type jType, boolean property) {
        if (jType == null)
            throw new NullPointerException("jType must be non null");
        if (property)
        	return propertyJava2JavaFXType.get(jType);
        return simpleJava2JavaFXType.get(jType);
    }

    protected void putInCache(Type jType, boolean property, ClientType javafxType) {
        if (jType == null || javafxType == null)
            throw new NullPointerException("jType and JavaFXType must be non null");
        if (property)
        	propertyJava2JavaFXType.put(jType, javafxType);
        else
        	simpleJava2JavaFXType.put(jType, javafxType);
    }
}
