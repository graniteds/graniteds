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

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.persistence.PersistentCollectionSnapshot;
import org.granite.messaging.jmf.persistence.PersistentCollectionSnapshot.CoreData;
import org.granite.messaging.jmf.persistence.PersistentCollectionSnapshot.InitializationData;
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

	public boolean canEncode(Class<?> cls) {
		return cls == hibernateCollectionClass;
	}

	public boolean canDecode(Class<?> cls) {
		return clientCollectionClassName.equals(cls.getName());
	}

	public String getEncodedClassName(Class<?> cls) {
		return clientCollectionClassName;
	}
	
	protected abstract H newCollection(boolean initialized);
	protected abstract Object[] getElements(H collection);
	protected abstract void setElements(H collection, Object[] elements);
	
	protected PersistentCollectionSnapshot newPersistentCollectionSnapshot(H collection) {
		if (!collection.wasInitialized())
			return new PersistentCollectionSnapshot();
		return new PersistentCollectionSnapshot(getElements(collection), collection.isDirty());
	}
	
	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		PersistentCollectionSnapshot snapshot = newPersistentCollectionSnapshot((H)v);
		snapshot.writeExternal(out);
	}

	public Object newInstance(ExtendedObjectInput in, Class<?> cls) throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		
		PersistentCollectionSnapshot snapshot = new PersistentCollectionSnapshot();
		InitializationData initializationData = snapshot.readInitializationData(in);
		
		return newCollection(initializationData.isInitialized());
	}

	public void decode(ExtendedObjectInput in, Object v) throws IOException, ClassNotFoundException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		H collection = (H)v;

		if (collection.wasInitialized()) {
			PersistentCollectionSnapshot snapshot = new PersistentCollectionSnapshot();
			CoreData coreData = snapshot.readCoreData(in);

			setElements(collection, coreData.getElements());
			
			if (coreData.isDirty())
				collection.dirty();
			else
				collection.clearDirty();
		}
	}
}
