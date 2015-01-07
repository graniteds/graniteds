/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
import java.util.List;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentBag;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

import com.sun.javafx.collections.ObservableListWrapper;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentBag<E> extends ObservableListWrapper<E> implements UnsafePersistentCollection<List<E>> {
	
    private static final long serialVersionUID = 1L;
	
	private final PersistentBag<E> persistentBag;

	public ObservablePersistentBag(PersistentBag<E> persistentBag) {
		super(persistentBag);
		
		this.persistentBag = persistentBag;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		persistentBag.writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		persistentBag.readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return persistentBag.wasInitialized();
	}

	@Override
	public void uninitialize() {
		persistentBag.uninitialize();
	}

	@Override
	public void initialize(List<E> list, final Initializer<List<E>> initializer) {
		persistentBag.initialize(list, initializer != null ? initializer : new Initializer<List<E>>() {
			public void initialize(List<E> list) {
				addAll(list);
			}
		});
	}

	@Override
	public void initializing() {
		persistentBag.initializing();
	}

	@Override
	public PersistentCollection<List<E>> clone(boolean uninitialize) {
		return persistentBag.clone(uninitialize);
	}

	@Override
	public Loader<List<E>> getLoader() {
		return persistentBag.getLoader();
	}

	@Override
	public void setLoader(Loader<List<E>> loader) {
		persistentBag.setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return persistentBag.isDirty();
	}

	@Override
	public void dirty() {
		persistentBag.dirty();
	}

	@Override
	public void clearDirty() {
		persistentBag.clearDirty();
	}

    @Override
    public void addListener(ChangeListener<List<E>> listener) {
        persistentBag.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<List<E>> listener) {
        persistentBag.removeListener(listener);
    }

	@Override
	public void addListener(InitializationListener<List<E>> listener) {
		persistentBag.addListener(listener);
	}

    @Override
    public void removeListener(InitializationListener<List<E>> listener) {
        persistentBag.removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback<List<E>> callback) {
		persistentBag.withInitialized(callback);
	}
	
	@Override
	public PersistentBag<E> internalPersistentCollection() {
		return persistentBag;
	}
	
	@Override
	public String toString() {
		return persistentBag.toString();
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(persistentBag);
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof UnsafePersistentCollection 
				&& ((UnsafePersistentCollection<?>)object).internalPersistentCollection() == persistentBag;
	}
}