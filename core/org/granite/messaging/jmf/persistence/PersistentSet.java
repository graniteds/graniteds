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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Franck WOLFF
 */
public class PersistentSet<E> extends AbstractPersistentSimpleCollection<E, Set<E>> implements Set<E> {

	public PersistentSet() {
	}

	public PersistentSet(Set<E> collection) {		
		this(collection, true);
	}

	public PersistentSet(Set<E> collection, boolean clone) {		
		if (collection instanceof SortedSet)
			throw new IllegalArgumentException("Should not be a SortedSet: " + collection);
		
		if (collection != null)
			init(clone ? new HashSet<E>(collection) : collection, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized())
			init(new HashSet<E>((Collection<? extends E>)Arrays.asList(snapshot.getElements())), snapshot.isDirty());
		else
			init(null, false);
	}
}
