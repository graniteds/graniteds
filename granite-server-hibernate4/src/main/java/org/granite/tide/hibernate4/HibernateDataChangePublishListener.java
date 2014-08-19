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
package org.granite.tide.hibernate4;

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

import org.granite.tide.data.AnnotationUtils;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataContext.EntityUpdate;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.granite.tide.data.ExcludeFromDataPublish;
import org.granite.tide.data.Utils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.internal.DefaultFlushEntityEventListener;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author William Drai
 */
public class HibernateDataChangePublishListener implements PostInsertEventListener, PostUpdateEventListener,
        PostDeleteEventListener, PreCollectionUpdateEventListener, FlushEntityEventListener {
	
	private static final long serialVersionUID = 1L;
	
	private DefaultFlushEntityEventListener defaultFlushEntityEventListener = new DefaultFlushEntityEventListener();


    public void onPostInsert(PostInsertEvent event) {
    	if (DataContext.get() == null)
    		return;
    	if (event.getEntity().getClass().isAnnotationPresent(ExcludeFromDataPublish.class))
    		return;
    	
        DataContext.addUpdate(EntityUpdateType.PERSIST, event.getEntity());
    }

    public void onPostUpdate(PostUpdateEvent event) {
    	if (DataContext.get() == null)
    		return;
    	if (event.getEntity().getClass().isAnnotationPresent(ExcludeFromDataPublish.class))
    		return;
    	
        if (event.getDirtyProperties() != null && event.getDirtyProperties().length > 0) {
        	Object change = getChange(event.getPersister(), event.getSession(), event.getPersister().getEntityName(), event.getId(), event.getEntity());
        	if (change instanceof Change) {
        		for (int i = 0; i < event.getDirtyProperties().length; i++) {
        			int pidx = event.getDirtyProperties()[i];
        			String pname = event.getPersister().getPropertyNames()[pidx];
        			if (AnnotationUtils.isAnnotatedWith(event.getEntity(), pname, ExcludeFromDataPublish.class))
        				continue;
        			
    				((Change)change).getChanges().put(pname, event.getState()[pidx]);
        		}
        	}
        	else if (change == null)
        		DataContext.addUpdate(EntityUpdateType.UPDATE, event.getEntity());
        }
    }

    public void onPostDelete(PostDeleteEvent event) {
    	if (DataContext.get() == null)
    		return;
    	if (event.getEntity().getClass().isAnnotationPresent(ExcludeFromDataPublish.class))
    		return;
    	
    	String uid = getUid(event.getPersister(), event.getEntity());
    	if (uid != null) {
			ChangeRef deleteRef = new ChangeRef(event.getPersister().getEntityName(), uid, event.getId());
    		DataContext.addUpdate(EntityUpdateType.REMOVE, deleteRef);
    	}
    	else
    		DataContext.addUpdate(EntityUpdateType.REMOVE, event.getEntity());
    }
    
    private static Object getChange(EntityPersister persister, Session session, String entityName, Serializable id, Object entity) {
    	Number version = (Number)persister.getVersion(entity);
    	String uid = getUid(persister, entity);
    	if (uid == null)
    		return null;
    	
		Object change = null;
    	for (EntityUpdate du : DataContext.get().getDataUpdates()) {
    		if (du.entity instanceof Change) {
	    		if (du.type == EntityUpdateType.UPDATE
	    				&& ((Change)du.entity).getClassName().equals(entityName)
	    	    		&& ((Change)du.entity).getId().equals(id)) {
	    			change = du.entity;
	    			break;
	    		}
    		}
    		else if (du.entity.getClass().getName().equals(entityName)
					&& id.equals(persister.getIdentifier(entity, (SessionImplementor)session))) {
				change = du.entity;
				break;
    		}
    	}
    	if (change == null) {
    		change = new Change(entityName, id, version, uid);
    		DataContext.addUpdate(EntityUpdateType.UPDATE, change, 1);
    	}
    	else if (change instanceof Change)
    		((Change)change).updateVersion(version);
		return change;
    }

	private static String getUid(EntityPersister persister, Object entity) {
		for (int i = 0; i < persister.getPropertyNames().length; i++) {
			if ("uid".equals(persister.getPropertyNames()[i]))
				return (String)persister.getPropertyValue(entity, i);
		}
		return null;
	}

    @SuppressWarnings("unchecked")
	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
    	if (DataContext.get() == null)
    		return;
    	
    	Object owner = event.getAffectedOwnerOrNull();
    	if (owner == null || owner.getClass().isAnnotationPresent(ExcludeFromDataPublish.class))
    		return;
    	
    	CollectionEntry collectionEntry = event.getSession().getPersistenceContext().getCollectionEntry(event.getCollection());
    	
    	String propertyName = collectionEntry.getRole().substring(collectionEntry.getLoadedPersister().getOwnerEntityPersister().getEntityName().length()+1);
		if (AnnotationUtils.isAnnotatedWith(owner, propertyName, ExcludeFromDataPublish.class))
			return;
    	
    	Object change = getChange(collectionEntry.getLoadedPersister().getOwnerEntityPersister(), event.getSession(), event.getAffectedOwnerEntityName(), event.getAffectedOwnerIdOrNull(), owner);
    	if (change == null || !(change instanceof Change))
    		return;
    	
    	PersistentCollection newColl = event.getCollection();
    	Serializable oldColl = collectionEntry.getSnapshot();
    	
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
    		
    		((Change)change).addCollectionChanges(propertyName, collChanges);
    	}
    	else if (oldColl != null && newColl instanceof List<?>) {
    		List<?> oldSnapshot = (List<?>)oldColl;
    		List<?> newList = (List<?>)newColl;
    		
    		List<Object[]> ops = Utils.diffLists(oldSnapshot, newList);    		
    		
    		CollectionChange[] collChanges = new CollectionChange[ops.size()];
    		int idx = 0;
    		for (Object[] obj : ops)
    			collChanges[idx++] = new CollectionChange((Integer)obj[0], obj[1], obj[2]);
    		
    		((Change)change).addCollectionChanges(propertyName, collChanges);
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
    		
    		((Change)change).addCollectionChanges(propertyName, collChanges);
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
    		for (Entry<?, ?> me : added)
    			collChanges[idx++] = new CollectionChange(1, me.getKey(), me.getValue());
    		
    		for (Entry<?, ?> me : removed)
    			collChanges[idx++] = new CollectionChange(-1, me.getKey(), me.getValue());
    		
    		((Change)change).addCollectionChanges(propertyName, collChanges);
    	}
    }

	public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
		defaultFlushEntityEventListener.onFlushEntity(event);
	}
	
	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}
	
	
}
