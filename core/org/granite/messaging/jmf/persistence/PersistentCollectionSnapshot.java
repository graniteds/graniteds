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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.granite.messaging.jmf.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class PersistentCollectionSnapshot implements Externalizable {
	
	protected boolean initialized = false;
	protected boolean dirty = false;
	protected Object[] elements = null;
	protected boolean sorted = false;
	protected String comparatorClassName = null;
	
	public PersistentCollectionSnapshot() {
	}
	
	public PersistentCollectionSnapshot(boolean sorted) {
		this.sorted = sorted;
	}

	public PersistentCollectionSnapshot(boolean initialized, boolean dirty, Collection<?> collection) {
		this.initialized = initialized;
		if (initialized) {
			this.dirty = dirty;
			this.elements = collection.toArray();
			
			if (collection instanceof SortedSet) {
				this.sorted = true;
				
				Comparator<?> comparator = ((SortedSet<?>)collection).comparator();
				if (comparator != null)
					this.comparatorClassName = comparator.getClass().getName();
			}
		}
	}

	public PersistentCollectionSnapshot(boolean initialized, boolean dirty, Map<?, ?> collection) {
		this.initialized = initialized;
		if (initialized) {
			this.dirty = dirty;
			
			Object[] entries = collection.entrySet().toArray();
			this.elements = new Object[entries.length * 2];
			
			int elementIndex = 0;
			for (int entryIndex = 0; entryIndex < entries.length; entryIndex++) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>)entries[entryIndex];
				this.elements[elementIndex++] = entry.getKey();
				this.elements[elementIndex++] = entry.getValue();
			}
			
			if (collection instanceof SortedMap) {
				this.sorted = true;
				
				Comparator<?> comparator = ((SortedMap<?, ?>)collection).comparator();
				if (comparator != null)
					this.comparatorClassName = comparator.getClass().getName();
			}
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean isSorted() {
		return sorted;
	}

	public String getComparatorClassName() {
		return comparatorClassName;
	}
	
	public <T> Comparator<T> newComparator(Reflection reflection)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException,
		IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
		
		if (comparatorClassName == null)
			return null;
		return reflection.newInstance(comparatorClassName);
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> getElementsAsCollection() {
		return (Collection<T>)Arrays.asList(elements);
	}
	
	public <K, V> Map<K, V> getElementsAsMap() {
		return new SnapshotMap<K, V>(elements);
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(initialized);
		if (initialized) {
			if (sorted)
				out.writeUTF(comparatorClassName);
			out.writeBoolean(dirty);
			out.writeObject(elements);
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		readInitializationData(in);
		if (initialized)
			readCoreData(in);
	}
	
	public void readInitializationData(ObjectInput in) throws IOException {
		initialized = in.readBoolean();
		
		if (initialized && sorted)
			comparatorClassName = in.readUTF();
	}
	
	public void readCoreData(ObjectInput in) throws IOException, ClassNotFoundException {
		this.dirty = in.readBoolean();
		this.elements = (Object[])in.readObject();
	}
	
	static class SnapshotMap<K, V> implements Map<K, V> {
		
		private final Object[] elements;
		
		public SnapshotMap(Object[] elements) {
			if ((elements.length % 2) != 0)
				throw new IllegalArgumentException("Elements must have an even length: " + elements.length);
			this.elements = elements;
		}
		
		public int size() {
			return elements.length / 2;
		}

		public boolean isEmpty() {
			return elements.length == 0;
		}

		public Set<Entry<K, V>> entrySet() {
			return new Set<Entry<K, V>>() {

				public int size() {
					return elements.length / 2;
				}

				public boolean isEmpty() {
					return elements.length == 0;
				}

				public Iterator<Entry<K, V>> iterator() {
					
					return new Iterator<Entry<K, V>>() {

						private int cursor = 0;
						
						public boolean hasNext() {
							return cursor < elements.length;
						}

						@SuppressWarnings("unchecked")
						public Entry<K, V> next() {
							if (cursor >= elements.length)
								throw new NoSuchElementException();
							
							K key = (K)elements[cursor++];
							V value = (V)elements[cursor++];
							return new SnapshotMapEntry<K, V>(key, value);
						}

						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}

				public boolean contains(Object o) {
					throw new UnsupportedOperationException();
				}

				public Object[] toArray() {
					throw new UnsupportedOperationException();
				}

				public <T> T[] toArray(T[] a) {
					throw new UnsupportedOperationException();
				}

				public boolean add(Entry<K, V> e) {
					throw new UnsupportedOperationException();
				}

				public boolean remove(Object o) {
					throw new UnsupportedOperationException();
				}

				public boolean containsAll(Collection<?> c) {
					throw new UnsupportedOperationException();
				}

				public boolean addAll(Collection<? extends Entry<K, V>> c) {
					throw new UnsupportedOperationException();
				}

				public boolean retainAll(Collection<?> c) {
					throw new UnsupportedOperationException();
				}

				public boolean removeAll(Collection<?> c) {
					throw new UnsupportedOperationException();
				}

				public void clear() {
					throw new UnsupportedOperationException();
				}

				@Override
				public int hashCode() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean equals(Object obj) {
					throw new UnsupportedOperationException();
				}
			};
		}

		public boolean containsKey(Object key) {
			throw new UnsupportedOperationException();
		}

		public boolean containsValue(Object value) {
			throw new UnsupportedOperationException();
		}

		public V get(Object key) {
			throw new UnsupportedOperationException();
		}

		public V put(K key, V value) {
			throw new UnsupportedOperationException();
		}

		public V remove(Object key) {
			throw new UnsupportedOperationException();
		}

		public void putAll(Map<? extends K, ? extends V> m) {
			throw new UnsupportedOperationException();
		}

		public void clear() {
			throw new UnsupportedOperationException();
		}

		public Set<K> keySet() {
			throw new UnsupportedOperationException();
		}

		public Collection<V> values() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object obj) {
			throw new UnsupportedOperationException();
		}
	}
	
	static class SnapshotMapEntry<K, V> implements Entry<K, V> {

		private final K key;
		private final V value;
		
		public SnapshotMapEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			return (key == null   ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Entry))
				return false;
			Entry<?, ?> e = (Entry<?, ?>)obj;
			return (
				(key == null ? e.getKey() == null : key.equals(e.getKey()))  &&
				(value == null ? e.getValue() == null : value.equals(e.getValue()))
			);
		}
	}
}