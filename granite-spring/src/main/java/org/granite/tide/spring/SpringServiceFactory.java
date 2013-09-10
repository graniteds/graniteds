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

package org.granite.tide.spring;

import java.util.Map;

import javax.servlet.ServletContext;

import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.service.ExtendedServiceExceptionHandler;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.TideServiceInvoker;
import org.granite.tide.data.PersistenceExceptionConverter;
import org.granite.util.TypeUtil;
import org.granite.util.XMap;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import flex.messaging.messages.RemotingMessage;


/**
 * @author Sebastien Deleuze
 */
public class SpringServiceFactory extends ServiceFactory {
    
	private static final Logger log = Logger.getLogger(SpringServiceFactory.class);
    
    public static final String PERSISTENCE_MANAGER_BEAN_NAME = "persistence-manager-bean-name";
    public static final String ENTITY_MANAGER_FACTORY_BEAN_NAME = "entity-manager-factory-bean-name";
    
    private ApplicationContext springContext = null;
    
    public void setApplicationContext(ApplicationContext applicationContext) {
    	this.springContext = applicationContext;
    }


    @Override
    public void configure(XMap properties) throws ServiceException {
        String sServiceExceptionHandler = properties.get("service-exception-handler");
        if (sServiceExceptionHandler == null) {
            XMap props = new XMap(properties);
            props.put("service-exception-handler", ExtendedServiceExceptionHandler.class.getName());
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
    }


    @Override
    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
   	
    	String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        GraniteContext context = GraniteContext.getCurrentInstance();
        Map<String, Object> cache = context.getSessionMap(false);
        if (cache == null)
        	cache = context.getRequestMap();
        Destination destination = context.getServicesConfig().findDestinationById(messageType, destinationId);
        if (destination == null)
            throw new ServiceException("No matching destination: " + destinationId);
        
        String key = TideServiceInvoker.class.getName() + '.' + destinationId;
        
        return getServiceInvoker(cache, destination, key);
        
    }

    private ServiceInvoker<?> getServiceInvoker(Map<String, Object> cache, Destination destination, String key) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context.getSessionMap(false) == null)
        	return internalGetServiceInvoker(cache, destination, key);
        
        synchronized (context.getSessionLock()) {
        	return internalGetServiceInvoker(cache, destination, key);
        }
    }
    
    private ServiceInvoker<?> internalGetServiceInvoker(Map<String, Object> cache, Destination destination, String key) {
        ServiceInvoker<?> invoker = (ServiceInvoker<?>)cache.get(key);
        if (invoker == null) {
            SpringServiceContext tideContext = null;
            ServletContext sc = ((HttpGraniteContext)GraniteContext.getCurrentInstance()).getServletContext();
            ApplicationContext springContext = this.springContext != null ? this.springContext : WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
            Map<String, ?> beans = springContext.getBeansOfType(SpringServiceContext.class);
            if (beans.size() > 1)
                throw new RuntimeException("More than one SpringServiceContext bean found");
            else if (beans.size() == 1)
                tideContext = (SpringServiceContext)beans.values().iterator().next();
            else {
            	// Try to create Spring MVC context when Spring MVC available
            	String className = "org.granite.tide.spring.SpringMVCServiceContext";
            	try {
            		Class<SpringServiceContext> clazz = TypeUtil.forName(className, SpringServiceContext.class);
            		tideContext = clazz.getConstructor(ApplicationContext.class).newInstance(springContext);
            	}
            	catch (Exception e) {
            		tideContext = new SpringServiceContext(springContext);
            	}
            }
            
            String persistenceManagerBeanName = destination.getProperties().get(PERSISTENCE_MANAGER_BEAN_NAME);
        	tideContext.setPersistenceManagerBeanName(persistenceManagerBeanName);
        	
        	String entityManagerFactoryBeanName = destination.getProperties().get(ENTITY_MANAGER_FACTORY_BEAN_NAME);
        	tideContext.setEntityManagerFactoryBeanName(entityManagerFactoryBeanName);
            
            invoker = new TideServiceInvoker<SpringServiceFactory>(destination, this, tideContext);
            cache.put(key, invoker);
        }
        return invoker;
    }
}
