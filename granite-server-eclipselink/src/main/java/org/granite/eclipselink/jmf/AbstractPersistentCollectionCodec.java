/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.eclipselink.jmf;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolder;
import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.persistence.JMFPersistentCollectionSnapshot;
import org.granite.messaging.persistence.PersistentCollectionSnapshot;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractPersistentCollectionCodec<H extends IndirectContainer> implements ExtendedObjectCodec {

	protected final Class<H> eclipseLinkCollectionClass;
	protected final String clientCollectionClassName;

	public AbstractPersistentCollectionCodec(Class<H> eclipseLinkCollectionClass) {
		this.eclipseLinkCollectionClass = eclipseLinkCollectionClass;
		this.clientCollectionClassName = JMFConstants.CLIENT_PERSISTENCE_COLLECTION_PACKAGE + "." + eclipseLinkCollectionClass.getSimpleName().replace("Indirect", "Persistent");
	}

	public boolean canEncode(ExtendedObjectOutput out, Object v) {
		return v.getClass() == eclipseLinkCollectionClass;
	}

	public String getEncodedClassName(ExtendedObjectOutput out, Object v) {
		return clientCollectionClassName;
	}

	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException {
		JMFPersistentCollectionSnapshot snapshot = null;

		IndirectContainer collection = (IndirectContainer)v;
		if (!collection.isInstantiated())
			snapshot = new JMFPersistentCollectionSnapshot(collection instanceof SortedSet || collection instanceof SortedMap, null);
		else if (collection instanceof Map)
			snapshot = new JMFPersistentCollectionSnapshot(true, null, false, (Map<?, ?>)collection);
		else
			snapshot = new JMFPersistentCollectionSnapshot(true, null, false, (Collection<?>)collection);

		snapshot.writeExternal(out);
	}

	public boolean canDecode(ExtendedObjectInput in, String className) {
		return clientCollectionClassName.equals(className);
	}

	public String getDecodedClassName(ExtendedObjectInput in, String className) {
		return eclipseLinkCollectionClass.getName();
	}

	@SuppressWarnings("unchecked")
	public void decode(ExtendedObjectInput in, Object v) throws IOException, ClassNotFoundException, IllegalAccessException {
		IndirectContainer collection = (IndirectContainer)v;
		if (collection.isInstantiated()) {
			PersistentCollectionSnapshot snapshot = new JMFPersistentCollectionSnapshot(null);
			snapshot.readCoreData(in);
			
			if (collection instanceof Map)
				((Map<Object, Object>)collection).putAll(snapshot.getElementsAsMap());
			else
				((Collection<Object>)collection).addAll(snapshot.getElementsAsCollection());
		}
		else if (collection.getValueHolder() instanceof UninstantiatedValueHolder)
			collection.setValueHolder(null);
		
	}
	
	protected static class UninstantiatedValueHolder extends ValueHolder {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean isInstantiated() {
			return false;
		}
	}
}
