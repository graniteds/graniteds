/**
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
package org.granite.client.tide.javafx;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.beans.value.WritableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.PersistenceManager;
import org.granite.client.tide.data.impl.AbstractDataManager;
import org.granite.client.util.WeakIdentityHashMap;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class JavaFXDataManager extends AbstractDataManager {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(JavaFXDataManager.class);
    
	
    private TrackingHandler trackingHandler;
    
    public void setTrackingHandler(TrackingHandler trackingHandler) {
        this.trackingHandler = trackingHandler;
    }
    
    public BooleanProperty dirty = new ReadOnlyBooleanWrapper(this, "dirty", false);
    
    public ReadOnlyBooleanProperty dirtyProperty() {
        return dirty;
    }
    
    public boolean isDirty() {
    	return dirty.get();
    }
    
    private Map<Object, ObservableDirtyEntity> dirtyEntityCache = new WeakIdentityHashMap<Object, ObservableDirtyEntity>();
    
    public ObservableDirtyEntity dirtyEntity(Object entity) {
    	ObservableDirtyEntity dirtyEntity = dirtyEntityCache.get(entity);
    	if (dirtyEntity == null) {
    		dirtyEntity = new ObservableDirtyEntity(entity);
    		dirtyEntityCache.put(entity, dirtyEntity);
    	}
    	return dirtyEntity;
    }

    private Map<Object, ObservableDeepDirtyEntity> deepDirtyEntityCache = new WeakIdentityHashMap<Object, ObservableDeepDirtyEntity>();
    
    public ObservableDeepDirtyEntity deepDirtyEntity(Object entity) {
    	ObservableDeepDirtyEntity deepDirtyEntity = deepDirtyEntityCache.get(entity);
    	if (deepDirtyEntity == null) {
    		deepDirtyEntity = new ObservableDeepDirtyEntity(entity);
    		deepDirtyEntityCache.put(entity, deepDirtyEntity);
    	}
    	return deepDirtyEntity;
    }
    
    private class ObservableDirtyEntity extends ReadOnlyBooleanPropertyBase {
    	
    	private Object entity;
    	private EntityManager entityManager;
    	private ChangeListener<Object> changeListener = new WeakChangeListener<Object>(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				ObservableDirtyEntity.this.entity = newValue;
				ObservableDirtyEntity.this.entityManager = PersistenceManager.getEntityManager(newValue);
				fireValueChangedEvent();
			}    		
    	});
    	
    	public ObservableDirtyEntity(Object entity) {
    		if (entity instanceof ObservableValue) {
    			ObservableValue<?> value = (ObservableValue<?>)entity;
    			this.entity = value.getValue();
    			value.addListener(changeListener);
    		}
    		else
    			this.entity = entity;
    		this.entityManager = PersistenceManager.getEntityManager(this.entity);
    	}
    	
		@Override
		public Object getBean() {
			return JavaFXDataManager.this;
		}
		
		@Override
		public String getName() {
			return "deepDirtyEntity";
		}
		
		@Override
		public boolean get() {
			if (entity == null)
				return false;
			if (entityManager == null) {
				// In case entity was not managed on initial binding
	    		entityManager = PersistenceManager.getEntityManager(entity);
	    		if (entityManager == null)
	    			return false;
			}
			return entityManager.isDirtyEntity(entity);
		}

		protected void fireValueChangedEvent() {
			super.fireValueChangedEvent();
		}
    }
   
    private class ObservableDeepDirtyEntity extends ReadOnlyBooleanPropertyBase {
    	
    	private Object entity;
    	private EntityManager entityManager;
    	private ChangeListener<Object> changeListener = new WeakChangeListener<Object>(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				ObservableDeepDirtyEntity.this.entity = newValue;
				ObservableDeepDirtyEntity.this.entityManager = PersistenceManager.getEntityManager(newValue);
				fireValueChangedEvent();
			}    		
    	});
    	
    	public ObservableDeepDirtyEntity(Object entity) {
    		if (entity instanceof ObservableValue) {
    			ObservableValue<?> value = (ObservableValue<?>)entity;
    			this.entity = value.getValue();
    			value.addListener(changeListener);
    		}
    		else
    			this.entity = entity;
    		this.entityManager = PersistenceManager.getEntityManager(this.entity);
    	}
    	
		@Override
		public Object getBean() {
			return JavaFXDataManager.this;
		}
		
		@Override
		public String getName() {
			return "deepDirtyEntity";
		}
		
		@Override
		public boolean get() {
			if (entity == null)
				return false;
			if (entityManager == null) {
				// In case entity was not managed on initial binding
	    		entityManager = PersistenceManager.getEntityManager(entity);
	    		if (entityManager == null)
	    			return false;
			}
			return entityManager.isDeepDirtyEntity(entity);
		}

		protected void fireValueChangedEvent() {
			super.fireValueChangedEvent();
		}
    }

    
    public class EntityPropertyChangeListener<E> implements ChangeListener<E> {
        @Override
        public void changed(ObservableValue<? extends E> property, E oldValue, E newValue) {
            if (property instanceof ReadOnlyProperty<?>) {
                if (((ReadOnlyProperty<?>)property).getBean() == null)
                    throw new IllegalStateException("Property bean must be defined");
                if (((ReadOnlyProperty<?>)property).getName() == null || ((ReadOnlyProperty<?>)property).getName().trim().length() == 0)
                    throw new IllegalStateException("Property name must be defined");
                trackingHandler.entityPropertyChangeHandler(((ReadOnlyProperty<?>)property).getBean(), ((ReadOnlyProperty<?>)property).getName(), oldValue, newValue);
            }
        }
    }
    
    public class EntityListChangeListener<E> implements ListChangeListener<E> {
        @Override
        public void onChanged(Change<? extends E> change) {
            while (change.next()) {
            	if (change.wasReplaced()) {
            		Object[] replaced = new Object[change.getAddedSize()];
            		for (int i = 0; i < change.getAddedSize(); i++)
            			replaced[i] = new Object[] { change.getRemoved().get(i), change.getAddedSubList().get(i) };
                    trackingHandler.entityCollectionChangeHandler(ChangeKind.REPLACE, change.getList(), change.getFrom(), replaced);
            		return;
            	}
            	if (change.wasRemoved())
                    trackingHandler.entityCollectionChangeHandler(ChangeKind.REMOVE, change.getList(), change.getFrom(), change.getRemoved().toArray());
                if (change.wasAdded())
                    trackingHandler.entityCollectionChangeHandler(ChangeKind.ADD, change.getList(), change.getFrom(), change.getAddedSubList().toArray());
//                else if (change.wasReplaced())
//                    trackingHandler.entityCollectionChangeHandler("replace", change.getList(), change.getFrom(), change.)
            }
        }
    }
    
    public class EntitySetChangeListener<E> implements SetChangeListener<E> {
        @Override
        public void onChanged(Change<? extends E> change) {
            if (change.wasRemoved())
                trackingHandler.entityCollectionChangeHandler(ChangeKind.REMOVE, change.getSet(), null, new Object[] { change.getElementRemoved() });
            if (change.wasAdded())
                trackingHandler.entityCollectionChangeHandler(ChangeKind.ADD, change.getSet(), null, new Object[] { change.getElementAdded() });
        }
    }
    
    public class DefaultListChangeListener<E> implements ListChangeListener<E> {
        @Override
        public void onChanged(ListChangeListener.Change<? extends E> change) {
            while (change.next()) {
            	if (change.wasReplaced()) {
            		Object[] replaced = new Object[change.getAddedSize()];
            		for (int i = 0; i < change.getAddedSize(); i++)
            			replaced[i] = new Object[] { change.getRemoved().get(i), change.getAddedSubList().get(i) };
                    trackingHandler.entityCollectionChangeHandler(ChangeKind.REPLACE, change.getList(), change.getFrom(), replaced);
            		return;
            	}
                if (change.wasRemoved())
                    trackingHandler.collectionChangeHandler(ChangeKind.REMOVE, change.getList(), change.getFrom(), change.getRemoved().toArray());
                if (change.wasAdded())
                    trackingHandler.collectionChangeHandler(ChangeKind.ADD, change.getList(), change.getFrom(), change.getAddedSubList().toArray());
            }
        }
    }
    
    public class DefaultSetChangeListener<E> implements SetChangeListener<E> {
        @Override
        public void onChanged(SetChangeListener.Change<? extends E> change) {
            if (change.wasRemoved())
                trackingHandler.collectionChangeHandler(ChangeKind.REMOVE, change.getSet(), null, new Object[] { change.getElementRemoved() });
            if (change.wasAdded())
                trackingHandler.collectionChangeHandler(ChangeKind.ADD, change.getSet(), null, new Object[] { change.getElementAdded() });
        }
    }
    
    public class EntityMapChangeListener<K, V> implements MapChangeListener<K, V> {
        @Override
        public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
            if (change.wasAdded() && change.wasRemoved())
                trackingHandler.entityMapChangeHandler(ChangeKind.REPLACE, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueRemoved(), change.getValueAdded() }});
            else if (change.wasRemoved())
                trackingHandler.entityMapChangeHandler(ChangeKind.REMOVE, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueRemoved() }});
            else if (change.wasAdded())
                trackingHandler.entityMapChangeHandler(ChangeKind.ADD, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueAdded() }});
        }
    }

    public class DefaultMapChangeListener<K, V> implements MapChangeListener<K, V> {
        @Override
        public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
            if (change.wasAdded() && change.wasRemoved())
                trackingHandler.mapChangeHandler(ChangeKind.REPLACE, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueRemoved(), change.getValueAdded() }});
            else if (change.wasRemoved())
                trackingHandler.mapChangeHandler(ChangeKind.REMOVE, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueRemoved() }});
            else if (change.wasAdded())
                trackingHandler.mapChangeHandler(ChangeKind.ADD, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueAdded() }});
        }
    }
 
    private ListChangeListener<Object> listChangeListener = new DefaultListChangeListener<Object>();
    private SetChangeListener<Object> setChangeListener = new DefaultSetChangeListener<Object>();
    private MapChangeListener<Object, Object> mapChangeListener = new DefaultMapChangeListener<Object, Object>();
    private ChangeListener<Object> entityPropertyChangeListener = new EntityPropertyChangeListener<Object>();
    private ListChangeListener<Object> entityListChangeListener = new EntityListChangeListener<Object>();
    private SetChangeListener<Object> entitySetChangeListener = new EntitySetChangeListener<Object>();
    private MapChangeListener<Object, Object> entityMapChangeListener = new EntityMapChangeListener<Object, Object>();
    
    private WeakIdentityHashMap<Object, TrackingType> trackingListeners = new WeakIdentityHashMap<Object, TrackingType>();
    
    
    @Override
    public void startTracking(Object previous, Object parent) {
        if (previous == null || trackingListeners.containsKey(previous))
            return;
        
        if (previous instanceof ObservableList<?>) {
            if (parent != null) {
                ((ObservableList<?>)previous).addListener(entityListChangeListener);
                trackingListeners.put(previous, TrackingType.ENTITY_LIST);
            }
            else {
                ((ObservableList<?>)previous).addListener(listChangeListener);
                trackingListeners.put(previous, TrackingType.LIST);
            }
        }
        else if (previous instanceof ObservableSet<?>) {
            if (parent != null) {
                ((ObservableSet<?>)previous).addListener(entitySetChangeListener);
                trackingListeners.put(previous, TrackingType.ENTITY_SET);
            }
            else {
                ((ObservableSet<?>)previous).addListener(setChangeListener);
                trackingListeners.put(previous, TrackingType.SET);
            }
        }
        else if (previous instanceof ObservableMap<?, ?>) {
            if (parent != null) {
                ((ObservableMap<?, ?>)previous).addListener(entityMapChangeListener);
                trackingListeners.put(previous, TrackingType.ENTITY_MAP);
            }
            else {
                ((ObservableMap<?, ?>)previous).addListener(mapChangeListener);
                trackingListeners.put(previous, TrackingType.MAP);
            }
        }
        else if (parent != null || isEntity(previous)) {
            for (ObservableValue<?> property : instrospectProperties(previous)) {
            	if (property instanceof WritableValue<?>)
            		property.addListener(entityPropertyChangeListener);
            }
            trackingListeners.put(previous, TrackingType.ENTITY_PROPERTY);
        }
    }

    @Override
    public void stopTracking(Object previous, Object parent) {
        if (previous == null || !trackingListeners.containsKey(previous))
            return;
        
        if (previous instanceof ObservableList<?>) {
            if (parent != null)
                ((ObservableList<?>)previous).removeListener(entityListChangeListener);
            else
                ((ObservableList<?>)previous).removeListener(listChangeListener);
        }
        else if (previous instanceof ObservableSet<?>) {
            if (parent != null)
                ((ObservableSet<?>)previous).removeListener(entitySetChangeListener);
            else
                ((ObservableSet<?>)previous).removeListener(setChangeListener);
        }
        else if (previous instanceof ObservableMap<?, ?>) {
            if (parent != null)
                ((ObservableMap<?, ?>)previous).removeListener(entityMapChangeListener);
            else
                ((ObservableMap<?, ?>)previous).removeListener(mapChangeListener);
        }
        else if (parent != null || isEntity(previous)) {
            for (ObservableValue<?> property : instrospectProperties(previous)) {
            	if (property instanceof WritableValue<?>)
            		property.removeListener(entityPropertyChangeListener);
            }
        }
        
        trackingListeners.remove(previous);
    }

    @Override
    public void clear() {
    	dirty.set(false);
    	
    	deepDirtyEntityCache.clear();
    	
        Iterator<Object> ikey = trackingListeners.keySet().iterator();
        while (ikey.hasNext()) {
            Object obj = ikey.next();
            TrackingType type = trackingListeners.get(obj);
            switch (type) {
            case LIST:
                ((ObservableList<?>)obj).removeListener(listChangeListener);
                break;
            case SET:
                ((ObservableSet<?>)obj).removeListener(setChangeListener);
                break;
            case MAP:
                ((ObservableMap<?, ?>)obj).removeListener(mapChangeListener);
                break;
            case ENTITY_PROPERTY:
                for (ObservableValue<?> property : instrospectProperties(obj)) {
                    if (property instanceof WritableValue<?>)
                        property.removeListener(entityPropertyChangeListener);
                }
                break;
            case ENTITY_LIST:
                ((ObservableList<?>)obj).removeListener(entityListChangeListener);
                break;
            case ENTITY_SET:
                ((ObservableSet<?>)obj).removeListener(entitySetChangeListener);
                break;
            case ENTITY_MAP:
                ((ObservableMap<?, ?>)obj).removeListener(entityMapChangeListener);
                break;
            }
        }
    }
    
    private List<ObservableValue<?>> instrospectProperties(Object obj) {
        List<ObservableValue<?>> properties = new ArrayList<ObservableValue<?>>();
        for (Method m : obj.getClass().getMethods()) {
            if (m.getParameterTypes().length != 0 || !m.getName().endsWith("Property") || !ObservableValue.class.isAssignableFrom(m.getReturnType()))
                continue;
            
            try {
                ObservableValue<?> property = (ObservableValue<?>)m.invoke(obj);
                properties.add(property);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not get property " + m.getName() + " on " + obj, e);
            }                
        }
        return properties;
    }

    @Override
    public void notifyDirtyChange(boolean oldDirty, boolean dirty) {
        this.dirty.set(dirty);
    }

    @Override
    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity) {
    	ObservableDirtyEntity dirtyEntity = dirtyEntityCache.get(entity);
    	if (dirtyEntity != null)
    		dirtyEntity.fireValueChangedEvent();
        
        for (ObservableDeepDirtyEntity deepDirtyEntity : deepDirtyEntityCache.values())
        	deepDirtyEntity.fireValueChangedEvent();
    }
}
