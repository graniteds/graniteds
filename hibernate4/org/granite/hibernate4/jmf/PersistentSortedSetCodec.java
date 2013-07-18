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

package org.granite.hibernate4.jmf;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.TreeSet;

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.persistence.JMFPersistentCollectionSnapshot;
import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.hibernate.collection.internal.PersistentSortedSet;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedSetCodec extends AbstractPersistentCollectionCodec<PersistentSortedSet> {

	public PersistentSortedSetCodec() {
		super(PersistentSortedSet.class);
	}

	public PersistentSortedSet newInstance(ExtendedObjectInput in, String className)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		
		PersistentCollectionSnapshot snapshot = new JMFPersistentCollectionSnapshot(true, null);
		snapshot.readInitializationData(in);
		
		if (!snapshot.isInitialized())
			return new PersistentSortedSet(null);
		
		Comparator<? super Object> comparator = snapshot.newComparator(in);
		return new PersistentSortedSet(null, new TreeSet<Object>(comparator));
	}
}
