package org.granite.test.tide.spring;

import org.granite.tide.hibernate4.HibernateDataPublishListener;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class Hibernate4Integrator implements Integrator {

    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

        final EventListenerRegistry eventListenerRegistry = serviceRegistry.getService( EventListenerRegistry.class );
        eventListenerRegistry.getEventListenerGroup(EventType.POST_INSERT).appendListener(new HibernateDataPublishListener());
        eventListenerRegistry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(new HibernateDataPublishListener());
        eventListenerRegistry.getEventListenerGroup(EventType.POST_DELETE).appendListener(new HibernateDataPublishListener());
    }
    
    public void integrate(MetadataImplementor configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}
}
