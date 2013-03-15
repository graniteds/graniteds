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
import java.util.HashSet;
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

    private final Map<String, ClientType> simpleJava2JavaFXType = new HashMap<String, ClientType>();
    private final Map<String, ClientType> propertyJava2JavaFXType = new HashMap<String, ClientType>();
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public DefaultJavaFXTypeFactory() {
    	simpleJava2JavaFXType.put(buildCacheKey(Boolean.TYPE), JavaFXType.BOOLEAN);
    	simpleJava2JavaFXType.put(buildCacheKey(Integer.TYPE), JavaFXType.INT);
    	simpleJava2JavaFXType.put(buildCacheKey(Long.TYPE), JavaFXType.LONG);
    	simpleJava2JavaFXType.put(buildCacheKey(Float.TYPE), JavaFXType.FLOAT);
    	simpleJava2JavaFXType.put(buildCacheKey(Double.TYPE), JavaFXType.DOUBLE);
    	simpleJava2JavaFXType.put(buildCacheKey(String.class), JavaFXType.STRING);
    	
    	propertyJava2JavaFXType.put(buildCacheKey(Boolean.TYPE), JavaFXType.BOOLEAN_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(Double.TYPE), JavaFXType.DOUBLE_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(Float.TYPE), JavaFXType.FLOAT_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(Long.TYPE), JavaFXType.LONG_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(Integer.TYPE), JavaFXType.INT_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(String.class), JavaFXType.STRING_PROPERTY);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    @Override
	public void configure(boolean externalizeLong, boolean externalizeBigInteger, boolean externalizeBigDecimal) {
	}
    
    private static final String buildCacheKey(Type jType) {
    	return buildCacheKey(jType, null, null);
    }
    private static final String buildCacheKey(Type jType, Class<?> declaringClass, ParameterizedType[] declaringTypes) {
		String key = jType.toString();
		if (declaringClass != null)
			key += "::" + declaringClass.toString();
		if (declaringTypes != null) {
			for (ParameterizedType dt : declaringTypes)
				key += "::" + dt.toString();
		}
		return key;
    }

	@Override
	public ClientType getClientType(Type jType, Class<?> declaringClass, ParameterizedType[] declaringTypes, boolean property) {
		String key = buildCacheKey(jType, declaringClass, declaringTypes);
		
		ClientType javafxType = getFromCache(key, property);
		
        if (javafxType == null) {
        	if (jType instanceof GenericArrayType) {
        		Type componentType = ((GenericArrayType)jType).getGenericComponentType();
        		javafxType = getClientType(componentType, declaringClass, declaringTypes, false).toArrayType();
        	}
        	else if (jType instanceof Class<?> && ((Class<?>)jType).isArray()) {
        		javafxType = getClientType(((Class<?>)jType).getComponentType(), declaringClass, declaringTypes, false).toArrayType();
        	}
        	else {
        		Set<String> imports = new HashSet<String>();
	        	Class<?> jClass = ClassUtil.classOfType(jType);
	        	String genericType = "";
	        	if (jType instanceof ParameterizedType)
	        		genericType = buildGenericTypeName((ParameterizedType)jType, declaringClass, declaringTypes, property, imports);
	        	
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
	            if (!imports.isEmpty())
	            	javafxType.addImports(imports);
        	}
        	
            putInCache(key, property, javafxType);
        }
        
        return javafxType;
    }

	@Override
	public ClientType getAs3Type(Class<?> jType) {
		return getClientType(jType, null, null, false);
	}

    protected JavaFXType createJavaFXType(Type jType, Class<?> declaringClass, ParameterizedType[] declaringTypes, boolean property) {
    	Set<String> imports = new HashSet<String>();
    	
    	Class<?> jClass = ClassUtil.classOfType(jType);
    	String name = jClass.getSimpleName();
    	if (jClass.isMemberClass())
    		name = jClass.getEnclosingClass().getSimpleName() + '$' + jClass.getSimpleName();
    	else if (jType instanceof ParameterizedType) {
    		ParameterizedType type = (ParameterizedType)jType;
    		name += buildGenericTypeName(type, declaringClass, declaringTypes, property, imports);
    	}
    	
    	JavaFXType javaFXType = null;
    	if (property) {
    		javaFXType = new JavaFXType(ClassUtil.getPackageName(jClass), name, 
    				"javafx.beans.property.ObjectProperty<" + name + ">", "javafx.beans.property.SimpleObjectProperty<" + name + ">", null);
    	}
    	else
    		javaFXType = new JavaFXType(ClassUtil.getPackageName(jClass), name, null);
    	javaFXType.addImports(imports);
    	return javaFXType;
    }

    protected ClientType getFromCache(String key, boolean property) {
        if (key == null)
            throw new NullPointerException("jType must be non null");
        if (property)
        	return propertyJava2JavaFXType.get(key);
        return simpleJava2JavaFXType.get(key);
    }

    protected void putInCache(String key, boolean property, ClientType javafxType) {
        if (key == null || javafxType == null)
            throw new NullPointerException("jType and JavaFXType must be non null");
        if (property)
        	propertyJava2JavaFXType.put(key, javafxType);
        else
        	simpleJava2JavaFXType.put(key, javafxType);
    }
    
    private String buildGenericTypeName(ParameterizedType type, Class<?> declaringClass, ParameterizedType[] declaringTypes, boolean property, Set<String> imports) {
		StringBuilder sb = new StringBuilder("<");
		boolean first = true;
		for (Type ata : type.getActualTypeArguments()) {
			if (first)
				first = false;
			else
				sb.append(", ");
			if (ata instanceof TypeVariable<?>) {
				Type resolved = GenericTypeUtil.resolveTypeVariable(ata, declaringClass, declaringTypes);
				if (resolved instanceof TypeVariable<?>)
					sb.append("?");
				else {
					sb.append(ClassUtil.classOfType(resolved).getSimpleName());
					imports.add(getClientType(resolved, declaringClass, declaringTypes, property).getQualifiedName());
				}
			}
			else if (ata instanceof WildcardType) {
				sb.append("?");
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
    					imports.add(getClientType(resolved, declaringClass, declaringTypes, property).getQualifiedName());
    				}
    				if (bounds.length() > 0)
    					sb.append(" super ").append(bounds);
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
    					imports.add(getClientType(resolved, declaringClass, declaringTypes, property).getQualifiedName());
    				}
    				if (bounds.length() > 0)
    					sb.append(" extends ").append(bounds);
				}
			}
			else {
				sb.append(ClassUtil.classOfType(ata).getSimpleName());
				imports.add(getClientType(ata, declaringClass, declaringTypes, property).getQualifiedName());
			}
		}
		sb.append(">");
		return sb.toString();
    }
}
