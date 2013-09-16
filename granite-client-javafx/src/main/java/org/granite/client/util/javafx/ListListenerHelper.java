/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.util.javafx;

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
