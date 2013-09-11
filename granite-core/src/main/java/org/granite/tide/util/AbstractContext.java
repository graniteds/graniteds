/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.tide.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.logging.Logger;
import org.granite.tide.invocation.ContextEvent;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractContext extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(AbstractContext.class);
	private static final ThreadLocal<AbstractContext> contexts = new ThreadLocal<AbstractContext>();
	
	private final Map<String, Set<Method>> observers;
	private final List<ContextEvent> remoteEvents;
	
	protected AbstractContext(Map<String, Set<Method>> observers) {
		synchronized (contexts) {
			if (contexts.get() != null)
				throw new IllegalStateException("Context already created");
			
			this.observers = observers;
			this.remoteEvents = new ArrayList<ContextEvent>();
			
			contexts.set(this);
		}
	}
	
	public List<ContextEvent> getRemoteEvents() {
		return remoteEvents;
	}
	
	protected abstract Set<String> getRemoteObservers();
	protected abstract Object callMethod(Method method, Object... args) throws Exception;
	
	public static AbstractContext instance() {
		return contexts.get();
	}
	
	public static void raiseEvent(String name, Object... args) {
		AbstractContext instance = instance();
		Map<String, Set<Method>> observers = instance.observers;
		if (observers.containsKey(name)) {
			for (Method method : observers.get(name)) {
				try {
					instance.callMethod(method, args);
				}
				catch (Exception e) {
					log.error(e, "Could not call method: %s", method);
				}
			}
		}
		Set<String> remoteObservers = instance.getRemoteObservers();
		if (remoteObservers.contains(name))
			instance.remoteEvents.add(new ContextEvent(name, args));
	}
	
	public static void remove() {
		AbstractContext context = contexts.get();
		if (context != null) {
			try {
				context.clear();
				context.remoteEvents.clear();
			}
			finally {
				contexts.remove();
			}
		}
	}
}
