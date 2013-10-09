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
package org.granite.tide.spring;

import javax.persistence.EntityManager;

import org.granite.logging.Logger;
import org.granite.tide.data.JPAPersistenceAdapter;
import org.granite.tide.data.TidePersistenceAdapter;
import org.granite.tide.data.TidePersistenceAdapterFactory;
import org.granite.util.TypeUtil;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * 	Implementation of TidePersistenceAdapterFactory for Spring
 *  Detects the platform transaction manager and creates Hibernate or JPA persistence adapters accordingly
 *  
 * 	@author William Drai
 */
public class SpringPersistenceAdapterFactory implements TidePersistenceAdapterFactory {
    
	private static final Logger log = Logger.getLogger(SpringPersistenceAdapterFactory.class);
	
	private final PlatformTransactionManager transactionManager;
	
	public SpringPersistenceAdapterFactory(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	public TidePersistenceAdapter newPersistenceAdapter() {
		if (transactionManager instanceof HibernateTransactionManager) {
			try {
				// Use reflection to avoid runtime dependency on Hibernate
				Object sf = transactionManager.getClass().getMethod("getSessionFactory").invoke(transactionManager);
				Class<?> sfClass = TypeUtil.forName("org.hibernate.SessionFactory");
				return (TidePersistenceAdapter)TypeUtil.newInstance("org.granite.tide.hibernate.HibernatePersistenceAdapter",
						new Class<?>[] { sfClass }, new Object[] { sf });
			}
			catch (Exception e) {
				log.error("Could not setup Hibernate persistence adapter, incremental changes disabled. Check that granite-hibernate.jar is present in the classpath.");
				return null;
			}
		}
		else if (transactionManager instanceof JpaTransactionManager) {
			EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(((JpaTransactionManager)transactionManager).getEntityManagerFactory());
			return new JPAPersistenceAdapter(em);
		}
		
		log.error("Unsupported Spring TransactionManager, incremental change s disabled. You may ");
		return null;
	}
}
