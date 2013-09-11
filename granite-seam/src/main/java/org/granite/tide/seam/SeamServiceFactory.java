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

package org.granite.tide.seam;

import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.data.PersistenceExceptionConverter;
import org.granite.util.XMap;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import flex.messaging.messages.RemotingMessage;


/**
 * @author William DRAI
 */
public class SeamServiceFactory extends ServiceFactory {

    private static final Log log = Logging.getLog(SeamServiceFactory.class);

    private Component contextComponent = null;


    @Override
    public void configure(XMap properties) throws ServiceException {
        String sServiceExceptionHandler = properties.get("service-exception-handler");
        if (sServiceExceptionHandler == null) {
            XMap props = new XMap(properties);
            props.put("service-exception-handler", "org.granite.tide.seam21.Seam21ServiceExceptionHandler");
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

        // Find the SeamServiceInvoker component
        contextComponent = Component.forName("org.granite.tide.seam.serviceContext");
        if (contextComponent == null) {
            String msg = "Unable to find the SeamServiceContext component";
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
        
        // Create an instance of the component
        SeamServiceInvoker invoker = new SeamServiceInvoker(destination, this);
        return invoker;
    }

}
