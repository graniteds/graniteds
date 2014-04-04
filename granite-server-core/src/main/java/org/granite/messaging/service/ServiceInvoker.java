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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.granite.clustering.TransientReference;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.service.security.RemotingDestinationSecurizer;

import flex.messaging.messages.RemotingMessage;

/**
 * Abstract base class for all service's methods calls. This class mainly implements a final
 * invocation method which deals with parameter conversions, security and listeners.
 * 
 * @author Franck WOLFF
 *
 * @see ServiceFactory
 * @see ServiceInvocationListener
 * @see ServiceExceptionHandler
 */
@TransientReference
public abstract class ServiceInvoker<T extends ServiceFactory> {

	private static final Logger log = Logger.getLogger(ServiceInvoker.class);

    protected final List<ServiceInvocationListener> invocationListeners;
    protected final Destination destination;
    protected final T factory;
    protected Object invokee = null;

    private ServiceExceptionHandler serviceExceptionHandler;

    /**
     * Constructs a new ServiceInvoker. This constructor is used by a dedicated {@link ServiceFactory}.
     * 
     * @param destination the remote destination of this service (services-config.xml).
     * @param factory the factory that have called this constructor.
     * @throws ServiceException if anything goes wrong.
     */
    protected ServiceInvoker(Destination destination, T factory) throws ServiceException {
        this.destination = destination;
        this.factory = factory;
        this.serviceExceptionHandler = factory.getServiceExceptionHandler();

        ServiceInvocationListener invocationListener =
            ((GraniteConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getInvocationListener();
        if (invocationListener != null) {
            this.invocationListeners = new ArrayList<ServiceInvocationListener>();
            this.invocationListeners.add(invocationListener);
        } else
            this.invocationListeners = null;
    }

    /**
     * Called at the beginning of the {@link #invoke(RemotingMessage)} method. Give a chance to modify the
     * the services (invokee) about to be called. Does nothing by default. The default invokee object is
     * created by actual implementations of this abstract class.
     * 
     * @param request the current remoting message (sent from Flex).
     * @param methodName the name of the method to be called.
     * @param args the method parameter values.
     * @return the (possibly adjusted) invokee object.
     * @throws ServiceException if anything goes wrong.
     */
    protected Object adjustInvokee(RemotingMessage request, String methodName, Object[] args) throws ServiceException {
        return invokee;
    }

    /**
     * Called before the {@link #invoke(RemotingMessage)} method starts to search for a method named <tt>methodName</tt>
     * with the arguments <tt>args</tt> on the invokee object. Give a chance to modify the method name or the paramaters.
     * Does nothing by default.
     * 
     * @param invokee the service instance used for searching the method with the specified arguments.
     * @param methodName the method name.
     * @param args the arguments of the method.
     * @return an array of containing the (possibly adjusted) method name and its arguments.
     */
    protected Object[] beforeMethodSearch(Object invokee, String methodName, Object[] args) {
        return new Object[] { methodName, args };
    }

    /**
     * Called before the invocation of the services method. Does nothing by default.
     * 
     * @param context the current invocation context.
     */
    protected void beforeInvocation(ServiceInvocationContext context) {
    }

    /**
     * Called after a failed invocation of the service's method. Returns <tt>false</tt> by default.
     * 
     * @param context the current invocation context.
     * @param t the exception that caused the invocation failure.
     * @return <tt>true</tt> if invocation should be retried, <tt>false</tt> otherwise.
     */
    protected boolean retryInvocation(ServiceInvocationContext context, Throwable t) {
    	return false;
    }

    /**
     * Called after a failed invocation of the service's method, possibly after a new attempt (see
     * {@link #retryInvocation(ServiceInvocationContext, Throwable)}. Does nothing by default.
     * 
     * @param context the current invocation context.
     * @param error the exception that caused the invocation failure.
     */
    protected void afterInvocationError(ServiceInvocationContext context, Throwable error) {
    }

    /**
     * Called after a successful invocation of the service's method. Does nothing by default.
     * 
     * @param context the current invocation context.
     * @param result the result of the invocation (returned by the called method).
     */
    protected Object afterInvocation(ServiceInvocationContext context, Object result) {
        return result;
    }

    /**
     * Call a service's method according to the informations contained in the given remoting message.
     * This method is final and implements a standard way of calling a service's method, independent of
     * the underlying framework (EJB3, Spring, Seam, etc.) It deals with security, parameter conversions,
     * exception handling and offers several ways of listening (and possibly adjusting) the invocation
     * process.
     * 
     * @param request the remoting message containing informations about the call.
     * @return the result of the service's method invocation.
     * @throws ServiceException if anything goes wrong (security, invocation target exception, etc.)
     */
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

        try {
	        beforeInvocation(invocationContext);
	        if (invocationListeners != null) {
	            for (ServiceInvocationListener invocationListener : invocationListeners)
	                invocationListener.beforeInvocation(invocationContext);
	        }
        }
        catch (Exception error) {
        	handleInvocationError(invocationContext, error);
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
	            if (config.hasSecurityService() && config.getSecurityService().acceptsContext())
	                result = config.getSecurityService().authorize(invocationContext);
	            else
	                result = invocationContext.invoke();
            } 
            catch (Exception e) {
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
        		handleInvocationError(invocationContext, error);
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
    
    
    private void handleInvocationError(ServiceInvocationContext invocationContext, Throwable error) throws ServiceException {
		afterInvocationError(invocationContext, error);
        if (invocationListeners != null) {
            for (ServiceInvocationListener invocationListener : invocationListeners)
                invocationListener.afterInvocationError(invocationContext, error);
        }
        if (error instanceof ServiceException)
        	throw (ServiceException)error;
		throw serviceExceptionHandler.handleInvocationException(invocationContext, error);
    }
}
