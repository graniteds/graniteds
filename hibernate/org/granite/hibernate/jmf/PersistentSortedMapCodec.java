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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.collection.PersistentSortedMap;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedMapCodec extends AbstractPersistentSortedCollectionCodec<PersistentSortedMap> {

	public PersistentSortedMapCodec() {
		super(PersistentSortedMap.class);
	}

	@Override
	protected String getComparatorClassName(PersistentSortedMap collection) {
		if (collection.wasInitialized())
			return (collection.comparator() != null ? collection.comparator().getClass().getName() : null);
		return null;
	}

	@Override
	protected PersistentSortedMap newCollection(boolean initialized, Comparator<Object> comparator) {
		return (initialized ? new PersistentSortedMap(null, new TreeMap<Object, Object>(comparator)) : new PersistentSortedMap(null));
	}

	@Override
	protected Object[] getElements(PersistentSortedMap collection) {
		Map.Entry<?, ?>[] entries = (Map.Entry<?, ?>[])collection.entrySet().toArray();
		Object[] elements = new Object[entries.length * 2];
		
		int j = 0;
		for (int i = 0; i < entries.length; i++) {
			Map.Entry<?, ?> entry = entries[i];
			elements[j++] = entry.getKey();
			elements[j++] = entry.getValue();
		}
		
		return elements;
	}

	@Override
	protected void setElements(PersistentSortedMap collection, Object[] elements) {
		if ((elements.length % 2) != 0)
			throw new IllegalArgumentException("elements length should be a multiple of 2: " + elements.length);
		
		final int length = elements.length / 2;
		for (int i = 0; i < length; i += 2)
			collection.put(elements[i], elements[i+1]);
	}
}
