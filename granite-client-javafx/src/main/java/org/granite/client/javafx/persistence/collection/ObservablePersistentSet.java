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
package org.granite.client.javafx.persistence.collection;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

import com.sun.javafx.collections.ObservableSetWrapper;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentSet<E> extends ObservableSetWrapper<E> implements UnsafePersistentCollection<PersistentSet<E>> {
	
    private static final long serialVersionUID = 1L;
	
	private final PersistentSet<E> persistentSet;

	public ObservablePersistentSet(PersistentSet<E> persistentSet) {
		super(persistentSet);
		
		this.persistentSet = persistentSet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		return super.iterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		persistentSet.writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		persistentSet.readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return persistentSet.wasInitialized();
	}

	@Override
	public void uninitialize() {
		persistentSet.uninitialize();
	}

	@Override
	public void initialize() {
		persistentSet.initialize();
	}

	@Override
	public void initializing() {
		persistentSet.initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return persistentSet.clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return persistentSet.getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
		persistentSet.setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return persistentSet.isDirty();
	}

	@Override
	public void dirty() {
		persistentSet.dirty();
	}

	@Override
	public void clearDirty() {
		persistentSet.clearDirty();
	}

    @Override
    public void addListener(ChangeListener listener) {
        persistentSet.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
        persistentSet.removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener listener) {
        persistentSet.addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener listener) {
        persistentSet.removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback callback) {
		persistentSet.withInitialized(callback);
	}
	
	@Override
	public PersistentSet<E> internalPersistentCollection() {
		return persistentSet;
	}
	
	@Override
	public String toString() {
		return persistentSet.toString();
	}
}