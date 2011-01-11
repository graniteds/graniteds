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

package org.granite.tide.seam21;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.lang.reflect.Method;

import org.granite.tide.seam.AbstractSeamServiceContext;
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
public class TideEvents extends org.granite.tide.seam.TideEvents {

    private static final long serialVersionUID = -5395975397632138270L;
    
    protected @Logger Log log;
    
        
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
}
