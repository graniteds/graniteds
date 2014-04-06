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

import java.lang.reflect.Method;

/**
 * @author William DRAI
 */
public class ListenerHelper<L> {

	private final Class<?> listenerInterface;
	private final Method listenerMethod;
	private L[] listeners = null;
	
	public ListenerHelper(Class<?> listenerInterface) {
		this.listenerInterface = listenerInterface;
		Method[] methods = listenerInterface.getMethods();
		if (!listenerInterface.isInterface())
			throw new RuntimeException("Listener class must be an interface");
		if (methods.length != 1)
			throw new RuntimeException("Cannot use ListenerHelper with listener interfaces having more than one method");
		listenerMethod = methods[0];
	}
	
	public void addListener(L listener) {
		listeners = ListenerUtil.add(listenerInterface, listeners, listener);
	}
	
	public void removeListener(L listener) {
		listeners = ListenerUtil.remove(listenerInterface, listeners, listener);
	}
	
    public void fireEvent(Object... args) {
    	if (listeners != null) {
    		for (L listener : listeners) {
    			try {
    				listenerMethod.invoke(listener, args);
    			}
    			catch (Exception e) {
    				throw new RuntimeException("Could not fire event", e);
    			}
    		}
    	}
    }
    
    public boolean hasListeners() {
        return listeners != null;
    }
    
    public void clear() {
    	listeners = null;
    }
	
}
