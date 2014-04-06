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

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

import com.sun.javafx.collections.ObservableListWrapper;

/**
 * @author William DRAI
 */
public class ObservablePersistentList<E> extends ObservableListWrapper<E> implements UnsafePersistentCollection<PersistentList<E>> {
	
    private static final long serialVersionUID = 1L;
	
	private final PersistentList<E> persistentList;

	public ObservablePersistentList(PersistentList<E> persistentList) {
		super(persistentList);
		
		this.persistentList = persistentList;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		persistentList.writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		persistentList.readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return persistentList.wasInitialized();
	}

	@Override
	public void uninitialize() {
		persistentList.uninitialize();
	}

	@Override
	public void initialize() {
		persistentList.initialize();
	}

	@Override
	public void initializing() {
		persistentList.initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return persistentList.clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return persistentList.getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
		persistentList.setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return persistentList.isDirty();
	}

	@Override
	public void dirty() {
		persistentList.dirty();
	}

	@Override
	public void clearDirty() {
		persistentList.clearDirty();
	}

    @Override
    public void addListener(ChangeListener listener) {
        persistentList.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
        persistentList.removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener listener) {
        persistentList.addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener listener) {
        persistentList.removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback callback) {
		persistentList.withInitialized(callback);
	}
	
	@Override
	public PersistentList<E> internalPersistentCollection() {
		return persistentList;
	}
	
	@Override
	public String toString() {
		return persistentList.toString();
	}
}