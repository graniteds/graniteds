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
package org.granite.tide.ejb;

import java.util.Map;

import javax.naming.InitialContext;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.service.ExtendedServiceExceptionHandler;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.scan.ScannedItemHandler;
import org.granite.tide.TideServiceInvoker;
import org.granite.tide.data.PersistenceExceptionConverter;
import org.granite.util.XMap;

import flex.messaging.messages.RemotingMessage;


/**
 * @author William DRAI
 */
public class EjbServiceFactory extends ServiceFactory {
    
    public static final String ENTITY_MANAGER_FACTORY_JNDI_NAME = "entity-manager-factory-jndi-name";
    public static final String ENTITY_MANAGER_JNDI_NAME = "entity-manager-jndi-name";

    private String lookup = null;
    private InitialContext initialContext = null;

    public static ScannedItemHandler getScannedItemHandler() {
    	return EjbScannedItemHandler.instance(true);
    }
    
    public String getLookup() {
        return lookup;
    }
    
    public void setInitialContext(InitialContext ic) {
    	this.initialContext = ic;
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
        
        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        config.registerExceptionConverter(PersistenceExceptionConverter.class);
        config.registerExceptionConverter(EJBAccessExceptionConverter.class);
        
        this.lookup = properties.get("lookup");
    }


    @Override
    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
        String messageType = request.getClass().getName();
        String destinationId = request.getDestination();

        GraniteContext context = GraniteContext.getCurrentInstance();
        Map<String, Object> cache = context.getSessionMap();
        Destination destination = ((ServicesConfig)context.getServicesConfig()).findDestinationById(messageType, destinationId);
        String key = TideServiceInvoker.class.getName() + '.' + destinationId;

        return getServiceInvoker(cache, destination, key);
    }

    private ServiceInvoker<?> getServiceInvoker(Map<String, Object> cache, Destination destination, String key) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        synchronized (context.getSessionLock()) {
            ServiceInvoker<?> invoker = (ServiceInvoker<?>)cache.get(key);
            if (invoker == null) {
                String lookup = getLookup();

                if (destination.getProperties().containsKey("lookup"))
                    lookup = destination.getProperties().get("lookup");
                
                EjbServiceContext tideContext = new EjbServiceContext(lookup, initialContext); 
                
                if (destination.getProperties().containsKey(ENTITY_MANAGER_FACTORY_JNDI_NAME)) {
                    tideContext.setEntityManagerFactoryJndiName(destination.getProperties().get(ENTITY_MANAGER_FACTORY_JNDI_NAME));
                } 
                else if (destination.getProperties().containsKey(ENTITY_MANAGER_JNDI_NAME)) {
                    tideContext.setEntityManagerJndiName(destination.getProperties().get(ENTITY_MANAGER_JNDI_NAME));
                }
                
                invoker = new TideServiceInvoker<EjbServiceFactory>(destination, this, tideContext);
                cache.put(key, invoker);
            }
            return invoker;
        }
    }
}
