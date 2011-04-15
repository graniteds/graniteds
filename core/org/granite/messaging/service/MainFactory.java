package org.granite.messaging.service;

import flex.messaging.messages.RemotingMessage;
import org.granite.config.flex.Destination;
import org.granite.config.flex.Factory;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.util.ClassUtil;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class MainFactory {
    private static final Logger log = Logger.getLogger(MainFactory.class);

    private final ReentrantLock lock = new ReentrantLock();

    public ServiceFactory getFactoryInstance(RemotingMessage request) throws ServiceException {

        GraniteContext context = GraniteContext.getCurrentInstance();

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
        return Factory.DEFAULT_FACTORY;
    }
}
