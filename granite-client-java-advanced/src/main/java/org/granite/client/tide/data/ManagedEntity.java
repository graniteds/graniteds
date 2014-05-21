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
package org.granite.client.tide.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.granite.binding.PropertyChangeHelper;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.data.impl.JavaBeanDataManager;
import org.granite.client.tide.data.spi.EntityRef;

/**
 * @author William DRAI
 */
public class ManagedEntity<T> implements ContextAware {
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
	private T instance = null;
    
	private EntityManager entityManager;
	private JavaBeanDataManager dataManager;
	
	private boolean dirty = false;
	private boolean saved = false;
	
//	private List<InstanceBinding<T, ?>> instanceBindings = new ArrayList<InstanceBinding<T, ?>>();

    public ManagedEntity() {
    }

	public ManagedEntity(EntityManager entityManager) {
		init(entityManager);
	}
	
	public ManagedEntity(EntityManager entityManager, T value) {
		init(entityManager);
		setInstance(value);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}
	
	public T getInstance() {
	    return instance;
	}
	public void setInstance(T instance) {
		if (instance == this.instance)
			return;
		T oldInstance = this.instance;
	    this.instance = instance;
    	instanceChanged(oldInstance, instance);
	    pcs.firePropertyChange("instance", oldInstance, instance);
	}	
	public void setInstance(EntityRef ref) {
		@SuppressWarnings("unchecked")
		T instance = (T)entityManager.getCachedObject(ref, true);
		setInstance(instance);
	}
	
	public boolean isSaved() {
		return saved;
	}
	private void updateSaved() {
		boolean saved = getInstance() != null ? getVersion(getInstance()) != null : false;
		if (saved == this.saved)
			return;
		boolean oldSaved = this.saved;
		this.saved = saved;
		pcs.firePropertyChange("saved", oldSaved, saved);
	}
	
	public boolean isDirty() {
		return dirty;
	}
	private void updateDirty() {
		boolean dirty = getInstance() != null ? entityManager.isDeepDirtyEntity(getInstance()) : false;
		if (dirty == this.dirty)
			return;
		boolean oldDirty = this.dirty;
		this.dirty = dirty;
		pcs.firePropertyChange("dirty", oldDirty, dirty);
	}
	
	private void instanceChanged(T oldValue, T newValue) {
		if (oldValue != null) {
			entityManager.resetEntity(oldValue);
			
//			for (InstanceBinding<T, ?> instanceBinding : instanceBindings)
//				instanceBinding.unbind(oldValue);
			
			String versionPropertyName = getVersionPropertyName(oldValue);
			PropertyChangeHelper.removePropertyChangeListener(oldValue, versionPropertyName, versionChangeListener);
		}
		
		if (newValue == null)
			return;
		
		String versionPropertyName = getVersionPropertyName(newValue);
		PropertyChangeHelper.addPropertyChangeListener(newValue, versionPropertyName, versionChangeListener);
		updateDirty();
		updateSaved();
		
		EntityManager entityManager = PersistenceManager.getEntityManager(newValue);
		if (entityManager == null)
			ManagedEntity.this.entityManager.mergeExternalData(newValue);
		
		else if (entityManager != ManagedEntity.this.entityManager)
			throw new RuntimeException("Entity " + newValue + " cannot be attached: already attached to another entity manager");
		
//		for (InstanceBinding<T, ?> instanceBinding : instanceBindings)
//			instanceBinding.bind(newValue);			
	}
	
	private PropertyChangeListener versionChangeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updateSaved();
		}
	};

    @Override
    public void setContext(Context context) {
        init(context.getEntityManager());
    }

	private void init(EntityManager entityManager) {
        if (this.entityManager == null)
        	this.entityManager = entityManager;
		this.dataManager = (JavaBeanDataManager)this.entityManager.getDataManager();
		this.dataManager.addPropertyChangeListener("deepDirtyEntity", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				if (pce.getSource() == getInstance())
					updateDirty();
			}
		});
	}
	
//	public <P> void addInstanceBinding(Property<P> property, ObservableValueGetter<T, P> propertyGetter) {
//		InstanceBinding<T, P> instanceBinding = new UnidirectionalInstanceBinding<P>(property, propertyGetter); 
//		instanceBindings.add(instanceBinding);
//		if (instance != null)
//			instanceBinding.bind(instance);
//	}
//	
//	public <P> void addBidirectionalInstanceBinding(Property<P> property, PropertyGetter<T, P> propertyGetter) {
//		InstanceBinding<T, P> instanceBinding = new BidirectionalInstanceBinding<P>(property, propertyGetter); 
//		instanceBindings.add(instanceBinding);
//		if (instance != null)
//			instanceBinding.bind(instance);
//	}
	
	public void reset() {
		if (instance == null)
			return;
		entityManager.resetEntity(instance);
	}
	
	private String getVersionPropertyName(Object value) {
		String versionPropertyName = dataManager.getVersionPropertyName(value);
		if (versionPropertyName == null)
			throw new RuntimeException("No version property found on entity " + value);
		return versionPropertyName;
	}
	
	private Object getVersion(Object value) {
		return dataManager.getVersion(value);
	}
	
	
//	private interface InstanceBinding<T, P> {
//		
//		public void bind(T instance);
//		
//		public void unbind(T instance);
//	}
//	
//	private final class BidirectionalInstanceBinding<P> implements InstanceBinding<T, P> {
//		
//		private final
//		private final PropertyGetter<T, P> entityPropertyGetter;
//		
//		public BidirectionalInstanceBinding(Property<P> inputProperty, PropertyGetter<T, P> entityPropertyGetter) {
//			this.inputProperty = inputProperty;
//			this.entityPropertyGetter = entityPropertyGetter;
//		}
//		
//		public void bind(T instance) {
//			this.inputProperty.bindBidirectional(entityPropertyGetter.getProperty(instance));
//		}
//		
//		public void unbind(T instance) {
//			this.inputProperty.unbindBidirectional(entityPropertyGetter.getProperty(instance));
//		}
//	}
//
//	private final class UnidirectionalInstanceBinding<P> implements InstanceBinding<T, P> {
//		
//		private final Property<P> inputProperty;
//		private final ObservableValueGetter<T, P> entityPropertyGetter;
//		
//		public UnidirectionalInstanceBinding(Property<P> inputProperty, ObservableValueGetter<T, P> entityPropertyGetter) {
//			this.inputProperty = inputProperty;
//			this.entityPropertyGetter = entityPropertyGetter;
//		}
//		
//		public void bind(T instance) {
//			this.inputProperty.bind(entityPropertyGetter.getObservableValue(instance));
//		}
//		
//		public void unbind(T instance) {
//			this.inputProperty.unbind();
//		}
//	}
//	
//	
//	public static interface PropertyGetter<T, P> {
//		
//		public Property<P> getProperty(T instance);
//	}
//
//	public static interface ObservableValueGetter<T, P> {
//		
//		public ObservableValue<P> getObservableValue(T instance);
//	}
}
