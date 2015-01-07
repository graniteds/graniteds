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
package org.granite.client.tide.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.tide.data.impl.ObjectUtil;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.server.ServerSession;
import org.granite.tide.IUID;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.CollectionChanges;

/**
 * 	ChangeSetBuilder is a utility class that builds a ChangeSet for an existing dirty context
 * 	or a particular entity instance
 *
 * 	@author William DRAI
 */
public class ChangeSetBuilder {

    private final EntityManager entityManager;
    private final DataManager dataManager;
    private final ServerSession serverSession;
    private final ClientAliasRegistry aliasRegistry;
    private final Map<Object, Map<String, Object>> savedProperties;
    private final boolean local;

    private EntityManager tmpEntityManager = null;


    public ChangeSetBuilder(EntityManager entityManager, ServerSession serverSession) {
        this(entityManager, serverSession, true);
    }

    public ChangeSetBuilder(EntityManager entityManager, ServerSession serverSession, boolean local) {
        this.entityManager = entityManager;
        this.dataManager = entityManager.getDataManager();
        this.serverSession = serverSession;
        this.aliasRegistry = serverSession.getAliasRegistry();
        this.savedProperties = entityManager.getSavedProperties();
        this.local = local;
        
        // Temporary context to store complete entities so we can uninitialize all collections
        // when possible
        tmpEntityManager = entityManager.newTemporaryEntityManager();
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
            sp.put(dataManager.getVersionPropertyName(entity), dataManager.getVersion(entity));
            entitySavedProperties.put(entity, sp);
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
    
    private String getType(String alias) {
    	String className = aliasRegistry.getTypeForAlias(alias);
    	return className != null ? className : alias;
    }
    
    private String getAlias(String className) {
    	String alias = aliasRegistry.getAliasForType(className);
    	return alias != null ? alias : className;
    }
    
    
    private boolean isForEntity(Change change, Object entity) {
        return entity.getClass().getName().equals(getType(change.getClassName())) && change.getUid().equals(dataManager.getUid(entity));
    }
    
    private boolean isEntity(Object entity) {
        return entityManager.getDataManager().isEntity(entity);
    }


    private void collectEntitySavedProperties(Object entity, Map<Object, Map<String, Object>> savedProperties, Map<Object, Boolean> cache) {
        if (cache == null)
            cache = new IdentityHashMap<Object, Boolean>();
        else if (cache.containsKey(entity))
            return;

        cache.put(entity, true);
        if (isEntity(entity) && this.savedProperties.containsKey(entity))
            savedProperties.put(entity, this.savedProperties.get(entity));

        Map<String, Object> properties = dataManager.getPropertyValues(entity, false, false);
        for (Object v : properties.values()) {
            if (v == null)
                continue;
            if (isEntity(entity) && !dataManager.isInitialized(v))
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

    @SuppressWarnings("unchecked")
	private ChangeSet internalBuildChangeSet(Map<Object, Map<String, Object>> savedProperties) {
        ChangeSet changeSet = new ChangeSet(new Change[0], local);
        Map<Object, Object> changeMap = new IdentityHashMap<Object, Object>();

        for (Map.Entry<Object, Map<String, Object>> mes : savedProperties.entrySet()) {
            Object entity = mes.getKey();
//            if (savedProperties[entity] is Array)
//            entity = _context.meta_getOwnerEntity(entity);

            if (!isEntity(entity) && !(entity instanceof IUID))
                entity = entityManager.getOwnerEntity(entity);

            if (changeMap.containsKey(entity))
                continue;

            Map<String, Object> save = mes.getValue();

            Change change = null;
            String versionPropertyName = null;
            if (isEntity(entity)) {
                versionPropertyName = dataManager.getVersionPropertyName(entity);
                // Unsaved objects should not be part of the ChangeSet
                if (save.get(versionPropertyName) == null)
                    continue;

                change = new Change(getAlias(entity.getClass().getName()),
                		dataManager.hasIdProperty(entity) ? (Serializable)dataManager.getId(entity) : null,
                        (Number)dataManager.getVersion(entity), dataManager.getUid(entity),
                        local);
            }
            else if (entity instanceof IUID) {
                change = new Change(getAlias(entity.getClass().getName()), null, null, dataManager.getUid(entity), local);
            }
            if (change == null) {
                changeMap.put(entity, false);
                continue;
            }

            changeMap.put(entity, change);
            Change[] changes = new Change[changeSet.getChanges().length+1];
            System.arraycopy(changeSet.getChanges(), 0, changes, 0, changeSet.getChanges().length);
            changes[changeSet.getChanges().length] = change;
            changeSet.setChanges(changes);

            for (Map.Entry<String, Object> me : dataManager.getPropertyValues(entity, true, true, false ).entrySet()) {
                String p = me.getKey();
                Object v = me.getValue();
                if (save != null && save.containsKey(p)) {
                    if (v instanceof Collection<?> || v instanceof Map<?, ?>) {
                        List<?> collSnapshot = (List<?>)save.get(p);
                        CollectionChanges collChanges = new CollectionChanges();
                        change.getChanges().put(p, collChanges);

                        List<Object[]> diffOps = null;
                        if (v instanceof Map<?, ?>)
                            diffOps = Utils.diffMaps(Utils.convertMapSnapshot((List<Object[]>)collSnapshot), (Map<?, ?>)v);
                        else if (v instanceof List<?>)
                            diffOps = Utils.diffLists(collSnapshot, (List<?>)v);
                        else
                            diffOps = Utils.diffColls(collSnapshot, (Collection<?>)v);

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
        tmpEntityManager.clear();

        return changeSet;
    }

    private Object buildRef(Object object) {
        if (!isEntity(object))
        	return object;
    
        if (!dataManager.hasVersionProperty(object))
            throw new IllegalArgumentException("Cannot build ChangeSet for non versioned entity " + object.getClass().getName());
        
        if (dataManager.getVersion(object) != null)
            return new ChangeRef(getAlias(object.getClass().getName()), dataManager.getUid(object), (Serializable)dataManager.getId(object));
        
        // Force attachment/init of uids of ref object in case some deep elements in the graph are not yet managed in the current context
        // So the next merge in the tmp context does not attach newly added objects to the tmp context
        entityManager.attach(object);
        return tmpEntityManager.mergeFromEntityManager(entityManager, serverSession, object, null, true);
    }
}
