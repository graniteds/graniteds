package org.granite.tide.hibernate4;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.DuplicationStrategy;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;


public class Hibernate4Integrator implements Integrator {

	public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        final EventListenerRegistry eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
        
        try {
	    	HibernateDataPublishListener listener = new HibernateDataPublishListener();    	
	        eventListenerRegistry.getEventListenerGroup(EventType.POST_INSERT).appendListener(listener);
	        eventListenerRegistry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(listener);
	        eventListenerRegistry.getEventListenerGroup(EventType.POST_DELETE).appendListener(listener);
	        
	        eventListenerRegistry.getEventListenerGroup(EventType.PERSIST).addDuplicationStrategy(new OverrideStrategy());
	        eventListenerRegistry.getEventListenerGroup(EventType.PERSIST).appendListener(new HibernatePersistListener());
	        eventListenerRegistry.getEventListenerGroup(EventType.PERSIST_ONFLUSH).addDuplicationStrategy(new OverrideStrategy());
	        eventListenerRegistry.getEventListenerGroup(EventType.PERSIST_ONFLUSH).appendListener(new HibernatePersistOnFlushListener());
	        eventListenerRegistry.getEventListenerGroup(EventType.SAVE_UPDATE).addDuplicationStrategy(new OverrideStrategy());
	        eventListenerRegistry.getEventListenerGroup(EventType.SAVE_UPDATE).appendListener(new HibernateSaveOrUpdateListener());
	        eventListenerRegistry.getEventListenerGroup(EventType.MERGE).addDuplicationStrategy(new OverrideStrategy());
	        eventListenerRegistry.getEventListenerGroup(EventType.MERGE).appendListener(new HibernateMergeListener());
	        eventListenerRegistry.getEventListenerGroup(EventType.DELETE).addDuplicationStrategy(new OverrideStrategy());
	        eventListenerRegistry.getEventListenerGroup(EventType.DELETE).appendListener(new HibernateDeleteListener());
	        eventListenerRegistry.getEventListenerGroup(EventType.LOCK).addDuplicationStrategy(new OverrideStrategy());
	        eventListenerRegistry.getEventListenerGroup(EventType.LOCK).appendListener(new HibernateLockListener());
	        eventListenerRegistry.getEventListenerGroup(EventType.AUTO_FLUSH).addDuplicationStrategy(new OverrideStrategy());
	        eventListenerRegistry.getEventListenerGroup(EventType.AUTO_FLUSH).appendListener(new HibernateAutoFlushListener());
	        eventListenerRegistry.getEventListenerGroup(EventType.FLUSH).addDuplicationStrategy(new OverrideStrategy());
	        eventListenerRegistry.getEventListenerGroup(EventType.FLUSH).appendListener(new HibernateFlushListener());
        }
        catch (Exception e) {   
        	throw new RuntimeException("Could not setup Hibernate 4 listeners", e);
        }
	}

    public void integrate(MetadataImplementor configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}
	
	public static class OverrideStrategy implements DuplicationStrategy {

		public boolean areMatch(Object listener, Object original) {
			for (Class<?> spiInterface : original.getClass().getInterfaces()) {
				if (spiInterface.getName().startsWith("org.hibernate.event.spi."))
					return spiInterface.isInstance(listener);
			}
			return false;
		}
		
		public Action getAction() {
			return Action.REPLACE_ORIGINAL;
		}
		
	}
}
