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
package org.granite.messaging.service;

import flex.messaging.messages.RemotingMessage;

import org.granite.config.flex.Destination;
import org.granite.config.flex.DestinationRemoveListener;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Franck WOLFF
 */
public class SimpleServiceFactory extends ServiceFactory implements DestinationRemoveListener {
    
    private Set<String> invalidKeys = new HashSet<String>();
    

    @Override
    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
        String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        GraniteContext context = GraniteContext.getCurrentInstance();
        Destination destination = ((ServicesConfig)context.getServicesConfig()).findDestinationById(messageType, destinationId);
        if (destination == null)
            throw new ServiceException("No matching destination: " + destinationId);

        destination.addRemoveListener(this);
        Map<String, Object> cache = getCache(destination);
        
        String key = SimpleServiceInvoker.class.getName() + '.' + destination.getId();
        if (invalidKeys.contains(key)) {
        	cache.remove(key);
        	invalidKeys.remove(key);
        }
        
        SimpleServiceInvoker service = (SimpleServiceInvoker)cache.get(key);
        if (service == null) {
            service = new SimpleServiceInvoker(destination, this);
            cache.put(key, service);
        }
        return service;
    }
    
    public void destinationRemoved(Destination destination) throws ServiceException {
        synchronized (invalidKeys) {
        	invalidKeys.add(SimpleServiceInvoker.class.getName() + '.' + destination.getId());
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
