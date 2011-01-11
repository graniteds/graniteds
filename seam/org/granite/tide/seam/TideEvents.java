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

package org.granite.tide.seam;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;

import org.granite.tide.seam.async.AsyncContext;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.async.Schedule;
import org.jboss.seam.core.Events;


/**
 * TideEvents override to intercept Seam events handling
 * 
 * @author William DRAI
 */
@Name("org.jboss.seam.core.events")
@Install(precedence=FRAMEWORK)
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@AutoCreate
public class TideEvents extends Events {

    private static final long serialVersionUID = -5395975397632138270L;
    
    
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
    
    
    protected static final String ASYNC_EVENT = "org.granite.tide.seam.AsyncEvent";
    
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

    
    @Override
    public void raiseAsynchronousEvent(String type, Object... parameters) {
        AbstractSeamServiceContext serviceContext = (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, false);
        String sessionId = serviceContext != null ? serviceContext.getSessionId() : null;
        if (serviceContext != null && sessionId != null)
            super.raiseAsynchronousEvent(ASYNC_EVENT, new WrappedEvent(serviceContext.getAsyncContext(), type, parameters));
        else
            super.raiseAsynchronousEvent(type, parameters);
    }
    
    
    // Seam 2.0
    @SuppressWarnings("all")
    public void raiseTimedEvent(String type, Object schedule, Object... parameters) {
        AbstractSeamServiceContext serviceContext = (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, false);
        String sessionId = serviceContext != null ? serviceContext.getSessionId() : null;
        if (serviceContext != null && sessionId != null)
            super.raiseTimedEvent(ASYNC_EVENT, (Schedule)schedule, new WrappedEvent(serviceContext.getAsyncContext(), type, parameters));
        else
            super.raiseTimedEvent(type, (Schedule)schedule, parameters);
    }
}
