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
package org.granite.client.persistence.collection;

import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.granite.messaging.persistence.PersistentCollectionSnapshot;

/**
 * @author Franck WOLFF
 */
public class PersistentList<E> extends AbstractPersistentSimpleCollection<E, List<E>> implements List<E> {

    private static final long serialVersionUID = 1L;
	
	public PersistentList() {
	}

	public PersistentList(boolean initialized) {
		this(initialized ? new ArrayList<E>() : null, false);
	}

	public PersistentList(List<E> collection) {
		this(collection, true);
	}

	public PersistentList(List<E> collection, boolean clone) {
		if (collection != null)
			init((clone ? new ArrayList<E>(collection) : collection), null, false);
	}
	
	@Override
	public void doInitialize(List<E> list, boolean empty) {
		init(empty ? new ArrayList<E>() : list, null, false);
	}
	
	public boolean addAll(int index, Collection<? extends E> c) {
		checkInitializedWrite();
		if (getCollection().addAll(index, c)) {
			dirty();
			return true;
		}
		return false;
	}

	public E get(int index) {
		if (!checkInitializedRead())
			return null;
		return getCollection().get(index);
	}

	public E set(int index, E element) {
		checkInitializedWrite();
		E previousElement = getCollection().set(index, element);
		if (previousElement == null ? element != null : !previousElement.equals(element))
			dirty();
		return previousElement;
	}

	public void add(int index, E element) {
		checkInitializedWrite();
		getCollection().add(index, element);
		dirty();
	}

	public E remove(int index) {
		checkInitializedWrite();
		E previousElement = getCollection().remove(index);
		dirty();
		return previousElement;
	}

	public int indexOf(Object o) {
		if (!checkInitializedRead())
			return -1;
		return getCollection().indexOf(o);
	}

	public int lastIndexOf(Object o) {
		if (!checkInitializedRead())
			return -1;
		return getCollection().lastIndexOf(o);
	}

	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	public ListIterator<E> listIterator(int index) {
		return new ListIteratorProxy<E>(getCollection().listIterator(index));
	}

	public List<E> subList(int fromIndex, int toIndex) {
		if (!checkInitializedRead())
			return null;
		return new ListProxy<E>(getCollection().subList(fromIndex, toIndex));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(ObjectInput in, PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized())
			init(new ArrayList<E>((Collection<? extends E>)snapshot.getElementsAsCollection()), snapshot.getDetachedState(), snapshot.isDirty());
		else
			init(null, snapshot.getDetachedState(), false);
	}
	
    public PersistentList<E> clone(boolean uninitialize) {
    	PersistentList<E> list = new PersistentList<E>();
    	if (wasInitialized() && !uninitialize)
    		list.init(getCollection(), null, isDirty());
        return list; 
    }
}
