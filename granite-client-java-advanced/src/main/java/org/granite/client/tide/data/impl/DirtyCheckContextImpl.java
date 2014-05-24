/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.data.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.tide.data.Value;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.DataManager.ChangeKind;
import org.granite.client.tide.data.spi.DirtyCheckContext;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.data.spi.Wrapper;
import org.granite.client.util.PropertyHolder;
import org.granite.client.util.WeakIdentityHashMap;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class DirtyCheckContextImpl implements DirtyCheckContext {
    
    private static Logger log = Logger.getLogger("org.granite.client.tide.data.DirtyCheckContextImpl");
    
    private DataManager dataManager;
    private int dirtyCount = 0;
    private WeakIdentityHashMap<Object, Map<String, Object>> savedProperties = new WeakIdentityHashMap<Object, Map<String, Object>>();
    private WeakIdentityHashMap<Object, Object> unsavedEntities = new WeakIdentityHashMap<Object, Object>();
    
    
    public DirtyCheckContextImpl(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    private boolean isEntity(Object obj) {
    	return dataManager.isEntity(obj);
    }
    
    public boolean isDirty() {
        return dirtyCount > 0;
    }
    
    public void notifyDirtyChange(boolean oldDirty) {
        if (isDirty() == oldDirty)
            return;
        
        dataManager.notifyDirtyChange(oldDirty, isDirty());
    }
    
    public boolean notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity) {
        boolean newDirtyEntity = isEntityChanged(entity);
        if (newDirtyEntity != oldDirtyEntity)
            dataManager.notifyEntityDirtyChange(entity, oldDirtyEntity, newDirtyEntity);
        return newDirtyEntity;
    }

    public Map<Object, Map<String, Object>> getSavedProperties() {
        return savedProperties;
    }

    public Map<String, Object> getSavedProperties(Object entity) {
        return savedProperties.get(entity);
    }
    
    public boolean isSaved(Object entity) {
        return savedProperties.containsKey(entity);
    }
	
	/**
	 *  Check if the object is marked as new in the context
	 *
	 *  @param object object to check
	 * 
	 *  @return true if the object has been newly attached
	 */ 
	public boolean isUnsaved(Object object) {
		return unsavedEntities.containsKey(object);
	}
	
	public void addUnsaved(Object entity) {
		unsavedEntities.put(entity, true);
	}
    
    public void clear(boolean notify) {
        boolean wasDirty = isDirty();
        dirtyCount = 0;
        savedProperties.clear();
        if (notify)
            notifyDirtyChange(wasDirty);
    }
    
    /**
     *  Check if entity property has been changed since last remote call
     *
     *  @param entity entity to check
     *  @param propertyName property to check
     *  @param value current value to compare with saved value
     *   
     *  @return true is value has been changed
     */ 
	public boolean isEntityPropertyChanged(Object entity, String propertyName, Object value) {
        Map<String, Object> source = savedProperties.get(entity);
        if (source != null)
        	return source.containsKey(propertyName) && !isSame(source.get(propertyName), value);
       	
		return !isSame(dataManager.getPropertyValue(entity, propertyName), value);
    }
    
    
    public boolean isEntityChanged(Object entity) {
        return isEntityChanged(entity, null, null, null);
    }
    
    /**
     *  Check if entity has changed since last save point
     *
     *  @param entity entity to check
     *  @param propName property name
     *  @param value
     *   
     *  @return entity is dirty
     */ 
    @SuppressWarnings("unchecked")
	public boolean isEntityChanged(Object entity, Object embedded, String propName, Object value) {
		if (!dataManager.isInitialized(entity))
			return false;
		
        boolean dirty = false;

        Map<String, Object> pval = dataManager.getPropertyValues(entity, true, false);

        Map<String, Object> save = savedProperties.get(entity);

        if (embedded == null)
            embedded = entity;

        for (String p : pval.keySet()) {
            Object val = (entity == embedded && p.equals(propName)) ? value : pval.get(p);
            Object saveval = save != null ? save.get(p) : null;

            if (save != null && ((val != null && (ObjectUtil.isSimple(val) || val instanceof byte[]))
                    || (saveval != null && (ObjectUtil.isSimple(saveval) || saveval instanceof byte[])))) {
                dirty = true;
                break;
            }
            else if (save != null && (val instanceof Value || saveval instanceof Value || val instanceof Enum || saveval instanceof Enum)) {
                if (saveval != null && ((val == null && saveval != null) || !val.equals(saveval))) {
                    dirty = true;
                    break;
                }
            }
            else if (save != null && (isEntity(val) || isEntity(saveval))) {
                if (saveval != null && val != save.get(p)) {
                    dirty = true;
                    break;
                }
            }
            else if (val instanceof Collection<?> || val instanceof Map<?, ?>) {
                if (!dataManager.isInitialized(val))
                    continue;

                List<Change> savedArray = (List<Change>)saveval;
                if (savedArray != null && !savedArray.isEmpty()) {
                    dirty = true;
                    break;
                }
            }
            else if (val != null
                && !(isEntity(val) || ObjectUtil.isSimple(val) || val instanceof Enum || val instanceof Value || val instanceof byte[])
                && isEntityChanged(val)) {
                dirty = true;
                break;
            }
        }
        return dirty;
    }
	
	public boolean isEntityDeepChanged(Object entity) {		
		return isEntityDeepChanged(entity, null, new IdentityHashMap<Object, Boolean>());
	}
	
	private boolean isEntityDeepChanged(Object entity, Object embedded, IdentityHashMap<Object, Boolean> cache) {
		if (cache == null)
			cache = new IdentityHashMap<Object, Boolean>();
		if (cache.containsKey(entity))
			return false;
		cache.put(entity, true);
		
		if (!dataManager.isInitialized(entity))
			return false;
		
        Map<String, Object> pval = dataManager.getPropertyValues(entity, true, false);

        if (embedded == null)
            embedded = entity;

        Map<String, Object> save = savedProperties.get(entity);

        for (String p : pval.keySet()) {
            Object val = pval.get(p);
            Object saveval = save != null ? save.get(p) : null;

            if (save != null && ((val != null && (ObjectUtil.isSimple(val) || val instanceof byte[]))
                    || (saveval != null && (ObjectUtil.isSimple(saveval) || saveval instanceof byte[])))) {
                return true;
            }
            else if (save != null && (val instanceof Value || saveval instanceof Value || val instanceof Enum || saveval instanceof Enum)) {
                if (saveval != null && ((val == null && saveval != null) || !val.equals(saveval))) {
                    return true;
                }
            }
            else if (isEntity(val) || isEntity(saveval)) {
                if (save != null && val != saveval)
                    return true;
                
                if (isEntityDeepChanged(val, null, cache))
                    return true;
            }
            else if (val instanceof Collection<?> || val instanceof Map<?, ?>) {
                 if (!dataManager.isInitialized(val))
                     continue;

                 @SuppressWarnings("unchecked")
                 List<Change> savedArray = (List<Change>)saveval;
                 if (savedArray != null && !savedArray.isEmpty())
                     return true;

                 if (val instanceof Collection<?>) {
                     for (Object elt : (Collection<?>)val) {
                         if (isEntityDeepChanged(elt, null, cache))
                             return true;
                     }
                 }
                 else if (val instanceof Map<?, ?>) {
                    for (Entry<?, ?> me : ((Map<?, ?>)val).entrySet()) {
                        if (isEntityDeepChanged(me.getKey(), null, cache))
                            return true;
                        if (isEntityDeepChanged(me.getValue(), null, cache))
                            return true;
                    }
                }
            }
            else if (val != null
                && !(isEntity(val) || ObjectUtil.isSimple(val) || val instanceof Enum || val instanceof Value || val instanceof byte[])
                && isEntityDeepChanged(val, embedded, cache)) {
                return true;
            }
        }

		return false;
	}


    private boolean isSame(Object val1, Object val2) {
        if (val1 == null && isEmpty(val2))
            return true;
        else if (val2 == null && isEmpty(val1))
            return true;
        else if (ObjectUtil.isSimple(val1) && ObjectUtil.isSimple(val2))
            return val1.equals(val2);
        else if (val1 instanceof byte[] && val2 instanceof byte[])
            return Arrays.equals((byte[])val1, (byte[])val2);
        else if ((val1 instanceof Value && val2 instanceof Value) || (val1 instanceof Enum && val2 instanceof Enum))
            return val1.equals(val2);
        
        Object n = val1 instanceof Wrapper ? ((Wrapper)val1).getWrappedObject() : val1;
        Object o = val2 instanceof Wrapper ? ((Wrapper)val2).getWrappedObject() : val2;
        if (isEntity(n) && isEntity(o))
            return dataManager.getUid(n) != null && dataManager.getUid(n).equals(dataManager.getUid(o));
        return n == o;
    }

    private boolean isSameList(List<Object> save, Collection<?> coll) {
    	if (save.size() != coll.size())
    		return false;

    	if (coll instanceof List<?>) {
    		List<?> list = (List<?>)coll;
	        for (int i = 0; i < save.size(); i++) {
	        	if (!isSame(save.get(i), list.get(i)))
	        		return false;
	        }
    	}
    	else {
            for (int i = 0; i < save.size(); i++) {
                boolean found = false;
                for (Iterator<?> ic = coll.iterator(); ic.hasNext(); ) {
                    Object obj = ic.next();
                    if (isSame(save.get(i), obj)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
            }
    	}
        return true;
    }    
    
    private boolean isSameMap(List<Object[]> save, Map<?, ?> map) {
    	if (save.size() != map.size())
    		return false;

        for (int i = 0; i < save.size(); i++) {
        	Object[] entry = save.get(i);
        	if (!map.containsKey(entry[0]))
        		return false;
        	if (!isSame(entry[1], map.get(entry[0])))
        		return false;
        }
        return true;
    }
    
    private boolean isSameExt(Object val1, Object val2) {
        if (val1 == null && isEmpty(val2))
            return true;
        else if (val2 == null && isEmpty(val1))
            return true;
        else if (ObjectUtil.isSimple(val1) && ObjectUtil.isSimple(val2))
            return val1.equals(val2);
        else if (val1 instanceof byte[] && val2 instanceof byte[])
            return Arrays.equals((byte[])val1, (byte[])val2);
        else if ((val1 instanceof Value && val2 instanceof Value) || (val1 instanceof Enum && val2 instanceof Enum))
            return val1.equals(val2);
        else if (val1 != null && val1.getClass().isArray() && val2 != null && val2.getClass().isArray()) {
            if (Array.getLength(val1) != Array.getLength(val2))
                return false;
            for (int idx = 0; idx < Array.getLength(val1); idx++) {
                if (!isSameExt(Array.get(val1, idx), Array.get(val2, idx)))
                    return false;
            }
            return true;
        }
        else if (val1 instanceof Set<?> && val2 instanceof Set<?>) {
			if ((val1 instanceof PersistentCollection && !((PersistentCollection)val1).wasInitialized()) 
					|| (val2 instanceof PersistentCollection && !((PersistentCollection)val2).wasInitialized()))
				return false;
            Collection<?> coll1 = (Collection<?>)val1;
            Collection<?> coll2 = (Collection<?>)val2;
            if (coll1.size() != coll2.size())
                return false;
            for (Object e : coll1) {
                boolean found = false;
                for (Object f : coll2) {
                    if (isSameExt(e, f)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
            }
            for (Object e : coll2) {
                boolean found = false;
                for (Object f : coll1) {
                    if (isSameExt(e, f)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
            }
            return true;
        }
        else if (val1 instanceof List<?> && val2 instanceof List<?>) {
			if ((val1 instanceof PersistentCollection && !((PersistentCollection)val1).wasInitialized()) 
					|| (val2 instanceof PersistentCollection && !((PersistentCollection)val2).wasInitialized()))
				return false;
            List<?> list1 = (List<?>)val1;
            List<?> list2 = (List<?>)val2;
            if (list1.size() != list2.size())
                return false;
            for (int idx = 0; idx < list1.size(); idx++) {
                if (!isSameExt(list1.get(idx), list2.get(idx)))
                    return false;
            }
            return true;
        }
        else if (val1 instanceof Collection<?> && val2 instanceof Collection<?>) {
            if ((val1 instanceof PersistentCollection && !((PersistentCollection)val1).wasInitialized()) 
                    || (val2 instanceof PersistentCollection && !((PersistentCollection)val2).wasInitialized()))
                return false;
            Collection<?> coll1 = (Collection<?>)val1;
            Collection<?> coll2 = (Collection<?>)val2;
            if (coll1.size() != coll2.size())
                return false;
            for (Object obj1 : coll1) {
                boolean found = false;
                for (Object obj2 : coll2) {
                    if (isSameExt(obj1, obj2)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
            }
            for (Object obj2 : coll2) {
                boolean found = false;
                for (Object obj1 : coll1) {
                    if (isSameExt(obj1, obj2)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
            }
            return true;
        }
        else if (val1 instanceof Map<?, ?> && val2 instanceof Map<?, ?>) {
			if ((val1 instanceof PersistentCollection && !((PersistentCollection)val1).wasInitialized()) 
					|| (val2 instanceof PersistentCollection && !((PersistentCollection)val2).wasInitialized()))
				return false;
            Map<?, ?> map1 = (Map<?, ?>)val1;
            Map<?, ?> map2 = (Map<?, ?>)val2;
            if (map1.size() != map2.size())
                return false;
            for (Object e : map1.keySet()) {
            	Object key = null;
                for (Object f : map2.keySet()) {
                    if (isSameExt(e, f)) {
                        key = f;
                        break;
                    }
                }
                if (key == null)
                    return false;
                if (!isSameExt(map1.get(e), map2.get(key)))
                    return false;
            }
            for (Object f : map2.keySet()) {
            	Object key = null;
                for (Object e : map1.keySet()) {
                    if (isSameExt(e, f)) {
                        key = e;
                        break;
                    }
                }
                if (key == null)
                    return false;
                if (!isSameExt(map1.get(key), map2.get(f)))
                    return false;
            }
            return true;
        }

        Object n = val1 instanceof Wrapper ? ((Wrapper)val1).getWrappedObject() : val1;
        Object o = val2 instanceof Wrapper ? ((Wrapper)val2).getWrappedObject() : val2;
        if (isEntity(n) && isEntity(o))
            return dataManager.getUid(n).equals(dataManager.getUid(o));
        
        return n == o;
    }
    
    /**
     *  Interceptor for managed entity setters
     *
     *  @param entity entity to intercept
     *  @param propName property name
     *  @param oldValue old value
     *  @param newValue new value
     */ 
    public void entityPropertyChangeHandler(Object entity, Object object, String propName, Object oldValue, Object newValue) {
        boolean oldDirty = isDirty();
        
        boolean diff = !isSame(oldValue, newValue);
        
        if (diff) {
            boolean oldDirtyEntity = isEntityChanged(entity, object, propName, oldValue);
            
            String versionPropertyName = dataManager.isEntity(entity) ? dataManager.getVersionPropertyName(entity) : null;
            Map<String, Object> save = savedProperties.get(object);
            boolean unsaved = save == null;
            
            if (unsaved || (versionPropertyName != null && 
                (save.get(versionPropertyName) != dataManager.getVersion(entity) 
                    && !(save.get(versionPropertyName) == null && dataManager.getVersion(entity) == null)))) {
                
                save = new HashMap<String, Object>();
                if (versionPropertyName != null)
                    save.put(versionPropertyName, dataManager.getVersion(entity));
                savedProperties.put(object, save);
                save.put(propName, oldValue);
                if (unsaved)
                    dirtyCount++;
            }
            
            if (save != null && (versionPropertyName == null 
                || save.get(versionPropertyName) == dataManager.getVersion(entity)
                || (save.get(versionPropertyName) == null && dataManager.getVersion(entity) == null))) {
                
                if (!save.containsKey(propName))
                    save.put(propName, oldValue);
                
                if (isSame(save.get(propName), newValue)) {
                    save.remove(propName);
                    int count = 0;
                    for (String p : save.keySet()) {
                        if (!p.equals(versionPropertyName))
                            count++;
                    }
                    if (count == 0) {
                        savedProperties.remove(object);
                        dirtyCount--;
                    }
                }
            }
            
            notifyEntityDirtyChange(entity, oldDirtyEntity);
        }
        
        notifyDirtyChange(oldDirty);
    }


    /**
     *  Collection event handler to save changes on managed collections
     *
     *  @param owner owner entity of the collection
     *  @param propName property name of the collection
     *  @param coll collection
     *  @param kind change kind
     *  @param location change location
     *  @param items changed items
     */ 
    @SuppressWarnings("unchecked")
	public void entityCollectionChangeHandler(Object owner, String propName, Collection<?> coll, ChangeKind kind, Integer location, Object[] items) {
        boolean oldDirty = isDirty();
        
        String versionPropertyName = dataManager.isEntity(owner) ? dataManager.getVersionPropertyName(owner) : null;
        boolean oldDirtyEntity = isEntityChanged(owner);
        
        Map<String, Object> esave = savedProperties.get(owner);
        boolean unsaved = esave == null;
        
        if (unsaved || (versionPropertyName != null && 
            (esave.get(versionPropertyName) != dataManager.getVersion(owner) 
                && !(esave.get(versionPropertyName) == null && dataManager.getVersion(owner) == null)))) {

            esave = new HashMap<String, Object>();
            if (versionPropertyName != null)
                esave.put(versionPropertyName, dataManager.getVersion(owner));
            savedProperties.put(owner, esave);
            if (unsaved)
                dirtyCount++;
        }
    
        List<Object> save = (List<Object>)esave.get(propName);
        if (save == null) {
            save = new ArrayList<Object>();
            esave.put(propName, save);
						
			// Save collection snapshot
			for (Object e : coll)
				save.add(e);
			
			// Adjust with last event
			if (kind == ChangeKind.ADD) {
				if (location != null) {
					for (int i = 0; i < items.length; i++)
						save.remove(location.intValue());
				}
				else {
					for (Object item : items)
						save.remove(item);
				}
			}
			else if (kind == ChangeKind.REMOVE) {
				if (location != null) {
					for (int i = 0; i < items.length; i++)
						save.add(location.intValue()+i, items[i]);
				}
				else {
					for (Object item : items)
						save.add(item);
				}
			}
			else if (kind == ChangeKind.REPLACE) {
				if (location != null)
					save.set(location.intValue(), ((Object[])items[0])[0]);
				else {
					save.remove(((Object[])items[0])[1]);
					save.add(((Object[])items[0])[0]);
				}
			}
        }
		else {
			if (isSameList(save, coll)) {
                esave.remove(propName);
                int count = 0;
                for (Object p : esave.keySet()) {
                    if (!p.equals(versionPropertyName))
                        count++;
                }
                if (count == 0) {
                    savedProperties.remove(owner);
                    dirtyCount--;
                }
			}
		}
        
        notifyEntityDirtyChange(owner, oldDirtyEntity);
        
        notifyDirtyChange(oldDirty);
    }


    /**
     *  Map event handler to save changes on managed maps
     *
     *  @param owner owner entity of the map
     *  @param propName property name of the map
     *  @param map map
     *  @param kind change kind
     *  @param items changed items
     */ 
    @SuppressWarnings("unchecked")
	public void entityMapChangeHandler(Object owner, String propName, Map<?, ?> map, ChangeKind kind, Object[] items) {
        boolean oldDirty = isDirty();
        
        String versionPropertyName = dataManager.isEntity(owner) ? dataManager.getVersionPropertyName(owner) : null;
        boolean oldDirtyEntity = isEntityChanged(owner);
        
        Map<String, Object> esave = savedProperties.get(owner);
        boolean unsaved = esave == null;
        
        if (unsaved || (versionPropertyName != null && 
            (esave.get(versionPropertyName) != dataManager.getVersion(owner) 
                && !(esave.get(versionPropertyName) == null && dataManager.getVersion(owner) == null)))) {

            esave = new HashMap<String, Object>();
            if (versionPropertyName != null)
                esave.put(versionPropertyName, dataManager.getVersion(owner));
            savedProperties.put(owner, esave);
            if (unsaved)
                dirtyCount++;
        }
        
        List<Object[]> save = (List<Object[]>)esave.get(propName);
        if (save == null) {
            save = new ArrayList<Object[]>();
            esave.put(propName, save);

			// Save map snapshot
			for (Entry<?, ?> entry : map.entrySet()) {
				boolean found = false;
				if (kind == ChangeKind.ADD) {
					for (Object item : items) {
						if (isSame(entry.getKey(), ((Object[])item)[0])) {
							found = true;
							break;
						}
					}
				}
				else if (kind == ChangeKind.REPLACE) {
					for (Object item : items) {
						if (isSame(entry.getKey(), ((Object[])item)[0])) {
							save.add(new Object[] { entry.getKey(), ((Object[])item)[1] });
							found = true;
							break;
						}
					}
				}
				if (!found)
					save.add(new Object[] { entry.getKey(), entry.getValue() });
			}
			
			// Add removed element if needed
			if (kind == ChangeKind.REMOVE) {
				for (Object item : items)
					save.add(new Object[] { ((Object[])item)[0], ((Object[])item)[1] });
			}
        }
		else {
			if (isSameMap(save, map)) {
                esave.remove(propName);
                int count = 0;
                for (Object p : esave.keySet()) {
                    if (!p.equals(versionPropertyName))
                        count++;
                }
                if (count == 0) {
                    savedProperties.remove(owner);
                    dirtyCount--;
                }
			}
		}
        
        notifyEntityDirtyChange(owner, oldDirtyEntity);
        
        notifyDirtyChange(oldDirty);
    }


    /**
     *  Mark an object merged from the server as not dirty
     *
     *  @param object merged object
     *  @param entity owner entity (when handling embedded objects)
     */ 
    public void markNotDirty(Object object, Object entity) {
    	if (entity != null)
    		unsavedEntities.remove(entity);
    	
        if (!savedProperties.containsKey(object))
            return;
        
        boolean oldDirty = isDirty();
        
        boolean oldDirtyEntity = false;
        if (entity == null && isEntity(object))
            entity = object;
        if (entity != null)
            oldDirtyEntity = isEntityChanged(entity);
        
        savedProperties.remove(object);
        
        if (entity != null)
            notifyEntityDirtyChange(entity, oldDirtyEntity);
        
        dirtyCount--;

        notifyDirtyChange(oldDirty);
    }
    
    
    /**
     *  Check if dirty properties of an object are the same than those of another entity
     *  When they are the same, unmark the dirty flag
     *
     *	@param mergeContext current merge context
     *  @param entity merged entity
     *  @param source source entity
     *  @param parent owner entity for embedded objects
     *  @return true if the entity is still dirty after comparing with incoming object
     */ 
    public boolean checkAndMarkNotDirty(MergeContext mergeContext, Object entity, Object source, Object parent) {
    	if (entity != null)
    		unsavedEntities.remove(entity);
    	
    	Map<String, Object> save = savedProperties.get(entity);
        if (save == null)
            return false;
        
		Object owner = isEntity(entity) ? entity : parent;
		
        boolean oldDirty = isDirty();
        boolean oldDirtyEntity = isEntityChanged(owner);
        
		List<String> merged = new ArrayList<String>();
		
        String versionPropertyName = dataManager.getVersionPropertyName(owner);
		
		if (isEntity(source) && versionPropertyName != null)
			save.put(versionPropertyName, dataManager.getVersion(source));
		
		Map<String, Object> pval = dataManager.getPropertyValues(entity, false, false);
		for (String propName : pval.keySet()) {
			if (propName.equals(versionPropertyName) || propName.equals("dirty"))
				continue;
			
			if (!dataManager.hasProperty(source, propName))
				continue;
			
			Object localValue = pval.get(propName);
			if (localValue instanceof PropertyHolder)
				localValue = ((PropertyHolder)localValue).getObject();
			
			Object sourceValue = dataManager.getPropertyValue(source, propName);
			
			if (isSameExt(sourceValue, localValue)) {
				merged.add(propName);
				continue;
			}
			
			if (sourceValue == null || ObjectUtil.isSimple(sourceValue) || sourceValue instanceof Value || sourceValue instanceof Enum) {
				save.put(propName, sourceValue);
			}
			else if (isEntity(sourceValue)) {
				save.put(propName, mergeContext.getFromCache(sourceValue));
			}
			else if (sourceValue instanceof Collection<?> && !(sourceValue instanceof PersistentCollection && !((PersistentCollection)sourceValue).wasInitialized())) {
				List<Object> snapshot = new ArrayList<Object>((Collection<?>)sourceValue);
				save.put(propName, snapshot);
			}
			else if (sourceValue instanceof Map<?, ?> && !(sourceValue instanceof PersistentCollection && !((PersistentCollection)sourceValue).wasInitialized())) {
				Map<?, ?> map = (Map<?, ?>)sourceValue;
				List<Object[]> snapshot = new ArrayList<Object[]>(map.size());
				for (Entry<?, ?> entry : map.entrySet())
					snapshot.add(new Object[] { entry.getKey(), entry.getValue() });
				save.put(propName, snapshot);
			}
		}
        
        for (String propName : merged)
            save.remove(propName);
        
        int count = 0;
        for (String propName : save.keySet()) {
            if (!propName.equals(versionPropertyName))
                count++;
        }
        if (count == 0) {
            savedProperties.remove(entity);
            dirtyCount--;
        }
        
        boolean newDirtyEntity = notifyEntityDirtyChange(owner, oldDirtyEntity);
        
        notifyDirtyChange(oldDirty);
        
        return newDirtyEntity;
    }
	
	
	public void fixRemovalsAndPersists(MergeContext mergeContext, List<Object> removals, List<Object> persists) {
		boolean oldDirty = dirtyCount > 0;
		
		for (Object object : savedProperties.keySet()) {
			Object owner = null;
			if (isEntity(object))
				owner = object;
			else {
				Object[] ownerEntity = mergeContext.getOwnerEntity(object);
				if (ownerEntity != null && isEntity(ownerEntity[0]))
					owner = ownerEntity[0];
			}
			
			String versionPropertyName = dataManager.getVersionPropertyName(owner);
			
			boolean oldDirtyEntity = isEntityChanged(object);
			
	    	Map<String, Object> save = savedProperties.get(object);
			
	    	Iterator<String> ip = save.keySet().iterator();
			while (ip.hasNext()) {
				String p = ip.next();
				Object sn = save.get(p);
				if (!(sn instanceof List<?>))
					continue;
				
				Object value = dataManager.getPropertyValue(object, p);
				if (value instanceof Collection<?>) {
					if (value instanceof PersistentCollection && !((PersistentCollection)value).wasInitialized())
						continue;
					
					@SuppressWarnings("unchecked")
					List<Object> snapshot = (List<Object>)sn;
					Collection<?> coll = (Collection<?>)value;
					if (removals != null) {
						Iterator<Object> isne = snapshot.iterator();
						while (isne.hasNext()) {
							Object sne = isne.next();
							for (Object removal : removals) {
								if (isEntity(sne) && ObjectUtil.objectEquals(dataManager, sne, removal))
									isne.remove();
							}
						}
					}
					if (persists != null) {
						for (Object persist : persists) {
							if (coll instanceof List<?>) {
								List<?> list = (List<?>)coll;
								List<Integer> found = new ArrayList<Integer>();
								for (int j = 0; j < list.size(); j++) {
									if (ObjectUtil.objectEquals(dataManager, list.get(j), persist))
										found.add(j);
								}
								for (int j = 0; j < snapshot.size(); j++) {
									if (ObjectUtil.objectEquals(dataManager, persist, snapshot.get(j))) {
										snapshot.remove(j);
										j--;
									}
								}
								for (int idx : found)
									snapshot.add(idx, persist);
							}
							else {
								if (coll.contains(persist) && !snapshot.contains(persist))
									snapshot.add(persist);
							}
						}
					}
					
					if (isSameList(snapshot, coll))
						ip.remove();
				}
				else if (value instanceof Map<?, ?>) {
					if (value instanceof PersistentCollection && !((PersistentCollection)value).wasInitialized())
						continue;
					
					@SuppressWarnings("unchecked")
					List<Object[]> snapshot = (List<Object[]>)sn;
					Map<?, ?> map = (Map<?, ?>)value;
					if (removals != null) {
						Iterator<Object[]> isne = snapshot.iterator();
						while (isne.hasNext()) {
							Object[] sne = isne.next();
							for (Object removal : removals) {
								if (isEntity(sne[0]) && ObjectUtil.objectEquals(dataManager, sne[0], removal))
									isne.remove();
								else if (isEntity(sne[1]) && ObjectUtil.objectEquals(dataManager, sne[1], removal))
									isne.remove();
							}
						}
					}
					// TODO: persist ?				
//					if (persists != null) {
//						for (Object persist : persists) {
//							boolean foundKey = false;
//							List<Object> foundValues = new ArrayList<Object>();
//							Iterator<Object[]> isne = snapshot.iterator();
//							while (isne.hasNext()) {
//								Object[] sne = isne.next();
//								if (ObjectUtil.objectEquals(dataManager, sne[0], persist))
//									foundKey = true;
//								if (ObjectUtil.objectEquals(dataManager, sne[1], persist)) {
//									foundValues.add(sne[0]);
//									isne.remove();
//								}
//							}
//							if (map.containsKey(persist) && !foundKey)
//								snapshot.add(new Object[] { persist, map.get(persist) });
//							if (map.containsValue(persist)) {
//								for (Entry<?, ?> e : map.entrySet()) {
//									if (e.getValue().equals(persist) && !e.getKey().equals(persist))
//										snapshot.add(new Object[] { e.getKey(), e.getValue() });
//								}
//							}
//						}
//					}
					
					if (isSameMap(snapshot, map))
						ip.remove();
				}
			}
			
            int count = 0;
            for (Object p : save.keySet()) {
                if (!p.equals(versionPropertyName))
                    count++;
            }
            if (count == 0) {
                savedProperties.remove(object);
                dirtyCount--;
            }
			
			notifyEntityDirtyChange(object, oldDirtyEntity);
		}
		
		notifyDirtyChange(oldDirty);
	}
    
    
    /**
     *  Internal implementation of entity reset
     */ 
    @SuppressWarnings("unchecked")
	public void resetEntity(MergeContext mergeContext, Object entity, Object parent, Set<Object> cache) {
        // Should not try to reset uninitialized entities
        if (!dataManager.isInitialized(entity))
            return;
        
        if (cache.contains(entity))
            return;
        cache.add(entity);
        
        Map<String, Object> save = savedProperties.get(entity);
        
        Map<String, Object> pval = dataManager.getPropertyValues(entity, true, false);
        
        for (String p : pval.keySet()) {
            Object val = pval.get(p);
            
            if (val instanceof Collection<?> && dataManager.isInitialized(val)) {
                Collection<Object> coll = (Collection<Object>)val;
                List<Object> savedArray = save != null ? (List<Object>)save.get(p) : null;
                
                if (savedArray != null) {
                	for (Object obj : coll) {
                		if (isEntity(obj))
                			resetEntity(mergeContext, obj, parent, cache);
                	}
                	coll.clear();
                	for (Object e : savedArray)
                		coll.add(e);
                    
                    // Must be here because collection reset may have triggered other useless collection change events
                    markNotDirty(val, parent);
                }
                
                for (Object o : coll) {
                    if (isEntity(o))
                        resetEntity(mergeContext, o, o, cache);
                }
            }
            else if (val instanceof Map<?, ?> && dataManager.isInitialized(val)) {
                Map<Object, Object> map = (Map<Object, Object>)val;
                List<Object[]> savedArray = save != null ? (List<Object[]>)save.get(p) : null;
                
                if (savedArray != null) {
                	for (Entry<Object, Object> entry : map.entrySet()) {
                		if (isEntity(entry.getKey()))
                			resetEntity(mergeContext, entry.getKey(), parent, cache);
                		if (isEntity(entry.getValue()))
                			resetEntity(mergeContext, entry.getValue(), parent, cache);
		            }
            		map.clear();
            		for (Object[] e : savedArray)
            			map.put(e[0], e[1]);
                    
                    // Must be here because collection reset has triggered other useless CollectionEvents
                    markNotDirty(val, parent);
                }
                
                for (Entry<Object, Object> me : map.entrySet()) {
                    if (isEntity(me.getKey()))
                        resetEntity(mergeContext, me.getKey(), me.getKey(), cache);
                    if (isEntity(me.getValue()))
                        resetEntity(mergeContext, me.getValue(), me.getValue(), cache);
                }
            }
            else if (save != null && (ObjectUtil.isSimple(val) || ObjectUtil.isSimple(save.get(p)))) {
                if (save.containsKey(p))
                    dataManager.setPropertyValue(entity, p, save.get(p));
            }
            else if (save != null && (val instanceof Enum || save.get(p) instanceof Enum || val instanceof Value || save.get(p) instanceof Value)) {
                if (save.containsKey(p))
                    dataManager.setPropertyValue(entity, p, save.get(p));
            } 
            else if (save != null && save.containsKey(p)) {
                if (!ObjectUtil.objectEquals(dataManager, val, save.get(p)))
                    dataManager.setPropertyValue(entity, p, save.get(p));
            }
            else if (isEntity(val))
                resetEntity(mergeContext, val, val, cache);
            else if (val != null && parent != null && !ObjectUtil.isSimple(val) && !(val instanceof Enum))
                resetEntity(mergeContext, val, parent, cache);
        }
        
        // Must be here because entity reset may have triggered useless new saved properties
        markNotDirty(entity, null);
    }
    
    
    /**
     *  Internal implementation of entity reset all
     *  
     *  @param mergeContext current merge context
     *  @param cache graph traversal cache
     */ 
    public void resetAllEntities(MergeContext mergeContext, Set<Object> cache) {
        boolean found = false;
        do {
            found = false;
            for (Object entity : savedProperties.keySet()) {
                if (isEntity(entity)) {
                    found = true;
                    resetEntity(mergeContext, entity, entity, cache);
                    break;
                }
            }
        }
        while (found);
        
        if (dirtyCount > 0)
            log.error("Incomplete reset of context, could be a bug");
    }
    
    
    /**
     *  Check if a value is empty
     *
     *	@param val value
     *  @return value is empty
     */ 
    public boolean isEmpty(Object val) {
        if (val == null)
            return true;
        else if (val instanceof String)
            return val.equals("");
        else if (val.getClass().isArray())
            return Array.getLength(val) == 0;
        else if (val instanceof Date)
            return ((Date)val).getTime() == 0L;
        else if (val instanceof Collection<?>)
            return ((Collection<?>)val).size() == 0;
        else if (val instanceof Map<?, ?>)
            return ((Map<?, ?>)val).size() == 0;
        return false; 
    }
    

    public static class Change {
        
        private ChangeKind kind;
        private int location;
        private Object[] items;
        
        public Change(ChangeKind kind, int location, Object[] items) {
            this.kind = kind;
            this.location = location;
            this.items = items;
        }
        
        public ChangeKind getKind() {
            return kind;
        }
        
        public int getLocation() {
            return location;
        }
        
        public Object[] getItems() {
            return items;
        }
        
        public void moveLocation(int offset) {
            location += offset;
        }
    }
}
