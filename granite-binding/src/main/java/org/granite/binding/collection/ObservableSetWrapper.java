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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.granite.binding.collection.CollectionChangeListener;
import org.granite.binding.collection.CollectionChangeSupport;
import org.granite.binding.collection.CollectionChangeEvent.Kind;

/**
 * @author William DRAI
 */
public class ObservableSetWrapper<E> implements ObservableSet<E> {
	
	protected CollectionChangeSupport ccs = new CollectionChangeSupport(this);
	private final Set<E> wrappedSet;
	
	public ObservableSetWrapper(Set<E> set) {
		this.wrappedSet = set;
	}
	
	public Set<E> getWrappedObservable() {
		return wrappedSet;
	}
	
	public void addCollectionChangeListener(CollectionChangeListener listener) {
		ccs.addCollectionChangeListener(listener);
	}
	
	public void removeCollectionChangeListener(CollectionChangeListener listener) {
		ccs.removeCollectionChangeListener(listener);
	}

	@Override
	public boolean add(E element) {
		boolean added = wrappedSet.add(element);
		if (added)
			ccs.fireCollectionChangeEvent(Kind.ADD, null, new Object[] { element });
		return added;
	}

	@Override
	public boolean addAll(Collection<? extends E> elements) {
		boolean added = wrappedSet.addAll(elements);
		if (added)
			ccs.fireCollectionChangeEvent(Kind.ADD, null, elements.toArray());
		return added;
	}

	@Override
	public boolean remove(Object element) {
		boolean removed = wrappedSet.remove(element);
		if (removed)
			ccs.fireCollectionChangeEvent(Kind.REMOVE, null, new Object[] { removed });
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean removed = false;
		for (Object element : collection) {
			if (remove(element))
				removed = true;
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean changed = false;
		for (Object element : collection) {
			if (!wrappedSet.contains(element) && remove(element))
				changed = true;
		}
		return changed;
	}

	@Override
	public void clear() {
		if (wrappedSet.size() == 0)
			return;
		Object[] elements = wrappedSet.toArray();
		wrappedSet.clear();
		ccs.fireCollectionChangeEvent(Kind.CLEAR, null, elements);		
	}

	@Override
	public boolean contains(Object element) {
		return wrappedSet.contains(element);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return wrappedSet.containsAll(collection);
	}

	@Override
	public boolean isEmpty() {
		return wrappedSet.isEmpty();
	}

	@Override
	public int size() {
		return wrappedSet.size();
	}

	@Override
	public Object[] toArray() {
		return wrappedSet.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return wrappedSet.toArray(array);
	}
	
	@Override
	public Iterator<E> iterator() {
		return new IteratorWrapper(wrappedSet.iterator());
	}
	
	
	private class IteratorWrapper implements Iterator<E> {
		
		private final Iterator<E> wrappedIterator;
		private E element;
		
		public IteratorWrapper(Iterator<E> iterator) {
			this.wrappedIterator = iterator;
		}

		public boolean hasNext() {
			return wrappedIterator.hasNext();
		}

		public E next() {
			element = wrappedIterator.next();
			return element;
		}

		public void remove() {
			wrappedIterator.remove();
			ccs.fireCollectionChangeEvent(Kind.REMOVE, null, new Object[] { element });
		}
	}
}
