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

package org.granite.messaging.service;

import flex.messaging.messages.RemotingMessage;

import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.context.GraniteManager;
import org.granite.logging.Logger;
import org.granite.util.ClassUtil;
import org.granite.util.XMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Franck WOLFF
 */
public class SimpleServiceFactory implements ServiceFactory {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(SimpleServiceFactory.class);

    private ServiceExceptionHandler serviceExceptionHandler;

    private Set<String> invalidKeys = new HashSet<String>();


    public void configure(XMap properties) throws ServiceException {

        log.debug(">> Configuring factory with: %s", properties);

        // service exception handler
        String sServiceExceptionHandler = properties.get("service-exception-handler");
        String enableLogging = properties.get("enable-exception-logging");
        if (sServiceExceptionHandler != null) {
            try {
                if (Boolean.TRUE.toString().equals(enableLogging) || Boolean.FALSE.toString().equals(enableLogging))
                    this.serviceExceptionHandler = (ServiceExceptionHandler) ClassUtil.newInstance(sServiceExceptionHandler.trim(),
                            new Class<?>[]{boolean.class}, new Object[]{Boolean.valueOf(enableLogging)});
                else
                    this.serviceExceptionHandler = (ServiceExceptionHandler) ClassUtil.newInstance(sServiceExceptionHandler.trim());
            } catch (Exception e) {
                throw new ServiceException("Could not instantiate service exception handler: " + sServiceExceptionHandler, e);
            }
        } else {
            if (Boolean.TRUE.toString().equals(enableLogging) || Boolean.FALSE.toString().equals(enableLogging))
                this.serviceExceptionHandler = new DefaultServiceExceptionHandler(Boolean.valueOf(enableLogging));
            else
                this.serviceExceptionHandler = new DefaultServiceExceptionHandler();
        }

        log.debug("<< Configuring factory done: %s", this);
    }

    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
        String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        GraniteContext context = GraniteManager.getCurrentInstance();
        Destination destination = context.getServicesConfig().findDestinationById(messageType, destinationId);
        if (destination == null)
            throw new ServiceException("No matching destination: " + destinationId);

        Map<String, Object> cache = getCache(destination);

        String key = SimpleServiceInvoker.class.getName() + '.' + destination.getId();
        if (invalidKeys.contains(key)) {
            cache.remove(key);
            invalidKeys.remove(key);
        }

        SimpleServiceInvoker service = (SimpleServiceInvoker) cache.get(key);
        if (service == null) {
            service = new SimpleServiceInvoker(destination, this);
            cache.put(key, service);
        }
        return service;
    }

    public ServiceExceptionHandler getServiceExceptionHandler() {
        return serviceExceptionHandler;
    }

    private Map<String, Object> getCache(Destination destination) throws ServiceException {
        GraniteContext context = GraniteManager.getCurrentInstance();
        String scope = destination.getProperties().get("scope");

        Map<String, Object> cache = null;
        if (scope == null || "request".equals(scope))
            cache = context.getRequestMap();
        else if ("session".equals(scope))
            cache = context.getSessionMap();
        else if ("application".equals(scope))
            cache = Collections.synchronizedMap(context.getApplicationMap());
        else
            throw new ServiceException("Illegal scope in destination: " + destination);

        return cache;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(String append) {
        return super.toString() + " {" +
                (append != null ? append : "") +
                "\n  serviceExceptionHandler: " + serviceExceptionHandler +
                "\n}";
    }
}
