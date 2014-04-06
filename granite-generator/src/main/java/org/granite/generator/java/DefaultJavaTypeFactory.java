/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.generator.java;

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
import java.util.SortedMap;
import java.util.SortedSet;

import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.PropertyType;
import org.granite.generator.util.GenericTypeUtil;
import org.granite.tide.data.Lazy;
import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 */
public class DefaultJavaTypeFactory implements As3TypeFactory {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private final Map<String, ClientType> simpleJava2JavaType = new HashMap<String, ClientType>();
    private final Map<String, ClientType> propertyJava2JavaType = new HashMap<String, ClientType>();
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public DefaultJavaTypeFactory() {
    	simpleJava2JavaType.put(buildCacheKey(Boolean.TYPE), JavaType.BOOLEAN);
    	simpleJava2JavaType.put(buildCacheKey(Integer.TYPE), JavaType.INT);
    	simpleJava2JavaType.put(buildCacheKey(Long.TYPE), JavaType.LONG);
    	simpleJava2JavaType.put(buildCacheKey(Float.TYPE), JavaType.FLOAT);
    	simpleJava2JavaType.put(buildCacheKey(Double.TYPE), JavaType.DOUBLE);
    	simpleJava2JavaType.put(buildCacheKey(String.class), JavaType.STRING);
    	simpleJava2JavaType.put(buildCacheKey(Lazy.class), JavaType.LAZY);
    	
    	propertyJava2JavaType.put(buildCacheKey(Boolean.TYPE), JavaType.BOOLEAN);
    	propertyJava2JavaType.put(buildCacheKey(Integer.TYPE), JavaType.INT);
    	propertyJava2JavaType.put(buildCacheKey(Long.TYPE), JavaType.LONG);
    	propertyJava2JavaType.put(buildCacheKey(Float.TYPE), JavaType.FLOAT);
    	propertyJava2JavaType.put(buildCacheKey(Double.TYPE), JavaType.DOUBLE);
        propertyJava2JavaType.put(buildCacheKey(String.class), JavaType.STRING);
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
	public ClientType getClientType(Type jType, Class<?> declaringClass, ParameterizedType[] declaringTypes, PropertyType propertyType) {
		String key = buildCacheKey(jType, declaringClass, declaringTypes);
		
		ClientType javaType = getFromCache(key, propertyType);
		
        if (javaType == null) {
        	if (jType instanceof GenericArrayType) {
        		Type componentType = ((GenericArrayType)jType).getGenericComponentType();
        		javaType = getClientType(componentType, declaringClass, declaringTypes, PropertyType.SIMPLE).toArrayType();
        	}
        	else if (jType instanceof Class<?> && ((Class<?>)jType).isArray()) {
        		javaType = getClientType(((Class<?>)jType).getComponentType(), declaringClass, declaringTypes, PropertyType.SIMPLE).toArrayType();
        	}
        	else {
        		Set<String> imports = new HashSet<String>();
	        	Class<?> jClass = ClassUtil.classOfType(jType);
	        	String genericType = "";
	        	if (jType instanceof ParameterizedType)
	        		genericType = buildGenericTypeName((ParameterizedType)jType, declaringClass, declaringTypes, propertyType, imports);
	        	
                if (propertyType.isProperty() && List.class.isAssignableFrom(jClass)) {
                    javaType = createJavaType(jType, declaringClass, declaringTypes, "org.granite.client.persistence.collection.PersistentList" + genericType, propertyType);
                }
                else if (propertyType.isProperty() && SortedSet.class.isAssignableFrom(jClass)) {
                    javaType = createJavaType(jType, declaringClass, declaringTypes, "org.granite.client.persistence.collection.PersistentSortedSet" + genericType, propertyType);
                }
                else if (propertyType.isProperty() && Set.class.isAssignableFrom(jClass)) {
                    javaType = createJavaType(jType, declaringClass, declaringTypes, "org.granite.client.persistence.collection.PersistentSet" + genericType, propertyType);
                }
                else if (propertyType.isProperty() && SortedMap.class.isAssignableFrom(jClass)) {
                    javaType = createJavaType(jType, declaringClass, declaringTypes, "org.granite.client.persistence.collection.PersistentSortedMap" + genericType, propertyType);
                }
                else if (propertyType.isProperty() && Map.class.isAssignableFrom(jClass)) {
                    javaType = createJavaType(jType, declaringClass, declaringTypes, "org.granite.client.persistence.collection.PersistentMap" + genericType, propertyType);
                }
                else if (jClass.getName().equals("com.google.appengine.api.datastore.Key")) {
	            	javaType = JavaType.STRING;
	            }
	            else if (jClass.getName().equals("org.springframework.data.domain.Page")) {
	            	javaType = new JavaType("org.granite.tide.data.model", "Page" + genericType, null);
	            }
	            else if (jClass.getName().equals("org.springframework.data.domain.Pageable")) {
	            	javaType = JavaType.PAGE_INFO;
	            }
	            else if (jClass.getName().equals("org.springframework.data.domain.Sort")) {
	            	javaType = JavaType.SORT_INFO;
	            }
	            else {
	            	javaType = createJavaType(jType, declaringClass, declaringTypes, null, propertyType);
	            }
	            if (!imports.isEmpty())
	            	javaType.addImports(imports);
        	}
        	
            putInCache(key, propertyType, javaType);
        }
        
        return javaType;
    }

	@Override
	public ClientType getAs3Type(Class<?> jType) {
		return getClientType(jType, null, null, PropertyType.SIMPLE);
	}

    protected JavaType createJavaType(Type jType, Class<?> declaringClass, ParameterizedType[] declaringTypes, String propertyImplTypeName, PropertyType propertyType) {
    	Set<String> imports = new HashSet<String>();
    	
    	Class<?> jClass = ClassUtil.classOfType(jType);
    	String name = jClass.getSimpleName();
    	if (jClass.isMemberClass())
    		name = jClass.getEnclosingClass().getSimpleName() + '$' + jClass.getSimpleName();
    	else if (jType instanceof ParameterizedType) {
    		ParameterizedType type = (ParameterizedType)jType;
    		name += buildGenericTypeName(type, declaringClass, declaringTypes, propertyType, imports);
    	}
    	
    	JavaType javaType = new JavaType(ClassUtil.getPackageName(jClass), name, propertyImplTypeName, null);
    	javaType.addImports(imports);
    	return javaType;
    }

    protected ClientType getFromCache(String key, PropertyType propertyType) {
        if (key == null)
            throw new NullPointerException("jType must be non null");
        if (propertyType == PropertyType.PROPERTY)
            return propertyJava2JavaType.get(key);
        return simpleJava2JavaType.get(key);
    }

    protected void putInCache(String key, PropertyType propertyType, ClientType javaType) {
        if (key == null || javaType == null)
            throw new NullPointerException("jType and JavaType must be non null");
        if (propertyType == PropertyType.PROPERTY)
            propertyJava2JavaType.put(key, javaType);
        else
            simpleJava2JavaType.put(key, javaType);
    }
    
    private String buildGenericTypeName(ParameterizedType type, Class<?> declaringClass, ParameterizedType[] declaringTypes, PropertyType propertyType, Set<String> imports) {
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
					imports.add(getClientType(resolved, declaringClass, declaringTypes, propertyType).getQualifiedName());
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
    					imports.add(getClientType(resolved, declaringClass, declaringTypes, propertyType).getQualifiedName());
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
    					imports.add(getClientType(resolved, declaringClass, declaringTypes, propertyType).getQualifiedName());
    				}
    				if (bounds.length() > 0)
    					sb.append(" extends ").append(bounds);
				}
			}
			else {
				sb.append(ClassUtil.classOfType(ata).getSimpleName());
				imports.add(getClientType(ata, declaringClass, declaringTypes, propertyType).getQualifiedName());
			}
		}
		sb.append(">");
		return sb.toString();
    }
}
