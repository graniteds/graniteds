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

package org.granite.messaging.persistence;

import java.util.Collection;
import java.util.Map;

import org.granite.messaging.jmf.persistence.JMFPersistentCollectionSnapshotFactory;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public abstract class PersistentCollectionSnapshotFactory {

	public static PersistentCollectionSnapshotFactory newInstance() {
		return newInstance(ContentType.JMF_AMF);
	}

	
	public static PersistentCollectionSnapshotFactory newInstance(ContentType contentType) {
		switch (contentType) {
		case JMF_AMF:
			return new JMFPersistentCollectionSnapshotFactory();
		default:
			throw new UnsupportedOperationException("Unsupported content type: " + contentType);
		}
	}
	
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot();
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean sorted);
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean initialized, boolean dirty, Collection<?> collection);
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean initialized, boolean dirty, Map<?, ?> collection);
}
