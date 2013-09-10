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

package org.granite.tide.seam.lazy;

import javax.persistence.EntityManager;

import org.granite.tide.TidePersistenceManager;
import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.Component.BijectedAttribute;
import org.jboss.seam.annotations.In;
import org.jboss.seam.framework.PersistenceController;


/**
 * Factory for creating the correct ITidePersistenceManager based on the 
 * persistence strategy passed in. Supported types are 
 * EntityManager,Session, EntityQuery, EntityHome, HibernateEntityHome and a
 * injected(@In) EntityManager or HibernateSession
 * @author CIngram
 */
public class TideHibernatePersistenceFactory {

	
	/**
	 * Create the ITidePersistenceManager. Supported types are 
     * EntityManager,Session, EntityQuery, EntityHome, HibernateEntityHome and a
     * injected(@In) EntityManager or HibernateSession
	 * 
	 * @param component
	 * @param persistenceType
	 * @return a ITidePersistenceManager.
	 */
	public static TidePersistenceManager createTidePersistence(Component component, Object persistenceType) {
		TidePersistenceManager pm = TidePersistenceFactory.createTidePersistence(component, persistenceType);
		if (pm != null) {
		    return pm;
		} else if (persistenceType instanceof Session) {
			return createTidePersistence(component, (Session)persistenceType);
		}
		
		return null;
	}
	
	
	/**
	 * Create a ITideInterceptor for a EntityManager.
	 * 
	 * @param component
	 * @param persistenceType
	 * @return a ITidePersistenceManager.
	 */
	public static TidePersistenceManager createTidePersistence(Component component, EntityManager persistenceType) {
	    return TidePersistenceFactory.createTidePersistence(component, persistenceType);
	}
	
	/**
	 * Create  ITidePersistenceManager for a HibernateSession
	 * 
	 * @param component
	 * @param persistenceType
	 * @return a ITidePersistenceManager.
	 */
	public static TidePersistenceManager createTidePersistence(Component component, Session persistenceType) {
	    return new HibernateContextManager(persistenceType);
	} 
    
    /**
     * Create  ITidePersistenceManager for a PersistenceController
	 * 
	 * @param component
     * @param controller
     * @return a ITidePersistenceManager.
     */
    public static TidePersistenceManager createTidePersistence(Component component, PersistenceController<?> controller) {
        TidePersistenceManager pm = TidePersistenceFactory.createTidePersistence(component, controller);
        if (pm != null)
            return pm;
        String controllerName = component.getName();
        if (controller.getPersistenceContext() instanceof Session)
            return new HibernatePersistenceControllerManager(controllerName);
        return null;
    } 
	
	/**
	 * Create a ITidePersistenceManager for a injected attribute(@In). 
	 * Supported Types are EntityManager or Session.
	 * 
	 * @param component
	 * @param att
	 * @return a ITidePersistenceManager.
	 */
	public static TidePersistenceManager createTidePersistence(Component component, BijectedAttribute<In> att) {
		TidePersistenceManager pm = TidePersistenceFactory.createTidePersistence(component, att);
		if (pm != null)
		    return pm;
		
		if (att.getType() == Session.class) {
			return new SeamHibernateManager(att.getName());
	    } 
		return null;
	}
}
