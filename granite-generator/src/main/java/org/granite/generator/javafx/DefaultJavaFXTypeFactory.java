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
public class DefaultJavaFXTypeFactory implements As3TypeFactory {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private final Map<String, ClientType> simpleJava2JavaFXType = new HashMap<String, ClientType>();
    private final Map<String, ClientType> propertyJava2JavaFXType = new HashMap<String, ClientType>();
    private final Map<String, ClientType> readOnlyPropertyJava2JavaFXType = new HashMap<String, ClientType>();
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public DefaultJavaFXTypeFactory() {
    	simpleJava2JavaFXType.put(buildCacheKey(Boolean.TYPE), JavaFXType.BOOLEAN);
    	simpleJava2JavaFXType.put(buildCacheKey(Integer.TYPE), JavaFXType.INT);
    	simpleJava2JavaFXType.put(buildCacheKey(Long.TYPE), JavaFXType.LONG);
    	simpleJava2JavaFXType.put(buildCacheKey(Float.TYPE), JavaFXType.FLOAT);
    	simpleJava2JavaFXType.put(buildCacheKey(Double.TYPE), JavaFXType.DOUBLE);
    	simpleJava2JavaFXType.put(buildCacheKey(String.class), JavaFXType.STRING);
    	simpleJava2JavaFXType.put(buildCacheKey(Lazy.class), JavaFXType.LAZY);
    	
    	propertyJava2JavaFXType.put(buildCacheKey(Boolean.TYPE), JavaFXType.BOOLEAN_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(Double.TYPE), JavaFXType.DOUBLE_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(Float.TYPE), JavaFXType.FLOAT_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(Long.TYPE), JavaFXType.LONG_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(Integer.TYPE), JavaFXType.INT_PROPERTY);
    	propertyJava2JavaFXType.put(buildCacheKey(String.class), JavaFXType.STRING_PROPERTY);
    	
    	readOnlyPropertyJava2JavaFXType.put(buildCacheKey(Boolean.TYPE), JavaFXType.BOOLEAN_READONLY_PROPERTY);
    	readOnlyPropertyJava2JavaFXType.put(buildCacheKey(Double.TYPE), JavaFXType.DOUBLE_READONLY_PROPERTY);
    	readOnlyPropertyJava2JavaFXType.put(buildCacheKey(Float.TYPE), JavaFXType.FLOAT_READONLY_PROPERTY);
    	readOnlyPropertyJava2JavaFXType.put(buildCacheKey(Long.TYPE), JavaFXType.LONG_READONLY_PROPERTY);
    	readOnlyPropertyJava2JavaFXType.put(buildCacheKey(Integer.TYPE), JavaFXType.INT_READONLY_PROPERTY);
    	readOnlyPropertyJava2JavaFXType.put(buildCacheKey(String.class), JavaFXType.STRING_READONLY_PROPERTY);
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
		
		ClientType javafxType = getFromCache(key, propertyType);
		
        if (javafxType == null) {
        	if (jType instanceof GenericArrayType) {
        		Type componentType = ((GenericArrayType)jType).getGenericComponentType();
        		javafxType = getClientType(componentType, declaringClass, declaringTypes, PropertyType.SIMPLE).toArrayType();
        	}
        	else if (jType instanceof Class<?> && ((Class<?>)jType).isArray()) {
        		javafxType = getClientType(((Class<?>)jType).getComponentType(), declaringClass, declaringTypes, PropertyType.SIMPLE).toArrayType();
        	}
        	else {
        		Set<String> imports = new HashSet<String>();
	        	Class<?> jClass = ClassUtil.classOfType(jType);
	        	String genericType = "";
	        	if (jType instanceof ParameterizedType)
	        		genericType = buildGenericTypeName((ParameterizedType)jType, declaringClass, declaringTypes, propertyType, imports);
	        	
	            if (propertyType.isProperty() && List.class.isAssignableFrom(jClass)) {
	            	imports.add("org.granite.client.javafx.persistence.collection.FXPersistentCollections");
	                javafxType = new JavaFXType("javafx.collections", "ObservableList" + genericType, "javafx.beans.property.ReadOnlyListProperty" + genericType, "javafx.beans.property.ReadOnlyListWrapper" + genericType, "FXPersistentCollections.readOnlyObservablePersistentList", null, true);
	            }
                else if (propertyType.isProperty() && SortedSet.class.isAssignableFrom(jClass)) {
                    imports.add("org.granite.client.javafx.persistence.collection.FXPersistentCollections");
                    javafxType = new JavaFXType("javafx.collections", "ObservableSet" + genericType, "javafx.beans.property.ReadOnlySetProperty" + genericType, "javafx.beans.property.ReadOnlySetWrapper" + genericType, "FXPersistentCollections.readOnlyObservablePersistentSortedSet", null, true);
                }
	            else if (propertyType.isProperty() && Set.class.isAssignableFrom(jClass)) {
	            	imports.add("org.granite.client.javafx.persistence.collection.FXPersistentCollections");
	                javafxType = new JavaFXType("javafx.collections", "ObservableSet" + genericType, "javafx.beans.property.ReadOnlySetProperty" + genericType, "javafx.beans.property.ReadOnlySetWrapper" + genericType, "FXPersistentCollections.readOnlyObservablePersistentSet", null, true);
	            }
                else if (propertyType.isProperty() && SortedMap.class.isAssignableFrom(jClass)) {
                    imports.add("org.granite.client.javafx.persistence.collection.FXPersistentCollections");
                    javafxType = new JavaFXType("javafx.collections", "ObservableMap" + genericType, "javafx.beans.property.ReadOnlyMapProperty" + genericType, "javafx.beans.property.ReadOnlyMapWrapper" + genericType, "FXPersistentCollections.readOnlyObservablePersistentSortedMap", null, true);
                }
	            else if (propertyType.isProperty() && Map.class.isAssignableFrom(jClass)) {
	            	imports.add("org.granite.client.javafx.persistence.collection.FXPersistentCollections");
	                javafxType = new JavaFXType("javafx.collections", "ObservableMap" + genericType, "javafx.beans.property.ReadOnlyMapProperty" + genericType, "javafx.beans.property.ReadOnlyMapWrapper" + genericType, "FXPersistentCollections.readOnlyObservablePersistentMap", null, true);
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
	            	javafxType = createJavaFXType(jType, declaringClass, declaringTypes, propertyType);
	            }
	            if (!imports.isEmpty())
	            	javafxType.addImports(imports);
        	}
        	
            putInCache(key, propertyType, javafxType);
        }
        
        return javafxType;
    }

	@Override
	public ClientType getAs3Type(Class<?> jType) {
		return getClientType(jType, null, null, PropertyType.SIMPLE);
	}

    protected JavaFXType createJavaFXType(Type jType, Class<?> declaringClass, ParameterizedType[] declaringTypes, PropertyType propertyType) {
    	Set<String> imports = new HashSet<String>();
    	
    	Class<?> jClass = ClassUtil.classOfType(jType);
    	String name = jClass.getSimpleName();
    	if (jClass.isMemberClass())
    		name = jClass.getEnclosingClass().getSimpleName() + '$' + jClass.getSimpleName();
    	else if (jType instanceof ParameterizedType) {
    		ParameterizedType type = (ParameterizedType)jType;
    		name += buildGenericTypeName(type, declaringClass, declaringTypes, propertyType, imports);
    	}
    	
    	JavaFXType javaFXType = null;
    	if (propertyType == PropertyType.PROPERTY) {
    		javaFXType = new JavaFXType(ClassUtil.getPackageName(jClass), name, 
    				"javafx.beans.property.ObjectProperty<" + name + ">", "javafx.beans.property.SimpleObjectProperty<" + name + ">", null);
    	}
    	else if (propertyType == PropertyType.READONLY_PROPERTY) {
    		javaFXType = new JavaFXType(ClassUtil.getPackageName(jClass), name, 
    				"javafx.beans.property.ReadOnlyObjectProperty<" + name + ">", "javafx.beans.property.ReadOnlyObjectWrapper<" + name + ">", null, null, true);
    	}
    	else
    		javaFXType = new JavaFXType(ClassUtil.getPackageName(jClass), name, null);
    	javaFXType.addImports(imports);
    	return javaFXType;
    }

    protected ClientType getFromCache(String key, PropertyType propertyType) {
        if (key == null)
            throw new NullPointerException("key must be non null");
        if (propertyType == PropertyType.PROPERTY)
        	return propertyJava2JavaFXType.get(key);
        else if (propertyType == PropertyType.READONLY_PROPERTY)
        	return readOnlyPropertyJava2JavaFXType.get(key);
        return simpleJava2JavaFXType.get(key);
    }

    protected void putInCache(String key, PropertyType propertyType, ClientType javafxType) {
        if (key == null || javafxType == null)
            throw new NullPointerException("key and javafxType must be non null");
        if (propertyType == PropertyType.PROPERTY)
        	propertyJava2JavaFXType.put(key, javafxType);
        else if (propertyType == PropertyType.READONLY_PROPERTY)
        	readOnlyPropertyJava2JavaFXType.put(key, javafxType);
        else
        	simpleJava2JavaFXType.put(key, javafxType);
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
