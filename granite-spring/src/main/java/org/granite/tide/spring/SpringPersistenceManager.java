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

package org.granite.tide.spring;

import org.granite.logging.Logger;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideTransactionManager;
import org.granite.tide.data.AbstractTidePersistenceManager;
import org.granite.tide.data.JDOPersistenceManager;
import org.granite.tide.data.NoPersistenceManager;
import org.granite.util.TypeUtil;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.jdo.JdoTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * 	Responsible for attaching a session with the persistence mangager
 * 	@author Cameron Ingram
 * 	@author William Dra�
 *
 */
public class SpringPersistenceManager implements TidePersistenceManager {
    
	private static final Logger log = Logger.getLogger(SpringPersistenceManager.class);
	
	private TidePersistenceManager pm;
	
	
	public SpringPersistenceManager(PlatformTransactionManager transactionManager) {
		TideTransactionManager tm = new SpringTransactionManager(transactionManager);
		if (transactionManager instanceof JpaTransactionManager) {
			pm = new SpringJPAPersistenceManager((JpaTransactionManager)transactionManager, tm);
			return;
		}
		
		if (transactionManager instanceof JdoTransactionManager) {
			pm = new JDOPersistenceManager(((JdoTransactionManager)transactionManager).getPersistenceManagerFactory(), tm);
			return;
		}
		
		// Check for Hibernate 3
		if (transactionManager instanceof HibernateTransactionManager) {
			try {
				Object sf = transactionManager.getClass().getMethod("getSessionFactory").invoke(transactionManager);
				Class<?> sfClass = TypeUtil.forName("org.hibernate.SessionFactory");
				pm = (TidePersistenceManager)TypeUtil.newInstance("org.granite.tide.hibernate.HibernatePersistenceManager", 
						new Class<?>[] { sfClass, TideTransactionManager.class }, new Object[] { sf, tm });
			}
			catch (Exception e) {
				log.error("Could not setup Hibernate 3 persistence manager, lazy-loading disabled. Check that granite-hibernate.jar is present in the classpath.");
				pm = new NoPersistenceManager();
			}
			return;
		}
		
		// Check for Hibernate 4
		try {
			Class<?> hibernate4TM = TypeUtil.forName("org.springframework.orm.hibernate4.HibernateTransactionManager");
			if (hibernate4TM.isInstance(transactionManager)) {
				try {
					Object sf = transactionManager.getClass().getMethod("getSessionFactory").invoke(transactionManager);
					Class<?> sfClass = TypeUtil.forName("org.hibernate.SessionFactory");
					pm = (TidePersistenceManager)TypeUtil.newInstance("org.granite.tide.hibernate4.HibernatePersistenceManager", 
							new Class<?>[] { sfClass, TideTransactionManager.class }, new Object[] { sf, tm });
				}
				catch (Exception e) {
					log.error("Could not setup Hibernate 4 persistence manager, lazy-loading disabled. Check that granite-hibernate4.jar is present in the classpath.");
					pm = new NoPersistenceManager();
				}
				return;
			}
		}
		catch (ClassNotFoundException e) {
			// Hibernate 4 not installed
		}
		
		log.error("Unsupported Spring TransactionManager, lazy-loading disabled");
		pm = new NoPersistenceManager();
	}


	public Object attachEntity(Object entity, String[] propertyNames) {
		if (pm instanceof AbstractTidePersistenceManager)
			return ((AbstractTidePersistenceManager)pm).attachEntity(this, entity, propertyNames);
		return pm.attachEntity(entity, propertyNames);
	}

}
