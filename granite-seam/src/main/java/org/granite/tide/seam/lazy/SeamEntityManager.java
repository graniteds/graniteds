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

package org.granite.tide.seam.lazy;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.Entity;


/**
 * Manager responsible for looking up the EntityManager from Seam for the
 * passed in SessionManagerName
 * @author CIngram
 */
public class SeamEntityManager extends PersistenceContextManager {

	private String emName = null;

	public SeamEntityManager(String emName) {
	    this.emName = emName;
	}
	
	
	/**
	 * Attach the passed in entity with the EntityManager retrieved
	 * from Seam.
	 * @param entity
	 * @return The attached entity
	 */
	@Override
    public Object fetchEntity(Object entity, String[] fetch) {
        EntityManager em = (EntityManager) Component.getInstance(emName);
        if (em == null)
		    throw new RuntimeException("Could not find entity, EntityManager [" + emName + "] not found");
        
        Serializable id = (Serializable)Entity.forClass(entity.getClass()).getIdentifier(entity);
        if (id == null)
            return null;
        
        if (fetch == null || em.getDelegate().getClass().getName().indexOf(".hibernate.") < 0)
        	return em.find(entity.getClass(), id);
        
        for (String f : fetch) {
	        Query q = em.createQuery("select e from " + entity.getClass().getName() + " e left join fetch e." + f + " where e = :entity");
	        q.setParameter("entity", entity);
	        entity = q.getSingleResult();
        }
        return entity;
    }
}
