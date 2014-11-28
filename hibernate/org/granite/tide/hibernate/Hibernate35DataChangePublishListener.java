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

package org.granite.tide.hibernate;

import static org.granite.util.Reflections.get;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Entity;

import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataContext.EntityUpdate;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.granite.tide.data.DataUtils;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.CollectionEntry;
import org.hibernate.event.FlushEntityEvent;
import org.hibernate.event.FlushEntityEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreCollectionUpdateEvent;
import org.hibernate.event.PreCollectionUpdateEventListener;
import org.hibernate.event.def.DefaultFlushEntityEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author William Draï
 */
public class Hibernate35DataChangePublishListener implements PostInsertEventListener, PostUpdateEventListener,
        PostDeleteEventListener, PreCollectionUpdateEventListener, FlushEntityEventListener {
	
	private static final long serialVersionUID = 1L;
	
	private DefaultFlushEntityEventListener defaultFlushEntityEventListener = new DefaultFlushEntityEventListener();


    public void onPostInsert(PostInsertEvent event) {
        DataContext.addUpdate(EntityUpdateType.PERSIST, event.getEntity());
    }

    public void onPostUpdate(PostUpdateEvent event) {
    	int[] dirtyProperties = (int[])DataContext.getEntityExtraData(event.getEntity());
        if (dirtyProperties != null && dirtyProperties.length > 0) {
        	Change change = getChange(event.getPersister(), event.getPersister().getEntityName(), event.getId(), event.getEntity());
        	if (change != null) {
        		for (int i = 0; i < dirtyProperties.length; i++) {
        			int pidx = dirtyProperties[i];
    				change.getChanges().put(event.getPersister().getPropertyNames()[pidx], event.getState()[pidx]);
        		}
        	}
        	else
        		DataContext.addUpdate(EntityUpdateType.UPDATE, event.getEntity());
        }
    }

    public void onPostDelete(PostDeleteEvent event) {
    	String uid = getUid(event.getPersister(), event.getEntity());
    	if (uid != null) {
			ChangeRef deleteRef = new ChangeRef(event.getPersister().getEntityName(), uid, event.getId());
    		DataContext.addUpdate(EntityUpdateType.REMOVE, deleteRef);
    	}
    	else
    		DataContext.addUpdate(EntityUpdateType.REMOVE, event.getEntity());
    }
    
    private static Change getChange(EntityPersister persister, String entityName, Serializable id, Object entity) {
    	Number version = (Number)persister.getVersion(entity, EntityMode.POJO);
    	String uid = getUid(persister, entity);
    	if (uid == null)
    		return null;
    	
		Change change = null;
    	for (EntityUpdate du : DataContext.get().getDataUpdates()) {
    		if (du.type.equals(EntityUpdateType.UPDATE.name()) 
    				&& ((Change)du.entity).getClassName().equals(entityName)
    	    		&& ((Change)du.entity).getId().equals(id)) {
    			change = (Change)du.entity;
    			break;
    		}
    	}
    	if (change == null) {
    		change = new Change(entityName, id, version, uid);
    		DataContext.addUpdate(EntityUpdateType.UPDATE, change, 1);
    	}
		return change;
    }

	private static String getUid(EntityPersister persister, Object entity) {
		for (int i = 0; i < persister.getPropertyNames().length; i++) {
			if ("uid".equals(persister.getPropertyNames()[i]))
				return (String)persister.getPropertyValue(entity, i, EntityMode.POJO);
		}
		return null;
	}

    @SuppressWarnings("unchecked")
	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
    	Object owner = event.getAffectedOwnerOrNull();
    	if (owner == null)
    		return;
    	
    	CollectionEntry collectionEntry = event.getSession().getPersistenceContext().getCollectionEntry(event.getCollection());
    	
    	Change change = getChange(collectionEntry.getLoadedPersister().getOwnerEntityPersister(), event.getAffectedOwnerEntityName(), event.getAffectedOwnerIdOrNull(), owner);
    	if (change == null)
    		return;
    	
    	PersistentCollection newColl = event.getCollection();
    	Serializable oldColl = collectionEntry.getSnapshot();
    	String propertyName = collectionEntry.getRole().substring(collectionEntry.getLoadedPersister().getOwnerEntityPersister().getEntityName().length()+1);
    	
    	if (oldColl == null && newColl.hasQueuedOperations()) {
    		List<Object[]> added = new ArrayList<Object[]>();    		
    		List<Object[]> removed = new ArrayList<Object[]>();
    		List<Object[]> updated = new ArrayList<Object[]>();
    		
			List<?> queuedOperations = get(newColl, "operationQueue", List.class);
			for (Object op : queuedOperations) {
				// Great !!
				if (op.getClass().getName().endsWith("$Add")) {
					added.add(new Object[] { get(op, "index"), get(op, "value") });
				}
				else if (op.getClass().getName().endsWith("$SimpleAdd")) {
					added.add(new Object[] { null, get(op, "value") });
				}
				else if (op.getClass().getName().endsWith("$Put")) {
					added.add(new Object[] { get(op, "index"), get(op, "value") });
				}
				else if (op.getClass().getName().endsWith("$Remove")) {
					removed.add(new Object[] { get(op, "index"), get(op, "old") });
				}
				else if (op.getClass().getName().endsWith("$SimpleRemove")) {
					removed.add(new Object[] { null, get(op, "value") });
				}
				else if (op.getClass().getName().endsWith("$Set")) {
					updated.add(new Object[] { get(op, "index"), get(op, "value") });
				}
			}
			
    		CollectionChange[] collChanges = new CollectionChange[added.size()+removed.size()+updated.size()];
    		int idx = 0;
    		for (Object[] obj : added)
    			collChanges[idx++] = new CollectionChange(1, obj[0], obj[1]);
    		
    		for (Object[] obj : removed) {
    			Object value = obj[1];
    			if (value != null && value.getClass().isAnnotationPresent(Entity.class)) {
    				org.granite.util.Entity e = new org.granite.util.Entity(value);
    				value = new ChangeRef(e.getName(), (String)get(value, "uid"), (Serializable)e.getIdentifier());
    			}
    			collChanges[idx++] = new CollectionChange(-1, obj[0], value);
    		}
    		
    		for (Object[] obj : updated)
    			collChanges[idx++] = new CollectionChange(0, obj[0], obj[1]);
    		
    		change.addCollectionChanges(propertyName, collChanges);
    	}
    	else if (oldColl != null && newColl instanceof List<?>) {
    		List<?> oldSnapshot = (List<?>)oldColl;
    		
    		List<?> newList = (List<?>)newColl;
    		
    		List<Object[]> ops = DataUtils.diffLists(oldSnapshot, newList);
    		
    		CollectionChange[] collChanges = new CollectionChange[ops.size()];
    		int idx = 0;
    		for (Object[] obj : ops)
    			collChanges[idx++] = new CollectionChange((Integer)obj[0], obj[1], obj[2]);
    		
    		change.addCollectionChanges(propertyName, collChanges);
    	}
    	else if (oldColl != null && newColl instanceof Collection<?>) {
    		Map<?, ?> oldSnapshot = (Map<?, ?>)oldColl;
    		
    		Set<Object> added = new HashSet<Object>();
    		added.addAll((Collection<?>)newColl);
    		added.removeAll(new HashSet<Object>(oldSnapshot.keySet()));
    		
    		Set<Object> removed = new HashSet<Object>();
    		removed.addAll(new HashSet<Object>(oldSnapshot.keySet()));
    		removed.removeAll((Collection<?>)newColl);
    		
    		CollectionChange[] collChanges = new CollectionChange[added.size()+removed.size()];
    		int idx = 0;
    		for (Object obj : added)
    			collChanges[idx++] = new CollectionChange(1, null, obj);
    		
    		for (Object obj : removed)
    			collChanges[idx++] = new CollectionChange(-1, null, obj);
    		
    		change.addCollectionChanges(propertyName, collChanges);
    	}
    	else if (oldColl != null && newColl instanceof Map<?, ?>) {
    		Map<?, ?> oldSnapshot = (Map<?, ?>)oldColl;
    		
    		Set<Entry<Object, Object>> added = new HashSet<Entry<Object, Object>>();
    		added.addAll(((Map<Object, Object>)newColl).entrySet());
    		added.removeAll(new HashMap<Object, Object>(oldSnapshot).entrySet());
    		
    		Set<Entry<Object, Object>> removed = new HashSet<Entry<Object, Object>>();
    		removed.addAll(new HashMap<Object, Object>(oldSnapshot).entrySet());
    		removed.removeAll(((Map<Object, Object>)newColl).entrySet());
    		
    		CollectionChange[] collChanges = new CollectionChange[added.size()+removed.size()];
    		int idx = 0;
    		for (Map.Entry<?, ?> me : added)
    			collChanges[idx++] = new CollectionChange(1, me.getKey(), me.getValue());
    		
    		for (Map.Entry<?, ?> me : removed)
    			collChanges[idx++] = new CollectionChange(-1, me.getKey(), me.getValue());
    		
    		change.addCollectionChanges(propertyName, collChanges);
    	}
    }

	public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
		defaultFlushEntityEventListener.onFlushEntity(event);
		
		Object entity = event.getEntity();
		if (event.getDirtyProperties() != null && event.getDirtyProperties().length > 0) {
			int[] dirtyProperties = (int[])DataContext.getEntityExtraData(entity);
			int[] newDirtyProperties;
			if (dirtyProperties == null)
				newDirtyProperties = event.getDirtyProperties();
			else {
				newDirtyProperties = new int[dirtyProperties.length + event.getDirtyProperties().length];
				System.arraycopy(dirtyProperties, 0, newDirtyProperties, 0, dirtyProperties.length);
				System.arraycopy(event.getDirtyProperties(), 0, newDirtyProperties, dirtyProperties.length, event.getDirtyProperties().length);
			}
			
			DataContext.addEntityExtraData(entity, newDirtyProperties);
		}
	}
    
}
