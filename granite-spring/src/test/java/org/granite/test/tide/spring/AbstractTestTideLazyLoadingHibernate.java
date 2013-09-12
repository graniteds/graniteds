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
package org.granite.test.tide.spring;

import javax.inject.Inject;

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.test.tide.data.Person;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


public class AbstractTestTideLazyLoadingHibernate extends AbstractTideTestCase {
	
	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private PlatformTransactionManager txManager;
    
	@Test
    public void testLazyLoading() {
		TransactionDefinition def = new DefaultTransactionDefinition();
		
		TransactionStatus tx = txManager.getTransaction(def);
		Person person = new Person();
		person.initUid();
		sessionFactory.getCurrentSession().persist(person);
		txManager.commit(tx);
		
		tx = txManager.getTransaction(def);
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
		criteria.add(Restrictions.idEq(person.getId()));
		person = (Person)criteria.uniqueResult();
		txManager.commit(tx);
		
        Person result = (Person)initializeObject(person, new String[] { "contacts" });
        
        ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        Assert.assertTrue("Person initialized", classGetter.isInitialized(null, null, result));
        Assert.assertTrue("Collection initialized", classGetter.isInitialized(result, "contacts", result.getContacts()));
        
		Assert.assertEquals("Sessions closed", sessionFactory.getStatistics().getSessionOpenCount(), 
				sessionFactory.getStatistics().getSessionCloseCount());
    }
}
