/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.util.Collection;
import java.util.Map;

import org.granite.client.messaging.amf.persistence.AMFPersistentCollectionSnapshotFactory;
import org.granite.client.messaging.jmf.persistence.JMFPersistentCollectionSnapshotFactory;
import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public abstract class PersistentCollectionSnapshotFactory {

	public static PersistentCollectionSnapshotFactory newInstance(Object io) {
		if (io instanceof ExtendedObjectInput || io instanceof ExtendedObjectOutput)
			return newInstance(ContentType.JMF_AMF);
		return newInstance(ContentType.AMF);
	}

	
	public static PersistentCollectionSnapshotFactory newInstance(ContentType contentType) {
		switch (contentType) {
		case JMF_AMF:
			return new JMFPersistentCollectionSnapshotFactory();
		case AMF:
			return new AMFPersistentCollectionSnapshotFactory();
		default:
			throw new UnsupportedOperationException("Unsupported content type: " + contentType);
		}
	}
	
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(String detachedState);
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean sorted, String detachedState);
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean initialized, String detachedState, boolean dirty, Collection<?> collection);
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean initialized, String detachedState, boolean dirty, Map<?, ?> collection);
}
