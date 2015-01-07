/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.tide.Context;
import org.granite.client.tide.collection.CollectionLoader;
import org.granite.client.tide.data.Conflict;
import org.granite.client.tide.data.DataConflictListener;
import org.granite.client.tide.data.DataMerger;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.PersistenceManager;
import org.granite.client.tide.data.RemoteInitializer;
import org.granite.client.tide.data.RemoteValidator;
import org.granite.client.tide.data.Value;
import org.granite.client.tide.data.impl.UIDWeakSet.Matcher;
import org.granite.client.tide.data.impl.UIDWeakSet.Operation;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.DataManager.ChangeKind;
import org.granite.client.tide.data.spi.DataManager.TrackingHandler;
import org.granite.client.tide.data.spi.DirtyCheckContext;
import org.granite.client.tide.data.spi.EntityRef;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.util.WeakIdentityHashMap;
import org.granite.logging.Logger;
import org.granite.util.TypeUtil;

/**
 * @author William DRAI
 */
public class EntityManagerImpl implements EntityManager {
    
    private static final Logger log = Logger.getLogger(EntityManagerImpl.class);
    
    private String id;
    private boolean active = false;
    private DataManager dataManager = null;
    private TrackingHandler trackingHandler = new DefaultTrackingHandler();
    private DirtyCheckContext dirtyCheckContext = null;
    private UIDWeakSet entitiesByUid = null;
    private WeakIdentityHashMap<Object, List<Object>> entityReferences = new WeakIdentityHashMap<Object, List<Object>>();
    
    private DataMerger[] customMergers = null;
    

    public EntityManagerImpl(String id, DataManager dataManager) {
        this.id = id;
        this.active = true;
        this.dataManager = dataManager != null ? dataManager : new JavaBeanDataManager();
        this.dataManager.setTrackingHandler(this.trackingHandler);
        this.entitiesByUid = new UIDWeakSet(this.dataManager);
        this.dirtyCheckContext = new DirtyCheckContextImpl(this.dataManager);
    }
    
    
    /**
     *  Return the entity manager id
     * 
     *  @return the entity manager id
     */
    public String getId() {
        return id;
    }
    
    /**
     *  {@inheritDoc}
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     *  Clear the current context
     *  Destroys all components/context variables
     */
    public void clear() {
    	entitiesByUid.apply(new Operation() {
            @Override
            public void apply(Object o) {
                PersistenceManager.setEntityManager(o, null);
            }
        });
        entitiesByUid.clear();
        entityReferences.clear();
        dirtyCheckContext.clear(false);
        dataManager.clear();
        active = true;
    }
    
    /**
     *  Clears entity cache
     */ 
    public void clearCache() {
       // _mergeContext.clear();
    }

    
    public DataManager getDataManager() {
    	return dataManager;
    }
    
    public TrackingHandler getTrackingHandler() {
    	return trackingHandler;
    }

    
    /**
     *  Setter for the array of custom mergers
     * 
     *  @param customMergers array of mergers
     */
    public void setCustomMergers(DataMerger[] customMergers) {
        if (customMergers != null && customMergers.length > 0)
            this.customMergers = customMergers;
        else
            this.customMergers = null;
    }


    private boolean uninitializeAllowed = true;
    
    @Override
    public void setUninitializeAllowed(boolean uninitializeAllowed) {
        this.uninitializeAllowed = uninitializeAllowed;
    }

    @Override
    public boolean isUninitializeAllowed() {
        return uninitializeAllowed;
    }


    private Propagation entityManagerPropagation = null;
        
    /**
     *  Setter for the propagation manager
     * 
     *  @param propagation propagation function that will visit child entity managers
     */
    public void setEntityManagerPropagation(Propagation propagation) {
        this.entityManagerPropagation = propagation;
    }
    
    /**
     *  Setter for active flag
     *  When EntityManager is not active, dirty checking is disabled
     * 
     *  @param active state
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     *  Setter for dirty check context implementation
     * 
     *  @param dirtyCheckContext dirty check context implementation
     */
    public void setDirtyCheckContext(DirtyCheckContext dirtyCheckContext) {
        if (dirtyCheckContext == null)
            throw new IllegalArgumentException("Dirty check context cannot be null");
        
        this.dirtyCheckContext = dirtyCheckContext;
    }

    
    private static int tmpEntityManagerId = 1;
    
    /**
     *  Create a new temporary entity manager
     */
    public EntityManager newTemporaryEntityManager() {
        try {
            DataManager tmpDataManager = TypeUtil.newInstance(dataManager.getClass(), DataManager.class);
            return new EntityManagerImpl("$$TMP$$" + (tmpEntityManagerId++), tmpDataManager);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not create temporaty entity manager", e);
        }
    }
    
    
    /**
     *  Attach an entity to this context
     * 
     *  @param entity an entity
     */
    public void attachEntity(Object entity) {
        attachEntity(entity, true);
    }
    
    /**
     *  Attach an entity to this context
     * 
     *  @param entity an entity
     *  @param putInCache put entity in cache
     */
    public void attachEntity(Object entity, boolean putInCache) {
        EntityManager em = PersistenceManager.getEntityManager(entity);
        if (em != null && em != this && !em.isActive()) {
            throw new Error("The entity instance " + entity + " cannot be attached to two contexts (current: " + em.getId() + ", new: " + id + ")");
        }
        
        PersistenceManager.setEntityManager(entity, this);
        if (putInCache) {
            if (entitiesByUid.put(entity) == null)
				dirtyCheckContext.addUnsaved(entity);
        }
    }
       
    
    /**
     *  Detach an entity from this context
     * 
     *  @param entity an entity
     *  @param removeFromCache remove entity from cache
     *  @param forceRemove remove even if persistent
     */
    public void detachEntity(Object entity, boolean removeFromCache, boolean forceRemove) {
		if (!forceRemove) {
			if (dataManager.hasVersionProperty(entity) && dataManager.getVersion(entity) != null)
				return;
		}
		
        dirtyCheckContext.markNotDirty(entity, entity);
        
        PersistenceManager.setEntityManager(entity, null);
        if (removeFromCache)
            entitiesByUid.remove(dataManager.getCacheKey(entity));
    }


    public void attach(Object object) {
        internalAttach(object, new IdentityHashMap<Object, Object>());
    }

    private void internalAttach(Object object, IdentityHashMap<Object, Object> cache) {
        if (object == null || isSimple(object))
            return;

        if (cache.containsKey(object))
            return;
        cache.put(object, object);

        if (isEntity(object))
            attachEntity(object);

        for (Map.Entry<String, Object> me : dataManager.getPropertyValues(object, false, true).entrySet()) {
            Object val = me.getValue();
            if (val != null && !dataManager.isInitialized(val))
                continue;
            
            if (val instanceof Collection<?>) {
                for (Object o : ((Collection<?>)val))
                    internalAttach(o, cache);
            }
            else if (val instanceof Map<?, ?>) {
                for (Map.Entry<?, ?> e : ((Map<?, ?>)val).entrySet()) {
                    internalAttach(e.getKey(), cache);
                    internalAttach(e.getValue(), cache);
                }
            }
            else if (!isSimple(val))
                internalAttach(val, cache);
        }
    }

    public static boolean isSimple(Object object) {
        return ObjectUtil.isSimple(object) || object instanceof Enum || object instanceof byte[] || object instanceof Value;
    }


    /**
     *  {@inheritDoc}
     */
    public boolean isPersisted(Object entity) {
        if (dataManager.hasVersionProperty(entity) && dataManager.getVersion(entity) != null)
            return true;
        return false;
    }
    
    private boolean isInitialized(Object entity) {
    	return dataManager.isInitialized(entity);
    }
    
    private boolean isEntity(Object entity) {
    	return dataManager.isEntity(entity);
    }
    
    
	/**
	 *  Internal implementation of object detach
	 * 
	 *  @param object object
	 *  @param cache internal cache to avoid graph loops
	 *  @param forceRemove force removal even if persisted
	 */ 
	public void detach(Object object, IdentityHashMap<Object, Object> cache, boolean forceRemove) {
		if (object == null || ObjectUtil.isSimple(object))
			return;
        if (!dataManager.isInitialized(object))
            return;

		if (cache.containsKey(object))
			return;
		cache.put(object, object);
		
		Map<String, Object> values = dataManager.getPropertyValues(object, true, true, false);
		
		if (isEntity(object) && entityReferences.containsKey(object)) {
			detachEntity(object, true, forceRemove);
			
			for (Entry<String, Object> me : values.entrySet())
				removeReference(me.getValue(), object, me.getKey());
		}
	}
    
    /**
     *  Retrieve an entity in the cache from its uid
     *  
     *  @param object an entity
     *  @param nullIfAbsent return null if entity not cached in context
     *  
     *  @return cached object with the same uid as the specified object
     */
    public Object getCachedObject(Object object, boolean nullIfAbsent) {
        Object entity = null;
        if (isEntity(object)) {
            entity = entitiesByUid.get(dataManager.getCacheKey(object));
        }
        else if (object instanceof EntityRef) {
            entity = entitiesByUid.get(((EntityRef)object).getClassName() + ":" + ((EntityRef)object).getUid());
        }
        else if (object instanceof String) {
        	entity = entitiesByUid.get((String)object);
        }
        
        if (entity != null)
            return entity;
        if (nullIfAbsent)
            return null;

        return object;
    }

    /** 
     *  Retrieve the owner entity of the provided object (collection/map/entity)
     *   
     *  @param object an entity
     *  @return array containing owner entity and property name
     */
    public Object[] getOwnerEntity(Object object) {
        List<Object> refs = entityReferences.get(object);
        if (refs == null)
            return null;
        
        for (int i = 0; i < refs.size(); i++) {
            if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0] instanceof String)
                return new Object[] { entitiesByUid.get((String)((Object[])refs.get(i))[0]), ((Object[])refs.get(i))[1] };
        }
        return null;
    }

    /**
     *  Retrieve the owner entity of the provided object (collection/map/entity)
     *
     *  @param object an entity
     *  @return list of arrays containing owner and property name
     */
    public List<Object[]> getOwnerEntities(Object object) {
        List<Object> refs = entityReferences.get(object);
        if (refs == null)
            return null;

        List<Object[]> owners = new ArrayList<Object[]>();
        for (int i = 0; i < refs.size(); i++) {
            if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0] instanceof String) {
            	Object owner = entitiesByUid.get((String)((Object[])refs.get(i))[0]);
            	if (owner != null)	// May have been garbage collected
            		owners.add(new Object[] { owner, ((Object[])refs.get(i))[1] });
            }
        }
        return owners;
    }

    /**
     *  Init references array for an object
     *   
     *  @param obj an entity
     *  @return list of current references
     */
    private List<Object> initRefs(Object obj) {
        List<Object> refs = entityReferences.get(obj);
        if (refs == null) {
            refs = new ArrayList<Object>();
            entityReferences.put(obj, refs);
        }
        return refs;
    }
    

    /**
     *  Register a reference to the provided object with either a parent or res
     * 
     *  @param obj an entity
     *  @param parent the parent entity
     *  @param propName name of the parent entity property that references the entity
     */
    public void addReference(Object obj, Object parent, String propName) {
        if (isEntity(obj))
            attachEntity(obj);
        
        dataManager.startTracking(obj, parent);

        List<Object> refs = entityReferences.get(obj);
        boolean found = false;
        if (isEntity(parent)) {
            String ref = dataManager.getCacheKey(parent);
            if (refs == null)
                refs = initRefs(obj);
            else {
                for (int i = 0; i < refs.size(); i++) {
                    if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0].equals(ref)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                refs.add(new Object[] { ref, propName });
        }
        else if (parent != null) {
            if (refs == null)
                refs = initRefs(obj);
            else {
                for (int i = 0; i < refs.size(); i++) {
                    if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0].equals(parent)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                refs.add(new Object[] { parent, propName });
        }
    }
    
    /**
     *  Remove a reference on the provided object
     *
     *  @param obj an entity
     *  @param parent the parent entity to dereference
     *  @param propName name of the parent entity property that references the entity
     *  @return true if actually removed
     */ 
    public boolean removeReference(Object obj, Object parent, String propName) {
        List<Object> refs = entityReferences.get(obj);
        if (refs == null)
            return true;
        
        int idx = -1;
        if (isEntity(parent)) {
            for (int i = 0; i < refs.size(); i++) {
                if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0].equals(dataManager.getCacheKey(parent))) {
                    idx = i;
                    break;                    
                }
            }
        }
        else if (parent != null) {
            for (int i = 0; i < refs.size(); i++) {
                if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0].equals(parent)) {
                    idx = i;
                    break;                    
                }
            }
        }
        if (idx >= 0)
            refs.remove(idx);
        
        boolean removed = false;
        if (refs.size() == 0) {
            entityReferences.remove(obj);
            removed = true;
            
            if (isEntity(obj))
                detachEntity(obj, true, false);
            
            dataManager.stopTracking(obj, parent);
        }
        
        if (obj instanceof PersistentCollection && !((PersistentCollection<?>)obj).wasInitialized())
        	return removed;
        
        if (obj instanceof Iterable<?>) {
            for (Object elt : (Iterable<?>)obj)
                removeReference(elt, parent, propName);
        }
        else if (obj != null && obj.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(obj); i++)
                removeReference(Array.get(obj, i), parent, propName);
        }
        else if (obj instanceof Map<?, ?>) {
            for (Entry<?, ?> me : ((Map<?, ?>)obj).entrySet()) {
                removeReference(me.getKey(), parent, propName);
                removeReference(me.getValue(), parent, propName);
            }
        }
        
        return removed;
    }


    public MergeContext initMerge(ServerSession serverSession) {
        if (serverSession != null) {
            DataMerger[] customMergers = serverSession.getContext().allByType(DataMerger.class);
            setCustomMergers(customMergers);
        }
        return new MergeContext(this, dirtyCheckContext, serverSession);
    }

    /**
     *  Merge an object coming from the server in the context
     *
     *
     * @param mergeContext current merge context
     *  @param obj external object
     *  @param previous previously existing object in the context (null if no existing object)
     *  @param parent parent object for collections
     *  @param propertyName property name of the current object in the parent object
     *  @param forceUpdate force update of property (used for externalized properties)
     *
     *  @return merged object (should === previous when previous not null)
     */
    @SuppressWarnings("unchecked")
    public Object mergeExternal(final MergeContext mergeContext, Object obj, Object previous, Object parent, String propertyName, boolean forceUpdate) {

        mergeContext.initMerge();
        
        boolean saveMergeUpdate = mergeContext.isMergeUpdate();
        boolean saveMerging = mergeContext.isMerging();
        
        try {
            mergeContext.setMerging(true);
            int stackSize = mergeContext.getMergeStackSize();
            
            boolean addRef = false;
            boolean fromCache = false;
            Object prev = mergeContext.getFromCache(obj);
            Object next = obj;
            if (prev != null) {
                next = prev;
                fromCache = true;
            }
            else {
                // Clear change tracking
				dataManager.stopTracking(previous, parent);
				
                if (obj == null) {
                    next = null;
                }
                else if (((obj instanceof PersistentCollection && !((PersistentCollection<?>)obj).wasInitialized()) 
                    || (obj instanceof PersistentCollection && !(previous instanceof PersistentCollection))) && isEntity(parent) && propertyName != null) {
                    next = mergePersistentCollection(mergeContext, (PersistentCollection<?>)obj, previous, parent, propertyName);
                    addRef = true;
                }
                else if (obj instanceof List<?>) {
                    next = mergeList(mergeContext, (List<Object>)obj, previous, parent, propertyName);
                    addRef = true;
                }
                else if (obj instanceof Set<?>) {
                    next = mergeSet(mergeContext, (Set<Object>)obj, previous, parent, propertyName);
                    addRef = true;
                }
                else if (obj instanceof Map<?, ?>) {
                    next = mergeMap(mergeContext, (Map<Object, Object>)obj, previous, parent, propertyName);
                    addRef = true;
                }
                else if (obj.getClass().isArray()) {
                	next = mergeArray(mergeContext, obj, previous, parent, propertyName);
                	addRef = true;
                }
                else if (isEntity(obj)) {
                    next = mergeEntity(mergeContext, obj, previous, parent, propertyName);
                    addRef = true;
                }
                else {
                    boolean merged = false;
                    if (customMergers != null) {
                        for (DataMerger merger : customMergers) {
                            if (merger.accepts(obj)) {
                                next = merger.merge(mergeContext, obj, previous, parent, propertyName);

                                // Keep notified of collection updates to notify the server at next remote call
                                dataManager.startTracking(previous, parent);
                                merged = true;
                                addRef = true;
                            }
                        }
                    }
                    if (!merged && !ObjectUtil.isSimple(obj) && !(obj instanceof Enum || obj instanceof Value || obj instanceof byte[])) {
                        next = mergeEntity(mergeContext, obj, previous, parent, propertyName);
                        addRef = true;
                    }
                }
            }
            
            if (next != null && !fromCache && addRef
                && (prev == null && parent != null)) {
                // Store reference from current object to its parent entity or root component expression
                // If it comes from the cache, we are probably in a circular graph 
                addReference(next, parent, propertyName);
            }
            
            mergeContext.setMergeUpdate(saveMergeUpdate);
            
            if (entityManagerPropagation != null && (mergeContext.isMergeUpdate() || forceUpdate) && !fromCache && isEntity(obj)) {
                // Propagate to existing conversation contexts where the entity is present
                entityManagerPropagation.propagate(obj, new Function() {
                    public void execute(EntityManager entityManager, Object entity) {
                        if (entityManager == mergeContext.getSourceEntityManager())
                            return;
                        if (entityManager.getCachedObject(entity, true) != null)
                            entityManager.mergeFromEntityManager(entityManager, mergeContext.getServerSession(), entity, mergeContext.getExternalDataSessionId(), mergeContext.isUninitializing());
                    }
                });
            }
            
			if (mergeContext.getMergeStackSize() > stackSize)
				mergeContext.popMerge();
			
            return next;
        }
        catch (Exception e) {
        	throw new RuntimeException("Merge error", e);
        }
        finally {
            mergeContext.setMerging(saveMerging);
        }
    }


    /**
     *  Merge an entity coming from the server in the context
     *
     *	@param mergeContext current merge context
     *  @param obj external entity
     *  @param previous previously existing object in the context (null if no existing object)
     *  @param parent parent object for collections
     *  @param propertyName propertyName from the owner object
     *
     *  @return merged entity (=== previous when previous not null)
     */ 
    private Object mergeEntity(MergeContext mergeContext, final Object obj, Object previous, Object parent, String propertyName) {
        if (obj != null || previous != null)
            log.debug("mergeEntity: %s previous %s%s", ObjectUtil.toString(obj), ObjectUtil.toString(previous), obj == previous ? " (same)" : "");
        
        Object dest = obj;
        Object p = null;
        if (!isInitialized(obj)) {
            // If entity is uninitialized, try to lookup the cached instance by its class name and id (only works with Hibernate proxies)
            if (dataManager.hasIdProperty(obj)) {
                p = entitiesByUid.find(new Matcher() {
                    public boolean match(Object o) {
                        return o.getClass().getName().equals(obj.getClass().getName()) && 
                        	ObjectUtil.objectEquals(dataManager, dataManager.getId(obj), dataManager.getId(o));
                    }
                });
                
                if (p != null) {
                    previous = p;
                    dest = previous;
                }
            }
        }
        else if (dataManager.isEntity(obj)) {
            p = entitiesByUid.get(dataManager.getCacheKey(obj));
            if (p != null) {
                // Trying to merge an entity that is already cached with itself: stop now, this is not necessary to go deeper in the object graph
                // it should be already instrumented and tracked
                if (obj == p)
                    return obj;
                
                previous = p;
                dest = previous;
            }
        }
        
        if (dest != previous && previous != null && (ObjectUtil.objectEquals(dataManager, previous, obj)
            || !isEntity(previous)))    // GDS-649 Case of embedded objects 
            dest = previous;
        
        if (dest == obj && p == null && obj != null && mergeContext.getSourceEntityManager() != null) {
            // When merging from another entity manager, ensure we create a new copy of the entity
            // An instance can exist in only one entity manager at a time 
            try {
                dest = TypeUtil.newInstance(obj.getClass(), Object.class);
                dataManager.copyUid(dest, obj);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not create class " + obj.getClass(), e);
            }
        }

        if (!isInitialized(obj) && ObjectUtil.objectEquals(dataManager, previous, obj)) {
            // Don't overwrite existing entity with an uninitialized proxy when optimistic locking is defined
            log.debug("ignored received uninitialized proxy");
            // Don't mark the object not dirty as we only received a proxy
            // dirtyCheckContext.markNotDirty(previous, null);
            return previous;
        }
        
        if (!isInitialized(dest))
            log.debug("initialize lazy entity: %s", dest.toString());
        
        if (dest != null && isEntity(dest) && dest == obj) {
            log.debug("received entity %s used as destination (ctx: %s)", obj.toString(), this.id);
        }
        
        boolean fromCache = (p != null && dest == p); 
        
        if (!fromCache && isEntity(dest))
            entitiesByUid.put(dest);            
        
        mergeContext.pushMerge(obj, dest);
        
        boolean tracking = false;
        if (mergeContext.isResolvingConflict()) {
            dataManager.startTracking(dest, parent);
            tracking = true;
        }
        
        boolean ignore = false;
        if (isEntity(dest)) {
            // If we are in an uninitialing temporary entity manager, try to reproxy associations when possible
            if (mergeContext.isUninitializing() && isEntity(parent) && propertyName != null) {
                if (dataManager.hasVersionProperty(dest) && dataManager.getVersion(obj) != null 
                        && dataManager.isLazyProperty(parent, propertyName)) {
                    if (dataManager.defineProxy(dest, obj))   // Only if entity can be proxied (has a detachedState)
                        return dest;
                }
            }
            
            // Associate entity with the current context
            attachEntity(dest, false);
            
            if (previous != null && dest == previous) {
                // Check version for optimistic locking
                if (dataManager.hasVersionProperty(dest) && !mergeContext.isResolvingConflict()) {
                    Number newVersion = (Number)dataManager.getVersion(obj);
                    Number oldVersion = (Number)dataManager.getVersion(dest);
                    if ((newVersion != null && oldVersion != null && newVersion.longValue() < oldVersion.longValue() 
                            || (newVersion == null && oldVersion != null))) {
                        log.warn("ignored merge of older version of %s (current: %d, received: %d)", 
                            dest.toString(), oldVersion, newVersion);
                        ignore = true;
                    }
                    else if ((newVersion != null && oldVersion != null && newVersion.longValue() > oldVersion.longValue()) 
                            || (newVersion != null && oldVersion == null)) {
                        // Handle changes when version number is increased
                        mergeContext.markVersionChanged(dest);
                        
						boolean entityChanged = dirtyCheckContext.isEntityChanged(dest);
                		if (mergeContext.getExternalDataSessionId() != null && entityChanged && dirtyCheckContext.checkAndMarkNotDirty(mergeContext, dest, obj, null)) {
                            // Conflict between externally received data and local modifications
                            log.warn("conflict with external data detected on %s (current: %d, received: %d)",
                                dest.toString(), oldVersion, newVersion);
                            
                            // Incoming data is different from local data
                            Map<String, Object> save = dirtyCheckContext.getSavedProperties(dest);
                            List<String> properties = new ArrayList<String>(save.keySet());
                            properties.remove(dataManager.getVersionPropertyName(dest));
                            Collections.sort(properties);
                            
                            mergeContext.addConflict(dest, obj, properties);
                            
                            ignore = true;
                        }
                        else
                            mergeContext.setMergeUpdate(true);
                    }
                    else {
                        // Data has been changed locally and not persisted, don't overwrite when version number is unchanged
                        if (dirtyCheckContext.isEntityChanged(dest))
                            mergeContext.setMergeUpdate(false);
                        else
                            mergeContext.setMergeUpdate(true);
                    }
                }
                else if (!mergeContext.isResolvingConflict())
                    mergeContext.markVersionChanged(dest);
            }
            else
                mergeContext.markVersionChanged(dest);
            
            if (!ignore)
                defaultMerge(mergeContext, obj, dest, parent, propertyName);
        }
        else
            defaultMerge(mergeContext, obj, dest, parent, propertyName);
        
        if (dest != null && !ignore && !mergeContext.isSkipDirtyCheck() && !mergeContext.isResolvingConflict())
            dirtyCheckContext.checkAndMarkNotDirty(mergeContext, dest, obj, isEntity(parent) && !isEntity(dest) ? parent : null);
        
        if (dest != null)
            log.debug("mergeEntity result: %s", dest.toString());
        
        // Keep notified of collection updates to notify the server at next remote call
        if (!tracking)
            dataManager.startTracking(dest, parent);
        
        return dest;
    }
    
    
    private Object mergeArray(MergeContext mergeContext, Object array, Object previous, Object parent, String propertyName) {
    	Object dest = mergeContext.getSourceEntityManager() == null ? array : Array.newInstance(array.getClass().getComponentType(), Array.getLength(array));
    	
		mergeContext.pushMerge(array, dest);
        
        for (int i = 0; i < Array.getLength(array); i++) {
        	Object obj = Array.get(array, i);
            obj = mergeExternal(mergeContext, obj, null, propertyName != null ? parent : null, propertyName, false);
            
            if (mergeContext.isMergeUpdate())
            	Array.set(dest, i, obj);
        }
        
        return dest;
    }
    
    
    /**
     *  Merge a collection coming from the server in the context
     *
     *	@param mergeContext current merge context
     *  @param coll external collection
     *  @param previous previously existing collection in the context (can be null if no existing collection)
     *  @param parent owner object for collections
     *  @param propertyName property name in owner object
     * 
     *  @return merged collection (=== previous when previous not null)
     */ 
    @SuppressWarnings("unchecked")
    private List<?> mergeList(MergeContext mergeContext, List<Object> coll, Object previous, Object parent, String propertyName) {
        log.debug("mergeList: %s previous %s", ObjectUtil.toString(coll), ObjectUtil.toString(previous));
        
        if (mergeContext.isUninitializing() && isEntity(parent) && propertyName != null) {
        	if (dataManager.hasVersionProperty(parent) && dataManager.getVersion(parent) != null
        		&& dataManager.isLazyProperty(parent, propertyName)) {
                mergeContext.pushMerge(coll, previous);
                
                if (previous instanceof PersistentCollection && ((PersistentCollection<List<?>>)previous).wasInitialized()) {
	                log.debug("uninitialize lazy collection %s", ObjectUtil.toString(previous));
	                ((PersistentCollection<List<?>>)previous).uninitialize();
                }
                
                return (List<?>)previous;
            }
        }
        
        if (previous != null && previous instanceof PersistentCollection && !((PersistentCollection<List<?>>)previous).wasInitialized()) {
            log.debug("initialize lazy collection %s", ObjectUtil.toString(previous));
            mergeContext.pushMerge(coll, previous);
            
            ((PersistentCollection<List<?>>)previous).initializing();
            
            List<Object> added = new ArrayList<Object>(coll.size());
            for (int i = 0; i < coll.size(); i++) {
                Object obj = coll.get(i);

                obj = mergeExternal(mergeContext, obj, null, propertyName != null ? parent : null, propertyName, false);
                added.add(obj);
            }
            
            ((PersistentCollection<List<?>>)previous).initialize(added, null);
            
            // Keep notified of collection updates to notify the server at next remote call
            dataManager.startTracking(previous, parent);

            return (List<?>)previous;
        }

        boolean tracking = false;
        
        List<?> nextList = null;
        List<Object> list = null;
        if (previous != null && previous instanceof List<?>)
            list = (List<Object>)previous;
        else if (mergeContext.getSourceEntityManager() != null) {
            try {
                list = coll.getClass().newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("Could not create class " + coll.getClass());
            }
        }
        else
            list = coll;
             
        mergeContext.pushMerge(coll, list);

        List<Object> prevColl = list != coll ? list : null;
        List<Object> destColl = prevColl;

        if (prevColl != null && mergeContext.isMergeUpdate()) {
            // Enable tracking before modifying collection when resolving a conflict
            // so the dirty checking can save changes
            if (mergeContext.isResolvingConflict()) {
                dataManager.startTracking(prevColl, parent);
                tracking = true;
            }
            
            for (int i = 0; i < destColl.size(); i++) {
                Object obj = destColl.get(i);
                boolean found = false;
                for (int j = 0; j < coll.size(); j++) {
                    Object next = coll.get(j);
                    if (ObjectUtil.objectEquals(dataManager, next, obj)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    destColl.remove(i);
                    i--;
                }
            }
        }
        for (int i = 0; i < coll.size(); i++) {
            Object obj = coll.get(i);
            if (destColl != null) {
                boolean found = false;
                for (int j = i; j < destColl.size(); j++) {
                    Object prev = destColl.get(j);
                    if (i < destColl.size() && ObjectUtil.objectEquals(dataManager, prev, obj)) {
                        obj = mergeExternal(mergeContext, obj, prev, propertyName != null ? parent : null, propertyName, false);
                        
                        if (j != i) {
                            destColl.remove(j);
                            if (i < destColl.size())
                                destColl.add(i, obj);
                            else
                                destColl.add(obj);
                            if (i > j)
                                j--;
                        }
                        else if (obj != prev)
                            destColl.set(i, obj);
                        
                        found = true;
                    }
                }
                if (!found) {
                    obj = mergeExternal(mergeContext, obj, null, propertyName != null ? parent : null, propertyName, false);
                    
                    if (mergeContext.isMergeUpdate()) {
                        if (i < prevColl.size())
                            destColl.add(i, obj);
                        else
                            destColl.add(obj);
                    }
                }
            }
            else {
                Object prev = obj;
                obj = mergeExternal(mergeContext, obj, null, propertyName != null ? parent : null, propertyName, false);
                if (obj != prev)
                    coll.set(i, obj);
            }
        }
        if (destColl != null && mergeContext.isMergeUpdate()) {
            if (!mergeContext.isResolvingConflict() && !mergeContext.isSkipDirtyCheck())
                dirtyCheckContext.markNotDirty(previous, parent);
            
            nextList = prevColl;
        }
        else if (prevColl instanceof PersistentCollection && !mergeContext.isMergeUpdate()) {
			nextList = prevColl;
		}
        else
            nextList = coll;
        
        // Wrap/instrument persistent collections
        if (isEntity(parent) && propertyName != null && nextList instanceof PersistentCollection 
        		&& !(((PersistentCollection<?>)nextList).getLoader() instanceof CollectionLoader)) {
            log.debug("instrument persistent collection from %s", ObjectUtil.toString(nextList));
            
            ((PersistentCollection<List<?>>)nextList).setLoader(new CollectionLoader<List<?>>(mergeContext.getServerSession(), parent, propertyName));
        }
        else
            log.debug("mergeList result: %s", ObjectUtil.toString(nextList));
        
        mergeContext.pushMerge(coll, nextList, false);
        
        if (!tracking)
            dataManager.startTracking(nextList, parent);

        return nextList;
    }
    
    /**
     *  Merge a collection coming from the server in the context
     *
     *	@param mergeContext current merge context
     *  @param coll external collection
     *  @param previous previously existing collection in the context (can be null if no existing collection)
     *  @param parent owner object for collections
     *  @param propertyName property name in owner object
     * 
     *  @return merged collection (=== previous when previous not null)
     */ 
    @SuppressWarnings("unchecked")
    private Set<?> mergeSet(MergeContext mergeContext, Set<Object> coll, Object previous, Object parent, String propertyName) {
        log.debug("mergeSet: %s previous %s", ObjectUtil.toString(coll), ObjectUtil.toString(previous));
        
        if (mergeContext.isUninitializing() && isEntity(parent) && propertyName != null) {
            if (dataManager.hasVersionProperty(parent) && dataManager.getVersion(parent) != null
                && dataManager.isLazyProperty(parent, propertyName)) {
                mergeContext.pushMerge(coll, previous);
                
            	if (previous instanceof PersistentCollection && ((PersistentCollection<Set<?>>)previous).wasInitialized()) {
	                log.debug("uninitialize lazy collection %s", ObjectUtil.toString(previous));
	                ((PersistentCollection<Set<?>>)previous).uninitialize();
            	}
            	
                return (Set<?>)previous;
            }
        }
        
        if (previous != null && previous instanceof PersistentCollection && !((PersistentCollection<Set<?>>)previous).wasInitialized()) {
            log.debug("initialize lazy collection %s", ObjectUtil.toString(previous));
            mergeContext.pushMerge(coll, previous);
            
            ((PersistentCollection<Set<?>>)previous).initializing();
            
            final Set<Object> added = new HashSet<Object>(coll.size());
            for (Iterator<Object> icoll = coll.iterator(); icoll.hasNext(); ) {
                Object obj = icoll.next();

                obj = mergeExternal(mergeContext, obj, null, propertyName != null ? parent : null, propertyName, false);
                added.add(obj);
            }
            
            ((PersistentCollection<Set<?>>)previous).initialize(added, null);
            
            // Keep notified of collection updates to notify the server at next remote call
            dataManager.startTracking(previous, parent);

            return (Set<?>)previous;
        }

        boolean tracking = false;
        
        Set<?> nextSet = null;
        Set<Object> set = null;
        if (previous != null && previous instanceof Set<?>)
            set = (Set<Object>)previous;
        else if (mergeContext.getSourceEntityManager() != null) {
            try {
            	if (coll instanceof SortedSet<?>) {
            		try {
            			set = coll.getClass().getConstructor(Comparator.class).newInstance(((SortedSet<?>)coll).comparator());
            		}
            		catch (NoSuchMethodException nsme) {
            		}
            	}
            	if (set == null)
            		set = coll.getClass().newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("Could not create class " + coll.getClass());
            }
        }
        else
            set = coll;
                        
        mergeContext.pushMerge(coll, set);

        Set<Object> prevColl = set != coll ? set : null;
        Set<Object> destColl = prevColl;

        if (prevColl != null && mergeContext.isMergeUpdate()) {
            // Enable tracking before modifying collection when resolving a conflict
            // so the dirty checking can save changes
            if (mergeContext.isResolvingConflict()) {
                dataManager.startTracking(prevColl, parent);
                tracking = true;
            }
            
            for (Iterator<Object> ic = destColl.iterator(); ic.hasNext(); ) {
                Object obj = ic.next();
                boolean found = false;
                for (Iterator<Object> jc = coll.iterator(); jc.hasNext(); ) {
                    Object next = jc.next();
                    if (ObjectUtil.objectEquals(dataManager, next, obj)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    ic.remove();
            }
        }
        Set<Object> changed = new HashSet<Object>();
        for (Iterator<Object> ic = coll.iterator(); ic.hasNext(); ) {
            Object obj = ic.next();
            if (destColl != null) {
                boolean found = false;
                for (Iterator<Object> jc = destColl.iterator(); jc.hasNext(); ) {
                    Object prev = jc.next();
                    if (ObjectUtil.objectEquals(dataManager, prev, obj)) {
                        obj = mergeExternal(mergeContext, obj, prev, propertyName != null ? parent : null, propertyName, false);
                        if (obj != prev) {
                            ic.remove();
                            changed.add(obj);
                        }
                        found = true;
                    }
                }
                if (!found) {
                    obj = mergeExternal(mergeContext, obj, null, propertyName != null ? parent : null, propertyName, false);
                    
                    if (mergeContext.isMergeUpdate())
                        destColl.add(obj);
                }
            }
            else {
                Object prev = obj;
                obj = mergeExternal(mergeContext, obj, null, propertyName != null ? parent : null, propertyName, false);
                if (obj != prev) {
                    ic.remove();
                    changed.add(obj);
                }
            }
        }
        if (destColl != null)
            destColl.addAll(changed);
        else
            coll.addAll(changed);
        
        if (destColl != null && mergeContext.isMergeUpdate()) {
            if (!mergeContext.isResolvingConflict() && !mergeContext.isSkipDirtyCheck())
                dirtyCheckContext.markNotDirty(previous, parent);
            
            nextSet = prevColl;
        }
        else if (prevColl instanceof PersistentCollection && !mergeContext.isMergeUpdate()) {
            nextSet = prevColl;
        }
        else
            nextSet = coll;
        
        // Wrap/instrument persistent collections
        if (isEntity(parent) && propertyName != null && nextSet instanceof PersistentCollection 
                && !(((PersistentCollection<Set<?>>)nextSet).getLoader() instanceof CollectionLoader)) {
            log.debug("instrument persistent collection from %s", ObjectUtil.toString(nextSet));
            
            ((PersistentCollection<Set<?>>)nextSet).setLoader(new CollectionLoader<Set<?>>(mergeContext.getServerSession(), parent, propertyName));
        }
        else
            log.debug("mergeSet result: %s", ObjectUtil.toString(nextSet));
        
        mergeContext.pushMerge(coll, nextSet, false);
        
        if (!tracking)
            dataManager.startTracking(nextSet, parent);

        return nextSet;
    }

    /**
     *  Merge a map coming from the server in the context
     *
     *	@param mergeContext current merge context
     *  @param map external map
     *  @param previous previously existing map in the context (null if no existing map)
     *  @param parent owner object for the map if applicable
     *  @param propertyName property name from the owner
     * 
     *  @return merged map (=== previous when previous not null)
     */ 
    @SuppressWarnings("unchecked")
    private Map<?, ?> mergeMap(MergeContext mergeContext, Map<Object, Object> map, Object previous, Object parent, String propertyName) {
        log.debug("mergeMap: %s previous %s", ObjectUtil.toString(map), ObjectUtil.toString(previous));
        
        if (mergeContext.isUninitializing() && isEntity(parent) && propertyName != null) {
        	if (dataManager.hasVersionProperty(parent) && dataManager.getVersion(parent) != null
        		&& dataManager.isLazyProperty(parent, propertyName)) {
                
                mergeContext.pushMerge(map, previous);
                
                if (previous instanceof PersistentCollection && ((PersistentCollection<Map<?, ?>>)previous).wasInitialized()) {
                    log.debug("uninitialize lazy map %s", ObjectUtil.toString(previous));
                	((PersistentCollection<Map<?, ?>>)previous).uninitialize();
                }
                
                return (Map<?, ?>)previous;
            }
        }

        if (previous != null && previous instanceof PersistentCollection && !((PersistentCollection<Map<?, ?>>)previous).wasInitialized()) {
            log.debug("initialize lazy map %s", ObjectUtil.toString(previous));
            mergeContext.pushMerge(map, previous);
            
            ((PersistentCollection<Map<?, ?>>)previous).initializing();
            
            Map<Object, Object> added = new HashMap<Object, Object>();
            for (Entry<?, ?> me : map.entrySet()) {
                Object key = mergeExternal(mergeContext, me.getKey(), null, propertyName != null ? parent: null, propertyName, false);
                Object value = mergeExternal(mergeContext, me.getValue(), null, propertyName != null ? parent : null, propertyName, false);
                added.put(key, value);
            }
            
            ((PersistentCollection<Map<?, ?>>)previous).initialize(added, null);
            
            // Keep notified of collection updates to notify the server at next remote call
            dataManager.startTracking(previous, parent);

            return (Map<?, ?>)previous;
        }
        
        boolean tracking = false;
        
        Map<Object, Object> nextMap = null;
        Map<Object, Object> m = null;
        if (previous != null && previous instanceof Map<?, ?>)
            m = (Map<Object, Object>)previous;
        else if (mergeContext.getSourceEntityManager() != null) {
            try {
	        	if (map instanceof SortedMap<?, ?>) {
	        		try {
	        			m = map.getClass().getConstructor(Comparator.class).newInstance(((SortedMap<?, ?>)map).comparator());
	        		}
	        		catch (NoSuchMethodException nsme) {
	        		}
	        	}
	        	if (m == null)
	                m = TypeUtil.newInstance(map.getClass(), Map.class);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not create class " + map.getClass());
            }
        }
        else
            m = map;
        mergeContext.pushMerge(map, m);
        
        Map<Object, Object> prevMap = m != map ? m : null;
        
        if (prevMap != null) {
            if (mergeContext.isResolvingConflict()) {
                dataManager.startTracking(prevMap, parent);
                tracking = true;
            }
            
            if (map != prevMap) {
                for (Entry<?, ?> me : map.entrySet()) {
                    Object newKey = mergeExternal(mergeContext, me.getKey(), null, parent, propertyName, false);
                    Object prevValue = prevMap.get(newKey);
                    Object value = mergeExternal(mergeContext, me.getValue(), prevValue, parent, propertyName, false);
                    if (mergeContext.isMergeUpdate() || prevMap.containsKey(newKey))
                        prevMap.put(newKey, value);
                }
                
                if (mergeContext.isMergeUpdate()) {
                    Iterator<Object> imap = prevMap.keySet().iterator();
                    while (imap.hasNext()) {
                        Object key = imap.next();
                        boolean found = false;
                        for (Object k : map.keySet()) {
                            if (ObjectUtil.objectEquals(dataManager, k, key)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            imap.remove();
                    }
                }
            }
            
            if (mergeContext.isMergeUpdate() && !mergeContext.isResolvingConflict() && !mergeContext.isSkipDirtyCheck())
                dirtyCheckContext.markNotDirty(previous, parent);
            
            nextMap = prevMap;
        }
        else {
            List<Object[]> addedToMap = new ArrayList<Object[]>();
            for (Entry<?, ?> me : map.entrySet()) {
                Object value = mergeExternal(mergeContext, me.getValue(), null, parent, propertyName, false);
                Object key = mergeExternal(mergeContext, me.getKey(), null, parent, propertyName, false);
                addedToMap.add(new Object[] { key, value });
            }
            map.clear();
            for (Object[] obj : addedToMap)
                map.put(obj[0], obj[1]);
            
            nextMap = map;
        }
        
        if (isEntity(parent) && propertyName != null && nextMap instanceof PersistentCollection 
        		&& !(((PersistentCollection<Map<?, ?>>)nextMap).getLoader() instanceof CollectionLoader)) {
            log.debug("instrument persistent map from %s", ObjectUtil.toString(nextMap));
            
            ((PersistentCollection<Map<?, ?>>)nextMap).setLoader(new CollectionLoader<Map<?, ?>>(mergeContext.getServerSession(), parent, propertyName));
        }
        else
            log.debug("mergeMap result: %s", ObjectUtil.toString(nextMap));
        
        mergeContext.pushMerge(map, nextMap, false);
        
        if (!tracking)
            dataManager.startTracking(nextMap, parent);
        
        return nextMap;
    } 


    /**
     *  Wraps a persistent collection to manage lazy initialization
     *
     *	@param mergeContext current merge context
     *  @param coll the collection to wrap
     *  @param previous the previous existing collection
     *  @param parent the owner object
     *  @param propertyName owner property
     * 
     *  @return the wrapped persistent collection
     */ 
    @SuppressWarnings("unchecked")
	protected Object mergePersistentCollection(MergeContext mergeContext, PersistentCollection<?> coll, Object previous, Object parent, String propertyName) {
        if (previous instanceof PersistentCollection) {
            mergeContext.pushMerge(coll, previous);
            if (((PersistentCollection<?>)previous).wasInitialized()) {
                if (mergeContext.isUninitializeAllowed() && mergeContext.hasVersionChanged(parent)) {
                    log.debug("uninitialize lazy collection %s", ObjectUtil.toString(previous));
                    ((PersistentCollection<?>)previous).uninitialize();
                }
                else
                    log.debug("keep initialized collection %s", ObjectUtil.toString(previous));
            }
            
            if (!(((PersistentCollection<?>)previous).getLoader() instanceof CollectionLoader)) {
	            log.debug("instrument persistent collection from %s", ObjectUtil.toString(previous));
	            ((PersistentCollection<Object>)previous).setLoader(new CollectionLoader<Object>(mergeContext.getServerSession(), parent, propertyName));
            }
            
            dataManager.startTracking(previous, parent);
            return previous;
        }
        
		PersistentCollection<?> pcoll = coll;
		if (previous instanceof PersistentCollection)
			pcoll = (PersistentCollection<?>)previous;
		if (coll.getLoader() instanceof CollectionLoader)
			pcoll = duplicatePersistentCollection(mergeContext, coll, parent, propertyName);
		else if (mergeContext.getSourceEntityManager() != null)
			pcoll = duplicatePersistentCollection(mergeContext, pcoll, parent, propertyName);
		
        mergeContext.pushMerge(coll, pcoll);
        
        if (pcoll.wasInitialized()) {
        	if (pcoll instanceof List<?>) {
				List<Object> plist = (List<Object>)pcoll;
	            for (int i = 0; i < plist.size(); i++) {
	                Object obj = mergeExternal(mergeContext, plist.get(i), null, parent, propertyName, false);
	                if (obj != plist.get(i)) 
	                	plist.set(i, obj);
	            }
        	}
        	else {
				Collection<Object> pset = (Collection<Object>)pcoll;
        		List<Object> toAdd = new ArrayList<Object>();
        		for (Iterator<Object> iset = pset.iterator(); iset.hasNext(); ) {
        			Object obj = iset.next();
	                Object merged = mergeExternal(mergeContext, obj, null, parent, propertyName, false);
	                if (merged != obj) { 
	                	iset.remove();
	                	toAdd.add(merged);
	                }
        		}
        		pset.addAll(toAdd);
        	}
            dataManager.startTracking(pcoll, parent);
        }
        else if (isEntity(parent) && propertyName != null)
            dataManager.setLazyProperty(parent, propertyName);
        
        if (!(coll.getLoader() instanceof CollectionLoader)) {
            log.debug("instrument persistent collection from %s", ObjectUtil.toString(pcoll));
            ((PersistentCollection<Object>)pcoll).setLoader(new CollectionLoader<Object>(mergeContext.getServerSession(), parent, propertyName));
        }
        return pcoll;
    }
    
    private PersistentCollection<?> duplicatePersistentCollection(MergeContext mergeContext, Object coll, Object parent, String propertyName) {
    	if (!(coll instanceof PersistentCollection))
			throw new RuntimeException("Not a persistent collection/map " + ObjectUtil.toString(coll));
		
    	PersistentCollection<?> ccoll = ((PersistentCollection<?>)coll).clone(mergeContext.isUninitializing());
		
		if (mergeContext.isUninitializing() && parent != null && propertyName != null) {
			if (dataManager.hasVersionProperty(parent) && dataManager.getVersion(parent) != null && dataManager.isLazyProperty(parent, propertyName))
				ccoll.uninitialize();
		}
		return ccoll;
    }

    
    /**
     *  Merge an object coming from another entity manager (in general in the global context) in the local context
     *
     *  @param sourceEntityManager source context of incoming data
     *  @param serverSession current server session
     *  @param obj external object
     *  @param externalDataSessionId is merge from external data
     *  @param uninitializing true to force folding of loaded lazy associations
     *
     *  @return merged object
     */
    public Object mergeFromEntityManager(EntityManager sourceEntityManager, ServerSession serverSession, Object obj, String externalDataSessionId, boolean uninitializing) {
        try {
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, serverSession);
            mergeContext.setSourceEntityManager(sourceEntityManager);
            mergeContext.setUninitializing(uninitializing);
            mergeContext.setExternalDataSessionId(externalDataSessionId);        
            
            Object next = externalDataSessionId != null
                ? internalMergeExternalData(mergeContext, obj, null, null, null) // Force handling of external data
                : mergeExternal(mergeContext, obj, null, null, null, false);
            
            return next;
        }
        finally {
            MergeContext.destroy(this);
        }
    }
    
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *
     *  @return merged object (should === previous when previous not null)
     */

    public Object mergeExternalData(Object obj) {
        return mergeExternalData(null, obj, null, null, null, null);
    }
    
    public Object mergeExternalData(ServerSession serverSession, Object obj) {
        return mergeExternalData(serverSession, obj, null, null, null, null);
    }
    
    public Object mergeExternalData(Object obj, Object prev, String externalDataSessionId, List<Object> removals, List<Object> persists) {
    	return mergeExternalData(null, obj, prev, externalDataSessionId, removals, persists);
    }
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *	@param serverSession server session
     *  @param obj external object
     *  @param prev existing local object to merge with
     *  @param externalDataSessionId sessionId from which the data is coming (other user/server), null if local or current user session
     *  @param removals list of entities to remove from the entity manager cache
     *  @param persists list of newly persisted entities
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object mergeExternalData(ServerSession serverSession, Object obj, Object prev, String externalDataSessionId, List<Object> removals, List<Object> persists) {
        try {
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, serverSession);
            mergeContext.setExternalDataSessionId(externalDataSessionId);
            
            return internalMergeExternalData(mergeContext, obj, prev, removals, persists);
        }
        finally {
            MergeContext.destroy(this);
        }
    }
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *	@param mergeContext current merge context
     *  @param obj external object
     *  @param prev existing local object to merge with
     *  @param removals array of entities to remove from the entity manager cache
     *  @param persists list of newly persisted entities
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object internalMergeExternalData(MergeContext mergeContext, Object obj, Object prev, List<Object> removals, List<Object> persists) {
        Object next = mergeExternal(mergeContext, obj, prev, null, null, false);

        if (removals != null)
            handleRemovalsAndPersists(mergeContext, removals, persists);

        if (mergeContext.getExternalDataSessionId() != null) {
            handleMergeConflicts(mergeContext);
            clearCache();
        }

        return next;
    }
    
    
    /**
     *  Merge conversation entity manager context variables in global entity manager 
     *  Only applicable to conversation contexts 
     * 
     *  @param entityManager conversation entity manager
     */
    public void mergeInEntityManager(final EntityManager entityManager, final ServerSession serverSession) {
        final Set<Object> cache = new HashSet<Object>();
        final EntityManager sourceEntityManager = this;
        entitiesByUid.apply(new UIDWeakSet.Operation() {
            public void apply(Object obj) {
                // Reset local dirty state, only server state can safely be merged in global context
                if (isEntity(obj))
                    resetEntity(obj, cache);
                entityManager.mergeFromEntityManager(sourceEntityManager, serverSession, obj, null, false);
            }
        });
    }


    @Override
    public boolean isDirty() {
        return dataManager.isDirty();
    }
    
    public boolean isDirtyEntity(Object entity) {
    	return dirtyCheckContext.isEntityChanged(entity);
    }
    
    public boolean isDeepDirtyEntity(Object entity) {
    	return dirtyCheckContext.isEntityDeepChanged(entity);
    }

    public boolean isSavedEntity(Object entity) {
        return dirtyCheckContext.getSavedProperties(entity) != null;
    }
    
        
    /**
     *  Remove elements from cache and managed collections
     *
     *	@param mergeContext current merge context
     *  @param removals list of entity instances to remove from the entity manager cache
     *  @param persists list of newly persisted entity instances
     */
    public void handleRemovalsAndPersists(MergeContext mergeContext, List<Object> removals, List<Object> persists) {
        for (Object removal : removals) {
            Object entity = getCachedObject(removal, true);
            if (entity == null) // Not found in local cache, cannot remove
                continue;

            if (mergeContext.getExternalDataSessionId() != null && !mergeContext.isResolvingConflict() 
                    && dirtyCheckContext.isEntityChanged(entity)) {
                // Conflict between externally received data and local modifications
                log.error("conflict with external data removal detected on %s", ObjectUtil.toString(entity));

                mergeContext.addConflict(entity, null, null);
            }
            else {
            	boolean saveMerging = mergeContext.isMerging();
            	try {
            		mergeContext.setMerging(true);
	            		
	                List<Object[]> owners = getOwnerEntities(entity);
	                if (owners != null) {
	                    for (Object[] owner : owners) {
	                        Object val = dataManager.getPropertyValue(owner[0], (String)owner[1]);
	                        if (val instanceof PersistentCollection && !((PersistentCollection<?>)val).wasInitialized())
	                            continue;
	                        if (val instanceof List<?>) {
	                            List<?> list = (List<?>)val;
	                            int idx = list.indexOf(entity);
	                            if (idx >= 0)
	                                list.remove(idx);
	                        }
	                        else if (val instanceof Collection<?>) {
	                            Collection<?> coll = (Collection<?>)val;
	                            if (coll.contains(entity))
	                                coll.remove(entity);
	                        }
	                        else if (val instanceof Map<?, ?>) {
	                            Map<?, ?> map = (Map<?, ?>)val;
	                            if (map.containsKey(entity))
	                                map.remove(entity);
	
	                            for (Iterator<?> ikey = map.keySet().iterator(); ikey.hasNext(); ) {
	                                Object key = ikey.next();
	                                if (ObjectUtil.objectEquals(dataManager, map.get(key), entity))
	                                    ikey.remove();
	                            }
	                        }
	                    }
	                }
	                
	                /* May not be necessary, should be cleaned up by weak reference */
	                Map<String, Object> pvalues = dataManager.getPropertyValues(entity, false, true);
	                for (Object val : pvalues.values()) {
	                    if (val instanceof Collection<?> || val instanceof Map<?, ?> || (val != null && val.getClass().isArray()))
	                        entityReferences.remove(val);
	                }
	                entityReferences.remove(entity);
	                
	                detach(entity, new IdentityHashMap<Object, Object>(), true);
            	}
				finally {
					mergeContext.setMerging(saveMerging);
				}
            }
        }
		
		dirtyCheckContext.fixRemovalsAndPersists(mergeContext, removals, persists);
    }
    
    
    private List<DataConflictListener> dataConflictListeners = new ArrayList<DataConflictListener>();
    
    public void addListener(DataConflictListener listener) {
        dataConflictListeners.add(listener);
    }
    
    public void removeListener(DataConflictListener listener) {
        dataConflictListeners.remove(listener);
    }

    /**
     *  Dispatch an event when last merge generated conflicts
     *   
     *	@param mergeContext current merge context
     */
    public void handleMergeConflicts(MergeContext mergeContext) {
        // Clear thread cache so acceptClient/acceptServer can work inside the conflicts handler
        // mergeContext.clearCache();
        mergeContext.initMergeConflicts();

        if (mergeContext.getMergeConflicts() != null) {
	        for (DataConflictListener listener : dataConflictListeners)
	            listener.onConflict(this, mergeContext.getMergeConflicts());
        }
    }
    
    /**
     *  Resolve merge conflicts
     * 
     *	@param mergeContext current merge context
     *  @param modifiedEntity the received entity
     *  @param localEntity the locally cached entity
     *  @param resolving true to keep client state
     */
    public void resolveMergeConflicts(MergeContext mergeContext, Object modifiedEntity, Object localEntity, boolean resolving) {
        try {
            mergeContext.setResolvingConflict(resolving);
            
            if (modifiedEntity == null)
                handleRemovalsAndPersists(mergeContext, Collections.singletonList(localEntity), Collections.emptyList());
            else
                mergeExternal(mergeContext, modifiedEntity, localEntity, null, null, false);
    
            mergeContext.checkConflictsResolved();
        }
        finally {
            mergeContext.setResolvingConflict(false);
        }
    }


    /**
     *  {@inheritDoc}
     */
    public Map<Object, Map<String, Object>> getSavedProperties() {
        return dirtyCheckContext.getSavedProperties();
    }

    /**
     *  {@inheritDoc}
     */
    public Map<String, Object> getSavedProperties(Object entity) {
        Object localEntity = getCachedObject(entity, true);
        if (localEntity == null)
            return null;
        return dirtyCheckContext.getSavedProperties(localEntity);
    }
    
    
    /**
     *  Default implementation of entity merge for simple ActionScript beans with public properties
     *  Can be used to implement Tide managed entities with simple objects
     *
     *	@param mergeContext current merge context
     *  @param obj source object
     *  @param dest destination object
     *  @param parent owning object
     *  @param propertyName property name of the owning object
     */ 
    public void defaultMerge(MergeContext mergeContext, Object obj, Object dest, Object parent, String propertyName) {
        // Merge internal state
    	if (isEntity(obj))
    		dataManager.copyProxyState(dest, obj);
        
    	// Don't merge version during conflict resolution
        Map<String, Object> pval = dataManager.getPropertyValues(obj, mergeContext.isResolvingConflict(), false);
        List<String> rw = new ArrayList<String>();
        
        boolean isEmbedded = isEntity(parent) && !isEntity(obj);
        for (Entry<String, Object> mval : pval.entrySet()) {
            String propName = mval.getKey();
            Object o = mval.getValue();
            Object d = dataManager.getPropertyValue(dest, propName);
            o = mergeExternal(mergeContext, o, d, isEmbedded ? parent : dest, isEmbedded ? propertyName + "." + propName : propName, false);
            if (o != d && mergeContext.isMergeUpdate())
                dataManager.setPropertyValue(dest, propName, o);
            
            rw.add(propName);
        }
        
        pval = dataManager.getPropertyValues(obj, mergeContext.isResolvingConflict(), true);
        for (Entry<String, Object> mval : pval.entrySet()) {
        	if (rw.contains(mval.getKey()))
        		continue;
            String propName = mval.getKey();
            Object o = mval.getValue();
            Object d = dataManager.getPropertyValue(dest, propName);
            if (isEntity(o) || isEntity(d))
                throw new IllegalStateException("Cannot merge the read-only property " + propName + " on bean " + obj + " with an Identifiable value, this will break local unicity and caching. Change property access to read-write.");  
            
            mergeExternal(mergeContext, o, d, parent != null ? parent : dest, propertyName != null ? propertyName + '.' + propName : propName, false);
        }
    }
    
	
    public boolean isEntityChanged(Object entity) {
        return dirtyCheckContext.isEntityChanged(entity);
    }
    
	public boolean isEntityDeepChanged(Object entity) {
		return dirtyCheckContext.isEntityDeepChanged(entity);
	}
    
    /**
     *  Discard changes of entity from last version received from the server
     *
     *  @param entity entity to restore
     */ 
    public void resetEntity(Object entity) {
    	if (entity == null)
    		throw new IllegalArgumentException("Entity cannot be null");
    	
    	EntityManager em = PersistenceManager.getEntityManager(entity);
    	if (em == null)
    		return;
    	
    	if (em != this)
    		throw new IllegalArgumentException("Cannot reset an entity attached to another entity manager " + entity);
    	
        Set<Object> cache = new HashSet<Object>();
        resetEntity(entity, cache);
    }

    private void resetEntity(Object entity, Set<Object> cache) {
        try {
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, null);
            // Disable dirty check during reset of entity
            mergeContext.setMerging(true);
            dirtyCheckContext.resetEntity(mergeContext, entity, entity, cache);
        }
        finally {
            MergeContext.destroy(this);
        }
    }

    /**
     *  Discard changes of all cached entities from last version received from the server
     */ 
    public void resetAllEntities() {
        try {
            Set<Object> cache = new HashSet<Object>();
            
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, null);
            // Disable dirty check during reset of entity
            mergeContext.setMerging(true);
            dirtyCheckContext.resetAllEntities(mergeContext, cache);
        }
        finally {
            MergeContext.destroy(this);
        }
    }
    
    /**
     *  {@inheritDoc}
     */ 
    public void acceptConflict(Conflict conflict, boolean client) {
        Object modifiedEntity = null;
        if (client) {
            // Copy the local entity to save local changes
            EntityManager entityManager = PersistenceManager.getEntityManager(conflict.getLocalEntity());
            EntityManager tmp = entityManager.newTemporaryEntityManager();
            modifiedEntity = tmp.mergeFromEntityManager(entityManager, conflict.getServerSession(), conflict.getLocalEntity(), null, false);
            tmp.clear();
        }
        else
            modifiedEntity = conflict.getReceivedEntity();

        try {
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, null);

            // Reset the local entity to its last stable state
            resetEntity(conflict.getLocalEntity());

            if (client) {
                // Merge with the incoming entity (to update version, id and all)
                if (conflict.getReceivedEntity() != null)
                    mergeExternal(mergeContext, conflict.getReceivedEntity(), conflict.getLocalEntity(), null, null, false);
            }

            // Finally reapply local changes on merged received result
            resolveMergeConflicts(mergeContext, modifiedEntity, conflict.getLocalEntity(), client);
        }
        finally {
            MergeContext.destroy(this);
        }
    }
    
    
    private RemoteInitializer remoteInitializer = null;
    
    @Override
    public void setRemoteInitializer(RemoteInitializer remoteInitializer) {
    	this.remoteInitializer = remoteInitializer;
    }
    
    /**
     *  {@inheritDoc}
     */
    public boolean initializeObject(ServerSession serverSession, Object entity, String propertyName, Object object) {
        boolean initialize = false;
        if (remoteInitializer != null)
            initialize = remoteInitializer.initializeObject(serverSession, entity, propertyName, object);

        return initialize;
    }

    
    public class DefaultTrackingHandler implements DataManager.TrackingHandler {
        
        /**
         *  Property change handler to save changes on embedded objects
         *
         *  @param target changed object
         *  @param property property name
         *  @param oldValue old value
         *  @param newValue new value
         */ 
        public void entityPropertyChangeHandler(Object target, String property, Object oldValue, Object newValue) {
            MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(target));
            if ((mergeContext != null && mergeContext.getSourceEntityManager() == this) || !isActive())
                return;
            
            if (newValue != oldValue) {
                if (isEntity(oldValue) || oldValue instanceof Collection<?> || oldValue instanceof Map<?, ?>) {
                    removeReference(oldValue, target, property);
                    dataManager.stopTracking(oldValue, target);
                }
                
                if (isEntity(newValue) || newValue instanceof Collection<?> || newValue instanceof Map<?, ?>) {
                    addReference(newValue, target, property);
                    dataManager.startTracking(newValue, target);
                }
            }
            
            log.debug("property changed: %s %s", ObjectUtil.toString(target), property);
            
            if (mergeContext == null || !mergeContext.isMerging() || mergeContext.isResolvingConflict()) {
                Object owner = isEntity(target) ? null : getOwnerEntity(target);
                if (owner == null)
                    dirtyCheckContext.entityPropertyChangeHandler(target, target, property, oldValue, newValue);
                else if (owner instanceof Object[] && isEntity(((Object[])owner)[0]))
                    dirtyCheckContext.entityPropertyChangeHandler(((Object[])owner)[0], target, property, oldValue, newValue);
            }
        }
        
        /**
         *  Collection change handler to save changes on collections
         *
         *  @param kind change kind
         *  @param target collection
         *  @param location location of change
         *  @param items changed items
         */ 
        public void collectionChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items) {
        }
        
        /**
         *  Collection change handler to save changes on owned collections
         *
         *  @param kind change kind
         *  @param target collection
         *  @param location location of change
         *  @param items changed items
         */ 
        public void entityCollectionChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items) {
            MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(target));
            if ((mergeContext != null && mergeContext.getSourceEntityManager() == this) || !isActive())
                return;
            
            int i = 0;
            
            Object[] parent = null;
            if (kind == ChangeKind.ADD && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                for (i = 0; i < items.length; i++) {
                    if (isEntity(items[i])) {
                        if (parent != null)
                            addReference(items[i], parent[0], (String)parent[1]);
                        else
                            attachEntity(items[i]);
                        dataManager.startTracking(items[i], parent != null ? parent[0] : null);
                    }
                }
            }
            else if (kind == ChangeKind.REMOVE && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                if (parent != null) {
                    for (i = 0; i < items.length; i++) {
                        if (isEntity(items[i]))
                            removeReference(items[i], parent[0], (String)parent[1]);
                    }
                }
            }
            else if (kind == ChangeKind.REPLACE && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                for (i = 0; i < items.length; i++) {
                    Object newValue = ((Object[])items[i])[1];
                    if (isEntity(newValue)) {
                        if (parent != null)
                            addReference(newValue, parent[0], (String)parent[1]);
                        else
                            attachEntity(newValue);
                        dataManager.startTracking(newValue, parent != null ? parent[0] : null);
                    }
                }
            }
            
            if (!(kind == ChangeKind.ADD || kind == ChangeKind.REMOVE || kind == ChangeKind.REPLACE))
                return;
            
            log.debug("collection changed: %s %s", kind, ObjectUtil.toString(target));
            
            if (mergeContext == null || !mergeContext.isMerging() || mergeContext.isResolvingConflict()) {
                if (parent == null)
                    log.warn("Owner entity not found for collection %s, cannot process dirty checking", ObjectUtil.toString(target));
                else
                    dirtyCheckContext.entityCollectionChangeHandler(parent[0], (String)parent[1], (Collection<?>)target, kind, location, items);
            }
        }
        
        /**
         *  Map change handler to save changes on maps
         *
         *  @param kind change kind
         *  @param target collection
         *  @param location location of change
         *  @param items changed items
         */ 
        public void mapChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items) {
        }
        
        /**
         *  Map change handler to save changes on owned maps
         *
         *  @param kind change kind
         *  @param target collection
         *  @param location location of change
         *  @param items changed items
         */ 
        public void entityMapChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items) {
            MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(target));
            if ((mergeContext != null && mergeContext.getSourceEntityManager() == this) || !isActive())
                return;
            
            Object[] parent = null;
            if (kind == ChangeKind.ADD && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                for (int i = 0; i < items.length; i++) {
                    if (isEntity(items[i])) {
                        if (parent != null)
                            addReference(items[i], parent[0], (String)parent[1]);
                        else
                            attachEntity(items[i]);
                        dataManager.startTracking(items[i], parent != null ? parent[0] : null);
                    }
                    else if (items[i] instanceof Object[]) {
                        Object[] obj = (Object[])items[i];
                        if (isEntity(obj[0])) {
                            if (parent != null)
                                addReference(obj[0], parent[0], (String)parent[1]);
                            else
                                attachEntity(obj[0]);
                            dataManager.startTracking(obj[0], parent != null ? parent[0] : null);
                        }
                        if (isEntity(obj[1])) {
                            if (parent != null)
                                addReference(obj[1], parent[0], (String)parent[1]);
                            else
                                attachEntity(obj[1]);
                            dataManager.startTracking(obj[1], parent != null ? parent[0] : null);
                        }
                    }
                }
            }
            else if (kind == ChangeKind.REMOVE && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                if (parent != null) {
                    for (int i = 0; i < items.length; i++) {
                        if (isEntity(items[i])) {
                            removeReference(items[i], parent[0], (String)parent[1]);
                        }
                        else if (items[i] instanceof Object[]) {
                            Object[] obj = (Object[])items[i];
                            if (isEntity(obj[0])) {
                                removeReference(obj[0], parent[0], (String)parent[1]);
                            }
                            if (isEntity(obj[1])) {
                                removeReference(obj[1], parent[0], (String)parent[1]);
                            }
                        }
                    }
                }
            }
            else if (kind == ChangeKind.REPLACE && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                for (int i = 0; i < items.length; i++) {
                    Object[] item = (Object[])items[i];
                    if (isEntity(item[1])) {
                        if (parent != null)
                            removeReference(item[1], parent[0], (String)parent[1]);
                    }
                    if (isEntity(item[2])) {
                        if (parent != null)
                            addReference(item[2], parent[0], (String)parent[1]);
                        else
                            attachEntity(item[2]);
                        dataManager.startTracking(item[2], parent != null ? parent[0] : null);
                    }
                }
            }
            
            if (!(kind == ChangeKind.ADD || kind == ChangeKind.REMOVE || kind == ChangeKind.REPLACE))
                return;
            
            log.debug("map changed: %s %s", kind, ObjectUtil.toString(target));
            
            if (mergeContext == null || !mergeContext.isMerging() || mergeContext.isResolvingConflict()) {
                if (parent == null)
                    log.warn("Owner entity not found for collection %s, cannot process dirty checking", ObjectUtil.toString(target));
                else
                    dirtyCheckContext.entityMapChangeHandler(parent[0], (String)parent[1], (Map<?, ?>)target, kind, items);
            }
        }
    }

    /**
     *  Handle data updates
     *
     *	@param mergeContext current merge context
     *  @param sourceSessionId sessionId from which data updates come (null when from current session) 
     *  @param updates list of data updates
     */
    public void handleUpdates(MergeContext mergeContext, String sourceSessionId, List<Update> updates) {
        List<Object> merges = new ArrayList<Object>();
        List<Object> removals = new ArrayList<Object>();
        List<Object> persists = new ArrayList<Object>();
        
        for (Update update : updates) {
            if (update.getKind() == UpdateKind.PERSIST || update.getKind() == UpdateKind.UPDATE)
                merges.add(update.getEntity());
            else if (update.getKind() == UpdateKind.REMOVE)
                removals.add(update.getEntity());
            if (update.getKind() == UpdateKind.PERSIST)
            	persists.add(update.getEntity());
        }
        
        mergeContext.setExternalDataSessionId(sourceSessionId);
        if (merges.size() == 1)
            internalMergeExternalData(mergeContext, merges.get(0), null, removals, persists);
        else if (merges.size() > 1)
            internalMergeExternalData(mergeContext, merges, null, removals, persists);
        else if (!removals.isEmpty() || !persists.isEmpty())
            internalMergeExternalData(mergeContext, null, null, removals, persists);
        
        for (Update update : updates) {
        	if (update.getEntity() instanceof String)
        		continue;
        	Object entity = getCachedObject(update.getEntity(), update.getKind() != UpdateKind.REMOVE);
        	if (entity != null)
        		update.setEntity(entity);
        }
    }
    
	public void raiseUpdateEvents(Context context, List<EntityManager.Update> updates) {
		List<String> refreshes = new ArrayList<String>();
		
		for (EntityManager.Update update : updates) {
			Object entity = update.getEntity();
			
			if (update.getKind() == UpdateKind.REFRESH) {
				String entityName = getUnqualifiedClassName((String)entity);
				refreshes.add(entityName);
			}
			else if (entity != null) {
				String entityName = entity instanceof EntityRef ? getUnqualifiedClassName(((EntityRef)entity).getClassName()) : entity.getClass().getSimpleName();
				String eventType = update.getKind().eventName() + "." + entityName;
				context.getEventBus().raiseEvent(context, eventType, entity);
				
				if (UpdateKind.PERSIST.equals(update.getKind()) || UpdateKind.REMOVE.equals(update.getKind())) {
					if (!refreshes.contains(entityName))
						refreshes.add(entityName);
				} 
			}
		}
		
		for (String refresh : refreshes)
			context.getEventBus().raiseEvent(context, UpdateKind.REFRESH.eventName() + "." + refresh);
	}
    
	private static String getUnqualifiedClassName(String className) {
		int idx = className.lastIndexOf(".");
		return idx >= 0 ? className.substring(idx+1) : className;
	}


    @Override
    public void setRemoteValidator(RemoteValidator remoteValidator) {
    }


    @Override
    public boolean validateObject(Object object, String property, Object value) {
        return false;
    }
}
