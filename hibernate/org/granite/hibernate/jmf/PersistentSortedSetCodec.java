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

package org.granite.hibernate.jmf;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import org.hibernate.collection.PersistentSortedSet;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedSetCodec extends AbstractPersistentSortedCollectionCodec<PersistentSortedSet> {

	public PersistentSortedSetCodec() {
		super(PersistentSortedSet.class);
	}

	@Override
	protected String getComparatorClassName(PersistentSortedSet collection) {
		if (collection.wasInitialized())
			return (collection.comparator() != null ? collection.comparator().getClass().getName() : null);
		return null;
	}

	@Override
	protected PersistentSortedSet newCollection(boolean initialized, Comparator<Object> comparator) {
		return (initialized ? new PersistentSortedSet(null, new TreeSet<Object>(comparator)) : new PersistentSortedSet(null));
	}

	@Override
	protected Object[] getElements(PersistentSortedSet collection) {
		return collection.toArray();
	}

	@Override
	protected void setElements(PersistentSortedSet collection, Object[] elements) {
		collection.addAll(Arrays.asList(elements));
	}
}
