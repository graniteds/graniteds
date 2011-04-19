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
import org.granite.config.flex.Factory;
import org.granite.config.flex.SimpleFactory;
import org.granite.context.GraniteContext;
import org.granite.context.GraniteManager;
import org.granite.logging.Logger;
import org.granite.util.ClassUtil;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleMainFactory implements MainFactory{
    private static final Logger log = Logger.getLogger(SimpleMainFactory.class);

    private final ReentrantLock lock = new ReentrantLock();

    public ServiceFactory getFactoryInstance(RemotingMessage request) throws ServiceException {

        GraniteContext context = GraniteManager.getCurrentInstance();

        String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        log.debug(">> Finding factoryId for messageType: %s and destinationId: %s", messageType, destinationId);

        Destination destination = context.getServicesConfig().findDestinationById(messageType, destinationId);
        if (destination == null)
            throw new ServiceException("Destination not found: " + destinationId);
        String factoryId = destination.getProperties().get("factory");

        log.debug(">> Found factoryId: %s", factoryId);

        Map<String, Object> cache = context.getApplicationMap();
        String key = ServiceFactory.class.getName() + '.' + factoryId;

        return getServiceFactory(cache, context, factoryId, key);
    }

    private ServiceFactory getServiceFactory(Map<String, Object> cache, GraniteContext context, String factoryId, String key) {
        lock.lock();
        try {
            ServiceFactory factory = (ServiceFactory) cache.get(key);
            if (factory == null) {

                log.debug(">> No cached factory for: %s", factoryId);

                Factory config = context.getServicesConfig().findFactoryById(factoryId);
                if (config == null)
                    config = getDefaultFactoryConfig();
                try {
                    Class<? extends ServiceFactory> clazz = ClassUtil.forName(config.getClassName(), ServiceFactory.class);
                    factory = clazz.newInstance();
                    factory.configure(config.getProperties());
                } catch (Exception e) {
                    throw new ServiceException("Could not instantiate factory: " + factory, e);
                }
                cache.put(key, factory);
            } else
                log.debug(">> Found a cached factory for: %s", factoryId);

            log.debug("<< Returning factory: %s", factory);

            return factory;
        } finally {
            lock.unlock();
        }
    }

    private Factory getDefaultFactoryConfig() {
        return SimpleFactory.DEFAULT_FACTORY;
    }
}
