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

package org.granite.tide;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.amf.io.util.DefaultClassGetter;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.tide.async.AsyncPublisher;
import org.granite.tide.data.DataMergeContext;
import org.granite.tide.data.DataMergeContext.CacheKey;
import org.granite.util.ArrayUtil;


/**
 * @author William DRAI
 */
public abstract class TideServiceContext implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Logger log = Logger.getLogger(TideServiceContext.class);
    
    protected static final Object[] EMPTY_ARGS = new Object[0];
    
    public static final String COMPONENT_ATTR = "__TIDE_COMPONENT__";
    public static final String COMPONENT_CLASS_ATTR = "__TIDE_COMPONENT_CLASS__";
    
    private String sessionId = null;
    
    
    public TideServiceContext() throws ServiceException {
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public void initCall() {
    }
    
        
    public Object adjustInvokee(Object instance, String componentName, Set<Class<?>> componentClasses) {
    	return instance;
    }
    
    public Object[] beforeMethodSearch(Object instance, String methodName, Object[] args) {
    	return new Object[] { args[2], args[3] };
    }
    
    public abstract Object findComponent(String componentName, Class<?> componentClass);
    
    public abstract Set<Class<?>> findComponentClasses(String componentName, Class<?> componentClass);
    
    public abstract void prepareCall(ServiceInvocationContext context, IInvocationCall call, String componentName, Class<?> componentClass);
    
    public abstract IInvocationResult postCall(ServiceInvocationContext context, Object result, String componentName, Class<?> componentClass);
    
    public void postCallFault(ServiceInvocationContext context, Throwable t, String componentName, Class<?> componentClass) {
    }


    protected abstract AsyncPublisher getAsyncPublisher();
    
    
    public void sendEvent(String componentName, Class<?> componentClass) {
        AsyncPublisher publisher = getAsyncPublisher();
        if (publisher != null) {
            IInvocationResult eventResult = postCall(null, null, componentName, componentClass);
            publisher.publishMessage(sessionId, eventResult);
        }
    }
    
    
    /**
     *  Create a TidePersistenceManager
     *  
     *  @param create create if not existent (can be false for use in entity merge)
     *  @return a PersistenceContextManager
     */
    protected abstract TidePersistenceManager getTidePersistenceManager(boolean create);
    	
    
    public Object mergeExternal(Object obj, Object previous) {
        TidePersistenceManager pm = getTidePersistenceManager(false);
        ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        return mergeExternal(pm, classGetter, obj, previous, null, null);
    }
    
    @SuppressWarnings("unchecked")
    protected Object mergeExternal(TidePersistenceManager pm, ClassGetter classGetter, Object obj, Object previous, Object owner, String propertyName) {
        if (obj == null)
            return null;
        
        if (pm == null)
            return obj;
        
        if (!classGetter.isInitialized(owner, propertyName, obj)) {
            if (previous != null)
                return previous;
            return obj;
        }

        Map<Object, Object> cache = DataMergeContext.getCache();
        Object key = CacheKey.key(obj, owner, propertyName);
        Object prev = cache.get(key);
        Object next = obj;
        if (prev != null) {
            next = prev;
        }
        else if (obj instanceof Collection) {
            next = mergeCollection(pm, classGetter, (Collection<Object>)obj, previous, owner, propertyName);
        }
        else if (obj.getClass().isArray()) {
            next = mergeArray(pm, classGetter, obj, previous, owner, propertyName);
        }
        else if (obj instanceof Map) {
            next = mergeMap(pm, classGetter, (Map<Object, Object>)obj, previous, owner, propertyName);
        }
        else if (classGetter.isEntity(obj) || obj.getClass().isAnnotationPresent(ExternalizedBean.class)) {
            next = mergeEntity(pm, classGetter, obj, previous, owner, propertyName);
        }

        return next;
    }
    
    protected boolean equals(Object obj1, Object obj2) {
        // TODO: Should we add a check on class equality ???
    	if (obj1 instanceof IUID && obj2 instanceof IUID)
    		return ((IUID)obj1).getUid() != null && ((IUID)obj1).getUid().equals(((IUID)obj2).getUid());
    	
    	return obj1.equals(obj2);
    }

    private Object mergeEntity(TidePersistenceManager pm, ClassGetter classGetter, Object obj, Object previous, Object owner, String propertyName) {
        Object dest = obj;
        boolean isEntity = classGetter.isEntity(obj);
        
        boolean sameEntity = false;
        if (isEntity) {
        	Object p = DataMergeContext.getLoadedEntity(obj);
        	if (p != null) {
        		// We are sure here that the application has loaded the entity in the persistence context
        		// It's safe to merge the incoming entity
    			previous = p;
        	}
        }
        
		sameEntity = previous != null && equals(previous, obj);
        if (sameEntity)
            dest = previous;
        
        DataMergeContext.getCache().put(CacheKey.key(obj, null, null), dest);
        
        List<Object[]> fieldValues = isEntity ? classGetter.getFieldValues(obj, dest) : DefaultClassGetter.defaultGetFieldValues(obj, dest);
        // Merges field values
        try {
            for (Object[] fieldValue : fieldValues) {
                Field field = (Field)fieldValue[0];
                Object objv = fieldValue[1];
                Object destv = fieldValue[2];
                objv = mergeExternal(pm, classGetter, objv, destv, obj, field.getName());
            	field.set(dest, objv);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not merge entity ", e);
        }
        
        return dest;
    }

    private Object mergeCollection(TidePersistenceManager pm, ClassGetter classGetter, Collection<Object> coll, Object previous, Object owner, String propertyName) {
        if (log.isDebugEnabled())
            log.debug("Context mergeCollection: " + coll + (previous != null ? " previous " + previous.getClass().getName() : ""));

        Map<Object, Object> cache = DataMergeContext.getCache();
        Object key = CacheKey.key(coll, owner, propertyName);
        if (previous != null && previous instanceof Collection<?>)
            cache.put(key, previous);
        else
            cache.put(key, coll);
        
        @SuppressWarnings("unchecked")
        Collection<Object> prevColl = previous instanceof Collection ? (Collection<Object>)previous : null;
        
        if (coll == prevColl) {
        	for (Object obj : coll)
                mergeExternal(pm, classGetter, obj, obj, null, null);
        }
        else {
	        List<Object> addedToColl = new ArrayList<Object>();
	        Iterator<Object> icoll = coll.iterator();
	        for (int i = 0; i < coll.size(); i++) {
	            Object obj = icoll.next();
	            if (prevColl instanceof List<?>) {
	                boolean found = false;
	                List<Object> prevList = (List<Object>)prevColl;
	                for (int j = 0; j < prevList.size(); j++) {
	                    Object prev = prevList.get(j);
	                    if (prev != null && equals(prev, obj)) {
	                        obj = mergeExternal(pm, classGetter, obj, prev, null, null);
	                        if (i < prevList.size()) {
		                        if (j != i)
		                            prevList.set(j, prevList.get(i));
		                        
		                        if (obj != prevList.get(i))
		                        	prevList.set(i, obj);
	                        }
	                        else if (obj != prevList.get(j))
	                        	prevList.set(j, obj);
	                        
	                        found = true;
	                    }
	                }
	                if (!found) {
	                    obj = mergeExternal(obj, null);
	                    prevColl.add(obj);
	                }
	            }
	            else if (prevColl != null) {
	                boolean found = false;
	                Iterator<Object> iprevcoll = prevColl.iterator();
	                List<Object> added = new ArrayList<Object>();
	                for (int j = 0; j < prevColl.size(); j++) {
	                    Object prev = iprevcoll.next();
	                    if (prev != null && equals(prev, obj)) {
	                        obj = mergeExternal(pm, classGetter, obj, prev, null, null);
	                        if (obj != prev) {
	                            if (prevColl instanceof List<?>)
	                                ((List<Object>)prevColl).set(j, obj);
	                            else {
	                                iprevcoll.remove();
	                                added.add(obj);
	                            }
	                        }
	                        found = true;
	                    }
	                }
	                prevColl.addAll(added);
	                if (!found) {
	                    obj = mergeExternal(pm, classGetter, obj, null, null, null);
	                    prevColl.add(obj);
	                }
	            }
	            else {
	                obj = mergeExternal(obj, null);
	                if (icoll instanceof ListIterator<?>)
	                    ((ListIterator<Object>)icoll).set(obj);
	                else
	                    addedToColl.add(obj);
	            }
	        }
	        if (!addedToColl.isEmpty()) {
	        	coll.removeAll(addedToColl);	// Ensure that all entities are replaced by the merged ones 
	        	coll.addAll(addedToColl);
	        }
	        if (prevColl != null) {
	            Iterator<Object> iprevcoll = prevColl.iterator();
	            for (int i = 0; i < prevColl.size(); i++) {
	                Object obj = iprevcoll.next();
	                boolean found = false;
	                for (Object next : coll) {
	                    if (next != null && equals(next, obj)) {
	                        found = true;
	                        break;
	                    }
	                }
	                if (!found) {
	                    iprevcoll.remove();
	                    i--;
	                }
	            }
	
	            return previous;
	        }
        }

        return coll;
    }

    private Object mergeArray(TidePersistenceManager pm, ClassGetter classGetter, Object array, Object previous, Object owner, String propertyName) {
        if (log.isDebugEnabled())
            log.debug("Context mergeArray: " + array + (previous != null ? " previous " + previous.getClass().getName() : ""));

        Object key = CacheKey.key(array, owner, propertyName);
        int length = Array.getLength(array);
        Object prevArray = ArrayUtil.newArray(ArrayUtil.getComponentType(array.getClass()), length);
        DataMergeContext.getCache().put(key, prevArray);
        
        for (int i = 0; i < length; i++) {
            Object obj = Array.get(array, i);
            Array.set(prevArray, i, mergeExternal(pm, classGetter, obj, null, null, null));
        }

        return prevArray;
    }

    private Object mergeMap(TidePersistenceManager pm, ClassGetter classGetter, Map<Object, Object> map, Object previous, Object owner, String propertyName) {
        if (log.isDebugEnabled())
            log.debug("Context mergeMap: " + map + (previous != null ? " previous " + previous.getClass().getName() : ""));

        Map<Object, Object> cache = DataMergeContext.getCache();
        Object cacheKey = CacheKey.key(map, owner, propertyName);
        if (previous != null && previous instanceof Map<?, ?>)
            cache.put(cacheKey, previous);
        else
            cache.put(cacheKey, map);
        
        @SuppressWarnings("unchecked")
        Map<Object, Object> prevMap = previous instanceof Map ? (Map<Object, Object>)previous : null;
        
        if (map == prevMap) {
        	for (Map.Entry<Object, Object> me : map.entrySet()) {
        		mergeExternal(pm, classGetter, me.getKey(), null, null, null);
        		mergeExternal(pm, classGetter, me.getValue(), null, null, null);
        	}
        }
        else {
	        if (prevMap != null) {
	            if (map != prevMap) {
	                prevMap.clear();
	                for (Map.Entry<Object, Object> me : map.entrySet()) {
	                    Object key = mergeExternal(pm, classGetter, me.getKey(), null, null, null);
	                    Object value = mergeExternal(pm, classGetter, me.getValue(), null, null, null);
	                    prevMap.put(key, value);
	                }
	            }
	            
	            return prevMap;
	        }
	        
	        Set<Object[]> addedToMap = new HashSet<Object[]>();
	        for (Iterator<Map.Entry<Object, Object>> ime = map.entrySet().iterator(); ime.hasNext(); ) {
	            Map.Entry<Object, Object> me = ime.next();
	            ime.remove();
	            Object key = mergeExternal(pm, classGetter, me.getKey(), null, null, null);
	            Object value = mergeExternal(pm, classGetter, me.getValue(), null, null, null);
	            addedToMap.add(new Object[] { key, value });
	        }
	        for (Object[] me : addedToMap)
	            map.put(me[0], me[1]);
        }

        return map;
    }
    
    
    /**
     * Initialize the lazy property for the passed in entity.
     * @param entity the entity that has a lazy relationship
     * @param propertyNames the properties of the entity that has been marked lazy
     * @return the lazy collection
     */
    public Object lazyInitialize(Object entity, String[] propertyNames)  {
        TidePersistenceManager pm = getTidePersistenceManager(true);
        if (pm == null) {
            log.warn("No persistence manager found: lazy initialization ignored for " + entity);
            return entity;
        }
        
        return pm.attachEntity(entity, propertyNames);
    }
    
}
