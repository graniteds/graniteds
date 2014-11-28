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


/**
 * Implementation of Tide persistence adapter with a JPA EntityManager
 * @author William DRAI
 *
 */
public class JPAPersistenceAdapter implements TidePersistenceAdapter {
	
	protected EntityManager entityManager;

	
	public JPAPersistenceAdapter(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

    /**
     * Find an entity in the persistence context
     * @param entityClass class of the looked up entity
     * @param id entity identifier
     * @return the entity with the persistence context.
     */
    public Object find(Class<?> entityClass, Serializable id) {
        return entityManager.find(entityClass, id);
    }

    /**
     * Throw an optimistic locking error
     * @param entity entity instance loaded from the database
     */
    public void throwOptimisticLockException(Object entity) {
        throw new OptimisticLockException("Could not apply change for stale object", null, entity);
    }

}
