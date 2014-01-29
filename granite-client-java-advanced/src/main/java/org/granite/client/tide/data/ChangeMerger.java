package org.granite.client.tide.data;

import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.impl.ObjectUtil;
import org.granite.client.tide.data.spi.EntityRef;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.logging.Logger;
import org.granite.tide.data.*;
import org.granite.util.TypeUtil;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by william on 10/01/14.
 */
public class ChangeMerger implements DataMerger {

    private static Logger log = Logger.getLogger(ChangeMerger.class);

    /**
     * 	Should return true if this merger is able to handle the specified object
     *
     *  @param obj an object
     *  @return true if object can be handled
     */
    public boolean accepts(Object obj) {
        return obj instanceof ChangeSet || obj instanceof Change;
    }

    private boolean isForEntity(MergeContext mergeContext, Change change, Object entity) {
        return entity.getClass().getName().equals(change.getClassName()) && change.getUid().equals(mergeContext.getDataManager().getUid(entity));
    }
    private boolean isForEntity(MergeContext mergeContext, ChangeRef changeRef, Object entity) {
        return entity.getClass().getName().equals(changeRef.getClassName()) && changeRef.getUid().equals(mergeContext.getDataManager().getUid(entity));
    }

    /**
     *  Merge an entity coming from the server in the entity manager
     *
     *  @param mergeContext current merge context
     *  @param changeSet incoming change/changeSet
     *  @param previous previously existing object in the context (null if no existing object)
     *  @param parent parent object for collections
     *  @param propertyName property name of the collection in the owner object
     *
     *  @return merged entity (=== previous when previous not null)
     */
    public Object merge(MergeContext mergeContext, Object changeSet, Object previous, Object parent, String propertyName) {
        if (changeSet != null || previous != null)
            log.debug("merge Change: {0} previous {1} (change)", ObjectUtil.toString(changeSet), ObjectUtil.toString(previous));

        Object next = null;

        // Local ChangeSet should not be replaced by its context value
        if (changeSet instanceof ChangeSet && ((ChangeSet)changeSet).isLocal())
            next = changeSet;

        Change[] changes = changeSet instanceof ChangeSet ? ((ChangeSet)changeSet).getChanges() : new Change[] { (Change)changeSet };
        boolean local = changeSet instanceof ChangeSet ? ((ChangeSet)changeSet).isLocal() : ((Change)changeSet).isLocal();

        for (Change change : changes) {

            if (change.isLocal() && next == null) {
                // Changes built locally must not be replaced merged
                next = change;
            }

            Object dest = mergeContext.getCachedObject(change);
            if (dest == null) {
                // Entity not found locally : nothing to do, we can't apply incremental changes
                log.warn("Incoming change received for unknown entity {0}", change.getClassName() + ":" + change.getUid());
                continue;
            }

            if (dest != previous && previous != null && !isForEntity(mergeContext, change, previous)) {
                // Cannot apply changes if provided change has not the same uid than the previous object
                continue;
            }

            boolean saveSkipDirtyCheck, saveUninitAllowed;

            if (local) {
                saveSkipDirtyCheck = mergeContext.isSkipDirtyCheck();
                saveUninitAllowed = mergeContext.isUninitializeAllowed();
                try {
                    mergeContext.setSkipDirtyCheck(true);
                    mergeContext.setUninitializeAllowed(false);

                    // Changes built locally just need to have their referenced content merged
                    // to initialize their uid and attach them to the local context
                    for (Map.Entry<String, Object> me : change.getChanges().entrySet()) {
                        String p = me.getKey();
                        Object val = me.getValue();

                        if (val instanceof CollectionChanges) {
                            for (CollectionChange cc : ((CollectionChanges)val).getChanges()) {
                                if (cc.getKey() != null && !(cc.getKey() instanceof EntityRef))
                                    mergeContext.mergeExternal(cc.getKey(), null, dest, p);
                                if (cc.getValue() != null && !(cc.getValue() instanceof EntityRef))
                                    mergeContext.mergeExternal(cc.getValue(), null, dest, p);
                            }
                        }
                        else
                            mergeContext.mergeExternal(val, null, dest, p);
                    }
                }
                finally {
                    mergeContext.setUninitializeAllowed(saveUninitAllowed);
                    mergeContext.setSkipDirtyCheck(saveSkipDirtyCheck);
                }

                continue;
            }

            if (next == null)
                next = dest;

            saveUninitAllowed = mergeContext.isUninitializeAllowed();
            try {
                mergeContext.setUninitializeAllowed(false);

                Map<String, Object> mergedChanges = new HashMap<String, Object>();
                Object templateObject = TypeUtil.newInstance(dest.getClass(), dest.getClass());
                Object incomingEntity = lookupEntity(mergeContext, change.getChanges(), dest, null);

                // Create an entity proxy for the current processed target and apply changes on it
                for (Map.Entry<String, Object> me : change.getChanges().entrySet()) {
                    String p = me.getKey();
                    Object val = me.getValue();

                    if (val instanceof CollectionChanges) {
                        Object coll = mergeContext.getDataManager().getPropertyValue(dest, p);
                        if (coll instanceof PersistentCollection && !((PersistentCollection)coll).wasInitialized()) {
                            // Cannot update an uninitialized collection
                            log.debug("Incoming change for uninitialized collection {0}:{1}.{2}", change.getClassName(), change.getUid(), p);
                            continue;
                        }

                        String cacheKey = "CollChange::" + dest.getClass().getName() + ":" + mergeContext.getDataManager().getUid(dest) + "." + p;
                        if (mergeContext.getCachedMerge(cacheKey) != null) {
                            log.warn("Incoming change skipped {0}:{1}.{2}, already processed", change.getClassName(), change.getUid(), p);
                            continue;
                        }
                        mergeContext.pushMerge(cacheKey, coll, false);

                        Map<String, Object> saved = mergeContext.getSavedProperties(dest);
                        boolean unsaved = mergeContext.isUnsaved(dest);
                        Object receivedEntity;

                        if (coll instanceof List<?>) {
                            List<Object> mergedColl = null;
                            receivedEntity = lookupEntity(mergeContext, val, dest, null);
                            // Check if we can find the complete initialized list in the incoming changes and use it instead of incremental updates
                            if (receivedEntity != null && mergeContext.getDataManager().getPropertyValue(receivedEntity, p) instanceof PersistentCollection
                                    && ((PersistentCollection)mergeContext.getDataManager().getPropertyValue(receivedEntity, p)).wasInitialized())
                                mergedColl = (List<Object>)mergeContext.getDataManager().getPropertyValue(receivedEntity, p);
                            else {
                                Object target = coll instanceof PropertyHolder ? ((PropertyHolder)coll).getObject() : coll;
                                mergedColl = TypeUtil.newInstance(target.getClass(), List.class);
                                if (!unsaved)
                                    mergedColl.addAll((List<?>)coll);

                                applyListChanges(mergeContext, mergedColl, (CollectionChanges)val, saved != null && saved.get(p) instanceof List<?> ? (List<Object>)saved.get(p) : null);
                            }

                            mergedChanges.put(p, mergedColl);
                        }
                        else if (coll instanceof Map<?, ?>) {
                            Map<Object, Object> mergedMap = null;
                            receivedEntity = lookupEntity(mergeContext, val, dest, null);
                            // Check if we can find the complete initialized map in the incoming changes and use it instead of incremental updates
                            if (receivedEntity != null && mergeContext.getDataManager().getPropertyValue(receivedEntity, p) instanceof PersistentCollection
                                    && ((PersistentCollection)mergeContext.getDataManager().getPropertyValue(receivedEntity, p)).wasInitialized())
                                mergedMap = (Map<Object, Object>)mergeContext.getDataManager().getPropertyValue(receivedEntity, p);
                            else {
                                Object target = coll instanceof PropertyHolder ? ((PropertyHolder)coll).getObject() : coll;
                                mergedMap = TypeUtil.newInstance(target.getClass(), Map.class);
                                if (!unsaved)
                                    mergedMap.putAll((Map<?, ?>)coll);

                                applyMapChanges(mergeContext, mergedMap, (CollectionChanges)val, saved != null && saved.get(p) instanceof List<?> ? (List<Object[]>)saved.get(p) : null);
                            }

                            mergedChanges.put(p, mergedMap);
                        }
                    }
                    else
                        mergedChanges.put(p, val);
                }

                String versionPropertyName = mergeContext.getDataManager().getVersionPropertyName(TypeUtil.forName(change.getClassName()));
                Number version = (Number)change.getChanges().get(versionPropertyName);
                // If dest version is greater than received change, use it instead
                // That means that the received Change change is probably inconsistent with its content
                if (incomingEntity != null && mergeContext.getDataManager().getVersion(incomingEntity) != null && (version == null || ((Number)mergeContext.getDataManager().getVersion(incomingEntity)).longValue() > version.longValue()))
                    version = (Number)mergeContext.getDataManager().getVersion(incomingEntity);

//                ChangeProxy changeProxy = new ChangeProxy(change.getUid(), mergeContext.getDataManager().getIdPropertyName(TypeUtil.forName(change.getClassName())),
//                        change.getId(), mergeContext.getDataManager().
//                        mergeContext.getDataManager().getVersionPropertyName(TypeUtil.forName(change.getClassName())), version, mergedChanges, templateObject);
//
//                // Merge the proxy (only actual changes will be merged, values not in mergedChanges will be ignored)
//                mergeContext.mergeExternal(changeProxy, dest, parent, propertyName);

                // Ensure updated collections/maps will be processed only once
                // Mark them in the current merge cache
                for (String p : mergedChanges.keySet()) {
                    Object v = mergeContext.getDataManager().getPropertyValue(dest, p);
                    if (v instanceof Collection<?> || v instanceof Map<?, ?>)
                        mergeContext.pushMerge(v, v, false);
                }
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Received Change for unknown class", e);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException("Error instantiating class", e);
            }
            catch (InstantiationException e) {
                throw new RuntimeException("Error instantiating class", e);
            }
            finally {
                mergeContext.setUninitializeAllowed(saveUninitAllowed);
            }

            if (dest != null)
                log.debug("merge change result: {0}", ObjectUtil.toString(dest));
        }

        return next;
    }


    private void applyListChanges(MergeContext mergeContext, List<Object> coll, CollectionChanges ccs, List<Object> savedArray) {
        if (savedArray != null) {
            // If list has been modified locally, apply received operations to the current saved snapshot
            List<Object> savedList = new ArrayList<Object>(savedArray);

            for (CollectionChange cc : ccs.getChanges()) {
                if (cc.getType() == -1) {
                    if (cc.getKey() != null && (Integer)cc.getKey() >= 0 && cc.getValue() instanceof ChangeRef
                            && isForEntity(mergeContext, (ChangeRef) cc.getValue(), savedList.get((Integer)cc.getKey())))
                        savedList.remove((Integer)cc.getKey());
                    else if (cc.getKey() != null && (Integer)cc.getKey() >= 0 && mergeContext.objectEquals(cc.getValue(), savedList.get((Integer)cc.getKey())))
                        savedList.remove((Integer)cc.getKey());
                    else if (cc.getKey() == null && cc.getValue() instanceof ChangeRef) {
                        for (int i = 0; i < savedList.size(); i++) {
                            if (isForEntity(mergeContext, (ChangeRef)cc.getValue(), savedList.get(i))) {
                                savedList.remove(i);
                                i--;
                            }
                        }
                    }
                    else if (cc.getKey() == null) {
                        for (int i = 0; i < savedList.size(); i++) {
                            if (mergeContext.objectEquals(cc.getValue(), savedList.get(i))) {
                                savedList.remove(i);
                                i--;
                            }
                        }
                    }
                }
                else if (cc.getType() == 1) {
                    if (cc.getKey() != null && (Integer)cc.getKey() >= 0)
                        savedList.add((Integer) cc.getKey(), cc.getValue());
                    else if (cc.getKey() != null)
                        savedList.add(cc.getValue());
                    else
                        savedList.add(cc.getValue());
                }
                else if (cc.getType() == 0 && cc.getKey() != null && (Integer)cc.getKey() >= 0) {
                    savedList.set((Integer)cc.getKey(), cc.getValue());
                }
            }

            // Replace local objects by received objects in merged collection
            for (int i = 0; i < coll.size(); i++) {
                for (Object e : savedList) {
                    if (mergeContext.objectEquals(coll.get(i), e) && coll.get(i) != e) {
                        coll.set(i, e);
                        break;
                    }
                }
            }

            savedArray.clear();
            savedArray.addAll(savedList);
        }
        else {
            // If list has not been modified locally, apply received operations to the current collection content
            for (CollectionChange cc : ccs.getChanges()) {
                if (cc.getType() == -1) {
                    if (cc.getKey() != null && (Integer)cc.getKey() >= 0 && cc.getValue() instanceof ChangeRef
                        && isForEntity(mergeContext, (ChangeRef)cc.getValue(), coll.get((Integer)cc.getKey())))
                        coll.remove((Integer)cc.getKey());
                    else if (cc.getKey() != null && (Integer)cc.getKey() >= 0 && mergeContext.objectEquals(cc.getValue(), coll.get((Integer)cc.getKey())))
                        coll.remove((Integer)cc.getKey());
                    else if (cc.getKey() == null && cc.getValue() instanceof ChangeRef) {
                        for (int i = 0; i < coll.size(); i++) {
                            if (isForEntity(mergeContext, (ChangeRef) cc.getValue(), coll.get(i))) {
                                coll.remove(i);
                                i--;
                            }
                        }
                    }
                    else if (cc.getKey() == null) {
                        for (int i = 0; i < coll.size(); i++) {
                            if (isForEntity(mergeContext, (ChangeRef) cc.getValue(), coll.get(i))) {
                                coll.remove(i);
                                i--;
                            }
                        }
                    }
                }
                else if (cc.getType() == 1) {
                    if (cc.getKey() != null && (Integer)cc.getKey() >= 0)
                        coll.add((Integer) cc.getKey(), cc.getValue());
                    else if (cc.getKey() != null)
                        coll.add(cc.getValue());
                    else
                        coll.add(cc.getValue());
                }
                else if (cc.getType() == 0 && cc.getKey() != null && (Integer)cc.getKey() >= 0) {
                    coll.set((Integer) cc.getKey(), cc.getValue());
                }
            }
        }
    }

    private void applyMapChanges(MergeContext mergeContext, Map<Object, Object> map, CollectionChanges ccs, List<Object[]> savedArray) {
        if (savedArray != null) {
            // If map has been modified locally, apply received operations to the current saved snapshot
            Map<Object, Object> savedMap = new HashMap<Object, Object>();
            for (Object[] se : savedArray)
                savedMap.put(se[0], se[1]);

            for (CollectionChange cc : ccs.getChanges()) {
                Object key = cc.getKey() instanceof ChangeRef ? mergeContext.getCachedObject(cc.getKey()) : cc.getKey();
                if (cc.getType() == -1) {
                    if (key != null && cc.getValue() instanceof ChangeRef && isForEntity(mergeContext, (ChangeRef)cc.getValue(), savedMap.get(key)))
                        savedMap.remove(key);
                    else if (key != null && mergeContext.objectEquals(cc.getValue(), savedMap.get(key)))
                        savedMap.remove(key);
                }
                else if (cc.getType() == 0 || cc.getType() == 1) {
                    savedMap.put(key, cc.getValue());
                }
            }

            // Replace local objects by received objects in merged map
            for (Map.Entry me : map.entrySet()) {
                Object key = me.getKey();
                Object value = me.getValue();
                for (Object k : savedMap.keySet()) {
                    if (mergeContext.objectEquals(key, k) && key != k) {
                        map.remove(key);
                        key = k;
                        map.put(key, value);
                    }
                    if (mergeContext.objectEquals(value, k) && value != k) {
                        value = k;
                        map.put(key, value);
                    }
                    Object v = savedMap.get(k);
                    if (mergeContext.objectEquals(key, v) && key != v) {
                        map.remove(key);
                        key = v;
                        map.put(key, value);
                    }
                    if (mergeContext.objectEquals(value, v) && value != v) {
                        value = v;
                        map.put(key, value);
                    }
                }
            }

            savedArray.clear();
            for (Map.Entry<Object, Object> me : savedMap.entrySet())
                savedArray.add(new Object[] { me.getKey(), me.getValue() });
        }
        else {
            // Map has not been modified, just apply received operations to current content
            for (CollectionChange cc : ccs.getChanges()) {
                Object key = cc.getKey() instanceof ChangeRef ? mergeContext.getCachedObject(cc.getKey()) : cc.getKey();

                if (cc.getType() == -1) {
                    if (key != null && cc.getValue() instanceof ChangeRef && isForEntity(mergeContext, (ChangeRef)cc.getValue(), map.get(key)))
                        map.remove(key);
                    else if (key != null && mergeContext.objectEquals(cc.getValue(), map.get(key)))
                        map.remove(key);
                }
                else if (cc.getType() == 0 || cc.getType() == 1) {
                    // Not found in local changes, apply remote change
                    map.put(key, cc.getValue());
                }
            }
        }
    }


    private Object lookupEntity(MergeContext mergeContext, Object graph, Object obj, IdentityHashMap<Object, Boolean> cache) {
        if (!(graph.getClass().isArray()) && (ObjectUtil.isSimple(graph) || graph instanceof Value || graph instanceof byte[] || graph instanceof Enum))
            return null;

        if (cache == null)
            cache = new IdentityHashMap<Object, Boolean>();

        if (cache.containsKey(graph))
            return null;
        cache.put(graph, true);

        if (mergeContext.getDataManager().isEntity(graph) && !mergeContext.getDataManager().isInitialized(graph))
            return null;

        if (mergeContext.objectEquals(graph, obj) && graph != obj)
            return graph;

        Object found = null;
        if (graph instanceof CollectionChanges) {
            for (CollectionChange cc : ((CollectionChanges)graph).getChanges()) {
                found = lookupEntity(mergeContext, cc, obj, cache);
                if (found != null)
                    return found;
            }
        }
        else if (graph instanceof CollectionChange) {
            if (((CollectionChange)graph).getKey() != null) {
                found = lookupEntity(mergeContext, ((CollectionChange)graph).getKey(), obj, cache);
                if (found != null)
                    return found;
            }
            if (((CollectionChange)graph).getValue() != null) {
                found = lookupEntity(mergeContext, ((CollectionChange)graph).getValue(), obj, cache);
                if (found != null)
                    return found;
            }
            return null;
        }

        if (graph instanceof PersistentCollection && !((PersistentCollection)graph).wasInitialized())
            return null;
        if (graph.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(graph); i++) {
                found = lookupEntity(mergeContext, Array.get(graph, i), obj, cache);
                if (found != null)
                    return found;
            }
        }
        else if (graph instanceof Collection<?>) {
            for (Object elt : ((Collection<?>)graph)) {
                found = lookupEntity(mergeContext, elt, obj, cache);
                if (found != null)
                    return found;
            }
            return null;
        }
        else if (graph instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> me : ((Map<?, ?>)graph).entrySet()) {
                found = lookupEntity(mergeContext, me.getKey(), obj, cache);
                if (found != null)
                    return found;
                found = lookupEntity(mergeContext, me.getValue(), obj, cache);
                if (found != null)
                    return found;
            }
            return null;
        }
        else {
            for (Object v : mergeContext.getDataManager().getPropertyValues(graph, true, false, true).values()) {
                found = lookupEntity(mergeContext, v, obj, cache);
                if (found != null)
                    return found;
            }
            return null;
        }

        return null;
    }
}
