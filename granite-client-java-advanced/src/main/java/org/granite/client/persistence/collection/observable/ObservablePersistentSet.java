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
package org.granite.client.persistence.collection.observable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import org.granite.binding.collection.ObservableSetWrapper;
import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

/**
 * @author William DRAI
 */
public class ObservablePersistentSet<E> extends ObservableSetWrapper<E> implements UnsafePersistentCollection<Set<E>> {
	
    private static final long serialVersionUID = 1L;
	
	private final PersistentSet<E> persistentSet;
	
	public ObservablePersistentSet() {
		super(new PersistentSet<E>());
		this.persistentSet = (PersistentSet<E>)getWrappedObservable();
	}
	
	public ObservablePersistentSet(PersistentSet<E> persistentSet) {
		super(persistentSet);
		
		this.persistentSet = persistentSet;
	}
	
	@Override
	public PersistentSet<E> internalPersistentCollection() {
		return persistentSet;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		internalPersistentCollection().writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		internalPersistentCollection().readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return internalPersistentCollection().wasInitialized();
	}

	@Override
	public void uninitialize() {
		internalPersistentCollection().uninitialize();
	}

	@Override
	public void initialize(Set<E> set, Initializer<Set<E>> initializer) {
		internalPersistentCollection().initialize(set, initializer != null ? initializer : new Initializer<Set<E>>() {
			@Override
			public void initialize(Set<E> set) {
				addAll(set);
			}
		});
	}

	@Override
	public void initializing() {
		internalPersistentCollection().initializing();
	}

	@Override
	public PersistentCollection<Set<E>> clone(boolean uninitialize) {
		return internalPersistentCollection().clone(uninitialize);
	}

	@Override
	public Loader<Set<E>> getLoader() {
		return internalPersistentCollection().getLoader();
	}

	@Override
	public void setLoader(Loader<Set<E>> loader) {
		internalPersistentCollection().setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return internalPersistentCollection().isDirty();
	}

	@Override
	public void dirty() {
		internalPersistentCollection().dirty();
	}

	@Override
	public void clearDirty() {
		internalPersistentCollection().clearDirty();
	}

    @Override
    public void addListener(ChangeListener<Set<E>> listener) {
    	internalPersistentCollection().addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<Set<E>> listener) {
    	internalPersistentCollection().removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener<Set<E>> listener) {
    	internalPersistentCollection().addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener<Set<E>> listener) {
    	internalPersistentCollection().removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback<Set<E>> callback) {
		internalPersistentCollection().withInitialized(callback);
	}

}
