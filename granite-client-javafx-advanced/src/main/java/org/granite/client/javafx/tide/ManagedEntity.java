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
package org.granite.client.javafx.tide;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.inject.Inject;
import javax.inject.Named;

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
    
    @Inject
	private EntityManager entityManager;
	private JavaFXDataManager dataManager;
	
	private ReadOnlyBooleanWrapper saved = new ReadOnlyBooleanWrapper(this, "saved", true);
	private ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper(this, "dirty", false);
	
	private List<InstanceBinding<T, ?>> instanceBindings = new ArrayList<InstanceBinding<T, ?>>();
	
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
	
	private ChangeListener<T> entityChangeListener = new ChangeListener<T>() {
		@Override
		public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
			if (oldValue != null) {
				entityManager.resetEntity(oldValue);
				
				for (InstanceBinding<T, ?> instanceBinding : instanceBindings)
					instanceBinding.unbind(oldValue);
				
				ReadOnlyProperty<Object> versionProperty = getVersionProperty(oldValue);
				versionProperty.removeListener(versionChangeListener);
				dirty.unbind();
			}
			
			if (newValue == null)
				return;
			
			ReadOnlyProperty<Object> versionProperty = getVersionProperty(newValue);
			versionProperty.addListener(versionChangeListener);
			saved.set(versionProperty.getValue() != null);
			
			EntityManager entityManager = PersistenceManager.getEntityManager(newValue);
			if (entityManager == null)
				ManagedEntity.this.entityManager.mergeExternalData(newValue);
			
			else if (entityManager != ManagedEntity.this.entityManager)
				throw new RuntimeException("Entity " + newValue + " cannot be attached: already attached to another entity manager");
			
			dirty.bind(dataManager.deepDirtyEntity(newValue));
			
			for (InstanceBinding<T, ?> instanceBinding : instanceBindings)
				instanceBinding.bind(newValue);			
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
		this.instance.addListener(entityChangeListener);
	}
	
	public <P> void addInstanceBinding(Property<P> property, ObservableValueGetter<T, P> propertyGetter) {
		InstanceBinding<T, P> instanceBinding = new UnidirectionalInstanceBinding<P>(property, propertyGetter); 
		instanceBindings.add(instanceBinding);
		if (instance.get() != null)
			instanceBinding.bind(instance.get());
	}
	
	public <P> void addBidirectionalInstanceBinding(Property<P> property, PropertyGetter<T, P> propertyGetter) {
		InstanceBinding<T, P> instanceBinding = new BidirectionalInstanceBinding<P>(property, propertyGetter); 
		instanceBindings.add(instanceBinding);
		if (instance.get() != null)
			instanceBinding.bind(instance.get());
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
	
	
	private interface InstanceBinding<T, P> {
		
		public void bind(T instance);
		
		public void unbind(T instance);
	}
	
	private final class BidirectionalInstanceBinding<P> implements InstanceBinding<T, P> {
		
		private final Property<P> inputProperty;
		private final PropertyGetter<T, P> entityPropertyGetter;
		
		public BidirectionalInstanceBinding(Property<P> inputProperty, PropertyGetter<T, P> entityPropertyGetter) {
			this.inputProperty = inputProperty;
			this.entityPropertyGetter = entityPropertyGetter;
		}
		
		public void bind(T instance) {
			this.inputProperty.bindBidirectional(entityPropertyGetter.getProperty(instance));
		}
		
		public void unbind(T instance) {
			this.inputProperty.unbindBidirectional(entityPropertyGetter.getProperty(instance));
		}
	}

	private final class UnidirectionalInstanceBinding<P> implements InstanceBinding<T, P> {
		
		private final Property<P> inputProperty;
		private final ObservableValueGetter<T, P> entityPropertyGetter;
		
		public UnidirectionalInstanceBinding(Property<P> inputProperty, ObservableValueGetter<T, P> entityPropertyGetter) {
			this.inputProperty = inputProperty;
			this.entityPropertyGetter = entityPropertyGetter;
		}
		
		public void bind(T instance) {
			this.inputProperty.bind(entityPropertyGetter.getObservableValue(instance));
		}
		
		public void unbind(T instance) {
			this.inputProperty.unbind();
		}
	}

	
	public static interface PropertyGetter<T, P> {
		
		public Property<P> getProperty(T instance);
	}

	public static interface ObservableValueGetter<T, P> {
		
		public ObservableValue<P> getObservableValue(T instance);
	}
}
