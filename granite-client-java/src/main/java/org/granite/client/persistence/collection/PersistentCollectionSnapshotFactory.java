/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.persistence.collection;

import java.util.Collection;
import java.util.Map;

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.granite.util.ContentType;
import org.granite.util.TypeUtil;

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
