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
package org.granite.hibernate4.jmf;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.persistence.JMFPersistentCollectionSnapshot;
import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.hibernate.collection.spi.PersistentCollection;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractPersistentCollectionCodec<H extends PersistentCollection> implements ExtendedObjectCodec {

	protected final Class<H> hibernateCollectionClass;
	protected final String clientCollectionClassName;

	public AbstractPersistentCollectionCodec(Class<H> hibernateCollectionClass) {
		this.hibernateCollectionClass = hibernateCollectionClass;
		this.clientCollectionClassName = JMFConstants.CLIENT_PERSISTENCE_COLLECTION_PACKAGE + "." + hibernateCollectionClass.getSimpleName();
	}

	public boolean canEncode(ExtendedObjectOutput out, Object v) {
		return v.getClass() == hibernateCollectionClass;
	}

	public String getEncodedClassName(ExtendedObjectOutput out, Object v) {
		return clientCollectionClassName;
	}

	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException {
		JMFPersistentCollectionSnapshot snapshot = null;

		PersistentCollection collection = (PersistentCollection)v;
		if (!collection.wasInitialized())
			snapshot = new JMFPersistentCollectionSnapshot(collection instanceof SortedSet || collection instanceof SortedMap, null);
		else if (collection instanceof Map)
			snapshot = new JMFPersistentCollectionSnapshot(true, null, collection.isDirty(), (Map<?, ?>)collection);
		else
			snapshot = new JMFPersistentCollectionSnapshot(true, null, collection.isDirty(), (Collection<?>)collection);

		snapshot.writeExternal(out);
	}

	public boolean canDecode(ExtendedObjectInput in, String className) {
		return clientCollectionClassName.equals(className);
	}

	public String getDecodedClassName(ExtendedObjectInput in, String className) {
		return hibernateCollectionClass.getName();
	}

	@SuppressWarnings("unchecked")
	public void decode(ExtendedObjectInput in, Object v) throws IOException, ClassNotFoundException, IllegalAccessException {
		PersistentCollection collection = (PersistentCollection)v;
		if (collection.wasInitialized()) {
			boolean sorted = (collection instanceof SortedSet || collection instanceof SortedMap);
			PersistentCollectionSnapshot snapshot = new JMFPersistentCollectionSnapshot(sorted, null);
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
