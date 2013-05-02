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
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.persistence.PersistentCollectionSnapshot;
import org.hibernate.collection.PersistentCollection;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractPersistentCollectionCodec<H extends PersistentCollection> implements ExtendedObjectCodec {

	protected static final String JMF_PERSISTENCE_PACKAGE = "org.granite.messaging.jmf.persistence";
	
	protected final Class<H> hibernateCollectionClass;
	protected final String clientCollectionClassName;

	public AbstractPersistentCollectionCodec(Class<H> hibernateCollectionClass) {
		this.hibernateCollectionClass = hibernateCollectionClass;
		this.clientCollectionClassName = JMF_PERSISTENCE_PACKAGE + "." + hibernateCollectionClass.getSimpleName();
	}

	public boolean canEncode(ExtendedObjectOutput out, Object v) {
		return v.getClass() == hibernateCollectionClass;
	}

	public boolean canDecode(ExtendedObjectInput in, String className) {
		return clientCollectionClassName.equals(className);
	}

	public String getEncodedClassName(ExtendedObjectOutput out, Object v) {
		return clientCollectionClassName;
	}

	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException {
		PersistentCollectionSnapshot snapshot = null;

		PersistentCollection collection = (PersistentCollection)v;
		if (!collection.wasInitialized())
			snapshot = new PersistentCollectionSnapshot(collection instanceof SortedSet || collection instanceof SortedMap);
		else if (collection instanceof Map)
			snapshot = new PersistentCollectionSnapshot(true, collection.isDirty(), (Map<?, ?>)collection);
		else
			snapshot = new PersistentCollectionSnapshot(true, collection.isDirty(), (Collection<?>)collection);

		snapshot.writeExternal(out);
	}

	@SuppressWarnings("unchecked")
	public void decode(ExtendedObjectInput in, Object v) throws IOException, ClassNotFoundException, IllegalAccessException {
		PersistentCollection collection = (PersistentCollection)v;
		if (collection.wasInitialized()) {
			boolean sorted = (collection instanceof SortedSet || collection instanceof SortedMap);
			PersistentCollectionSnapshot snapshot = new PersistentCollectionSnapshot(sorted);
			snapshot.readCoreData(in);
			
			if (collection instanceof Map)
				((Map<Object, Object>)collection).putAll(snapshot.getElementsAsMap());
			else
				((Collection<Object>)collection).addAll(snapshot.getElementsAsCollection());

			if (snapshot.isDirty())
				collection.dirty();
			else
				collection.clearDirty();
		}
	}
}
