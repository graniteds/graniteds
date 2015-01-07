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
package org.granite.tide.data;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import org.granite.logging.Logger;
import org.granite.tide.TideTransactionManager;


/**
 * Responsible for attaching a entity with the entity mangager
 * @author cingram
 *
 */
public class JPAPersistenceManager extends AbstractTidePersistenceManager implements TideTransactionPersistenceManager {
	
	private static final Logger log = Logger.getLogger(JPAPersistenceManager.class);
	
	protected EntityManager entityManager;
	protected EntityManagerFactory entityManagerFactory;
	protected boolean shouldCloseEntityManager = false;

	
	public JPAPersistenceManager(TideTransactionManager tm) {
		super(tm);
	}

	public JPAPersistenceManager(EntityManager em) {
		this(em, null);
	}
	
	public JPAPersistenceManager(EntityManager em, TideTransactionManager tm) {
		super(tm != null ? tm : new JPATransactionManager());
		
    	if (em == null)
    		throw new RuntimeException("entity manager cannot be null");
    	
    	this.entityManager =  em;
	}
	
	public JPAPersistenceManager(EntityManagerFactory emf) {
		this(emf, null);
	}
	
	public JPAPersistenceManager(EntityManagerFactory emf, TideTransactionManager tm) {
		super(tm != null ? tm : new JPATransactionManager());
		
    	if (emf == null)
    		throw new RuntimeException("entity manager factory cannot be null");
    	
    	this.entityManagerFactory = emf;
	}
	
	public Object getCurrentTransaction() {
		initEntityManager();
	    EntityTransaction et = entityManager.getTransaction();   // Try to get a local resource transaction
	    et.begin();
	    return et;
	}
	
	protected void initEntityManager() {
		if (this.entityManager != null)
			return;
    	this.entityManager = entityManagerFactory.createEntityManager();
    	shouldCloseEntityManager = true;
	}
	
	@Override
	public void close() {
		if (shouldCloseEntityManager && this.entityManager != null)
			this.entityManager.close();
	}
	
	
    /**
     * Finds the entity with the JPA context.
     * @return the entity with the JPA context.
     */
	@Override
	public Object fetchEntity(Object entity, String[] fetch) {
		org.granite.util.Entity tideEntity = new org.granite.util.Entity(entity);
		Serializable id = (Serializable)tideEntity.getIdentifier();
		
        if (id == null)
            return null;
        
        initEntityManager();
        
        if (fetch == null || entityManager.getDelegate().getClass().getName().indexOf(".hibernate.") < 0)
        	return entityManager.find(entity.getClass(), id);
        
        for (String f : fetch) {
	        Query q = entityManager.createQuery("select e from " + entity.getClass().getName() + " e left join fetch e." + f + " where e = :entity");
	        q.setParameter("entity", entity);
	        List<?> results = q.getResultList();
	        if (!results.isEmpty())
	        	entity = results.get(0);
	        else
	        	log.warn("Could not find entity %s to initialize, id: %s", entity.getClass().getName(), id);  
        }
        return entity;
	}

}
