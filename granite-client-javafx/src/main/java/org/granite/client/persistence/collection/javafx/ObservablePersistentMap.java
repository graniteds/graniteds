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
package org.granite.client.persistence.collection.javafx;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

import com.sun.javafx.collections.ObservableMapWrapper;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentMap<K, V> extends ObservableMapWrapper<K, V> implements UnsafePersistentCollection<PersistentMap<K, V>> {
	
	private final PersistentMap<K, V> persistentMap;

	public ObservablePersistentMap(PersistentMap<K, V> persistentMap) {
		super(persistentMap);
		
		this.persistentMap = persistentMap;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		persistentMap.writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		persistentMap.readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return persistentMap.wasInitialized();
	}

	@Override
	public void uninitialize() {
		persistentMap.uninitialize();
	}

	@Override
	public void initialize() {
		persistentMap.initialize();
	}

	@Override
	public void initializing() {
		persistentMap.initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return persistentMap.clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return persistentMap.getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
		persistentMap.setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return persistentMap.isDirty();
	}

	@Override
	public void dirty() {
		persistentMap.dirty();
	}

	@Override
	public void clearDirty() {
		persistentMap.clearDirty();
	}

    @Override
    public void addListener(ChangeListener listener) {
        persistentMap.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
        persistentMap.removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener listener) {
        persistentMap.addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener listener) {
        persistentMap.removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback callback) {
		persistentMap.withInitialized(callback);
	}
	
	@Override
	public PersistentMap<K, V> internalPersistentCollection() {
		return persistentMap;
	}
}