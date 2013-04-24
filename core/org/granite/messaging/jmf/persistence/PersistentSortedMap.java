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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedMap<K, V> extends AbstractPersistentMapCollection<K, V, SortedMap<K, V>> implements SortedMap<K, V>, PersistentSortedCollection<K> {

	public PersistentSortedMap() {
	}

	public PersistentSortedMap(SortedMap<K, V> collection) {		
		this(collection, true);
	}

	public PersistentSortedMap(SortedMap<K, V> collection, boolean clone) {	
		if (collection instanceof SortedSet)
			throw new IllegalArgumentException("Should not be a SortedSet: " + collection);
		
		if (collection != null)
			init(clone ? new TreeMap<K, V>(collection) : collection, false);
	}

	public Comparator<? super K> comparator() {
		checkInitialized();
		return getCollection().comparator();
	}

	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		checkInitialized();
		return new SortedMapProxy<K, V>(this, getCollection().subMap(fromKey, toKey));
	}

	public SortedMap<K, V> headMap(K toKey) {
		checkInitialized();
		return new SortedMapProxy<K, V>(this, getCollection().headMap(toKey));
	}

	public SortedMap<K, V> tailMap(K fromKey) {
		checkInitialized();
		return new SortedMapProxy<K, V>(this, getCollection().tailMap(fromKey));
	}

	public K firstKey() {
		checkInitialized();
		return getCollection().firstKey();
	}

	public K lastKey() {
		checkInitialized();
		return getCollection().lastKey();
	}

	@Override
	protected PersistentCollectionSnapshot newSnapshot(boolean blank) {
		if (blank || !wasInitialized())
			return new PersistentSortedCollectionSnapshot();
		
		String comparatorClassName = null;
		if (getCollection().comparator() != null)
			comparatorClassName = getCollection().comparator().getClass().getName();
		
		return new PersistentSortedCollectionSnapshot(getMapElements(), isDirty(), comparatorClassName);
	}

	@Override
	protected Map<K, V> newCollection(PersistentCollectionSnapshot snapshot) {
		PersistentSortedCollectionSnapshot sortedSnapshot = (PersistentSortedCollectionSnapshot)snapshot;

		Comparator<? super K> comparator = null;
		String comparatorClassName = sortedSnapshot.getComparatorClassName();
		
		if (comparatorClassName != null) {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			try {
				@SuppressWarnings("unchecked")
				Class<Comparator<? super K>> comparatorClass = (Class<Comparator<? super K>>)classLoader.loadClass(comparatorClassName);
				comparator = comparatorClass.getDeclaredConstructor().newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException("Could not create instance of comparator: " + comparatorClassName, e);
			}
		}
		
		return new TreeMap<K, V>(comparator);
	}
}
