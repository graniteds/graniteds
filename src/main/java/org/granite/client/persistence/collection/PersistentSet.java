/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.client.persistence.collection;

import java.io.ObjectInput;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.granite.messaging.persistence.PersistentCollectionSnapshot;

/**
 * @author Franck WOLFF
 */
public class PersistentSet<E> extends AbstractPersistentSimpleCollection<E, Set<E>> implements Set<E> {

	public PersistentSet() {
	}

	public PersistentSet(boolean initialized) {
		this(initialized ? new HashSet<E>() : null, false);
	}

	public PersistentSet(Set<E> collection) {		
		this(collection, true);
	}

	public PersistentSet(Set<E> collection, boolean clone) {		
		if (collection instanceof SortedSet)
			throw new IllegalArgumentException("Should not be a SortedSet: " + collection);
		
		if (collection != null)
			init(clone ? new HashSet<E>(collection) : collection, null, false);
	}
	
	@Override
	public void doInitialize() {
		init(new HashSet<E>(), null, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(ObjectInput in, PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized())
			init(new HashSet<E>((Collection<? extends E>)snapshot.getElementsAsCollection()), snapshot.getDetachedState(), snapshot.isDirty());
		else
			init(null, snapshot.getDetachedState(), false);
	}
	
    public PersistentSet<E> clone(boolean uninitialize) {
    	PersistentSet<E> set = new PersistentSet<E>();
    	if (wasInitialized() && !uninitialize)
    		set.init(getCollection(), null, isDirty());
        return set; 
    }
}
