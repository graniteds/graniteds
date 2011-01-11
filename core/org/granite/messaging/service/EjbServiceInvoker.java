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

import javax.ejb.NoSuchEJBException;
import javax.naming.NamingException;

import org.granite.config.flex.Destination;
import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class EjbServiceInvoker extends ServiceInvoker<EjbServiceFactory> {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(EjbServiceInvoker.class);

    public static final String CAPITALIZED_DESTINATION_ID = "{capitalized.destination.id}";
    public static final String DESTINATION_ID = "{destination.id}";
    
    private final EjbServiceMetadata metadata;
    
    public EjbServiceInvoker(Destination destination, EjbServiceFactory factory) throws ServiceException {
        super(destination, factory);

        String lookup = factory.getLookup();
        if (destination.getProperties().containsKey("lookup"))
            lookup = destination.getProperties().get("lookup");

        // Compute EJB JNDI binding.
        String name = destination.getId();
        if (lookup != null) {
            name = lookup;
            if (lookup.contains(CAPITALIZED_DESTINATION_ID))
                name = lookup.replace(CAPITALIZED_DESTINATION_ID, capitalize(destination.getId()));
            if (lookup.contains(DESTINATION_ID))
                name = lookup.replace(DESTINATION_ID, destination.getId());
        }

        log.debug(">> New EjbServiceInvoker looking up: %s", name);

        try {
            invokee = factory.lookup(name);
        } catch (NamingException e) {
            throw new ServiceException("Could not lookup for: " + name, e);
        }
        
        this.metadata = destination.getScannedClass() != null ?
        	new EjbServiceMetadata(destination.getScannedClass(), invokee.getClass()) :
        	new EjbServiceMetadata(destination.getProperties(), invokee.getClass());
    }

	public EjbServiceMetadata getMetadata() {
		return metadata;
	}

	@Override
	protected void afterInvocationError(ServiceInvocationContext context, Throwable error) {
    	// "A NoSuchEJBException is thrown if an attempt is made to invoke a method
    	// on an object that no longer exists" (javadoc on NoSuchEJBException).
    	if (error instanceof NoSuchEJBException || (
    			metadata.isStateful() &&
    			metadata.isRemoveMethod(context.getMethod()) &&
    			!metadata.getRetainIfException(context.getMethod())
    		))
    		factory.removeFromCache(destination.getId());
	}

	@Override
	protected Object afterInvocation(ServiceInvocationContext context, Object result) {
		if (metadata.isStateful() && metadata.isRemoveMethod(context.getMethod()))
			factory.removeFromCache(destination.getId());
		return result;
	}
	
	private String capitalize(String s) {
        if (s == null || s.length() == 0)
            return s;
        if (s.length() == 1)
            return s.toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
