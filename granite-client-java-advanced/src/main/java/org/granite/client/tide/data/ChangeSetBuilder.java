/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide.data;

import org.granite.client.tide.Context;
import org.granite.client.tide.data.impl.ObjectUtil;
import org.granite.logging.Logger;
import org.granite.tide.IUID;
import org.granite.tide.data.*;

import java.io.Serializable;
import java.util.*;

/**
 * 	ChangeSetBuilder is a utility class that builds a ChangeSet for an existing dirty context
 * 	or a particular entity instance
 *
 * 	@author William DRAI
 */
public class ChangeSetBuilder {

    private static final Logger log = Logger.getLogger(ChangeSetBuilder.class);

    private final Context context;
    private final Map<Object, Map<String, Object>> savedProperties;
    private final boolean local;

    private Context tmpContext = null;


    public ChangeSetBuilder(Context context) {
        this(context, true);
    }

    public ChangeSetBuilder(Context context, boolean local) {
        this.context = context;
        this.savedProperties = context.getEntityManager().getSavedProperties();
        this.local = local;

        // Temporary context to store complete entities so we can uninitialize all collections
        // when possible
        tmpContext = context.newTemporaryContext();
    }

    /**
     *  Build a ChangeSet object for the current context
     *
     * 	@return the change set for this context
     */
    public ChangeSet buildChangeSet() {
        return internalBuildChangeSet(savedProperties);
    }

    /**
     *  Build a Change object for the entity in the current context
     *
     * 	@return the change for this entity in the context
     */
    public ChangeSet buildEntityChangeSet(Object entity) {
        Map<Object, Map<String, Object>> entitySavedProperties = new IdentityHashMap<Object, Map<String, Object>>();
        collectEntitySavedProperties(entity, entitySavedProperties, null);
        if (!entitySavedProperties.containsKey(entity)) {
            Map<String, Object> sp = new HashMap<String, Object>();
            sp.put(context.getDataManager().getVersionPropertyName(entity), context.getDataManager().getVersion(entity));
        }

        ChangeSet changeSet = internalBuildChangeSet(entitySavedProperties);

        // Place Change for initial entity first
        int pos = 0;
        for (int i = 0; i < changeSet.getChanges().length; i++) {
            if (isForEntity(changeSet.getChanges()[i], entity)) {
                pos = i;
                break;
            }
        }
        if (pos > 0) {
            Change change = changeSet.getChanges()[pos];
            System.arraycopy(changeSet.getChanges(), 0, changeSet.getChanges(), 1, pos);
            changeSet.getChanges()[0] = change;
        }
        return changeSet;
    }

    private boolean isForEntity(Change change, Object entity) {
        return entity.getClass().getName().equals(change.getClassName()) && change.getUid().equals(context.getDataManager().getUid(entity));
    }

    private boolean isEntity(Object entity) {
        return context.getDataManager().isEntity(entity);
    }


    private void collectEntitySavedProperties(Object entity, Map<Object, Map<String, Object>> savedProperties, Map<Object, Boolean> cache) {
        if (cache == null)
            cache = new IdentityHashMap();
        else if (cache.containsKey(entity))
            return;

        cache.put(entity, true);
        if (isEntity(entity) && this.savedProperties.containsKey(entity))
            savedProperties.put(entity, this.savedProperties.get(entity));

        Map<String, Object> properties = context.getDataManager().getPropertyValues(entity, false, false);
        for (Map.Entry<String, Object> mep : properties.entrySet()) {
            String p = mep.getKey();
            Object v = mep.getValue();
            if (v == null)
                continue;
            if (isEntity(entity) && !context.getDataManager().isInitialized(v))
                continue;

            if (v instanceof Collection<?>) {
                for (Object e : ((Collection<?>)v))
                    collectEntitySavedProperties(e, savedProperties, cache);
            }
            else if (v instanceof Map<?, ?>) {
                for (Map.Entry<?, ?> me : ((Map<?, ?>)v).entrySet()) {
                    collectEntitySavedProperties(me.getKey(), savedProperties, cache);
                    collectEntitySavedProperties(me.getValue(), savedProperties, cache);
                }
            }
            else if (!ObjectUtil.isSimple(v) && !(v instanceof Enum || v instanceof Value || v instanceof byte[])) {
                collectEntitySavedProperties(v, savedProperties, cache);
            }
        }
    }

    private ChangeSet internalBuildChangeSet(Map<Object, Map<String, Object>> savedProperties) {
        ChangeSet changeSet = new ChangeSet(new Change[0], local);
        Map<Object, Object> changeMap = new IdentityHashMap<Object, Object>();

        for (Map.Entry<Object, Map<String, Object>> mes : savedProperties.entrySet()) {
            Object entity = mes.getKey();
//            if (savedProperties[entity] is Array)
//            entity = _context.meta_getOwnerEntity(entity);

            if (!isEntity(entity) && !(entity instanceof IUID))
                entity = context.getEntityManager().getOwnerEntity(entity);

            if (changeMap.containsKey(entity))
                continue;

            Map<String, Object> save = mes.getValue();

            Change change = null;
            String versionPropertyName = null;
            if (isEntity(entity)) {
                versionPropertyName = context.getDataManager().getVersionPropertyName(entity);
                // Unsaved objects should not be part of the ChangeSet
                if (save.get(versionPropertyName) == null)
                    continue;

                change = new Change(entity.getClass().getName(),
                        context.getDataManager().hasIdProperty(entity) ? (Serializable)context.getDataManager().getId(entity) : null,
                        (Number)context.getDataManager().getVersion(entity),
                        context.getDataManager().getUid(entity),
                        local);
            }
            else if (entity instanceof IUID) {
                change = new Change(entity.getClass().getName(), null, null, context.getDataManager().getUid(entity), local);
            }
            if (change == null) {
                changeMap.put(entity, false);
                continue;
            }

            changeMap.put(entity, change);
            Change[] changes = new Change[changeSet.getChanges().length+1];
            System.arraycopy(changeSet.getChanges(), 0, changes, 0, changeSet.getChanges().length);
            changeSet.getChanges()[changeSet.getChanges().length] = change;
            changeSet.setChanges(changes);

            for (Map.Entry<String, Object> me : context.getDataManager().getPropertyValues(entity, true, true, false ).entrySet()) {
                String p = me.getKey();
                Object v = me.getValue();
                if (save != null && save.containsKey(p)) {
                    if (v instanceof Collection<?>) {
                        List<?> collSnapshot = (List<?>)save.get(p);
                        CollectionChanges collChanges = new CollectionChanges();
                        change.getChanges().put(p, collChanges);

                        List<Object[]> diffOps = null;
                        if (v instanceof Map<?, ?>)
                            diffOps = DataUtils.diffMaps(DataUtils.convertMapSnapshot((List<Object[]>)collSnapshot), (Map<?, ?>)v);
                        else if (v instanceof List<?>)
                            diffOps = DataUtils.diffLists(collSnapshot, (List<?>)v);
                        else
                            diffOps = DataUtils.diffColls(collSnapshot, (Collection<?>)v);

                        CollectionChange[] collectionChanges = new CollectionChange[diffOps.size()];
                        int idx = 0;
                        for (Object[] op : diffOps)
                            collectionChanges[idx++] = new CollectionChange((Integer)op[0], buildRef(op[1]), buildRef(op[2]));

                        collChanges.setChanges(collectionChanges);
                    }
                    else {
                        change.getChanges().put(p, buildRef(v));
                    }
                }
                else if (savedProperties.containsKey(v) && !isEntity(v) && !(v instanceof IUID)) {
                    // Embedded objects
                    change.getChanges().put(p, v);
                    changeMap.put(v, false);
                }
            }
        }

        // Cleanup tmp context to detach all new entities
        tmpContext.clear();

        return changeSet;
    }

    private Object buildRef(Object object) {
        if (isEntity(object)) {
            if (!context.getDataManager().hasVersionProperty(object))
                throw new IllegalArgumentException("Cannot build ChangeSet for non versioned entity " + object.getClass().getName());

            if (context.getDataManager().getVersion(object) != null)
                return new ChangeRef(object.getClass().getName(), context.getDataManager().getUid(object), (Serializable)context.getDataManager().getId(object));
            else {
                // Force attachment/init of uids of ref object in case some deep elements in the graph are not yet managed in the current context
                // So the next merge in the tmp context does not attach newly added objects to the tmp context
                context.getEntityManager().attach(object);
                return tmpContext.getEntityManager().mergeFromEntityManager(context.getEntityManager(), object, null, true);
            }
        }
        return object;
    }
}
