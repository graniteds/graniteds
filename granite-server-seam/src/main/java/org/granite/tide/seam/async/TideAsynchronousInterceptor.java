/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.seam.async;

import java.lang.annotation.Annotation;
import java.util.Date;

import org.granite.tide.seam.AbstractSeamServiceContext;
import org.granite.tide.seam.TideInvocation;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Duration;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.FinalExpiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.async.AbstractDispatcher;
import org.jboss.seam.async.AsynchronousInterceptor;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Slightly modified version of the Seam asynchronous interceptor which saves the current username
 * 
 * Dispatches method calls to @Asynchronous methods
 * asynchronously, and returns the "timer" object
 * if necessary.
 * 
 * @author William DRAI
 *
 */
@Interceptor(stateless=true, type=InterceptorType.CLIENT, around={ AsynchronousInterceptor.class })
public class TideAsynchronousInterceptor extends AbstractInterceptor {
    
    private static final long serialVersionUID = 9194177339867853303L;
    
    
    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocation) throws Exception {
        if (invocation.getTarget() instanceof AsynchronousInvoker) {
        	if (Contexts.getEventContext().isSet(AbstractDispatcher.EXECUTING_ASYNCHRONOUS_CALL))
        		TideInvocation.init();
            return invocation.proceed();
        }
        
        boolean scheduleAsync = invocation.getMethod().isAnnotationPresent(Asynchronous.class) && 
            (!Contexts.getEventContext().isSet(AbstractDispatcher.EXECUTING_ASYNCHRONOUS_CALL) 
                    || Contexts.getEventContext().isSet("org.jboss.seam.async.AsynchronousIntercepter.REENTRANT"));
        if (scheduleAsync) {
            AbstractSeamServiceContext serviceContext = (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, false);
            if (serviceContext != null && serviceContext.getSessionId() != null) {
                Annotation[][] parameterAnnotations = invocation.getMethod().getParameterAnnotations();
                Long duration = null;
                Date expiration = null;
                Date finalExpiration = null;
                Long intervalDuration = null;
                String intervalCron = null;
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    Annotation[] annotations = parameterAnnotations[i];
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType().equals(Duration.class))
                            duration = (Long)invocation.getParameters()[i];
                        else if (annotation.annotationType().equals(Expiration.class))
                            expiration = (Date)invocation.getParameters()[i];
                        else if (annotation.annotationType().equals(FinalExpiration.class))
                            finalExpiration = (Date)invocation.getParameters()[i];
                        else if (annotation.annotationType().equals(IntervalDuration.class))
                            intervalDuration = (Long)invocation.getParameters()[i];
                        else if (annotation.annotationType().equals(IntervalCron.class))
                            intervalCron = (String)invocation.getParameters()[i];
                    }
                }
                
                String targetComponentName = getComponent().getName();
                Class<?> targetComponentClass = getComponent().getBeanClass();
                String methodName = invocation.getMethod().getName();
                Class<?>[] paramTypes = invocation.getMethod().getParameterTypes();
                Object[] params = invocation.getParameters();
                
                AsynchronousInvoker invoker = (AsynchronousInvoker)Component.getInstance("org.granite.tide.seam.asynchronousInvoker");
                
                AsyncContext asyncContext = serviceContext.getAsyncContext();
                
                if (intervalCron != null)
                    return invoker.invokeAsynchronousCron(asyncContext, targetComponentName, targetComponentClass, methodName, paramTypes, params, 
                            duration, expiration, finalExpiration, intervalCron);
                
                if (finalExpiration != null)
                    return invoker.invokeAsynchronousDuration(asyncContext, targetComponentName, targetComponentClass, methodName, paramTypes, params, 
                            duration, expiration, finalExpiration, intervalDuration);
                
                return invoker.invokeAsynchronousDuration(asyncContext, targetComponentName, targetComponentClass, methodName, paramTypes, params, 
                        duration, expiration, intervalDuration);
            }
        }
        
        return invocation.proceed();
    }

	// Needed for Seam 2.1
    public boolean isInterceptorEnabled() {
        return true;
    }
}
