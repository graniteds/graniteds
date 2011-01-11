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

package org.granite.seam;

import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.util.XMap;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Cameron INGRAM, Stuart Robertson
 */
public class SeamServiceFactory extends ServiceFactory {

    private static final long serialVersionUID = 1L;

    private static final Log log = Logging.getLog(SeamServiceFactory.class);

    @Override
    public void configure(XMap properties) throws ServiceException {
        super.configure(properties);
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
        String componentName = destination.getProperties().get("source");

        // Find the component we're calling
        Component component = Component.forName(componentName);

        if (component == null) {
            String msg = "Unable to create a Seam component named [" + componentName + "]";
            log.error(msg);
            ServiceException e = new ServiceException(msg);
            throw e;
        }

        //Create an instance of the component
        Object instance = Component.getInstance(componentName, true);

        return new SeamServiceInvoker(destination, this, instance);
    }

}
