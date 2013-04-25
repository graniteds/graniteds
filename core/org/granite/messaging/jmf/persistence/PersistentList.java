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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Franck WOLFF
 */
public class PersistentList<E> extends AbstractPersistentSimpleCollection<E, List<E>> implements List<E> {

	public PersistentList() {
	}

	public PersistentList(List<E> collection) {
		this(collection, true);
	}

	public PersistentList(List<E> collection, boolean clone) {
		if (collection != null)
			init((clone ? new ArrayList<E>(collection) : collection), false);
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		checkInitialized();
		if (getCollection().addAll(index, c)) {
			dirty();
			return true;
		}
		return false;
	}

	public E get(int index) {
		checkInitialized();
		return getCollection().get(index);
	}

	public E set(int index, E element) {
		checkInitialized();
		E previousElement = getCollection().set(index, element);
		if (previousElement == null ? element != null : !previousElement.equals(element))
			dirty();
		return previousElement;
	}

	public void add(int index, E element) {
		checkInitialized();
		getCollection().add(index, element);
		dirty();
	}

	public E remove(int index) {
		checkInitialized();
		E previousElement = getCollection().remove(index);
		dirty();
		return previousElement;
	}

	public int indexOf(Object o) {
		checkInitialized();
		return getCollection().indexOf(o);
	}

	public int lastIndexOf(Object o) {
		checkInitialized();
		return getCollection().lastIndexOf(o);
	}

	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	public ListIterator<E> listIterator(int index) {
		checkInitialized();
		return new ListIteratorProxy<E>(this, getCollection().listIterator(index));
	}

	public List<E> subList(int fromIndex, int toIndex) {
		checkInitialized();
		return new ListProxy<E>(this, getCollection().subList(fromIndex, toIndex));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized())
			init(new ArrayList<E>((Collection<? extends E>)snapshot.getElementsAsCollection()), snapshot.isDirty());
		else
			init(null, false);
	}
}
