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
package org.granite.tide.ejb;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.granite.logging.Logger;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.tide.util.Observer;

/**
 * @author Franck WOLFF
 */
public class EjbScannedItemHandler implements ScannedItemHandler {

	private static final Logger log = Logger.getLogger(EjbScannedItemHandler.class);
	private static final EjbScannedItemHandler instance = new EjbScannedItemHandler();
	
	private final Map<Class<?>, Class<?>> scannedClasses = new HashMap<Class<?>, Class<?>>();
	private final Map<String, Set<Method>> observers = new HashMap<String, Set<Method>>();
	
	public static EjbScannedItemHandler instance() {
		return instance;
	}
	
	static EjbScannedItemHandler instance(boolean reset) {
		instance.scannedClasses.clear();
		instance.observers.clear();
		return instance;
	}
	
	private EjbScannedItemHandler() {
	}
	
	public boolean handleMarkerItem(ScannedItem item) {
		return false;
	}

	public void handleScannedItem(ScannedItem item) {
		if ("class".equals(item.getExtension()) && item.getName().indexOf('$') == -1) {
			try {
				Class<?> clazz = item.loadAsClass();
				if (clazz.isAnnotationPresent(Stateless.class) || clazz.isAnnotationPresent(Stateful.class)) {
					scannedClasses.put(clazz, clazz);	// Interface-less EJB 3.1
					if (clazz.isAnnotationPresent(Local.class)) {
						for (Class<?> i : clazz.getAnnotation(Local.class).value())
							scannedClasses.put(i, clazz);
					}
					if (clazz.isAnnotationPresent(Remote.class)) {
						for (Class<?> i : clazz.getAnnotation(Remote.class).value())
							scannedClasses.put(i, clazz);
					}
		            for (Class<?> i : clazz.getInterfaces()) {
		            	if (i.isAnnotationPresent(Local.class))
		            		scannedClasses.put(i, clazz);
		            	if (i.isAnnotationPresent(Remote.class))
		            		scannedClasses.put(i, clazz);
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
			}
			catch (Exception e) {
				log.debug(e, "Could not introspect scanned item: %s", item);
			}
		}
	}
	
	public Map<Class<?>, Class<?>> getScannedClasses() {
		return scannedClasses;
	}
	
	public Map<String, Set<Method>> getObservers() {
		return observers;
	}
}
