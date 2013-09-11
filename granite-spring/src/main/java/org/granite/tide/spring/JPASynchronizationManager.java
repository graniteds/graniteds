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

package org.granite.tide.spring;

import java.util.HashMap;
import java.util.Map;

import org.granite.tide.data.TideSynchronizationManager;
import org.granite.util.TypeUtil;
import org.springframework.orm.jpa.EntityManagerHolder;

/**
 * Responsible for registering a publishing synchronization on the JPA entity manager
 * Delegated to Hibernate-specific workarounds when necessary
 * @author william
 */
public class JPASynchronizationManager implements TideSynchronizationManager {
	
	private Map<String, TideSynchronizationManager> syncsMap = new HashMap<String, TideSynchronizationManager>();
	
	public JPASynchronizationManager() {
		try {
			syncsMap.put("org.hibernate.impl.SessionImpl", TypeUtil.newInstance("org.granite.tide.spring.Hibernate3SynchronizationManager", TideSynchronizationManager.class));
		}
		catch (Throwable t) {
			// Hibernate 3 not found
		}
		try {
			syncsMap.put("org.hibernate.internal.SessionImpl", TypeUtil.newInstance("org.granite.tide.spring.Hibernate4SynchronizationManager", TideSynchronizationManager.class));
		}
		catch (Throwable t) {
			// Hibernate 4 not found
		}
	}
	
	public boolean registerSynchronization(Object resource, boolean shouldRemoveContextAtEnd) {
		EntityManagerHolder entityManagerHolder = (EntityManagerHolder)resource;
		Object delegate = entityManagerHolder.getEntityManager().getDelegate();
		if (syncsMap.containsKey(delegate.getClass().getName()))
			return syncsMap.get(delegate.getClass().getName()).registerSynchronization(delegate, shouldRemoveContextAtEnd);
		
		return false;
	}
}
