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

package org.granite.tide.seam.async;

import java.util.Date;

import org.granite.messaging.service.ServiceException;
import org.granite.tide.seam.AbstractSeamServiceContext;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Duration;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.FinalExpiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.annotations.async.IntervalDuration;


/**
 * @author William DRAI
 */
@Scope(ScopeType.STATELESS)
@Name("org.granite.tide.seam.asynchronousInvoker")
public class AsynchronousInvoker {
    
    
    @In(create=false,required=false) 
    private AbstractSeamServiceContext serviceContext;
    
    
    public AsynchronousInvoker() throws ServiceException {
        super();
    }
    
    
    /**
     * Implementations of intercepted asynchronous calls (cron interval)
     * @param asyncContext current context (session id)
     * @param targetComponentName target component name
     * @param methodName method name
     * @param paramTypes method argument types
     * @param params argument values
     * @param duration optional duration
     * @param expiration optional expiration date
     * @param finalExpiration optional final expiration date
     * @param intervalCron cron interval
     * @return result
     */
    @Asynchronous
    public Object invokeAsynchronousCron(AsyncContext asyncContext, String targetComponentName, Class<?> targetComponentClass, String methodName, Class<?>[] paramTypes, Object[] params,
            @Duration Long duration, @Expiration Date expiration, @FinalExpiration Date finalExpiration, @IntervalCron String intervalCron) {
        return serviceContext.invokeAsynchronous(asyncContext, targetComponentName, targetComponentClass, methodName, paramTypes, params);
    }
    
    /**
     * Implementations of intercepted asynchronous calls (duration interval)
     * @param asyncContext current context (session id)
     * @param targetComponentName target component name
     * @param methodName method name
     * @param paramTypes method argument types
     * @param params argument values
     * @param duration optional duration
     * @param expiration optional expiration date
     * @param intervalDuration duration interval
     * @return result
     */
    @Asynchronous
    public Object invokeAsynchronousDuration(AsyncContext asyncContext, String targetComponentName, Class<?> targetComponentClass, String methodName, Class<?>[] paramTypes, Object[] params,
            @Duration Long duration, @Expiration Date expiration, @IntervalDuration Long intervalDuration) {
        return serviceContext.invokeAsynchronous(asyncContext, targetComponentName, targetComponentClass, methodName, paramTypes, params);
    }
    
    /**
     * Implementations of intercepted asynchronous calls (duration interval)
     * @param asyncContext current context (session id)
     * @param targetComponentName target component name
     * @param methodName method name
     * @param paramTypes method argument types
     * @param params argument values
     * @param duration optional duration
     * @param expiration optional expiration date
     * @param finalExpiration optional final expiration date
     * @param intervalDuration duration interval
     * @return result
     */
    @Asynchronous
    public Object invokeAsynchronousDuration(AsyncContext asyncContext, String targetComponentName, Class<?> targetComponentClass, String methodName, Class<?>[] paramTypes, Object[] params,
            @Duration Long duration, @Expiration Date expiration, @FinalExpiration Date finalExpiration, @IntervalDuration Long intervalDuration) {
        return serviceContext.invokeAsynchronous(asyncContext, targetComponentName, targetComponentClass, methodName, paramTypes, params);
    }
}
