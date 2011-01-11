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

package org.granite.guice;

import javax.servlet.ServletContext;

import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.messaging.service.SimpleServiceFactory;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.util.XMap;

import com.google.inject.Injector;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Matt GIACOMI
 */
public class GuiceServiceFactory extends SimpleServiceFactory {

    private static final long serialVersionUID = 1L;

    private Injector injector = null;

    @Override
    public void configure(XMap properties)throws ServiceException {
        super.configure(properties);

        // getting guice injector from container
        GraniteContext context = GraniteContext.getCurrentInstance();
        ServletContext sc = ((HttpGraniteContext)context).getServletContext();
        injector = (Injector) sc.getAttribute(Injector.class.getName());
    }

    @Override
    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
        String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        GraniteContext context = GraniteContext.getCurrentInstance();
        Destination destination = context.getServicesConfig().findDestinationById(messageType, destinationId);
        if (destination == null)
            throw new ServiceException("No matching destination: " + destinationId);

        String className = destination.getProperties().get("source");

        try {
            Class<?> gclass = Class.forName(className, true, getClass().getClassLoader());
            Object gobject = injector.getInstance(gclass);
            return new GuiceServiceInvoker(destination, this, gobject);
        }
        catch(Exception e) {
            throw new ServiceException("Class not found in classloader: "+ className, e);
        }
    }

    @Override
    public String toString() {
        return toString("\n  Guice Injector: " + injector);
    }
}