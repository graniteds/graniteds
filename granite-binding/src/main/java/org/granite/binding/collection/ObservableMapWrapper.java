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
package org.granite.binding.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.granite.binding.collection.CollectionChangeEvent.Kind;

/**
 * @author William DRAI
 */
public class ObservableMapWrapper<K, V> implements ObservableMap<K, V> {
	
	protected CollectionChangeSupport ccs = new CollectionChangeSupport(this);
	private final Map<K, V> wrappedMap;
	
	public ObservableMapWrapper(Map<K, V> map) {
		this.wrappedMap = map;
	}
	
	public Map<K, V> getWrappedObservable() {
		return wrappedMap;
	}
	
	public void addCollectionChangeListener(CollectionChangeListener listener) {
		ccs.addCollectionChangeListener(listener);
	}
	public void removeCollectionChangeListener(CollectionChangeListener listener) {
		ccs.removeCollectionChangeListener(listener);
	}

	@Override
	public V put(K key, V value) {
		V oldValue = wrappedMap.put(key, value);
		if (oldValue != null)
			ccs.fireCollectionChangeEvent(Kind.REPLACE, key, new Object[] { new Object[] { key, value }});
		else
			ccs.fireCollectionChangeEvent(Kind.ADD, key, new Object[] { new Object[] { key, value }});
		return oldValue;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Entry<? extends K, ? extends V> me : map.entrySet())
			put(me.getKey(), me.getValue());
	}
	
	@Override
	public V remove(Object key) {
		V oldValue = wrappedMap.remove(key);
		if (oldValue != null)
			ccs.fireCollectionChangeEvent(Kind.REMOVE, key, new Object[] { new Object[] { key, oldValue }});
		return oldValue;
	}

	@Override
	public void clear() {
		if (wrappedMap.size() == 0)
			return;
		Object[] elements = new Object[wrappedMap.size()];
		int i = 0;
		for (Entry<? extends K, ? extends V> me : wrappedMap.entrySet())
			elements[i++] = new Object[] { me.getKey(), me.getValue() };
		wrappedMap.clear();
		ccs.fireCollectionChangeEvent(Kind.CLEAR, null, elements);		
	}

	@Override
	public V get(Object key) {
		return wrappedMap.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return wrappedMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return wrappedMap.containsValue(value);
	}

	@Override
	public Set<K> keySet() {
		return wrappedMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return wrappedMap.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return wrappedMap.entrySet();
	}

	@Override
	public int size() {
		return wrappedMap.size();
	}

	@Override
	public boolean isEmpty() {
		return wrappedMap.isEmpty();
	}
}
