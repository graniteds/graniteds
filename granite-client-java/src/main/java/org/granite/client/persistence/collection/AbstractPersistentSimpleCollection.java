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
package org.granite.client.persistence.collection;

import java.util.Collection;
import java.util.Iterator;

import org.granite.messaging.persistence.PersistentCollectionSnapshot;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractPersistentSimpleCollection<E, C extends Collection<E>> extends AbstractPersistentCollection<C> implements Collection<E> {

	public AbstractPersistentSimpleCollection() {
	}

	public int size() {
		if (!checkInitializedRead())
			return 0;
		return getCollection().size();
	}

	public boolean isEmpty() {
		if (!checkInitializedRead())
			return true;
		return getCollection().isEmpty();
	}

	public boolean contains(Object o) {
		if (!checkInitializedRead())
			return false;
		return getCollection().contains(o);
	}

	public Iterator<E> iterator() {
		if (!checkInitializedRead())
			return null;
		return new IteratorProxy<E>(getCollection().iterator());
	}

	public Object[] toArray() {
		if (!checkInitializedRead())
			return null;
		return getCollection().toArray();
	}

	public <T> T[] toArray(T[] a) {
		if (!checkInitializedRead())
			return null;
		return getCollection().toArray(a);
	}

	public boolean add(E e) {
		checkInitializedWrite();
		if (getCollection().add(e)) {
			dirty();
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		checkInitializedWrite();
		if (getCollection().remove(o)) {
			dirty();
			return true;
		}
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		if (!checkInitializedRead())
			return false;
		return getCollection().containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		checkInitializedWrite();
		if (getCollection().addAll(c)) {
			dirty();
			return true;
		}
		return false;
	}

	public boolean removeAll(Collection<?> c) {
		checkInitializedWrite();
		if (getCollection().removeAll(c)) {
			dirty();
			return true;
		}
		return false;
	}

	public boolean retainAll(Collection<?> c) {
		checkInitializedWrite();
		if (getCollection().retainAll(c)) {
			dirty();
			return true;
		}
		return false;
	}

	public void clear() {
		checkInitializedWrite();
		if (!getCollection().isEmpty()) {
			getCollection().clear();
			dirty();
		}
	}

	@Override
	protected PersistentCollectionSnapshot createSnapshot(Object io, boolean forReading) {
		PersistentCollectionSnapshotFactory factory = PersistentCollectionSnapshotFactory.newInstance(io);
		if (forReading || !wasInitialized())
			return factory.newPersistentCollectionSnapshot(getDetachedState());
		return factory.newPersistentCollectionSnapshot(true, getDetachedState(), isDirty(), getCollection());
	}
}
