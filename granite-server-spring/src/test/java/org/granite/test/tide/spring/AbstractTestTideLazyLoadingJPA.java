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
package org.granite.test.tide.spring;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.granite.config.ConvertersConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.test.tide.data.Person;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-jpa.xml" })
public class AbstractTestTideLazyLoadingJPA extends AbstractTideTestCase {
	
	@PersistenceContext
	protected EntityManager entityManager;
	
	@Inject
	private PlatformTransactionManager txManager;
    
	@Test
    public void testLazyLoading() {
		TransactionDefinition def = new DefaultTransactionDefinition();
		
		TransactionStatus tx = txManager.getTransaction(def);
		Person person = new Person();
		entityManager.persist(person);
		txManager.commit(tx);
		
		tx = txManager.getTransaction(def);
		person = entityManager.find(Person.class, person.getId());
		txManager.commit(tx);
		
        Object result = initializeObject(person, new String[] { "contacts" });
        
        ClassGetter classGetter = ((ConvertersConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getClassGetter();
        Assert.assertTrue("Collection initialized", classGetter.isInitialized(result, "contacts", null));
        
        checkSessionsClosed();
    }
	
	protected void checkSessionsClosed() {		
	}
}
