package org.granite.test.tide.spring;

import junit.framework.Assert;

import org.hibernate.Session;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-jpa-hibernate.xml" })
public class Hibernate3TideLazyLoadingJPATest extends AbstractTideLazyLoadingJPATest {
	
	protected void checkSessionsClosed() {
		Session session = (Session)entityManager.getDelegate();
		Assert.assertEquals("Sessions closed", session.getSessionFactory().getStatistics().getSessionOpenCount(), 
				session.getSessionFactory().getStatistics().getSessionCloseCount());
	}
}
