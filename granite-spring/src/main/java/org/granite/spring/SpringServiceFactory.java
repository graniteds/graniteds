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

package org.granite.spring;

import javax.servlet.ServletContext;

import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.messaging.service.SimpleServiceFactory;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.util.XMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Igor SAZHNEV
 */
public class SpringServiceFactory extends SimpleServiceFactory {

    private ApplicationContext springContext = null;

    @Override
    public void configure(XMap properties)throws ServiceException {
        super.configure(properties);

        // getting spring context from container
        GraniteContext context = GraniteContext.getCurrentInstance();
        ServletContext sc = ((HttpGraniteContext)context).getServletContext();
        springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
    }

    @Override
    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
        String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        GraniteContext context = GraniteContext.getCurrentInstance();
        Destination destination = context.getServicesConfig().findDestinationById(messageType, destinationId);
        if (destination == null)
            throw new ServiceException("No matching destination: " + destinationId);
        
        // all we need is to get bean name
        // Scopes are configured in Spring config file, not in destination
        String beanName = destination.getProperties().get("source");
        if (beanName == null)
        	beanName = destination.getId();
        
        try {
            Object bean = springContext.getBean(beanName);
            return createSpringServiceInvoker(destination, this, bean);
        } 
        catch (NoSuchBeanDefinitionException nexc) {
            String msg = "Spring service named '" + beanName + "' does not exist.";
            ServiceException e = new ServiceException(msg, nexc);
            throw e;
        } 
        catch (BeansException bexc) {
            String msg = "Unable to create Spring service named '" + beanName + "'";
            ServiceException e = new ServiceException(msg, bexc);
            throw e;
        }
    }

    @Override
    public String toString() {
        return toString("\n  springContext: " + springContext);
    }
    
    protected ServiceInvoker<?> createSpringServiceInvoker(Destination destination, SpringServiceFactory factory, Object bean) {
        return new SpringServiceInvoker(destination, this, bean);
    }
}