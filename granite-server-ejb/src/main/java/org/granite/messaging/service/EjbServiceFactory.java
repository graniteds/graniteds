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

import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.granite.config.flex.Destination;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.util.XMap;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Franck WOLFF
 */
public class EjbServiceFactory extends ServiceFactory {

    private static final Logger log = Logger.getLogger(EjbServiceFactory.class);

    private Properties environment = null;
    private transient InitialContext initialContext = null;
    private String lookup = null;

    public EjbServiceFactory() throws ServiceException {
    }
    
    public synchronized Object lookup(String name) throws NamingException {
    	if (initialContext == null) {
    		if (environment == null || environment.isEmpty())
    			initialContext = new InitialContext();
    		else
    			initialContext = new InitialContext(environment);
    	}
    	return initialContext.lookup(name);
    }
    
    public String getLookup() {
        return lookup;
    }

    @Override
    public void configure(XMap properties) throws ServiceException {
        super.configure(properties);
        
    	try {
            environment = new Properties();
            
            for (XMap property : properties.getAll("initial-context-environment/property")) {
            	String name = property.get("name");
            	String value = property.get("value");
            	
            	if ("Context.PROVIDER_URL".equals(name))
            		environment.put(Context.PROVIDER_URL, value);
            	else if ("Context.INITIAL_CONTEXT_FACTORY".equals(name))
            		environment.put(Context.INITIAL_CONTEXT_FACTORY, value);
            	else if ("Context.URL_PKG_PREFIXES".equals(name))
            		environment.put(Context.URL_PKG_PREFIXES, value);
            	else if ("Context.SECURITY_PRINCIPAL".equals(name))
            		environment.put(Context.SECURITY_PRINCIPAL, value);
            	else if ("Context.SECURITY_CREDENTIALS".equals(name))
            		environment.put(Context.SECURITY_CREDENTIALS, value);
            	else
            		log.warn("Unknown InitialContext property: %s (ignored)", name);
            }
    		
            initialContext = new InitialContext(environment.size() > 0 ? environment : null);
    	} catch (Exception e) {
    		throw new ServiceException("Could not create InitialContext", e);
    	}
        
    	lookup = properties.get("lookup");
    }

    @Override
    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
        GraniteContext context = GraniteContext.getCurrentInstance();

        String destinationId = request.getDestination();
        String key = getUniqueKey(destinationId);

        EjbServiceInvoker invoker = null;

        // Synchronize on unique key.
        synchronized (key) {
        	// Retrieve cached instance.
        	invoker = (EjbServiceInvoker)context.getApplicationMap().get(key);
        	if (invoker == null) {
        		Map<String, Object> sessionMap = context.getSessionMap(false);
        		if (sessionMap != null)
        			invoker = (EjbServiceInvoker)sessionMap.get(key);
        	}
        }

        // Not found, lookup and cache.
    	if (invoker == null) {
            Destination destination = ((ServicesConfig)context.getServicesConfig()).findDestinationById(
            	request.getClass().getName(),
            	destinationId
            );
            invoker = new EjbServiceInvoker(destination, this);

        	Map<String, Object> cache = invoker.getMetadata().isStateful() ?
    			context.getSessionMap(true) :
				context.getApplicationMap();

    		// Synchronize on unique key (put if absent)...
            synchronized (key) {
        		EjbServiceInvoker previousInvoker = (EjbServiceInvoker)cache.get(key);
            	if (previousInvoker != null)
            		invoker = previousInvoker;
            	else
            		cache.put(key, invoker);
			}
    	}
        
        return invoker;
    }
    
    protected String getUniqueKey(String destinationId) {
    	return new StringBuilder(EjbServiceInvoker.class.getName().length() + 1 + destinationId.length())
	    	.append(EjbServiceInvoker.class.getName())
	    	.append('.')
	    	.append(destinationId)
	    	.toString()
	    	.intern();
    }
    
    public void removeFromCache(String destinationId) {
    	GraniteContext context = GraniteContext.getCurrentInstance();
    	String key = getUniqueKey(destinationId);
    	// Synchronize on unique key.
    	synchronized (key) {
    		context.getApplicationMap().remove(key);
    		Map<String, Object> sessionMap = context.getSessionMap(false);
    		if (sessionMap != null)
    			context.getSessionMap().remove(key);
    	}
    }

    @Override
    public String toString() {
        return toString("\n  lookup: " + lookup);
    }
}
