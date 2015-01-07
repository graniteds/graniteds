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
package org.granite.client.javafx.tide;

import java.lang.reflect.Method;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.inject.Inject;
import javax.inject.Named;

import org.granite.client.javafx.util.ChainedObservableValue;
import org.granite.client.javafx.util.ChainedObservableValue.ObservableValueGetter;
import org.granite.client.javafx.util.ChainedProperty;
import org.granite.client.javafx.util.ChainedProperty.PropertyGetter;
import org.granite.client.javafx.util.ChangeWatcher;
import org.granite.client.javafx.util.ChangeWatcher.Trigger;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.PersistenceManager;

/**
 * @author William DRAI
 */
@Named
public class ManagedEntity<T> implements ContextAware {
    
    private ObjectProperty<T> instance = new SimpleObjectProperty<T>(this, "instance");
    private ChangeWatcher<T> instanceWatcher = ChangeWatcher.watch(instance);
    
    @Inject
	private EntityManager entityManager;
	private JavaFXDataManager dataManager;
	
	private ReadOnlyBooleanWrapper saved = new ReadOnlyBooleanWrapper(this, "saved", true);
	private ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper(this, "dirty", false);
	
    public ManagedEntity() {
    }
    
	public ManagedEntity(EntityManager entityManager) {
		init(entityManager);
	}
	
	public ManagedEntity(EntityManager entityManager, T value) {
		init(entityManager);
		instance.set(value);
	}
	
	public ObjectProperty<T> instanceProperty() {
	    return instance;
	}
	public T getInstance() {
	    return instance.get();
	}
	public void setInstance(T value) {
	    this.instance.set(value);
	}
	
	public <X> ChainedProperty<T, X> instanceProperty(PropertyGetter<T, X> getter) {
		return new ChainedProperty<T, X>(instanceWatcher, getter);
	}
	
	public <X> ChainedObservableValue<T, X> instanceObservableValue(ObservableValueGetter<T, X> getter) {
		return new ChainedObservableValue<T, X>(instanceWatcher, getter);
	}
	
	public ReadOnlyBooleanProperty savedProperty() {
		return saved.getReadOnlyProperty();
	}
	public boolean isSaved() {
		return saved.get();
	}
	
	public ReadOnlyBooleanProperty dirtyProperty() {
		return dirty.getReadOnlyProperty();
	}
	public boolean isDirty() {
		return dirty.get();
	}
	
	private Trigger<T, Object> entityChangeTrigger = new Trigger<T, Object>() {
		@Override
		public Object beforeChange(T oldInstance) {
			if (oldInstance != null) {
				entityManager.resetEntity(oldInstance);
				
				ReadOnlyProperty<Object> versionProperty = getVersionProperty(oldInstance);
				versionProperty.removeListener(versionChangeListener);
				dirty.unbind();
			}
			return null;
		}
		
		@Override
		public void afterChange(T newInstance, Object value) {
			if (newInstance == null)
				return;
			
			ReadOnlyProperty<Object> versionProperty = getVersionProperty(newInstance);
			versionProperty.addListener(versionChangeListener);
			saved.set(versionProperty.getValue() != null);
			
			EntityManager entityManager = PersistenceManager.getEntityManager(newInstance);
			if (entityManager == null)
				ManagedEntity.this.entityManager.mergeExternalData(newInstance);
			
			else if (entityManager != ManagedEntity.this.entityManager)
				throw new RuntimeException("Entity " + newInstance + " cannot be attached: already attached to another entity manager");
			
			dirty.bind(dataManager.deepDirtyEntity(newInstance));
		}
	};
	
	private ChangeListener<Object> versionChangeListener = new ChangeListener<Object>() {
		@Override
		public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
			saved.set(newValue != null);
		}
	};

    @Override
    public void setContext(Context context) {
        init(context.getEntityManager());
    }

	private void init(EntityManager entityManager) {
        if (this.entityManager == null)
        	this.entityManager = entityManager;
		this.dataManager = (JavaFXDataManager)this.entityManager.getDataManager();		
		this.instanceWatcher.addTrigger(entityChangeTrigger);
	}
	
	public void reset() {
		if (instance.get() == null)
			return;
		entityManager.resetEntity(instance.get());
	}
	
	@SuppressWarnings("unchecked")
	private ReadOnlyProperty<Object> getVersionProperty(Object value) {
		String versionPropertyName = dataManager.getVersionPropertyName(value);
		if (versionPropertyName == null)
			throw new RuntimeException("No version property found on entity " + value);
		try {
			Method m = value.getClass().getMethod(versionPropertyName + "Property");
			return (ReadOnlyProperty<Object>)m.invoke(value);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not get version property on entity " + value, e);
		}		
	}
}
