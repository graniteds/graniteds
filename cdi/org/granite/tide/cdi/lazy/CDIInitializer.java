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

package org.granite.tide.cdi.lazy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.el.ELResolver;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.granite.logging.Logger;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.cdi.PersistenceConfiguration;
import org.granite.tide.data.DataMergeContext;
import org.granite.tide.data.JPAPersistenceManager;


@ConversationScoped
public class CDIInitializer implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Logger log = Logger.getLogger(CDIInitializer.class);
    
    private transient TidePersistenceManager pm = null;
    
    private Set<Object> loadedEntities = new HashSet<Object>();
    
    @Inject
    private BeanManager manager;
    
    @Inject @Any
    private Instance<TidePersistenceManager> persistenceManagers;
    
    @Inject @Any
    private Instance<EntityManager> entityManagers;
    
    @Inject
    private PersistenceConfiguration persistenceConfiguration;
    
    
    /**
     * Initiliazes the property for the passed in entity. It is attached to an associated context
     * and then the property is accessed.
     * @return Returns result from initializing the property.   
     */
    public Object lazyInitialize(Object entity, String[] propertyNames) {
	    if (entity instanceof String) {
            String expression = "${" + entity + "}";
            ELResolver elResolver = manager.getELResolver();
            entity = elResolver.getValue(null, expression, null);
	    }
	    
		if (pm == null) {
		    pm = getPersistenceManager();
			if (pm == null)
			    throw new RuntimeException("TideInitializer is null, Entities with Lazy relationships have to be retrieved in a conversation");
		}	
		
		return pm.attachEntity(entity, propertyNames);
	}
	
	
	/**
	 * Try to determine what type of persistence the application is using. 
	 * If the EntityManager is stored under entityManager or if the Hibernate session is 
	 * stored under session. Then the context will be found and used. This is only called if a 
	 * ITidePersistenceManager is not found, probably because the query was not run in a conversation.
	 * @return The appropriate manager for the persistence context being used, if it can be determined
	 * otherwise a null is returned. 
	 */  
	public TidePersistenceManager getPersistenceManager() {
		try {
			if (persistenceManagers != null && !persistenceManagers.isUnsatisfied()) {
				if (persistenceManagers.isAmbiguous()) {
					log.error("The application defines more than one TidePersistenceManager, please define only one to be used for Tide lazy loading");
					return null;
				}
				return persistenceManagers.get();
			}
			InitialContext ic = new InitialContext();
			if (persistenceConfiguration.getEntityManagerFactoryJndiName() != null) {
				EntityManagerFactory emf = (EntityManagerFactory)ic.lookup(persistenceConfiguration.getEntityManagerFactoryJndiName());
				return new JPAPersistenceManager(emf);
			}
			else if (persistenceConfiguration.getEntityManagerJndiName() != null) {
				EntityManager em = (EntityManager)ic.lookup(persistenceConfiguration.getEntityManagerJndiName());
				return new JPAPersistenceManager(em);
			}
			else if (entityManagers != null) {
				// Try with injected EntityManager defined as Resource
				Iterator<EntityManager> iem = entityManagers.iterator();
				EntityManager em = iem.hasNext() ? iem.next() : null;
				if (em == null || iem.hasNext()) {
					log.error("The application defines zero or more than one Persistence Unit, please define which one should be used for lazy loading in entity-manager-jndi-name");
					return null;
				}
				return new JPAPersistenceManager(em);
			}
		}
		catch (Exception e) {
			log.error("Could not get EntityManager", e);
		}
		return null;
	}
	
    
    public void restoreLoadedEntities() {
    	DataMergeContext.restoreLoadedEntities(loadedEntities);
    }
    
    public void saveLoadedEntities() {
    	for (Object entity : DataMergeContext.getLoadedEntities())
    		loadedEntities.add(entity);
    }
}

