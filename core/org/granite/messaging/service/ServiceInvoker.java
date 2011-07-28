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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.service.security.RemotingDestinationSecurizer;
import org.granite.util.TransientReference;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Franck WOLFF
 */
@TransientReference
public abstract class ServiceInvoker<T extends ServiceFactory> {

	private static final Logger log = Logger.getLogger(ServiceInvoker.class);

    protected final List<ServiceInvocationListener> invocationListeners;
    protected final Destination destination;
    protected final T factory;
    protected Object invokee = null;

    private ServiceExceptionHandler serviceExceptionHandler;

    protected ServiceInvoker(Destination destination, T factory) throws ServiceException {
        this.destination = destination;
        this.factory = factory;
        this.serviceExceptionHandler = factory.getServiceExceptionHandler();

        ServiceInvocationListener invocationListener =
            GraniteContext.getCurrentInstance().getGraniteConfig().getInvocationListener();
        if (invocationListener != null) {
            this.invocationListeners = new ArrayList<ServiceInvocationListener>();
            this.invocationListeners.add(invocationListener);
        } else
            this.invocationListeners = null;
    }

    protected Object adjustInvokee(RemotingMessage request, String methodName, Object[] args) throws ServiceException {
        return invokee;
    }

    protected Object[] beforeMethodSearch(Object invokee, String methodName, Object[] args) {
        return new Object[] { methodName, args };
    }

    protected void beforeInvocation(ServiceInvocationContext context) {
    }

    protected boolean retryInvocation(ServiceInvocationContext context, Throwable t) {
    	return false;
    }

    protected void afterInvocationError(ServiceInvocationContext context, Throwable error) {
    }

    protected Object afterInvocation(ServiceInvocationContext context, Object result) {
        return result;
    }

    public final Object invoke(RemotingMessage request) throws ServiceException {

        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();

        String methodName = request.getOperation();
        Object[] args = (Object[])request.getBody();

        // Adjust invokee (SimpleServiceInvoker with runtime "source" attribute)
        Object invokee = adjustInvokee(request, methodName, args);

        // Try to find out method to call (and convert parameters).
        Object[] call = beforeMethodSearch(invokee, methodName, args);
        methodName = (String)call[0];
        args = (Object[])call[1];
        if (invocationListeners != null) {
            for (ServiceInvocationListener invocationListener : invocationListeners)
                args = invocationListener.beforeMethodSearch(invokee, methodName, args);
        }

        log.debug(">> Trying to find method: %s%s in %s", methodName, args, invokee != null ? invokee.getClass() : "");

        ServiceInvocationContext invocationContext = null;
        try {
            invocationContext = config.getMethodMatcher().findServiceMethod(
                request,
                destination,
                invokee,
                methodName,
                args
            );
        } catch (NoSuchMethodException e) {
            throw serviceExceptionHandler.handleNoSuchMethodException(
                request, destination, invokee, methodName, args, e
            );
        }

        beforeInvocation(invocationContext);
        if (invocationListeners != null) {
            for (ServiceInvocationListener invocationListener : invocationListeners)
                invocationListener.beforeInvocation(invocationContext);
        }

        log.debug(">> Invoking method: %s with ", invocationContext.getMethod(), args);

        Throwable error = null;
        Object result = null;
        try {

            // Check security 1 (destination).
            if (destination.getSecurizer() instanceof RemotingDestinationSecurizer)
                ((RemotingDestinationSecurizer)destination.getSecurizer()).canExecute(invocationContext);

            boolean retry = false;
            try {
                // Check security 2 (security service).
	            if (config.hasSecurityService())
	                result = config.getSecurityService().authorize(invocationContext);
	            else
	                result = invocationContext.invoke();
            } catch (Exception e) {
            	if (retryInvocation(invocationContext, (e instanceof InvocationTargetException ? e.getCause() : e)))
            		retry = true;
            	else
            		throw e;
            }
            
            if (retry) {
                // Check security 2 (security service).
	            if (config.hasSecurityService())
	                result = config.getSecurityService().authorize(invocationContext);
	            else
	                result = invocationContext.invoke();
            }

        } catch (InvocationTargetException e) {
        	error = e.getTargetException();
        } catch (Throwable e) {
        	error = e;
        } finally {
        	if (error != null) {
        		afterInvocationError(invocationContext, error);
                if (invocationListeners != null) {
                    for (ServiceInvocationListener invocationListener : invocationListeners)
                        invocationListener.afterInvocationError(invocationContext, error);
                }
        		throw serviceExceptionHandler.handleInvocationException(invocationContext, error);
        	}
        }

        result = afterInvocation(invocationContext, result);
        if (invocationListeners != null) {
            for (ServiceInvocationListener invocationListener : invocationListeners)
                result = invocationListener.afterInvocation(invocationContext, result);
        }

        log.debug("<< Returning result: %s", result);

        return result;
    }
}
