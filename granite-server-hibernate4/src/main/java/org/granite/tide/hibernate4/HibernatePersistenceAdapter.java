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
package org.granite.tide.hibernate4;

import java.io.Serializable;

import org.granite.hibernate4.HibernateOptimisticLockException;
import org.granite.tide.data.TidePersistenceAdapter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

/**
 * Implementation of Tide persistence adapter with a Hibernate Session
 * @author William DRAI
 *
 */
public class HibernatePersistenceAdapter implements TidePersistenceAdapter {
	
	private Session session;
		
	public HibernatePersistenceAdapter(SessionFactory sessionFactory) {
		this.session = sessionFactory.getCurrentSession();
	}
	
	public HibernatePersistenceAdapter(Session session) {
		this.session = session;
	}

    /**
     * Find an entity in the persistence context
     * @param entityClass class of the looked up entity
     * @param id entity identifier
     * @return the entity with the persistence context.
     */
    public Object find(Class<?> entityClass, Serializable id) {
    	// Use Criteria instead of Session.load() to avoid getting proxies
    	Criteria criteria = session.createCriteria(entityClass);
    	criteria.add(Restrictions.idEq(id));
        return criteria.uniqueResult();
    }

    /**
     * Throw an optimistic locking error
     * @param entity entity instance loaded from the database
     */
    public void throwOptimisticLockException(Object entity) {
        throw new HibernateOptimisticLockException("Change detected on stale object", null, entity);
    }
}
