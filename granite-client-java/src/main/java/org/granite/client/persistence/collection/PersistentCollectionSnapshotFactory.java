/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.granite.util.ContentType;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public abstract class PersistentCollectionSnapshotFactory {

	public static PersistentCollectionSnapshotFactory newInstance(Object io) {
		if (io.getClass().getSimpleName().startsWith("AMF3"))	// AMF3Serializer / AMF3Deserializer
			return newInstance(ContentType.AMF);
		return newInstance(ContentType.JMF_AMF);
	}
	
	
	public static PersistentCollectionSnapshotFactory newInstance(ContentType contentType) {
		try {
			switch (contentType) {
			case JMF_AMF:
				return TypeUtil.newInstance("org.granite.client.messaging.jmf.persistence.JMFPersistentCollectionSnapshotFactory", PersistentCollectionSnapshotFactory.class);
			case AMF:
				return TypeUtil.newInstance("org.granite.client.messaging.amf.persistence.AMFPersistentCollectionSnapshotFactory", PersistentCollectionSnapshotFactory.class);
			default:
				throw new UnsupportedOperationException("Unsupported content type: " + contentType);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Could not create collection snapshot factory: " + contentType, e);
		}
	}
	
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(String detachedState);
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean sorted, String detachedState);
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean initialized, String detachedState, boolean dirty, Collection<?> collection);
	public abstract PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean initialized, String detachedState, boolean dirty, Map<?, ?> collection);
}
