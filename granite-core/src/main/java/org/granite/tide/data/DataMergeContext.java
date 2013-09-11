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

package org.granite.tide.data;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.tide.IUID;


/**
 * @author William DRAI
 */
public class DataMergeContext {
    
    private static ThreadLocal<DataMergeContext> dataMergeContext = new ThreadLocal<DataMergeContext>() {
        @Override
        protected DataMergeContext initialValue() {
            return new DataMergeContext();
        }
    };
    
    public static DataMergeContext get() {
        return dataMergeContext.get();
    }
    
    public static void remove() {
    	dataMergeContext.remove();
    }
    
    private final Map<Object, Object> cache = new HashMap<Object, Object>();
    private final Map<Object, Object> loadedEntities = new HashMap<Object, Object>();


    public static Map<Object, Object> getCache() {
    	return dataMergeContext.get().cache;
    }
    
    public static void addLoadedEntity(Object entity) {
    	DataMergeContext mergeContext = dataMergeContext.get();
    	mergeContext.entityLoaded(entity);
    }
    
    public static void addResultEntity(Object result) {
    	DataMergeContext mergeContext = dataMergeContext.get();
    	ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
    	mergeContext.addResultEntity(result, classGetter, new HashSet<Object>());
    }
    
    @SuppressWarnings("unchecked")
    public void addResultEntity(Object result, ClassGetter classGetter, Set<Object> cache) {
    	if (result == null || cache.contains(result))
    		return;
    	
    	cache.add(result);
    	
    	if (classGetter.isEntity(result))
    		addLoadedEntity(result);
    	
    	List<Object[]> values = classGetter.getFieldValues(result, result);
    	for (Object[] value : values) {
    		Object val = value[1];
    		if (val == null || val.getClass().isPrimitive())
    			continue;
        	if (!classGetter.isInitialized(result, ((Field)value[0]).getName(), val))
        		continue;
        		
    		if (val.getClass().isArray()) {
    			for (int i = 0; i < Array.getLength(val); i++)
    				addResultEntity(Array.get(val, i), classGetter, cache);
    		}
    		else if (val instanceof Collection) {
    			for (Iterator<?> i = ((Collection<?>)val).iterator(); i.hasNext(); )
    				addResultEntity(i.next(), classGetter, cache);
    		}
    		else if (val instanceof Map) {
    			for (Iterator<Map.Entry<Object, Object>> i = ((Map<Object, Object>)val).entrySet().iterator(); i.hasNext(); ) {
    				Map.Entry<Object, Object> me = i.next();
    				addResultEntity(me.getKey(), classGetter, cache);
    				addResultEntity(me.getValue(), classGetter, cache);
    			}
    		}
    		else
    			addResultEntity(val, classGetter, cache);
    	}
    }
    
    private void entityLoaded(Object entity) {
    	Object key = CacheKey.key(entity, null, null);
    	Object cachedEntity = cache.get(key);
    	if (cachedEntity != null) { // && cachedEntity != entity) {
    		// Entity has already been merged before, replace current merge context by the one from JPA
    		cache.clear();
    	}
    	
    	if (entity instanceof IUID)
    		loadedEntities.put(entity.getClass().getName() + "-" + ((IUID)entity).getUid(), entity);
    	else
    		loadedEntities.put(entity, entity);
    }
    
    public static Object getLoadedEntity(Object entity) {
    	if (entity instanceof IUID)
    		return dataMergeContext.get().loadedEntities.get(entity.getClass().getName() + "-" + ((IUID)entity).getUid());
    	return dataMergeContext.get().loadedEntities.get(entity);
    }
    
    public static Collection<Object> getLoadedEntities() {
    	return dataMergeContext.get().loadedEntities.values();
    }
    
    public static void restoreLoadedEntities(Set<Object> loadedEntities) {
    	DataMergeContext mergeContext = dataMergeContext.get();
    	for (Object entity : loadedEntities) {
    		if (entity instanceof IUID)
    			mergeContext.loadedEntities.put(entity.getClass().getName() + "-" + ((IUID)entity).getUid(), entity);
    		else
    			mergeContext.loadedEntities.put(entity, entity);
    	}
    }
    
    
    public static class CacheKey {
        
        public static Object key(Object obj, Object owner, String propertyName) {
            if (obj instanceof IUID)
            	return new UIDKey(obj.getClass(), ((IUID)obj).getUid());
            if (owner != null && (obj instanceof Collection<?> || obj instanceof Map<?, ?>))
                return new CollectionKey(owner, propertyName);
            return obj;
        }
    }
    
    public static class UIDKey extends CacheKey {
    	
    	private Class<?> clazz;
    	private String uid;
    	
    	public UIDKey(Class<?> clazz, String uid) {
    		this.clazz = clazz;
    		this.uid = uid;
    	}
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(UIDKey.class))
                return false;
            return this.clazz.equals(((UIDKey)obj).clazz) && this.uid.equals(((UIDKey)obj).uid);
        }
        
        @Override
        public int hashCode() {
            return (clazz.getName() + "-" + uid).hashCode();
        }
    }
    
    public static class CollectionKey extends CacheKey {
        
        private Object owner;
        private String propertyName;
        
        public CollectionKey(Object owner, String propertyName) {
            this.owner = owner;
            this.propertyName = propertyName;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(CollectionKey.class))
                return false;
            return this.owner.equals(((CollectionKey)obj).owner) && this.propertyName.equals(((CollectionKey)obj).propertyName);
        }
        
        @Override
        public int hashCode() {
            return owner.hashCode()*31 + propertyName.hashCode();
        }
    }
}
