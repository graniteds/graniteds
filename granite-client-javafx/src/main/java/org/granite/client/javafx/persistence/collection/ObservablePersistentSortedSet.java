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
import java.util.Iterator;
import java.util.SortedSet;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentSortedSet;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

import com.sun.javafx.collections.ObservableSetWrapper;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentSortedSet<E> extends ObservableSetWrapper<E> implements UnsafePersistentCollection<SortedSet<E>> {
	
    private static final long serialVersionUID = 1L;
	
	private final PersistentSortedSet<E> persistentSortedSet;

	public ObservablePersistentSortedSet(PersistentSortedSet<E> persistentSortedSet) {
		super(persistentSortedSet);
		
		this.persistentSortedSet = persistentSortedSet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		return super.iterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		persistentSortedSet.writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		persistentSortedSet.readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return persistentSortedSet.wasInitialized();
	}

	@Override
	public void uninitialize() {
		persistentSortedSet.uninitialize();
	}

	@Override
	public void initialize(SortedSet<E> set, Initializer<SortedSet<E>> initializer) {
		persistentSortedSet.initialize(set, initializer != null ? initializer : new Initializer<SortedSet<E>>() {
			public void initialize(SortedSet<E> set) {
				addAll(set);
			}
		});
	}
	
	@Override
	public void initializing() {
		persistentSortedSet.initializing();
	}

	@Override
	public PersistentCollection<SortedSet<E>> clone(boolean uninitialize) {
		return persistentSortedSet.clone(uninitialize);
	}

	@Override
	public Loader<SortedSet<E>> getLoader() {
		return persistentSortedSet.getLoader();
	}

	@Override
	public void setLoader(Loader<SortedSet<E>> loader) {
		persistentSortedSet.setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return persistentSortedSet.isDirty();
	}

	@Override
	public void dirty() {
		persistentSortedSet.dirty();
	}

	@Override
	public void clearDirty() {
		persistentSortedSet.clearDirty();
	}

    @Override
    public void addListener(ChangeListener<SortedSet<E>> listener) {
        persistentSortedSet.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<SortedSet<E>> listener) {
        persistentSortedSet.removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener<SortedSet<E>> listener) {
        persistentSortedSet.addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener<SortedSet<E>> listener) {
        persistentSortedSet.removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback<SortedSet<E>> callback) {
		persistentSortedSet.withInitialized(callback);
	}
	
	@Override
	public PersistentSortedSet<E> internalPersistentCollection() {
		return persistentSortedSet;
	}
	
	@Override
	public String toString() {
		return persistentSortedSet.toString();
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(persistentSortedSet);
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof UnsafePersistentCollection 
				&& ((UnsafePersistentCollection<?>)object).internalPersistentCollection() == persistentSortedSet;
	}
}