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
