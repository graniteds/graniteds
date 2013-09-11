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
package org.granite.client.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.messaging.reflect.Property;
import org.granite.messaging.reflect.PropertyAccessException;
import org.granite.messaging.reflect.PropertyNotFoundException;
import org.granite.messaging.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class Persistence {

	private static final String INITIALIZED_FIELD_NAME = "__initialized__";
	private static final String DETACHED_STATE_FIELD_NAME = "__detachedState__";

	private final Reflection reflection;
	
	///////////////////////////////////////////////////////////////////////////
	// Constructor
	
	public Persistence(Reflection reflection) {
		this.reflection = reflection;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Entity
	
	public boolean isEntity(Class<?> cls) {
		return cls != null && cls.isAnnotationPresent(Entity.class);
	}
	
	protected void checkEntity(Class<?> entityClass) {
		if (!isEntity(entityClass))
			throw new PropertyNotFoundException("Not annotated with @" + Entity.class.getName() + ": " + entityClass);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Initialized
	
	protected Property getInitializedProperty(Class<?> entityClass, boolean throwIfNotFound) {
		checkEntity(entityClass);
		
		Property property = reflection.findProperty(entityClass, INITIALIZED_FIELD_NAME, Boolean.TYPE);
		if (property == null && throwIfNotFound)
			throw new PropertyNotFoundException("No boolean " + INITIALIZED_FIELD_NAME + " property in " + entityClass);
		return property;
	}
	
	public Property getInitializedProperty(Class<?> entityClass) {
		return getInitializedProperty(entityClass, false);
	}
	
	public boolean hasInitializedProperty(Class<?> entityClass) {
		return getInitializedProperty(entityClass, false) != null;
	}
	
	public boolean isInitialized(Object o) {
		if (o instanceof PersistentCollection)
			return ((PersistentCollection)o).wasInitialized();
		
		Class<?> cls = o.getClass();
		if (!isEntity(cls))
			return true;

		Property property = getInitializedProperty(cls, false);
		if (property == null)
			return true;
		
		try {
			return property.getBoolean(o);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not get " + property + " of object " + o, e);
		}
	}
	
	public void setInitialized(Object entity, boolean value) {
		Property property = getInitializedProperty(entity.getClass(), true);
		try {
			property.setBoolean(entity, value);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not set " + property + " of object " + entity + " to value " + value, e);
		}		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Detached state
	
	protected Property getDetachedStateProperty(Class<?> entityClass, boolean throwIfNotFound) {
		checkEntity(entityClass);
		
		Property property = reflection.findProperty(entityClass, DETACHED_STATE_FIELD_NAME, String.class);
		if (property == null && throwIfNotFound)
			throw new PropertyNotFoundException("No String " + DETACHED_STATE_FIELD_NAME + " property in " + entityClass);
		return property;
	}
	
	public Property getDetachedStateProperty(Class<?> entityClass) {
		return getDetachedStateProperty(entityClass, false);
	}
	
	public boolean hasDetachedStateProperty(Class<?> entityClass) {
		return getDetachedStateProperty(entityClass, false) != null;
	}
	
	public String getDetachedState(Object entity) {
		Property property = getDetachedStateProperty(entity.getClass(), true);
		try {
			return (String)property.getObject(entity);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not get " + property + " of object " + entity, e);
		}
	}
	
	public void setDetachedState(Object entity, String value) {
		Property property = getDetachedStateProperty(entity.getClass(), true);
		try {
			property.setObject(entity, value);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not set " + property + " of object " + entity + " to value " + value, e);
		}		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Id
	
	protected Property getIdProperty(Class<?> entityClass, boolean throwIfNotFound) {
		checkEntity(entityClass);
		
		Property property = reflection.findProperty(entityClass, Id.class);
		if (property == null && throwIfNotFound)
			throw new PropertyNotFoundException("No property annotated with " + Id.class.getName() + " in " + entityClass);
		return property;
	}
	
	public Property getIdProperty(Class<?> entityClass) {
		return getIdProperty(entityClass, false);
	}
	
	public boolean hasIdProperty(Class<?> entityClass) {
		return getIdProperty(entityClass, false) != null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getId(Object entity) {
		Property property = getIdProperty(entity.getClass(), true);
		try {
			return (T)property.getObject(entity);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not get " + property + " of object " + entity, e);
		}
	}

	public void setId(Object entity, Object value) {
		Property property = getIdProperty(entity.getClass(), true);
		try {
			property.setObject(entity, value);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not set " + property + " of object " + entity + " to value " + value, e);
		}		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Uid
	
	protected Property getUidProperty(Class<?> entityClass, boolean throwIfNotFound) {
		checkEntity(entityClass);
		
		Property property = reflection.findProperty(entityClass, Uid.class);
		if (property == null && throwIfNotFound)
			throw new PropertyNotFoundException("No property annotated with " + Uid.class.getName() + " in " + entityClass);
		return property;
	}
	
	public Property getUidProperty(Class<?> entityClass) {
		return getUidProperty(entityClass, false);
	}
	
	public boolean hasUidProperty(Class<?> entityClass) {
		return getUidProperty(entityClass, false) != null;
	}
	
	public String getUid(Object entity) {
		Property property = getUidProperty(entity.getClass(), true);
		try {
			return (String)property.getObject(entity);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not get " + property + " of object " + entity, e);
		}
	}
	
	public void setUid(Object entity, String value) {
		Property property = getUidProperty(entity.getClass(), true);
		try {
			property.setObject(entity, value);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not set " + property + " of object " + entity + " to value " + value, e);
		}		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Version
	
	protected Property getVersionProperty(Class<?> entityClass, boolean throwIfNotFound) {
		checkEntity(entityClass);
		
		Property property = reflection.findProperty(entityClass, Version.class);
		if (property == null && throwIfNotFound)
			throw new PropertyNotFoundException("No property annotated with " + Version.class.getName() + " in " + entityClass);
		return property;
	}
	
	public Property getVersionProperty(Class<?> entityClass) {
		return getVersionProperty(entityClass, false);
	}
	
	public boolean hasVersionProperty(Class<?> entityClass) {
		return getVersionProperty(entityClass, false) != null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getVersion(Object entity) {
		Property property = getVersionProperty(entity.getClass(), true);
		try {
			return (T)property.getObject(entity);
		}
		catch (Exception e) {
			throw new PropertyAccessException("Could not get " + property + " of object " + entity, e);
		}
	}
	
	public List<Property> getLazyProperties(Class<?> entityClass) {
		List<Property> properties = reflection.findSerializableProperties(entityClass);
		List<Property> lazyProperties = new ArrayList<Property>();
		for (Property property : properties) {
			if (property.isAnnotationPresent(Lazy.class))
				lazyProperties.add(property);
		}
		return lazyProperties;
	}

	
	public List<Property> getProperties(Class<?> entityClass) {
		return reflection.findSerializableProperties(entityClass);
	}
	
	public Object getPropertyValue(Object entity, String name, boolean raw) {
		Property property = reflection.findSerializableProperty(entity.getClass(), name);
		try {
			return raw ? property.getRawObject(entity) : property.getObject(entity);
        }
        catch (Exception e) {
        	throw new RuntimeException("Could not get " + property + " of object " + entity);
        }
	}
	
	public void setPropertyValue(Object entity, String name, Object value) {
		Property property = reflection.findSerializableProperty(entity.getClass(), name);
		try {
			property.setObject(entity, value);
        }
        catch (Exception e) {
        	throw new RuntimeException("Could not set " + property + " of object " + entity);
        }
	}
	
    public Map<String, Object> getPropertyValues(Object entity, boolean raw, boolean excludeIdUid, boolean excludeVersion, boolean includeReadOnly) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        
        List<Property> properties = reflection.findSerializableProperties(entity.getClass());
        
        List<Property> excludedProperties = new ArrayList<Property>();
        if (isEntity(entity.getClass())) {
            excludedProperties.add(getInitializedProperty(entity.getClass()));
            excludedProperties.add(getDetachedStateProperty(entity.getClass()));
	        if (excludeIdUid && hasIdProperty(entity.getClass()))
	        	excludedProperties.add(getIdProperty(entity.getClass()));
	        if (excludeIdUid && hasUidProperty(entity.getClass()))
	        	excludedProperties.add(getUidProperty(entity.getClass()));
	        if (excludeVersion && hasVersionProperty(entity.getClass()))
	        	excludedProperties.add(getVersionProperty(entity.getClass()));
        }
        
        for (Property property : properties) {
            if (excludedProperties.contains(property))
                continue;
            
            if (!includeReadOnly && !property.isWritable())
            	continue;
            
            try {
            	values.put(property.getName(), raw ? property.getRawObject(entity) : property.getObject(entity));
            }
            catch (Exception e) {
            	throw new RuntimeException("Could not get property " + property.getName() + " on entity " + entity);
            }
        }
        return values;
    }
}
