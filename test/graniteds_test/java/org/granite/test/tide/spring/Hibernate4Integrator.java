package org.granite.test.tide.spring;

import org.granite.util.TypeUtil;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;


public class Hibernate4Integrator implements Integrator {

	public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        final EventListenerRegistry eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
        
        try {
	    	Object listener = TypeUtil.newInstance("org.granite.tide.hibernate4.HibernateDataPublishListener");    	
	        eventListenerRegistry.getEventListenerGroup(EventType.POST_INSERT).appendListener((PostInsertEventListener)listener);
	        eventListenerRegistry.getEventListenerGroup(EventType.POST_UPDATE).appendListener((PostUpdateEventListener)listener);
	        eventListenerRegistry.getEventListenerGroup(EventType.POST_DELETE).appendListener((PostDeleteEventListener)listener);
        }
        catch (Exception e) {   
        	throw new RuntimeException("Could not setup Hibernate 4 listeners", e);
        }
	}

    public void integrate(MetadataImplementor configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}
}
