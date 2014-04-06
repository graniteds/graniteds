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
package org.granite.client.collection;

import org.granite.client.collection.CollectionChangeEvent.Kind;

/**
 * @author William DRAI
 */
public class CollectionChangeSupport {

	private final Object collection;
	private CollectionChangeListener[] listeners = null;
	
	public CollectionChangeSupport(Object collection) {
		this.collection = collection;
	}
	
	public void addCollectionChangeListener(CollectionChangeListener listener) {
		if (listeners == null)
			listeners = new CollectionChangeListener[] { listener };
		else {
			CollectionChangeListener[] newListeners = new CollectionChangeListener[listeners.length+1];
			System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
			newListeners[listeners.length] = listener;
			listeners = newListeners;
		}
	}
	
	public void fireCollectionChangeEvent(Kind kind, Object key, Object[] values) {
		if (listeners == null)
			return;
		CollectionChangeEvent event = new CollectionChangeEvent(collection, kind, key, values);
		for (CollectionChangeListener listener : listeners)
			listener.collectionChange(event);
	}
	
	public void removeCollectionChangeListener(CollectionChangeListener listener) {
		if (listeners == null)
			return;
		if (listeners.length == 1) {
			if (listeners[0] == listener)
				listeners = null;
		}
		else {
			int index = -1;
			for (int i = 0; i < listeners.length; i++) {
				if (listeners[i] == listener) {
					index = i;
					break;
				}
			}
			if (index >= 0) {
				CollectionChangeListener[] newListeners = new CollectionChangeListener[listeners.length-1];
				if (index > 0)
					System.arraycopy(listeners, 0, newListeners, 0, index);
				if (index < listeners.length-1)
					System.arraycopy(listeners, index+1, newListeners, index, listeners.length-index-1);
				listeners = newListeners;
			}
		}
	}
}
