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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.binding.PropertyChangeHelper;
import org.granite.binding.collection.CollectionChangeEvent;
import org.granite.binding.collection.CollectionChangeListener;
import org.granite.binding.collection.ObservableCollection;
import org.granite.binding.collection.CollectionChangeEvent.Kind;
import org.granite.client.util.WeakIdentityHashMap;
import org.granite.util.Introspector;
import org.granite.util.PropertyDescriptor;


/**
 * @author William DRAI
 */
public class JavaBeanDataManager extends AbstractDataManager {
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}	
	
	
	@Override
	public void setPropertyValue(Object entity, String name, Object value) {
    	if (entity == null)
    		return;
    	try {
    		boolean found = false;
	    	PropertyDescriptor[] pds = Introspector.getPropertyDescriptors(entity.getClass());
			for (PropertyDescriptor pd : pds) {
				if (pd.getName().equals(name) && pd.getWriteMethod() != null) {
					Object oldValue = null;
					if (pd.getReadMethod() != null)
						oldValue = pd.getReadMethod().invoke(entity);
					pd.getWriteMethod().invoke(entity, value);
					if (pd.getReadMethod() != null)
						PropertyChangeHelper.firePropertyChange(entity, name, oldValue, value);
					found = true;
					break;
				}
			}
			if (!found)
				super.setPropertyValue(entity, name, value);
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Could not set property " + name + " on entity " + entity, e);
    	}
	}
	
	
    private TrackingHandler trackingHandler;
    
    public void setTrackingHandler(TrackingHandler trackingHandler) {
        this.trackingHandler = trackingHandler;
    }
    
    
    public class EntityPropertyChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            trackingHandler.entityPropertyChangeHandler(pce.getSource(), pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
        }
    }
    
    public class EntityCollectionChangeListener implements CollectionChangeListener {
        @Override
        public void collectionChange(CollectionChangeEvent ce) {
        	if (ce.getKind() == Kind.ADD)        		
        		trackingHandler.entityCollectionChangeHandler(ChangeKind.ADD, ce.getCollection(), (Integer)ce.getKey(), ce.getValues());
        	else if (ce.getKind() == Kind.REMOVE)
        		trackingHandler.entityCollectionChangeHandler(ChangeKind.REMOVE, ce.getCollection(), (Integer)ce.getKey(), ce.getValues());
        	else if (ce.getKind() == Kind.REPLACE)
        		trackingHandler.entityCollectionChangeHandler(ChangeKind.REPLACE, ce.getCollection(), (Integer)ce.getKey(), ce.getValues());
        }
    }
    
    public class DefaultCollectionChangeListener implements CollectionChangeListener {
        @Override
        public void collectionChange(CollectionChangeEvent ce) {
        	if (ce.getKind() == Kind.ADD)        		
        		trackingHandler.collectionChangeHandler(ChangeKind.ADD, ce.getCollection(), (Integer)ce.getKey(), ce.getValues());
        	else if (ce.getKind() == Kind.REMOVE)
        		trackingHandler.collectionChangeHandler(ChangeKind.REMOVE, ce.getCollection(), (Integer)ce.getKey(), ce.getValues());
        	else if (ce.getKind() == Kind.REPLACE)
        		trackingHandler.collectionChangeHandler(ChangeKind.REPLACE, ce.getCollection(), (Integer)ce.getKey(), ce.getValues());
        }
    }
    
    public class EntityMapChangeListener implements CollectionChangeListener {
        @Override
        public void collectionChange(CollectionChangeEvent ce) {
        	if (ce.getKind() == Kind.ADD)        		
        		trackingHandler.entityMapChangeHandler(ChangeKind.ADD, ce.getCollection(), null, ce.getValues());
        	else if (ce.getKind() == Kind.REMOVE)
        		trackingHandler.entityMapChangeHandler(ChangeKind.REMOVE, ce.getCollection(), null, ce.getValues());
        	else if (ce.getKind() == Kind.REPLACE)
        		trackingHandler.entityMapChangeHandler(ChangeKind.REPLACE, ce.getCollection(), null, ce.getValues());
        }
    }
    
    public class DefaultMapChangeListener implements CollectionChangeListener {
        @Override
        public void collectionChange(CollectionChangeEvent ce) {
        	if (ce.getKind() == Kind.ADD)        		
        		trackingHandler.mapChangeHandler(ChangeKind.ADD, ce.getCollection(), null, ce.getValues());
        	else if (ce.getKind() == Kind.REMOVE)
        		trackingHandler.mapChangeHandler(ChangeKind.REMOVE, ce.getCollection(), null, ce.getValues());
        	else if (ce.getKind() == Kind.REPLACE)
        		trackingHandler.mapChangeHandler(ChangeKind.REPLACE, ce.getCollection(), null, ce.getValues());
        }
    }
 
    private CollectionChangeListener listChangeListener = new DefaultCollectionChangeListener();
    private CollectionChangeListener setChangeListener = new DefaultCollectionChangeListener();
    private CollectionChangeListener mapChangeListener = new DefaultMapChangeListener();
    private PropertyChangeListener entityPropertyChangeListener = new EntityPropertyChangeListener();
    private CollectionChangeListener entityListChangeListener = new EntityCollectionChangeListener();
    private CollectionChangeListener entitySetChangeListener = new EntityCollectionChangeListener();
    private CollectionChangeListener entityMapChangeListener = new EntityMapChangeListener();
    
    private WeakIdentityHashMap<Object, TrackingType> trackingListeners = new WeakIdentityHashMap<Object, TrackingType>();
    

    @Override
    public void startTracking(Object previous, Object parent) {
        if (previous == null || trackingListeners.containsKey(previous))
            return;
        
        if (previous instanceof ObservableCollection && previous instanceof List<?>) {
            if (parent != null) {
                ((ObservableCollection)previous).addCollectionChangeListener(entityListChangeListener);
                trackingListeners.put(previous, TrackingType.ENTITY_LIST);
            }
            else {
                ((ObservableCollection)previous).addCollectionChangeListener(listChangeListener);
                trackingListeners.put(previous, TrackingType.LIST);
            }
        }
        else if (previous instanceof ObservableCollection && previous instanceof Set<?>) {
            if (parent != null) {
                ((ObservableCollection)previous).addCollectionChangeListener(entitySetChangeListener);
                trackingListeners.put(previous, TrackingType.ENTITY_SET);
            }
            else {
                ((ObservableCollection)previous).addCollectionChangeListener(setChangeListener);
                trackingListeners.put(previous, TrackingType.SET);
            }
        }
        else if (previous instanceof ObservableCollection && previous instanceof Map<?, ?>) {
            if (parent != null) {
                ((ObservableCollection)previous).addCollectionChangeListener(entityMapChangeListener);
                trackingListeners.put(previous, TrackingType.ENTITY_MAP);
            }
            else {
                ((ObservableCollection)previous).addCollectionChangeListener(mapChangeListener);
                trackingListeners.put(previous, TrackingType.MAP);
            }
        }
        else if (parent != null || isEntity(previous)) {
        	PropertyChangeHelper.addPropertyChangeListener(previous, entityPropertyChangeListener);
            trackingListeners.put(previous, TrackingType.ENTITY_PROPERTY);
        }
    }

    @Override
    public void stopTracking(Object previous, Object parent) {
        if (previous == null || !trackingListeners.containsKey(previous))
            return;
        
        if (previous instanceof ObservableCollection && previous instanceof List<?>) {
            if (parent != null)
                ((ObservableCollection)previous).removeCollectionChangeListener(entityListChangeListener);
            else
            	((ObservableCollection)previous).removeCollectionChangeListener(listChangeListener);
        }
        else if (previous instanceof ObservableCollection && previous instanceof Set<?>) {
            if (parent != null)
            	((ObservableCollection)previous).removeCollectionChangeListener(entitySetChangeListener);
            else
            	((ObservableCollection)previous).removeCollectionChangeListener(setChangeListener);
        }
        else if (previous instanceof ObservableCollection && previous instanceof Map<?, ?>) {
            if (parent != null)
            	((ObservableCollection)previous).removeCollectionChangeListener(entityMapChangeListener);
            else
            	((ObservableCollection)previous).removeCollectionChangeListener(mapChangeListener);
        }
        else if (parent != null || isEntity(previous)) {
    		PropertyChangeHelper.removePropertyChangeListener(previous, entityPropertyChangeListener);
        }
        
        trackingListeners.remove(previous);
    }

    @Override
    public void clear() {
        Iterator<Object> ikey = trackingListeners.keySet().iterator();
        while (ikey.hasNext()) {
            Object obj = ikey.next();
            TrackingType type = trackingListeners.get(obj);
            switch (type) {
            case LIST:
                ((ObservableCollection)obj).removeCollectionChangeListener(listChangeListener);
                break;
            case SET:
            	((ObservableCollection)obj).removeCollectionChangeListener(setChangeListener);
                break;
            case MAP:
            	((ObservableCollection)obj).removeCollectionChangeListener(mapChangeListener);
                break;
            case ENTITY_PROPERTY:
        		PropertyChangeHelper.removePropertyChangeListener(obj, entityPropertyChangeListener);
                break;
            case ENTITY_LIST:
            	((ObservableCollection)obj).removeCollectionChangeListener(entityListChangeListener);
                break;
            case ENTITY_SET:
            	((ObservableCollection)obj).removeCollectionChangeListener(entitySetChangeListener);
                break;
            case ENTITY_MAP:
            	((ObservableCollection)obj).removeCollectionChangeListener(entityMapChangeListener);
                break;
            }
        }
    }

    private boolean dirty = false;
    
    public boolean isDirty() {
        return dirty;
    }
    
    @Override
    public void notifyDirtyChange(boolean oldDirty, boolean dirty) {
    	this.dirty = dirty;
    	pcs.firePropertyChange("dirty", oldDirty, dirty);
    }
    
    @Override
    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity) {
    	pcs.firePropertyChange(new PropertyChangeEvent(entity, "dirtyEntity", oldDirtyEntity, newDirtyEntity));
    	pcs.firePropertyChange(new PropertyChangeEvent(entity, "deepDirtyEntity", oldDirtyEntity, newDirtyEntity));
    }
}
