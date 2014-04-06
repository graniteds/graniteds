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

import java.io.ObjectInput;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.granite.messaging.persistence.PersistentCollectionSnapshot;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedSet<E> extends AbstractPersistentSimpleCollection<E, SortedSet<E>> implements SortedSet<E>, PersistentSortedCollection<E> {

    private static final long serialVersionUID = 1L;
	
	public PersistentSortedSet() {
	}

	public PersistentSortedSet(boolean initialized) {
		this(initialized ? new TreeSet<E>() : null, false);
	}

	public PersistentSortedSet(SortedSet<E> collection) {		
		this(collection, true);
	}

	public PersistentSortedSet(SortedSet<E> collection, boolean clone) {		
		if (collection != null)
			init(clone ? new TreeSet<E>(collection) : collection, null, false);
	}
	
	@Override
	protected void doInitialize() {
		init(new TreeSet<E>(), null, false);
	}

	public Comparator<? super E> comparator() {
		return getCollection().comparator();
	}

	public SortedSet<E> subSet(E fromElement, E toElement) {
		if (!checkInitializedRead())
			return null;
		return new SortedSetProxy<E>(getCollection().subSet(fromElement, toElement));
	}

	public SortedSet<E> headSet(E toElement) {
		if (!checkInitializedRead())
			return null;
		return new SortedSetProxy<E>(getCollection().headSet(toElement));
	}

	public SortedSet<E> tailSet(E fromElement) {
		if (!checkInitializedRead())
			return null;
		return new SortedSetProxy<E>(getCollection().tailSet(fromElement));
	}

	public E first() {
		if (!checkInitializedRead())
			return null;
		return getCollection().first();
	}

	public E last() {
		if (!checkInitializedRead())
			return null;
		return getCollection().last();
	}

	@Override
	protected PersistentCollectionSnapshot createSnapshot(Object io, boolean forReading) {
		PersistentCollectionSnapshotFactory factory = PersistentCollectionSnapshotFactory.newInstance(io);
		if (forReading || !wasInitialized())
			return factory.newPersistentCollectionSnapshot(true, getDetachedState());
		return factory.newPersistentCollectionSnapshot(true, getDetachedState(), isDirty(), getCollection());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(ObjectInput in, PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized()) {
			Comparator<? super E> comparator = null;
			try {
				comparator = snapshot.newComparator(in);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not create instance of comparator", e);
			}
			SortedSet<E> set = new TreeSet<E>(comparator);
			set.addAll((Collection<? extends E>)snapshot.getElementsAsCollection());
			init(set, snapshot.getDetachedState(), snapshot.isDirty());
		}
		else
			init(null, snapshot.getDetachedState(), false);
	}
	
    public PersistentSortedSet<E> clone(boolean uninitialize) {
    	PersistentSortedSet<E> set = new PersistentSortedSet<E>();
    	if (wasInitialized() && !uninitialize)
    		set.init(getCollection(), null, isDirty());
        return set; 
    }
}
