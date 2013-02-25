package org.granite.test.tide.spring;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-jpa-hibernate.xml" })
public class Hibernate3TideLazyLoadingJPATest extends AbstractTideLazyLoadingJPATest {
	
	@Override
	protected void checkOpenSessions() {
		SessionFactory sessionFactory = ((Session)entityManager.getDelegate()).getSessionFactory();
		Assert.assertEquals("Sessions closed", sessionFactory.getStatistics().getSessionOpenCount(), sessionFactory.getStatistics().getSessionCloseCount());
	}
}
