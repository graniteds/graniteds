/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.tide.data;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;

import org.granite.messaging.service.ServiceException;
import org.granite.util.ClassUtil;
import org.granite.util.Entity;


/**
 * 	Utility class that applies a ChangeSet on a JPA EntityManager context
 * 	@author William DRAI
 *
 */
public class JPAChangeSetApplier extends AbstractChangeSetApplier {
	
	private EntityManager em;
	
	public JPAChangeSetApplier(EntityManager em) {
		this.em = em;
	}
	
	@Override
	protected Object find(Class<?> entityClass, Serializable id) {
		return em.find(entityClass, id);
	}
	
	@Override
	protected long getVersion(Object entity) {
		Entity e = new Entity(entity);
		if (!e.isVersioned())
			throw new IllegalStateException("Cannot apply change set on non versioned entity " + entity.getClass());
		
		Number version = (Number)e.getVersion();
		return version.longValue();
	}
	
	@Override
	protected void throwOptimisticLockException(Object entity) {
		throw new OptimisticLockException("Could not apply changed for stale object", null, entity);					
	}
	
	@Override
	protected Object dereference(Object entity) {
		if (entity instanceof ChangeRef) {
			ChangeRef ref = (ChangeRef)entity;
			try {
				Class<?> entityClass = ClassUtil.forName(ref.getClassName());
				return em.find(entityClass, ref.getId());
			}
			catch (ClassNotFoundException cnfe) {
				throw new ServiceException("Could not find class " + ref.getClassName(), cnfe);
			}
		}
//		else if (entity.getClass().isAnnotationPresent(javax.persistence.Entity.class)) {
//			Entity e = new Entity(entity);
//			Object id = e.getIdentifier();
//			if (id == null)
//				return em.merge(entity);
//		}
		return entity;
	}
}
