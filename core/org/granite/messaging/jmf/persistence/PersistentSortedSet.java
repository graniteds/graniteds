/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.messaging.jmf.persistence;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedSet<E> extends AbstractPersistentSimpleCollection<E, SortedSet<E>> implements SortedSet<E>, PersistentSortedCollection<E> {

	public PersistentSortedSet() {
	}

	public PersistentSortedSet(SortedSet<E> collection) {		
		this(collection, true);
	}

	public PersistentSortedSet(SortedSet<E> collection, boolean clone) {		
		if (collection != null)
			init(clone ? new TreeSet<E>(collection) : collection, false);
	}

	public Comparator<? super E> comparator() {
		checkInitialized();
		return getCollection().comparator();
	}

	public SortedSet<E> subSet(E fromElement, E toElement) {
		checkInitialized();
		return new SortedSetProxy<E>(this, getCollection().subSet(fromElement, toElement));
	}

	public SortedSet<E> headSet(E toElement) {
		checkInitialized();
		return new SortedSetProxy<E>(this, getCollection().headSet(toElement));
	}

	public SortedSet<E> tailSet(E fromElement) {
		checkInitialized();
		return new SortedSetProxy<E>(this, getCollection().tailSet(fromElement));
	}

	public E first() {
		checkInitialized();
		return getCollection().first();
	}

	public E last() {
		checkInitialized();
		return getCollection().last();
	}

	@Override
	protected PersistentCollectionSnapshot createSnapshot(boolean forReading) {
		if (forReading || !wasInitialized())
			return new PersistentCollectionSnapshot(true);
		return new PersistentCollectionSnapshot(true, isDirty(), getCollection());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized()) {
			Comparator<? super E> comparator = null;
			try {
				comparator = snapshot.newComparator(getClassLoader());
			}
			catch (Exception e) {
				throw new RuntimeException("Could not create instance of comparator", e);
			}
			SortedSet<E> set = new TreeSet<E>(comparator);
			set.addAll((Collection<? extends E>)snapshot.getElementsAsCollection());
			init(set, snapshot.isDirty());
		}
		else
			init(null, false);
	}
}
