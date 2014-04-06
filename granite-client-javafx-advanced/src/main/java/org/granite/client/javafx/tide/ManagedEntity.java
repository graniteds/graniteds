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
package org.granite.client.javafx.tide;

import java.lang.reflect.Method;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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
    
	private EntityManager entityManager;
	private JavaFXDataManager dataManager;
	
	private BooleanProperty saved = new SimpleBooleanProperty(this, "saved", true);
	private BooleanProperty dirty = new SimpleBooleanProperty(this, "dirty", false);

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
	
	public BooleanProperty savedProperty() {
		return saved;
	}
	public boolean isSaved() {
		return saved.get();
	}
	
	public BooleanProperty dirtyProperty() {
		return dirty;
	}
	public boolean isDirty() {
		return dirty.get();
	}
	
	private ChangeListener<Object> entityChangeListener = new ChangeListener<Object>() {
		@Override
		public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
			if (oldValue != null) {
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
        if (this.entityManager != null)
            return;
		this.entityManager = entityManager;
		this.dataManager = (JavaFXDataManager)entityManager.getDataManager();		
		this.instance.addListener(entityChangeListener);
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
