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
import java.util.Map;
import java.util.Set;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractPersistentMapCollection<K, V, C extends Map<K, V>> extends AbstractPersistentCollection<C> implements Map<K, V> {

	public AbstractPersistentMapCollection() {
	}

	public int size() {
		checkInitialized();
		return getCollection().size();
	}

	public boolean isEmpty() {
		checkInitialized();
		return getCollection().isEmpty();
	}

	public boolean containsKey(Object key) {
		checkInitialized();
		return getCollection().containsKey(key);
	}

	public boolean containsValue(Object value) {
		checkInitialized();
		return getCollection().containsValue(value);
	}

	public V get(Object key) {
		checkInitialized();
		return getCollection().get(key);
	}

	public V put(K key, V value) {
		return put(key, value, true);
	}
	
	protected V put(K key, V value, boolean checkInitialized) {
		if (checkInitialized)
			checkInitialized();
		
		boolean containsKey = getCollection().containsKey(key);
		V previousValue = getCollection().put(key, value);
		if (!containsKey || (previousValue == null ? value != null : !previousValue.equals(value)))
			dirty();
		return previousValue;
	}

	public V remove(Object key) {
		checkInitialized();
		
		boolean containsKey = getCollection().containsKey(key);
		V removedValue = getCollection().remove(key);
		if (containsKey)
			dirty();
		return removedValue;
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		checkInitialized();
		
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
			put(entry.getKey(), entry.getValue(), false);
	}

	public void clear() {
		checkInitialized();
		if (!getCollection().isEmpty()) {
			getCollection().clear();
			dirty();
		}
	}

	public Set<K> keySet() {
		checkInitialized();
		return new SetProxy<K>(this, getCollection().keySet());
	}

	public Collection<V> values() {
		checkInitialized();
		return new CollectionProxy<V>(this, getCollection().values());
	}

	public Set<Map.Entry<K, V>> entrySet() {
		checkInitialized();
		return new SetProxy<Map.Entry<K, V>>(this, getCollection().entrySet());
	}
}
