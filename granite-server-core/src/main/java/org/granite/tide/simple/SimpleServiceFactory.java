/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.tide.simple;

import flex.messaging.messages.RemotingMessage;
import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.scan.ScannedItemHandler;
import org.granite.tide.TideServiceInvoker;

import java.util.Collections;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public class SimpleServiceFactory extends ServiceFactory {

    public static final String ENTITY_MANAGER_FACTORY_JNDI_NAME = "entity-manager-factory-jndi-name";
    public static final String ENTITY_MANAGER_JNDI_NAME = "entity-manager-jndi-name";

    public static ScannedItemHandler getScannedItemHandler() {
        return SimpleScannedItemHandler.instance(true);
    }

    @Override
    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
        String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        GraniteContext context = GraniteContext.getCurrentInstance();
        Map<String, Object> cache = context.getApplicationMap();
        Destination destination = context.getServicesConfig().findDestinationById(messageType, destinationId);
        String key = TideServiceInvoker.class.getName() + '.' + destinationId;

        return getServiceInvoker(cache, destination, key);
    }

    private ServiceInvoker<?> getServiceInvoker(Map<String, Object> cache, Destination destination, String key) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        synchronized (this) {
            ServiceInvoker<?> invoker = (ServiceInvoker<?>)cache.get(key);
            if (invoker == null) {
                SimpleServiceContext tideContext = new SimpleServiceContext();

                if (destination.getProperties().containsKey(ENTITY_MANAGER_FACTORY_JNDI_NAME)) {
                    tideContext.setEntityManagerFactoryJndiName(destination.getProperties().get(ENTITY_MANAGER_FACTORY_JNDI_NAME));
                }
                else if (destination.getProperties().containsKey(ENTITY_MANAGER_JNDI_NAME)) {
                    tideContext.setEntityManagerJndiName(destination.getProperties().get(ENTITY_MANAGER_JNDI_NAME));
                }

                invoker = new TideServiceInvoker<SimpleServiceFactory>(destination, this, tideContext);
                cache.put(key, invoker);
            }
            return invoker;
        }
    }
    
    
    private Map<String, Object> getCache(Destination destination) throws ServiceException {
        GraniteContext context = GraniteContext.getCurrentInstance();
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
}
