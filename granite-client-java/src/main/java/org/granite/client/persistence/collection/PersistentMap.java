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

import java.io.ObjectInput;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.granite.messaging.persistence.PersistentCollectionSnapshot;

/**
 * @author Franck WOLFF
 */
public class PersistentMap<K, V> extends AbstractPersistentMapCollection<K, V, Map<K, V>> implements Map<K, V> {

    private static final long serialVersionUID = 1L;
	
	public PersistentMap() {
	}

	public PersistentMap(boolean initialized) {
		this(initialized ? new HashMap<K, V>() : null, false);
	}

	public PersistentMap(Map<K, V> collection) {		
		this(collection, true);
	}

	public PersistentMap(Map<K, V> collection, boolean clone) {	
		if (collection instanceof SortedMap)
			throw new IllegalArgumentException("Should not be a SortedMap: " + collection);
		
		if (collection != null)
			init(clone ? new HashMap<K, V>(collection) : collection, null, false);
	}
	
	@Override
	public void doInitialize(Map<K, V> map, boolean empty) {
		init(empty ? new HashMap<K, V>() : map, null, false);
	}
	
	@Override
	protected PersistentCollectionSnapshot createSnapshot(Object io, boolean forReading) {
		PersistentCollectionSnapshotFactory factory = PersistentCollectionSnapshotFactory.newInstance(io);
		if (forReading || !wasInitialized())
			return factory.newPersistentCollectionSnapshot(getDetachedState());
		return factory.newPersistentCollectionSnapshot(true, getDetachedState(), isDirty(), this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(ObjectInput in, PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized())
			init(new HashMap<K, V>((Map<K, V>)snapshot.getElementsAsMap()), snapshot.getDetachedState(), snapshot.isDirty());
		else
			init(null, snapshot.getDetachedState(), false);
	}
	
    public PersistentMap<K, V> clone(boolean uninitialize) {
    	PersistentMap<K, V> map = new PersistentMap<K, V>();
    	if (wasInitialized() && !uninitialize)
    		map.init(getCollection(), null, isDirty());
        return map;
    }
}
