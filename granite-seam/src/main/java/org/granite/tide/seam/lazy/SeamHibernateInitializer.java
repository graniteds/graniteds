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

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import javax.persistence.EntityManager;

import org.granite.tide.TidePersistenceManager;
import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Initializes a request for a passed in entity and a lazy property.
 
 * @author CIngram,VDanda
 */
@Name("org.granite.tide.seam.seamInitializer")
@Scope(ScopeType.CONVERSATION)
@Install(precedence=FRAMEWORK+1, classDependencies="org.hibernate.Session")
@BypassInterceptors
public class SeamHibernateInitializer extends SeamInitializer {

    private static final long serialVersionUID = 1L;
	
	
	/**
	 * Try to determine what type of persistence the application is using. 
	 * If the EntityManager is stored under entityManager or if the Hibernate session is 
	 * stored under session. Then the context will be found and used. This is only called if a 
	 * ITidePersistenceManager is not found, probably because the query was not run in a conversation.
	 * @return The appropriate manager for the persistence context being used, if it can be determined
	 * otherwise a null is returned. 
	 */  
    @Override
	protected TidePersistenceManager tryToDetermineInitiailzer() {
        EntityManager em = findEntityManager();
        if (em != null) 
            return TideHibernatePersistenceFactory.createTidePersistence(null, em);

		Session session = findHibernateSession();
		if (session != null)
			return TideHibernatePersistenceFactory.createTidePersistence(null, session);
		
		return null;
	}
	
    
	/**
	 * Try to find the hibernateSession if possible. Assume that the session is stored under
	 * session.
	 * @return The Current Session
	 */
	private Session findHibernateSession() {
		return (Session) Component.getInstance("session");
	}
}

