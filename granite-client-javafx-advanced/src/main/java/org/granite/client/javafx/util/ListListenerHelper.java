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
package org.granite.client.javafx.util;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;

/**
 * @author William DRAI
 */
public class ListListenerHelper<E> {

	private InvalidationListener[] invalidationListeners = null;
	private ListChangeListener<? super E>[] listChangeListeners = null;
	
	public void addListener(InvalidationListener listener) {
		invalidationListeners = ListenerUtil.add(InvalidationListener.class, invalidationListeners, listener);
	}
	
	public void removeListener(InvalidationListener listener) {
		invalidationListeners = ListenerUtil.remove(InvalidationListener.class, invalidationListeners, listener);
	}
	
	public void addListener(ListChangeListener<? super E> listener) {
		listChangeListeners = ListenerUtil.add(ListChangeListener.class, listChangeListeners, listener);
	}
	
	public void removeListener(ListChangeListener<? super E> listener) {
		listChangeListeners = ListenerUtil.remove(ListChangeListener.class, listChangeListeners, listener);
	}
	
    public void fireValueChangedEvent(ListChangeListener.Change<E> change) {
    	if (invalidationListeners != null) {
    		for (InvalidationListener invalidationListener : invalidationListeners)
    			invalidationListener.invalidated(change.getList());
    	}
        if (listChangeListeners != null) {
        	for (ListChangeListener<? super E> listChangeListener : listChangeListeners) {
        		change.reset();
        		listChangeListener.onChanged(change);
        	}
        }
    }

    public boolean hasListeners() {
        return invalidationListeners != null || listChangeListeners != null;
    }
    
    public int getInvalidationListenersSize() {
    	return invalidationListeners.length;
    }
    
    public int getListChangeListenersListenersSize() {
    	return listChangeListeners.length;
    }
    
    public void clear() {
    	invalidationListeners = null;
    	listChangeListeners = null;
    }
	
}
