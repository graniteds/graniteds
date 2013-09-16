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
package org.granite.tide.seam21;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.granite.tide.seam.AbstractSeamServiceContext;
import org.granite.tide.seam.TideInvocation;
import org.granite.tide.seam.async.AsyncContext;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.async.AbstractDispatcher;
import org.jboss.seam.async.Dispatcher;
import org.jboss.seam.async.Schedule;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;


/**
 * TideEvents override to intercept Seam events handling
 * 
 * @author William DRAI
 */
@Name("org.jboss.seam.core.events")
@Install(precedence=FRAMEWORK+1)
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@AutoCreate
public class TideEvents extends Events {
    
    protected @Logger Log log;
    
    protected static final String ASYNC_EVENT = "org.granite.tide.seam.AsyncEvent";
    
    
    @Override
    public void raiseEvent(String type, Object... parameters) {
        if (ASYNC_EVENT.equals(type)) {
        	TideInvocation.init();
            AbstractSeamServiceContext serviceContext = (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, true);
            
            WrappedEvent event = (WrappedEvent)parameters[0];
            serviceContext.setAsyncContext(event.getAsyncContext());    // Reset context
            
            raiseEvent(event.getType(), event.getParams());
            
            // Send event through Gravity only 
            serviceContext.sendEvent(null, null);
        }
        else {
            super.raiseEvent(type, parameters);
            
            // Ignore built-in Seam events to avoid stack overflow in component initialization
            if (!type.startsWith("org.jboss.seam.pre") && !type.startsWith("org.jboss.seam.post")) {
                // Event should be always handled if we want to allow to send them through Gravity
                AbstractSeamServiceContext serviceContext = (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, false);
                if (serviceContext != null)     // ServiceContext is null during Seam initialization
                    serviceContext.raiseEvent(type, parameters);
            }
        }
    }
    
    
    @Override
    public void raiseAsynchronousEvent(String type, Object... parameters) {
        AbstractSeamServiceContext serviceContext = (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, false);
        String sessionId = serviceContext != null ? serviceContext.getSessionId() : null;
        if (serviceContext != null && sessionId != null)
            super.raiseAsynchronousEvent(ASYNC_EVENT, new WrappedEvent(serviceContext.getAsyncContext(), type, parameters));
        else
            super.raiseAsynchronousEvent(type, parameters);
    }
    
        
    // Seam 2.1
    private static final Class<?>[] SEAM21_TIMED_EVENT_ARGS = new Class<?>[] { String.class, Schedule.class, Object[].class };
    
    @SuppressWarnings("all")
    public void raiseTimedEvent(String type, Schedule schedule, Object... parameters) {
        AbstractSeamServiceContext serviceContext = (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, false);
        
        String sessionId = serviceContext != null ? serviceContext.getSessionId() : null;
        Dispatcher dispatcher = AbstractDispatcher.instance();
        if (dispatcher != null) {
        	try {
	        	Method m = dispatcher.getClass().getMethod("scheduleTimedEvent", SEAM21_TIMED_EVENT_ARGS);
		    	if (serviceContext != null && sessionId != null)
		        	m.invoke(dispatcher, ASYNC_EVENT, schedule, new Object[] { new WrappedEvent(serviceContext.getAsyncContext(), type, parameters) });
		    	else
		    		m.invoke(dispatcher, type, schedule, parameters);
        	}
        	catch (Exception e) {
        		log.error("Could not raise timed event", e);
        	}
        }
    }
    
    
    protected static class WrappedEvent implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private AsyncContext asyncContext;
        private String type;
        private Object[] params;
        
        public WrappedEvent(AsyncContext asyncContext, String type, Object[] params) {
            this.asyncContext = asyncContext;
            this.type = type;
            this.params = params;
        }
        
        public AsyncContext getAsyncContext() {
            return asyncContext;
        }
        
        public String getType() {
            return type;
        }
        
        public Object[] getParams() {
            return params;
        }
    }
}
