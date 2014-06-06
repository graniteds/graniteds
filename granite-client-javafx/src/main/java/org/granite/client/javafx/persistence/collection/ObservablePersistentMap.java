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
package org.granite.client.javafx.persistence.collection;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

import com.sun.javafx.collections.ObservableMapWrapper;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentMap<K, V> extends ObservableMapWrapper<K, V> implements UnsafePersistentCollection<Map<K, V>> {
	
    private static final long serialVersionUID = 1L;
	
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
	public void initialize(Map<K, V> map, Initializer<Map<K, V>> initializer) {
		persistentMap.initialize(map, initializer != null ? initializer : new Initializer<Map<K, V>>() {
			public void initialize(Map<K, V> map) {
				putAll(map);
			}
		});		
	}
	
	@Override
	public void initializing() {
		persistentMap.initializing();
	}

	@Override
	public PersistentCollection<Map<K, V>> clone(boolean uninitialize) {
		return persistentMap.clone(uninitialize);
	}

	@Override
	public Loader<Map<K, V>> getLoader() {
		return persistentMap.getLoader();
	}

	@Override
	public void setLoader(Loader<Map<K, V>> loader) {
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
    public void addListener(ChangeListener<Map<K, V>> listener) {
        persistentMap.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<Map<K, V>> listener) {
        persistentMap.removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener<Map<K, V>> listener) {
        persistentMap.addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener<Map<K, V>> listener) {
        persistentMap.removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback<Map<K, V>> callback) {
		persistentMap.withInitialized(callback);
	}
	
	@Override
	public PersistentMap<K, V> internalPersistentCollection() {
		return persistentMap;
	}
	
	@Override
	public String toString() {
		return persistentMap.toString();
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(persistentMap);
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof UnsafePersistentCollection 
				&& ((UnsafePersistentCollection<?>)object).internalPersistentCollection() == persistentMap;
	}
}