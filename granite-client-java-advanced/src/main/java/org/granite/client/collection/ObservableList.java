/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.granite.client.collection.CollectionChangeListener;
import org.granite.client.collection.CollectionChangeSupport;
import org.granite.client.collection.ObservableCollection;
import org.granite.client.collection.CollectionChangeEvent.Kind;

/**
 * @author William DRAI
 */
public class ObservableList<E> implements List<E>, ObservableCollection {
	
	protected CollectionChangeSupport ccs = new CollectionChangeSupport(this);
	private final List<E> wrappedList;
	
	public ObservableList(List<E> list) {
		this.wrappedList = list;
	}
	
	protected List<E> getWrappedList() {
		return wrappedList;
	}
	
	public void addCollectionChangeListener(CollectionChangeListener listener) {
		ccs.addCollectionChangeListener(listener);
	}
	
	public void removeCollectionChangeListener(CollectionChangeListener listener) {
		ccs.removeCollectionChangeListener(listener);
	}

	@Override
	public boolean add(E element) {
		int index = wrappedList.size();
		boolean added = wrappedList.add(element);
		if (added) {
			ccs.fireCollectionChangeEvent(Kind.ADD, index, new Object[] { element });
			trackElement(element);
		}
		return added;
	}

	@Override
	public void add(int index, E element) {
		wrappedList.add(index, element);
		ccs.fireCollectionChangeEvent(Kind.ADD, index, new Object[] { element });
		trackElement(element);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> elements) {
		int index = wrappedList.size();
		boolean added = wrappedList.addAll(elements);
		if (added)
			ccs.fireCollectionChangeEvent(Kind.ADD, index, elements.toArray());
		return added;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> elements) {
		boolean added = wrappedList.addAll(index, elements);
		if (added) {
			ccs.fireCollectionChangeEvent(Kind.ADD, index, elements.toArray());
			for (Object element : elements)
				trackElement(element);
		}
		return added;
	}

	@Override
	public E remove(int index) {
		E element = wrappedList.remove(index);
		ccs.fireCollectionChangeEvent(Kind.REMOVE, index, new Object[] { element });
		untrackElement(element);
		return element;
	}

	@Override
	public boolean remove(Object element) {
		int index = wrappedList.indexOf(element);
		boolean removed = wrappedList.remove(element);
		if (removed) {
			ccs.fireCollectionChangeEvent(Kind.REMOVE, index, new Object[] { element });
			untrackElement(element);
		}
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean removed = false;
		List<Object> removedElements = new ArrayList<Object>();
		int index = 0;
		for (Object element : collection) {
			if (remove(element)) {
				removed = true;
				removedElements.add(element);
				ccs.fireCollectionChangeEvent(Kind.REMOVE, index, new Object[] { element });
				untrackElement(element);
			}
			index++;
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean changed = false;
		int index = 0;
		for (Object element : collection) {
			if (!wrappedList.contains(element) && remove(element)) {
				changed = true;
				ccs.fireCollectionChangeEvent(Kind.REMOVE, index, new Object[] { element });
				untrackElement(element);
			}
			index++;
		}
		return changed;
	}

	@Override
	public E set(int index, E element) {
		E oldElement = wrappedList.set(index, element);
		if (element != oldElement) {
			ccs.fireCollectionChangeEvent(Kind.REPLACE, index, new Object[] { oldElement, element });
			untrackElement(oldElement);
			trackElement(element);
		}
		return oldElement;
	}
	
	@Override
	public void clear() {
		if (wrappedList.size() == 0)
			return;
		Object[] elements = wrappedList.toArray();
		wrappedList.clear();
		ccs.fireCollectionChangeEvent(Kind.CLEAR, null, elements);
		for (Object element : elements)
			untrackElement(element);
	}

	@Override
	public boolean contains(Object element) {
		return wrappedList.contains(element);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return wrappedList.containsAll(collection);
	}

	@Override
	public E get(int index) {
		return wrappedList.get(index);
	}

	@Override
	public int indexOf(Object element) {
		return wrappedList.indexOf(element);
	}

	@Override
	public int lastIndexOf(Object element) {
		return wrappedList.lastIndexOf(element);
	}

	@Override
	public boolean isEmpty() {
		return wrappedList.isEmpty();
	}

	@Override
	public int size() {
		return wrappedList.size();
	}

	@Override
	public Object[] toArray() {
		return wrappedList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return wrappedList.toArray(array);
	}

	@Override
	public List<E> subList(int start, int end) {
		return new ObservableList<E>(wrappedList.subList(start, end));
	}
	
	@Override
	public Iterator<E> iterator() {
		return new IteratorWrapper(wrappedList.iterator());
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ListIteratorWrapper(wrappedList.listIterator());
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListIteratorWrapper(wrappedList.listIterator(index), index);
	}
	
	
	private class IteratorWrapper implements Iterator<E> {
		
		private final Iterator<E> wrappedIterator;
		private int index = -1;
		private E element;
		
		public IteratorWrapper(Iterator<E> iterator) {
			this.wrappedIterator = iterator;
		}

		public boolean hasNext() {
			return wrappedIterator.hasNext();
		}

		public E next() {
			index++;
			element = wrappedIterator.next();
			return element;
		}

		public void remove() {
			wrappedIterator.remove();
			ccs.fireCollectionChangeEvent(Kind.REMOVE, index, new Object[] { element });
		}
	}
	
	private class ListIteratorWrapper implements ListIterator<E> {
		
		private final ListIterator<E> wrappedIterator;
		private int index = -1;
		private E element;
		
		public ListIteratorWrapper(ListIterator<E> iterator) {
			this.wrappedIterator = iterator;
		}

		public ListIteratorWrapper(ListIterator<E> iterator, int index) {
			this.wrappedIterator = iterator;
			this.index = index;			
		}
		
		public void add(E element) {
			wrappedIterator.add(element);
			ccs.fireCollectionChangeEvent(Kind.ADD, index, new Object[] { element });
		}

		public void remove() {
			wrappedIterator.remove();
			ccs.fireCollectionChangeEvent(Kind.REMOVE, index, new Object[] { element });
		}

		public void set(E element) {
			Object oldElement = this.element;
			wrappedIterator.set(element);
			if (element != oldElement)
				ccs.fireCollectionChangeEvent(Kind.REPLACE, index, new Object[] { oldElement, element });
		}

		public boolean hasNext() {
			return wrappedIterator.hasNext();
		}

		public boolean hasPrevious() {
			return wrappedIterator.hasPrevious();
		}

		public E next() {
			index++;
			element = wrappedIterator.next();
			return element;
		}

		public int nextIndex() {
			return wrappedIterator.nextIndex();
		}

		public E previous() {
			index--;
			element = wrappedIterator.previous();
			return element;
		}

		public int previousIndex() {
			return wrappedIterator.previousIndex();
		}		
	}
	
	
	private PropertyChangeListener trackingListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			ccs.fireCollectionChangeEvent(Kind.UPDATE, null, new Object[] { event });
		}
	};
	
	private void trackElement(Object obj) {
		if (obj == null)
			return;
		try {
			Method m = obj.getClass().getMethod("addPropertyChangeListener", PropertyChangeListener.class);
			m.invoke(obj, trackingListener);
		}
		catch (Exception e) {
		}
	}
	
	private void untrackElement(Object obj) {
		if (obj == null)
			return;
		try {
			Method m = obj.getClass().getMethod("removePropertyChangeListener", PropertyChangeListener.class);
			m.invoke(obj, trackingListener);
		}
		catch (Exception e) {
		}
	}
}
