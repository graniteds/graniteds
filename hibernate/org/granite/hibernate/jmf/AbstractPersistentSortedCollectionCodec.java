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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.persistence.PersistentSortedCollectionSnapshot;
import org.granite.messaging.jmf.persistence.PersistentSortedCollectionSnapshot.SortedInitializationData;
import org.hibernate.collection.PersistentCollection;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractPersistentSortedCollectionCodec<H extends PersistentCollection> extends AbstractPersistentCollectionCodec<H> {
	
	public AbstractPersistentSortedCollectionCodec(Class<H> hibernateCollectionClass) {
		super(hibernateCollectionClass);
	}

	@Override
	protected H newCollection(boolean initialized) {
		throw new RuntimeException("Not implemented");
	}
	
	protected abstract H newCollection(boolean initialized, Comparator<Object> comparator);
	protected abstract String getComparatorClassName(H collection);

	@Override
	protected PersistentSortedCollectionSnapshot newPersistentCollectionSnapshot(H collection) {
		if (!collection.wasInitialized())
			return new PersistentSortedCollectionSnapshot();
		return new PersistentSortedCollectionSnapshot(getElements(collection), collection.isDirty(), getComparatorClassName(collection));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object newInstance(ExtendedObjectInput in, Class<?> cls) throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		
		PersistentSortedCollectionSnapshot snapshot = new PersistentSortedCollectionSnapshot();
		SortedInitializationData initializationData = snapshot.readInitializationData(in);
		
		Comparator<Object> comparator = null;
		if (initializationData.getComparatorClassName() != null) {
			String comparatorClassName = initializationData.getComparatorClassName();
			if (comparatorClassName != null) {
				ClassLoader classLoader = in.getClassLoader();
				comparator = (Comparator<Object>)classLoader.loadClass(comparatorClassName).getDeclaredConstructor().newInstance();
			}
		}
		
		return newCollection(initializationData.isInitialized(), comparator);
	}
}
