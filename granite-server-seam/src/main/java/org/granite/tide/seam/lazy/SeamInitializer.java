/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.seam.lazy;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.granite.tide.TidePersistenceManager;
import org.granite.tide.data.DataMergeContext;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.log.Log;

/**
 * Initializes a request for a passed in entity and a lazy property.
 
 * @author CIngram,VDanda
 */
@Name("org.granite.tide.seam.seamInitializer")
@Scope(ScopeType.CONVERSATION)
@Install(precedence=FRAMEWORK)
@BypassInterceptors
public class SeamInitializer implements Serializable {

    private static final long serialVersionUID = 1L;
	
    @Logger Log log;
    
    private transient TidePersistenceManager pm = null;
    
    private Set<Object> loadedEntities = new HashSet<Object>();
    
    
    /**
     * Initiliazes the property for the passed in entity. It is attached to an associated context
     * and then the property is accessed.
     * @return Returns result from initializing the property.   
     */
	@Transactional
    public Object lazyInitialize(Object entity, String[] propertyNames) {
		boolean removeAfterCall = false;
		
		restoreLoadedEntities();
		
		try {
		    if (entity instanceof String) {
                String expression = "${" + entity + "}";
                Expressions.ValueExpression<Object> valueExpr = Expressions.instance().createValueExpression(expression, Object.class);
                entity = valueExpr.getValue();
		    }
		    
			if (pm == null) {
				removeAfterCall = true;
			    pm = tryToDetermineInitiailzer();
				if (pm == null)
				    throw new RuntimeException("TideInitializer is null, Entities with Lazy relationships have to be retrieved in a conversation, or the EntityManager name must be entityManager");
			}	
			
			Object initializedEntity = pm.attachEntity(entity, propertyNames);

			saveLoadedEntities();
	        
			return initializedEntity;
		} finally {
			if (removeAfterCall)
				Component.forName("org.granite.tide.seam.seamInitializer").destroy(this);
		}
	}
	
	
	/**
	 * Try to determine what type of persistence the application is using. 
	 * If the EntityManager is stored under entityManager or if the Hibernate session is 
	 * stored under session. Then the context will be found and used. This is only called if a 
	 * ITidePersistenceManager is not found, probably because the query was not run in a conversation.
	 * @return The appropriate manager for the persistence context being used, if it can be determined
	 * otherwise a null is returned. 
	 */  
	protected TidePersistenceManager tryToDetermineInitiailzer() {
		EntityManager em = findEntityManager();
		if (em != null) 
			return TidePersistenceFactory.createTidePersistence(null, em);
		
		return null;
	}
	
	/**
	 * Try to find the entityManager if possible. Assume that the entityManager is stored under
	 * entityManager.
	 * @return The Current Entity manager
	 */
	protected EntityManager findEntityManager() {
		return (EntityManager) Component.getInstance("entityManager");
	}
	
	/**
	 * @return A instance of this component for the conversation.
	 */
    public static SeamInitializer instance() { 
    	return (SeamInitializer)Component.getInstance(SeamInitializer.class);
    }
	
    public void setTidePersistenceManager(TidePersistenceManager pm) {
    	this.pm = pm;
    }
    
    public TidePersistenceManager getTidePersistenceManager() {
        return this.pm;
    }
    
    public void restoreLoadedEntities() {
    	DataMergeContext.restoreLoadedEntities(loadedEntities);
    }
    
    public void saveLoadedEntities() {
    	for (Object entity : DataMergeContext.getLoadedEntities())
    		loadedEntities.add(entity);
    }
}

