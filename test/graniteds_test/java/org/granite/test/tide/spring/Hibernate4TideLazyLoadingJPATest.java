package org.granite.test.tide.spring;

import junit.framework.Assert;

import org.hibernate.Session;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-jpa-hibernate4.xml" })
public class Hibernate4TideLazyLoadingJPATest extends AbstractTideLazyLoadingJPATest {
	
	protected void checkSessionsClosed() {
		Session session = (Session)entityManager.getDelegate();
		Assert.assertEquals("Sessions closed", 0L, session.getSessionFactory().getStatistics().getSessionOpenCount());
	}
}
