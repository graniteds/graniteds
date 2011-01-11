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

package org.granite.tide.spring;

import org.granite.logging.Logger;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideTransactionManager;
import org.granite.tide.data.AbstractTidePersistenceManager;
import org.granite.tide.data.JPAPersistenceManager;
import org.granite.tide.data.NoPersistenceManager;
import org.granite.util.ClassUtil;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * 	Responsible for attaching a session with the persistence mangager
 * 	@author Cameron Ingram
 * 	@author William Draï
 *
 */
public class SpringPersistenceManager implements TidePersistenceManager {
    
	private static final Logger log = Logger.getLogger(SpringPersistenceManager.class);
	
	private TidePersistenceManager pm;
	
	
	public SpringPersistenceManager(PlatformTransactionManager transactionManager) {
		TideTransactionManager tm = new SpringTransactionManager(transactionManager);
		if (transactionManager instanceof HibernateTransactionManager) {
			try {
				Object sf = transactionManager.getClass().getMethod("getSessionFactory").invoke(transactionManager);
				Class<?> sfClass = ClassUtil.forName("org.hibernate.SessionFactory");
				pm = (TidePersistenceManager)ClassUtil.newInstance("org.granite.tide.hibernate.HibernatePersistenceManager", 
						new Class<?>[] { sfClass, TideTransactionManager.class }, new Object[] { sf, tm });
			}
			catch (Exception e) {
				log.error("Could not setup Hibernate persistence manager, lazy-loading disabled. Check that granite-hibernate.jar is present in the classpath.");
				pm = new NoPersistenceManager();
			}
		}
		else if (transactionManager instanceof JpaTransactionManager) {
			pm = new JPAPersistenceManager(((JpaTransactionManager)transactionManager).getEntityManagerFactory(), tm);
		}
		else {
			log.error("Unsupported Spring TransactionManager, lazy-loading disabled");
			pm = new NoPersistenceManager();
		}
	}


	public Object attachEntity(Object entity, String[] propertyNames) {
		if (pm instanceof AbstractTidePersistenceManager)
			return ((AbstractTidePersistenceManager)pm).attachEntity(this, entity, propertyNames);
		return pm.attachEntity(entity, propertyNames);
	}
}
