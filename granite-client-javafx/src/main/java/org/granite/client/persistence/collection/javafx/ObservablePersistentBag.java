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
import org.granite.client.persistence.collection.PersistentBag;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

import com.sun.javafx.collections.ObservableListWrapper;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentBag<E> extends ObservableListWrapper<E> implements UnsafePersistentCollection<PersistentBag<E>> {
	
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
	public void initialize() {
		persistentBag.initialize();
	}

	@Override
	public void initializing() {
		persistentBag.initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return persistentBag.clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return persistentBag.getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
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
    public void addListener(ChangeListener listener) {
        persistentBag.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
        persistentBag.removeListener(listener);
    }

	@Override
	public void addListener(InitializationListener listener) {
		persistentBag.addListener(listener);
	}

    @Override
    public void removeListener(InitializationListener listener) {
        persistentBag.removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback callback) {
		persistentBag.withInitialized(callback);
	}
	
	@Override
	public PersistentBag<E> internalPersistentCollection() {
		return persistentBag;
	}
}