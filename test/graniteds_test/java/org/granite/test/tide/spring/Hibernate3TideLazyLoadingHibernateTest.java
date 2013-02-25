package org.granite.test.tide.spring;

import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-hibernate.xml" })
public class Hibernate3TideLazyLoadingHibernateTest extends AbstractTideLazyLoadingHibernateTest {
}
