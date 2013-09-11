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
/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.tide.cdi;

import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.granite.cdi.CDIUtils;
import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.TideServiceInvoker;
import org.granite.tide.data.PersistenceExceptionConverter;
import org.granite.util.XMap;

import flex.messaging.messages.RemotingMessage;


/**
 * @author William DRAI
 */
public class CDIServiceFactory extends ServiceFactory {

    private static final Logger log = Logger.getLogger(CDIServiceFactory.class);
    
    public static final String ENTITY_MANAGER_FACTORY_JNDI_NAME = "entity-manager-factory-jndi-name";
    public static final String ENTITY_MANAGER_JNDI_NAME = "entity-manager-jndi-name";

    private BeanManager manager;
    
    public BeanManager getManager() {
    	return manager;
    }
    

    @Override
    public void configure(XMap properties) throws ServiceException {
        String sServiceExceptionHandler = properties.get("service-exception-handler");
        if (sServiceExceptionHandler == null) {
            XMap props = new XMap(properties);
            props.put("service-exception-handler", "org.granite.messaging.service.ExtendedServiceExceptionHandler");
            super.configure(props);
        }
        else
            super.configure(properties);
        
        GraniteContext graniteContext = GraniteContext.getCurrentInstance();
        try {
        	graniteContext.getGraniteConfig().registerExceptionConverter(PersistenceExceptionConverter.class);
        }
        catch (Throwable t) {
	    	log.info(t, "JPA exception converter not registered (JPA not found on classpath)");
        }

        try {
	        manager = CDIUtils.lookupBeanManager(((HttpGraniteContext)graniteContext).getServletContext());
        }
        catch (Exception e) {
	        log.warn("Unable to find the CDI Manager in JNDI, lookup in ServletContext");
	        
	        manager = (BeanManager)((HttpGraniteContext)graniteContext).getServletContext().getAttribute("javax.enterprise.inject.spi.BeanManager");
	        if (manager == null) { 
		        ServiceException se = new ServiceException(e.getMessage());
		        throw se;
	        }
        }
        
        // Find the JCDIServiceContext component
        Set<Bean<?>> cc = manager.getBeans(CDIServiceContext.class);
        if (cc.size() != 1) {
            String msg = cc.isEmpty() 
            	? "Unable to find the CDIServiceContext bean"
            	: "More than one CDIServiceContext bean found";
            log.error(msg);
            ServiceException e = new ServiceException(msg);
            throw e;
        }
    }


    @Override
    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
        String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        Destination destination = context.getServicesConfig().findDestinationById(messageType, destinationId);
        if (destination == null)
            throw new ServiceException("No matching destination: " + destinationId);
        
        if (!destination.getProperties().containsKey(TideServiceInvoker.VALIDATOR_CLASS_NAME))
        	destination.getProperties().put(TideServiceInvoker.VALIDATOR_CLASS_NAME, "org.granite.tide.validation.BeanValidation");
        
        @SuppressWarnings("unchecked")
        Bean<PersistenceConfiguration> pcBean = (Bean<PersistenceConfiguration>)manager.getBeans(PersistenceConfiguration.class).iterator().next();
        PersistenceConfiguration persistenceConfiguration = (PersistenceConfiguration)manager.getReference(pcBean, PersistenceConfiguration.class, manager.createCreationalContext(pcBean));
        if (destination.getProperties().containsKey(ENTITY_MANAGER_FACTORY_JNDI_NAME))
        	persistenceConfiguration.setEntityManagerFactoryJndiName(destination.getProperties().get(ENTITY_MANAGER_FACTORY_JNDI_NAME));
        else if (destination.getProperties().containsKey(ENTITY_MANAGER_JNDI_NAME))
        	persistenceConfiguration.setEntityManagerJndiName(destination.getProperties().get(ENTITY_MANAGER_JNDI_NAME));
        
        // Create an instance of the component
        CDIServiceInvoker invoker = new CDIServiceInvoker(destination, this);
        return invoker;
    }
}
