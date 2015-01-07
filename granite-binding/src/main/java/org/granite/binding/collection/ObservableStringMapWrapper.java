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
package org.granite.binding.collection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.granite.binding.collection.CollectionChangeEvent.Kind;

/**
 * @author William DRAI
 */
public class ObservableStringMapWrapper<V> implements ObservableMap<String, V> {
	
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	protected CollectionChangeSupport ccs = new CollectionChangeSupport(this);
	private final Map<String, V> wrappedMap;
	
	public ObservableStringMapWrapper(Map<String, V> map) {
		this.wrappedMap = map;
	}
	
	public Map<String, V> getWrappedObservable() {
		return wrappedMap;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(property, listener);
	}
	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(property, listener);
	}
	
	public void addCollectionChangeListener(CollectionChangeListener listener) {
		ccs.addCollectionChangeListener(listener);
	}
	public void removeCollectionChangeListener(CollectionChangeListener listener) {
		ccs.removeCollectionChangeListener(listener);
	}

	@Override
	public V put(String key, V value) {
		V oldValue = wrappedMap.put(key, value);
		pcs.firePropertyChange(key, oldValue, value);
		if (oldValue != null)
			ccs.fireCollectionChangeEvent(Kind.REPLACE, key, new Object[] { new Object[] { key, value }});
		else
			ccs.fireCollectionChangeEvent(Kind.ADD, key, new Object[] { new Object[] { key, value }});
		return oldValue;
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> map) {
		for (Entry<? extends String, ? extends V> me : map.entrySet())
			put(me.getKey(), me.getValue());
	}
	
	@Override
	public V remove(Object key) {
		V oldValue = wrappedMap.remove(key);
		if (oldValue != null) {
			pcs.firePropertyChange((String)key, oldValue, null);
			ccs.fireCollectionChangeEvent(Kind.REMOVE, key, new Object[] { new Object[] { key, oldValue }});
		}
		return oldValue;
	}

	@Override
	public void clear() {
		if (wrappedMap.size() == 0)
			return;
		Object[][] elements = new Object[wrappedMap.size()][];
		int i = 0;
		for (Entry<? extends String, ? extends V> me : wrappedMap.entrySet())
			elements[i++] = new Object[] { me.getKey(), me.getValue() };
		wrappedMap.clear();
		for (Object[] element : elements)
			pcs.firePropertyChange((String)element[0], element[1], null);
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
	public Set<String> keySet() {
		return wrappedMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return wrappedMap.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {
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
