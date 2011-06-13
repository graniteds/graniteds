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

package org.granite.tide.hibernate;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.granite.hibernate.HibernateOptimisticLockException;
import org.granite.messaging.service.ServiceException;
import org.granite.tide.data.AbstractChangeSetApplier;
import org.granite.tide.data.ChangeRef;
import org.granite.util.ClassUtil;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;


/**
 * 	Utility class that applies a ChangeSet on a JPA EntityManager context
 * 	@author William DRAI
 *
 */
public class HibernateChangeSetApplier extends AbstractChangeSetApplier {
	
	private Session session;
	
	public HibernateChangeSetApplier(Session session) {
		this.session = session;
	}
	
	@Override
	protected Object find(Class<?> entityClass, Serializable id) {
		return session.load(entityClass, id);
	}
	
	@Override
	protected long getVersion(Object entity) {
		ClassMetadata cmd = session.getSessionFactory().getClassMetadata(session.getEntityName(entity));
		if (!cmd.isVersioned())
			throw new IllegalStateException("Cannot apply change set on non versioned entity " + entity.getClass());
		
		String versionPropertyName = cmd.getPropertyNames()[cmd.getVersionProperty()];
		try {
			Method m = entity.getClass().getMethod("get" + versionPropertyName.substring(0, 1).toUpperCase() + versionPropertyName.substring(1));
			Number version = (Number)m.invoke(entity);
			return version.longValue();
		}
		catch (Exception e) {
			throw new ServiceException("Could not get version for entity " + entity.getClass());
		}
	}
	
	@Override
	protected void throwOptimisticLockException(Object entity) {
		throw new HibernateOptimisticLockException("Could not apply changed for stale object", null, entity);					
	}
	
	@Override
	protected Object dereference(Object entity) {
		if (entity instanceof ChangeRef) {
			ChangeRef ref = (ChangeRef)entity;
			try {
				Class<?> entityClass = ClassUtil.forName(ref.getClassName());
				return session.load(entityClass, ref.getId());
			}
			catch (ClassNotFoundException cnfe) {
				throw new ServiceException("Could not find class " + ref.getClassName(), cnfe);
			}
		}
		
//		ClassMetadata cmd = session.getSessionFactory().getClassMetadata(entity.getClass());
//		if (cmd != null) {
//			Object id = cmd.getIdentifier(entity, EntityMode.POJO);
//			if (id == null)
//				return session.merge(entity);
//		}

		return entity;
	}
}
