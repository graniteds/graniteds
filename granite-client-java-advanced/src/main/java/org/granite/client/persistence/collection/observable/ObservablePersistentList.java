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
package org.granite.client.persistence.collection.observable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.client.collection.ObservableList;
import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

/**
 * @author William DRAI
 */
public class ObservablePersistentList<E> extends ObservableList<E> implements UnsafePersistentCollection<PersistentList<E>> {
	
    private static final long serialVersionUID = 1L;
	
	public ObservablePersistentList(PersistentList<E> persistentList) {
		super(persistentList);
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
	public void initialize() {
		internalPersistentCollection().initialize();
	}

	@Override
	public void initializing() {
		internalPersistentCollection().initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return internalPersistentCollection().clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return internalPersistentCollection().getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
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
    public void addListener(ChangeListener listener) {
    	internalPersistentCollection().addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
    	internalPersistentCollection().removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener listener) {
    	internalPersistentCollection().addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener listener) {
    	internalPersistentCollection().removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback callback) {
		internalPersistentCollection().withInitialized(callback);
	}
	
	@Override
	public PersistentList<E> internalPersistentCollection() {
		return internalPersistentCollection();
	}

}
