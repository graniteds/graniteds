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
	public void doInitialize() {
		init(new HashMap<K, V>(), null, false);
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
