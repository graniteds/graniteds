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
/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.granite.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.Version;



/**
 * A wrapper for a entity, This code was pulled from Entity.java 
 * in the seam project www.seamframework.org jboss-seam-2.0.0.GA author Gavin King
 * @author gavin king
 */

public class Entity {
	
	private Class<?> entityClass;
	private Method identifierGetter;
	private Field identifierField;
	private Method versionGetter;
	private Field versionField;
	private Object wrappedEntity;
	private String name;
	
	
	public Entity(Object entity) {
		if (entity instanceof Class<?>)
			this.entityClass = (Class<?>)entity;
		else {
			this.entityClass = entity.getClass();
			this.wrappedEntity = entity;
		}
		
        if (entityClass.isAnnotationPresent(javax.persistence.Entity.class)) {
	        if (!"".equals(entityClass.getAnnotation(javax.persistence.Entity.class).name()))
	            name = entityClass.getAnnotation(javax.persistence.Entity.class).name();
	        else
	            name = entityClass.getName();
        }
		
	    for (Class<?> clazz = entityClass; clazz != Object.class; clazz = clazz.getSuperclass())  {
            for (Method method : clazz.getDeclaredMethods()) {
			    if (method.isAnnotationPresent(Id.class) || method.isAnnotationPresent(EmbeddedId.class))
			        identifierGetter = method;
			    
			    if (method.isAnnotationPresent(Version.class))
			    	versionGetter = method;
			}
            
	    }
	    
        if (identifierGetter == null) {
            for (Class<?> clazz = entityClass; clazz != Object.class; clazz = clazz.getSuperclass())	{
               for (Field field : clazz.getDeclaredFields()) {
                   if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                	   identifierField = field;
                       if (!field.isAccessible())
                           field.setAccessible(true);
                   }
              
                   if (field.isAnnotationPresent(Version.class)) {
                	   versionField = field;
                       if (!field.isAccessible())
                           field.setAccessible(true);
                   }
               }
           }
        }
	}
	
	
	
	public Object getIdentifier() {
		if (wrappedEntity == null)
			throw new IllegalStateException("No entity instance defined");
		
		return getIdentifier(wrappedEntity);
	}
	
	public Object getIdentifier(Object entity) {		
	    if (identifierGetter != null)
	    	return Reflections.invokeAndWrap(identifierGetter, entity);
	    else if (identifierField != null)
	    	return Reflections.getAndWrap(identifierField, entity);
	    else
	    	throw new IllegalStateException("@Id attribute not found for entity class: " + entity.getClass().getName());
	}
	
	public Object getVersion() {
		if (wrappedEntity == null)
			throw new IllegalStateException("No entity instance defined");
		
		return getVersion(wrappedEntity);
	}
	
	public Object getVersion(Object entity) {
	    if (versionGetter != null)
	    	return Reflections.invokeAndWrap(versionGetter, entity);
	    else if (versionField != null)
	    	return Reflections.getAndWrap(versionField, entity);
	    return null;
	}

	
	public Method getIdentifierGetter() {
		return identifierGetter;
	}

	public Field getIdentifierField() {
		return identifierField;
	}

	public Type getIdentifierType() {
	    if (identifierGetter != null)
	    	return identifierGetter.getGenericReturnType();
	    else if (identifierField != null)
	    	return identifierField.getGenericType();
	    else
	    	throw new IllegalStateException("@Id attribute not found for entity class: " + entityClass.getName());
	}
	
	
	public Method getVersionGetter() {
		return versionGetter;
	}

	public Field getVersionField() {
		return versionField;
	}
	
	public boolean isVersioned() {
		return versionGetter != null || versionField != null;
	}


	public String getName() {
		return name;
	}
}
