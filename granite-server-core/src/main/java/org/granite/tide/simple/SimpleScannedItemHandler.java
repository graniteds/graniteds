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
package org.granite.tide.simple;

import org.granite.logging.Logger;
import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.tide.util.Observer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Franck WOLFF
 */
public class SimpleScannedItemHandler implements ScannedItemHandler {

	private static final Logger log = Logger.getLogger(SimpleScannedItemHandler.class);
	private static final SimpleScannedItemHandler instance = new SimpleScannedItemHandler();

	private final Map<Class<?>, Class<?>> scannedClasses = new HashMap<Class<?>, Class<?>>();
    private final Map<String, Class<?>> scannedClassesById = new HashMap<String, Class<?>>();
	private final Map<String, Set<Method>> observers = new HashMap<String, Set<Method>>();

	public static SimpleScannedItemHandler instance() {
		return instance;
	}

	static SimpleScannedItemHandler instance(boolean reset) {
		instance.scannedClasses.clear();
		instance.observers.clear();
		return instance;
	}

	private SimpleScannedItemHandler() {
	}
	
	public boolean handleMarkerItem(ScannedItem item) {
		return false;
	}

	public void handleScannedItem(ScannedItem item) {
		if ("class".equals(item.getExtension()) && item.getName().indexOf('$') == -1) {
			try {
				Class<?> clazz = item.loadAsClass();
                if (clazz.isAnnotationPresent(RemoteDestination.class)) {
                    scannedClasses.put(clazz, clazz);
                    if (clazz.getAnnotation(RemoteDestination.class).id().length() > 0)
                        scannedClassesById.put(clazz.getAnnotation(RemoteDestination.class).id(), clazz);
                }

                for (Class<?> i : clazz.getInterfaces()) {
                    if (i.isAnnotationPresent(RemoteDestination.class)) {
                        scannedClasses.put(i, clazz);
                        if (i.getAnnotation(RemoteDestination.class).id().length() > 0)
                            scannedClassesById.put(i.getAnnotation(RemoteDestination.class).id(), clazz);
                    }
                }

                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(Observer.class)) {
                        Observer o = method.getAnnotation(Observer.class);
                        Set<Method> methods = observers.get(o.value());
                        if (methods == null) {
                            methods = new HashSet<Method>();
                            observers.put(o.value(), methods);
                        }
                        methods.add(method);
                    }
                }
			}
			catch (Throwable e) {
				log.debug(e, "Could not introspect scanned item: %s", item);
			}
		}
	}
	
	public Map<Class<?>, Class<?>> getScannedClasses() {
		return scannedClasses;
	}

    public Map<String, Class<?>> getScannedClassesById() {
        return scannedClassesById;
    }

    public Map<String, Set<Method>> getObservers() {
		return observers;
	}
}
